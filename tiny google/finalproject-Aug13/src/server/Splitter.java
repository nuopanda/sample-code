package server;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import tools.Address;
import tools.WriteToLocalTool;

/**
 * The type Splitter.
 */
public class Splitter {

  private static final long HELPER_TIMEOUT_MS = 10000;
  private ExecutorService executorServer;

  /**
   * Instantiates a new Splitter.
   *
   * @param executorServer the executor server
   */
  public Splitter(ExecutorService executorServer) {
    this.executorServer = executorServer;
  }

  /**
   * Do index.
   *
   * @param fileName the file name
   * @param AddressAndHelperTaskMap <HelperAddress, HelperTask>
   * @param WordAndCountMap <Word, Count>
   * @throws Exception the exception
   */
  protected void doIndex(String fileName,
      ConcurrentHashMap<Address, HelperTaskWrapper> AddressAndHelperTaskMap
      , ConcurrentHashMap<String, Integer> WordAndCountMap)
      throws Exception {
    List<Address> helperList = SearchServer.getAvailableHelperListFromNameServer();
    List<Long> breakPoints = splitFile(fileName, helperList.size());
    Timer timer = new Timer();
    timer.schedule(new TimerTask() {
      @Override
      public void run() {
        try {
          sendUnfinishedTaskToIdleHelper(AddressAndHelperTaskMap, WordAndCountMap);
        } catch (Exception e) {
          e.printStackTrace();
        }
      }
    }, 0, HELPER_TIMEOUT_MS);
    for (int i = 0; i < breakPoints.size(); i++) {
      Address helperAddress = helperList.get(i);
      System.out.println(String.format("Sending indexing task to %s helper server{%s}", i ,helperAddress.toString()));
      HelperTaskWrapper task;
      if (i == breakPoints.size() - 1) {
        long startIndex = breakPoints.get(i);
        long endIndex = -1;
        File file = new File(fileName);
        if (file.exists() && file.isFile()) {
          endIndex = file.length();
        }
        task = new HelperTaskWrapper(fileName, startIndex, endIndex);
      } else {
        long startIndex = breakPoints.get(i);
        long endIndex = breakPoints.get(i + 1) - 1;
        task = new HelperTaskWrapper(fileName, startIndex, endIndex);
      }
      AddressAndHelperTaskMap.put(helperAddress, task);
      try {
        executorServer.submit(new Reducer(helperAddress,
          AddressAndHelperTaskMap, WordAndCountMap, task));
      } catch (Exception e) {
        System.err.println("Error: Helper Server :" + helperAddress.toString());
        e.printStackTrace();
        continue;
      }

    }

    while (!allHelperTasksDone(AddressAndHelperTaskMap)) {
    }
    timer.purge();
    timer.cancel();

    if (SearchServer.fileAndWordCount.containsKey(fileName)) {
      System.out.println("Updating existing info...");
      SearchServer.fileAndWordCount.remove(fileName);
    }
    SearchServer.fileAndWordCount.put(fileName, WordAndCountMap);
    System.out.println("Search Server has been updated for file: " + fileName);
    for (String word : WordAndCountMap.keySet()) {
      int count = WordAndCountMap.get(word);
      if (!SearchServer.wordAndFileCount.containsKey(word)) {
        ConcurrentHashMap<String, Integer> table = new ConcurrentHashMap<>();
        table.put(fileName, count);
        SearchServer.wordAndFileCount.put(word, table);
      } else {
        ConcurrentHashMap<String, Integer> table = SearchServer.wordAndFileCount
            .get(word);
        table.put(fileName, count);
        SearchServer.wordAndFileCount.put(word, table);
      }
    }
    WriteToLocalTool.writeObjectToLocal(SearchServer.wordAndFileCount, "InvertedIndexTable.data");
  }


  private boolean allHelperTasksDone(
      ConcurrentHashMap<Address, HelperTaskWrapper> AddressAndHelperTaskMap) {
    if (AddressAndHelperTaskMap == null) {
      return false;
    }
    for (Address addr : AddressAndHelperTaskMap.keySet()) {
      if (AddressAndHelperTaskMap.get(addr).isDone() == false) {
        return false;
      }
    }
    return true;
  }


  private void sendUnfinishedTaskToIdleHelper(
      ConcurrentHashMap<Address, HelperTaskWrapper> AddressAndHelperTaskMap,
      ConcurrentHashMap<String, Integer> WordAndCountMap) throws NumberFormatException {
    for (Address addr : AddressAndHelperTaskMap.keySet()) {
      HelperTaskWrapper task = AddressAndHelperTaskMap.get(addr);
      if (!task.isDone()) {
        List<Address> completedHelpers = SearchServer
            .completedHelpersList(AddressAndHelperTaskMap);
        if (completedHelpers.size() == 0) {
          return;
        }
        int index = (int) (Math.random() * completedHelpers.size());
        Address helperAddress = completedHelpers.get(index);
        AddressAndHelperTaskMap.put(helperAddress, task);
        executorServer.submit(new Reducer(helperAddress, AddressAndHelperTaskMap,
            WordAndCountMap, task));
      }
    }
  }

  /**
   * Split file to be indexed and return a list of break points.
   *
   * @param filePath the file path of file to be indexed
   * @param numOfHelpers the helper number
   * @return a list of break points.
   * @throws IOException
   */
  public static List<Long> splitFile(String filePath, int numOfHelpers)
      throws IOException {
    if (filePath == null || !new File(filePath).exists()) {
      System.err.println("Invalid file path!");
      return null;
    }
    RandomAccessFile file = new RandomAccessFile(filePath, "rw");
    List<Long> list = new ArrayList<>();
    long chunkLength = file.length() / numOfHelpers;
    for (int i = 0; i < numOfHelpers; i++) {
      long position = i * chunkLength;
      if (position == 0) {
        list.add(position);
        continue;
      }
      file.seek(position);
      char curPtr = (char) file.read();
      long left = position, right = position;
      if (Character.isLetterOrDigit(curPtr) || curPtr == '-') {
        while (left > 0 && right < file.length() - 1) {
          left--;
          file.seek(left);
          char leftPtr = (char) file.read();
          if (!Character.isLetterOrDigit(leftPtr) && leftPtr == '-') {
            list.add(left + 1);
            break;
          }
        }
      } else {
        while (right < file.length()) {
          right++;
          file.seek(right);
          char rightPtr = (char) file.read();
          if (!Character.isLetterOrDigit(rightPtr) || rightPtr == '-') {
            list.add(right);
            break;
          }
        }
      }
    }
    file.close();
    return list;
  }
}
