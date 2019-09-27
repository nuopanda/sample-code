package server;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.ServerSocket;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.*;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import nameServer.NameForServerQuery;
import tools.Address;
import tools.ServerTool;
import tools.WriteToLocalTool;

/**
 * The type Search server.
 */
public class SearchServer {

  protected static Address nameServerAddr;
  protected static ServerSocket listener;
  protected static Address searchServerAddr;
  protected static String searchForNameQueryAddr = null;
  protected static String searchForClientQueryAddr = null;
  protected static Registry registry = null;
  protected static NameForServerQuery nameForSearchStub;
  protected static ExecutorService executorService;
  protected static ConcurrentHashMap<String, ConcurrentHashMap<String, Integer>> fileAndWordCount = new ConcurrentHashMap<>();
  protected static ConcurrentHashMap<String, ConcurrentHashMap<String, Integer>> wordAndFileCount = new ConcurrentHashMap<>();

  /**
   * Instantiates a new Search server.
   */
  public SearchServer() {
    try {
      listener = new ServerSocket(0);
      searchServerAddr = ServerTool.getAddress(listener);
      System.out.println("Search Server is starting: " + searchServerAddr.toString());
      executorService = Executors.newCachedThreadPool();
    } catch (Exception e) {
      System.err.println("Error: listener " + e.toString());
    }
    searchForClientQueryAddr = "//" + searchServerAddr.toString() + "/searchServerForClient";
    searchForNameQueryAddr = "//" + searchServerAddr.toString() + "/searchServerForName";
  }

  /**
   * Remote interface registration.
   */
  protected void remoteInterfaceRegistration() {
    try {
      registry = LocateRegistry.createRegistry(Integer.parseInt(searchServerAddr.getPort()));
      SearchForClientQuery clientStub = new SearchForClientRemote();
      SearchForNameQuery nameStub = new SearchForNameRemote();
      Naming.bind(searchForClientQueryAddr, clientStub);
      Naming.bind(searchForNameQueryAddr, nameStub);
      System.out.println("Search Server RMI is ready.");
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  /**
   * Gets available helper list from name server.
   *
   * @return the available helper list from name server
   * @throws RemoteException the remote exception
   * @throws NotBoundException the not bound exception
   * @throws MalformedURLException the malformed url exception
   */
  public static List<Address> getAvailableHelperListFromNameServer()
      throws RemoteException, NotBoundException, MalformedURLException {
    getNameServerAddress();
    nameForSearchStub = (NameForServerQuery) Naming
        .lookup("//" + nameServerAddr.toString() + "/NameServerForServer");
    String result = nameForSearchStub.getHelperServerAddr();
    List<String> list = new ArrayList<>();
    list.addAll(Arrays.asList(result.split("A")));
    List<Address> helperList = new ArrayList<>();
    for (String curr : list) {
      //System.out.println(curr);
      String[] divideCurr = curr.split(":");
      helperList.add(new Address(divideCurr[0], divideCurr[1]));
    }
    return helperList;
  }


  /**
   * Completed helpers list list.
   *
   * @param AddressAndHelperTaskMap the helper indexing workload table
   * @return the list
   */
  public static List<Address> completedHelpersList(
      ConcurrentHashMap<Address, HelperTaskWrapper> AddressAndHelperTaskMap) {
    List<Address> completedHelpers = new ArrayList<>();
    for (Address addr : AddressAndHelperTaskMap.keySet()) {
      if (AddressAndHelperTaskMap.get(addr).isDone()) {
        completedHelpers.add(addr);
      }
    }
    return completedHelpers;
  }


  /**
   * Gets total word by count table.
   *
   * @return the total word by count table
   */
  public static ConcurrentHashMap<String, Integer> getTotalWordByCountTable() {
    ConcurrentHashMap<String, Integer> totalWordByCountTable = new ConcurrentHashMap<>();
    if (SearchServer.fileAndWordCount == null
        || SearchServer.fileAndWordCount.size() == 0) {
      return totalWordByCountTable;
    }
    fileAndWordCount.entrySet().stream().parallel().unordered().forEach(entry -> {
      String filePath = entry.getKey();
      Map<String, Integer> map = entry.getValue();
      int count = 0;
      for (String word : map.keySet()) {
        count += map.get(word);
      }
      totalWordByCountTable.put(filePath, count);
    });
    return totalWordByCountTable;
  }

  /**
   * Gets file by count table.
   *
   * @param keywordsList the keywords list
   * @return the file by count table
   */
// files are ranked by total count of key words
  public static ConcurrentHashMap<String, Integer> getFileByCountTable(List<String> keywordsList) {
    System.out.println("Reading inverted index table from local disk");
    readFromLocal();//read from local inverted index table in case the previous server the client talks to crashed.
    fileAndWordCount = new ConcurrentHashMap<>();
    //populate fileAndWordCount
    for (Map.Entry<String, ConcurrentHashMap<String, Integer>> e : wordAndFileCount.entrySet()) {
      ConcurrentHashMap<String, Integer> fileCnts = e.getValue();
      for (Map.Entry<String, Integer> filecnt : fileCnts.entrySet()) {
        ConcurrentHashMap<String, Integer> wordCnts = fileAndWordCount
            .getOrDefault(filecnt.getKey(), new ConcurrentHashMap<>());
        wordCnts.put(e.getKey(), filecnt.getValue());
        fileAndWordCount.put(filecnt.getKey(), wordCnts);
      }
    }

    //process file ranking
    ConcurrentHashMap<String, Integer> fileByCountTable = new ConcurrentHashMap<>();
    if (fileAndWordCount == null || fileAndWordCount.size() == 0) {
      System.out.println("No book has been indexed before:");

      if (fileAndWordCount == null || fileAndWordCount.size() == 0) {
        System.err.println("Searching request aborted. No book has been indexed before");
        return fileByCountTable;
      }
    }

    for (String filePath : fileAndWordCount.keySet()) {
      ConcurrentHashMap<String, Integer> wordByCountTable = fileAndWordCount.get(filePath);
      int count = 0;
      System.out.println("\n  [" + new File(filePath).getName() + "]");
      for (String keyword : keywordsList) {
        if (wordByCountTable.get(keyword) == null) {
          System.err.println("No such word (" + keyword + ") in this book!");
          continue;
        }
        count += wordByCountTable.get(keyword);
        System.out.println("\n    (" + keyword + ") occurred "
            + wordByCountTable.get(keyword) + " time in this book.");
      }
      System.out.println();
      if (fileByCountTable.containsKey(filePath)) {
        fileByCountTable.remove(filePath);
      }
      fileByCountTable.put(filePath, count);
    }
    return fileByCountTable;
  }

  /**
   * Gets ranked files.
   *
   * @param keywordsList the keywords list
   * @return the ranked files
   */
// file ranked with given keywords
  public static List<String> getRankedFiles(List<String> keywordsList) {
    List<String> rankedFilesList = new ArrayList<>();
    List<Map.Entry<String, Double>> fileFreqList = new ArrayList<>();
    ConcurrentHashMap<String, Integer> fileByCountTable = getFileByCountTable(keywordsList);
    ConcurrentHashMap<String, Integer> totalWorkCountByFile = getTotalWordByCountTable();
    for (String filePath : fileByCountTable.keySet()) {
      double keywordCount = fileByCountTable.get(filePath);
      double total = totalWorkCountByFile.get(filePath);
      double portion = keywordCount / total;
      fileFreqList.add(new AbstractMap.SimpleEntry<>(filePath, portion));
    }
    Collections.sort(fileFreqList, (o1, o2) -> o1.getValue() > o2.getValue() ? -1 : 1);
    for (int i = 0; i < fileFreqList.size(); i++) {
      rankedFilesList.add(getFileNameFromPath(fileFreqList.get(i).getKey()));
    }
    return rankedFilesList;
  }

  /**
   * Gets file name from path.
   *
   * @param filePath the file path
   * @return the file name from path
   */
  public static String getFileNameFromPath(String filePath) {
    File f = new File(filePath);
    String name = f.getName();
    int len = name.length();
    return name.substring(0, len - 4);
  }


  /**
   * Index file boolean.
   *
   * @param file the file
   * @return the boolean
   */
  public static boolean indexFile(String file) {
    System.out.println("Search server received a client query for indexing...");
    //System.out.println("filepath :" + file);
    try {
      boolean result = executorService.submit(new HandleRequest(file)).get();
      if (result) {
        System.out.println("Get result from helper servers. File is indexed.");
        return true;
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
    System.err.println("Error: Failed to index file.");
    return false;
  }

  /**
   * Search word string.
   *
   * @param keyWords the key words
   * @return the string
   */
  public static String searchWord(String keyWords) {
    System.out.println("Search server received a client query for searching...");
    List<String> words = new ArrayList<>();
    words.addAll(Arrays.asList(keyWords.trim().split(" ")));
    List<String> rankedList = null;
    try {
      HandleRequest handleRequest = new HandleRequest(words);
      boolean result = executorService.submit(handleRequest).get();
      rankedList = handleRequest.getRankedFiles();
    } catch (Exception e) {
      System.err.println(e.toString());
    }
    String files = "";
    for (String file : rankedList) {
      files += file + ";";
    }
    return files;
  }

  public static void getNameServerAddress() {
    nameServerAddr = ServerTool.getNameServerAddress();
    System.out.println("Found Name Server's address :" + nameServerAddr.toString());
    if (nameServerAddr == null) {
      System.err.println("Error: No available Name Server!");
      System.exit(1);
    }
  }

  /**
   * The entry point of application.
   *
   * @param args the input arguments
   * @throws RemoteException the remote exception
   * @throws NotBoundException the not bound exception
   * @throws MalformedURLException the malformed url exception
   */
  public static void main(String[] args) throws RemoteException, NotBoundException, MalformedURLException {
    SearchServer searchServer = new SearchServer();
    // get Name Server address
    getNameServerAddress();
    // call Name Server remote interface, register to Name Server
    nameForSearchStub = (NameForServerQuery) Naming
      .lookup("//" + nameServerAddr.toString() + "/NameServerForServer");
    nameForSearchStub.SearchServerRegistration(searchServerAddr);
    System.out.println("Registered to Name Server.");
    searchServer.remoteInterfaceRegistration();
  }

  /**
   * Read from local disk.
   *
   * @throws IOException the io exception
   */
  @SuppressWarnings("unchecked")
  public static void readFromLocal() {
    System.out.println("reading inverted index table from local disk");
    wordAndFileCount = (ConcurrentHashMap<String, ConcurrentHashMap<String, Integer>>)
        WriteToLocalTool.readObjectFromLocal("InvertedIndexTable.data");
    System.out.println("finishing reading inverted index table from local disk");
    System.out.println("print wordAndFileCount map: size=" + wordAndFileCount.size());
  }
}

/**
 * The type Handle request.
 */
class HandleRequest implements Callable<Boolean> {

  private ExecutorService executorServer;
  private String file;
  private List<String> words;
  private List<String> rankedFiles;


  /**
   * Instantiates a new Handle request.
   *
   * @param file the file
   */
  public HandleRequest(String file) {
    this.file = file;
  }

  /**
   * Instantiates a new Handle request.
   *
   * @param words the words
   */
  public HandleRequest(List<String> words) {
    this.words = words;
  }

  @Override
  public Boolean call() throws Exception {
    ConcurrentHashMap<Address, HelperTaskWrapper> AddressAndHelperTaskMap = new ConcurrentHashMap<>();
    ConcurrentHashMap<String, Integer> WordAndCountMap = new ConcurrentHashMap<>();
    executorServer = Executors.newCachedThreadPool();
    // if client sent index query
    if (file != null && file.length() > 0) {
      Splitter splitter = new Splitter(executorServer);
      splitter.doIndex(file, AddressAndHelperTaskMap, WordAndCountMap);
    } else if (words != null && words.size() > 0) {
      // if client sent search query
      rankedFiles = SearchServer.getRankedFiles(words);
    } else {
      System.out.println("Invalid client request type.");
      return false;
    }
    return true;
  }

  /**
   *
   * @return the list of all ranked files.
   */
  public List<String> getRankedFiles() {
    return rankedFiles;
  }
}
