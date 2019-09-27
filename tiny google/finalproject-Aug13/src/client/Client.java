package client;

import java.io.File;
import java.net.MalformedURLException;
import java.net.ServerSocket;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import javax.swing.JOptionPane;
import nameServer.NameForClientQuery;
import server.SearchForClientQuery;
import tools.Address;
import tools.ServerTool;

/**
 * The class Client.
 */
public class Client {

  private final static SimpleDateFormat sdf = new SimpleDateFormat("yyyy-mm-dd.hh.MM.ss.ms");
  private static Address nameServerAddr;
  private static Address searchServerAddr;
  private static Address clientAddr;
  private static int DEFAULT_TIME_OUT = 10;
  private static ServerSocket socket;
  protected static NameForClientQuery nameForClientStub;
  protected static SearchForClientQuery searchForClientStub;


  public static void getTime() {
    long sendTime = System.currentTimeMillis();
    Date sendDate = new Date(sendTime);
    System.out.println(sdf.format(sendDate));
  }

  private static Object executeWithTimeout(Callable<Object> task) {
    ExecutorService executor = Executors.newCachedThreadPool();
    Future<Object> future = executor.submit(task);
    try {
      return future.get(DEFAULT_TIME_OUT, TimeUnit.SECONDS);
    } catch (TimeoutException e) {
      System.err.println("time out.");
      JOptionPane.showMessageDialog(null, "Error (timeout1). Please try later");
      System.exit(1);
    } catch (Exception e) {
      JOptionPane.showMessageDialog(null, "Error (timeout2). Please try later");
      System.exit(1);
    } finally {
      future.cancel(true);
    }
    return null;
  }

  /**
   * The entry point of application.
   *
   * @param args the input arguments
   */
  public static void main(String[] args) {
    while (true) {
      // start doing tasks
      String userRequestMode = JOptionPane.showInputDialog(null,
          "Enter [1] for indexing a file, [2] for indexing all files in a folder, [3] " +
              "for searching by keywords, [Exit] or click the [Cancel] button to exit.",
          "Connection: Success", JOptionPane.INFORMATION_MESSAGE);
      // if hit cancel button, exit main
      if (userRequestMode == null) {
        System.out.println("Exiting the program...");
        System.exit(0);
        return;
      }
      if (userRequestMode.toLowerCase().equals("exit")) {
        System.out.println("Exiting the program...");
        System.exit(0);
        return;
      }
      while (!userRequestMode.equals("1") && !userRequestMode.equals("2") && !userRequestMode
          .equals("3") && (!userRequestMode.toLowerCase().equals("exit"))) {
        userRequestMode = JOptionPane.showInputDialog(null,
            "Invalid input!\n Enter [1] for indexing a file, [2] for indexing all files in a folder, "
                + "[3] for searching by keywords, [Exit] or click the [Cancel] button to exit.",
            "Connection: Success", JOptionPane.INFORMATION_MESSAGE);
      }
      // RequestMode = 2 for indexing all books in the ./books folder
      List<String> allFileNamesList = getAllFileNames();
      try {
        getTime();
        socket = new ServerSocket(0);
        clientAddr = ServerTool.getAddress(socket);
        if (clientAddr == null) {
          System.err.println("error: failed to get client's address");
          return;
        }
        nameServerAddr = ServerTool.getNameServerAddress();
        if (nameServerAddr == null) {
          System.err.println(
              "Error: No available Name Server. \nPlease check if name server is operating...");
          JOptionPane.showMessageDialog(null,
              "No available Name Server. Please try later.");
          return;
        }
        nameForClientStub = (NameForClientQuery) Naming
            .lookup("//" + nameServerAddr.toString() + "/NameServerForClient");
        searchServerAddr = nameForClientStub.getSearchServerAddr();
        System.out.println("Found search server : " + searchServerAddr.toString());
        if (searchServerAddr == null) {
          System.err.println(
              "Error: No available Search Server. \nPlease try later.");
          JOptionPane.showMessageDialog(null,
              "No available Search Server. Please try later.");
          return;
        }
      } catch (Exception e) {
        return;
      }
      // client want to index the file
      if (Integer.parseInt(userRequestMode) == 1) {
        String enteredFileName = JOptionPane.showInputDialog("Enter the filename you want to " +
            "index from the ./books directory");
        if (enteredFileName == null) {
          return;
        }
        // added .txt to incomplete filename
        if (!enteredFileName.endsWith(".txt")) {
          enteredFileName += ".txt";
        }
        while (enteredFileName.equals(".txt") || !allFileNamesList.contains(enteredFileName)) {
          JOptionPane.showMessageDialog(null,
              "Invalid file name or file does not exists. Please try again!");
          enteredFileName = JOptionPane.showInputDialog("Enter the filename you want to " +
              "index from the ./books directory");
          if (enteredFileName == null) {
            return;
          }
        }
        if (indexQuery(enteredFileName)) {
          JOptionPane.showMessageDialog(null, "Indexing is completed!");
          if (enteredFileName == null) {
            return;
          }
        } else {
          JOptionPane.showMessageDialog(null, "Error: Failed to index. Please try later.");
          return;
        }
        System.out.println("****Thanks for your indexing inquiry****.");
        // client want to index folder
      } else if (Integer.parseInt(userRequestMode) == 2) {
        int count = 0;
        int size = allFileNamesList.size();
        while (allFileNamesList.size() > 0) {
          String currFileName = allFileNamesList.get(0);
          System.out.println("*****currentFile is :****" + currFileName);
          if (indexQuery(currFileName)) {
            count++;
          }
          allFileNamesList.remove(currFileName);
        }
        if (count == size) {
          JOptionPane.showMessageDialog(null, "Indexing for all books completed!");
        } else {
          JOptionPane.showMessageDialog(null,
              "Some books failed to index.");
        }
        // client want to search for words
      } else if (Integer.parseInt(userRequestMode) == 3) {
        String enteredKeyWords = JOptionPane.showInputDialog("Enter the keywords you want to " +
            "search for, separated by space(e.g. love peace)");
        while (enteredKeyWords == null || enteredKeyWords.length() == 0) {
          JOptionPane.showMessageDialog(null, "Invalid input! Please Try again!");
          enteredKeyWords = JOptionPane.showInputDialog("Enter the keywords you want to " +
              "search for, separated by space(e.g. love peace)");
          if (enteredKeyWords == null) {
            return;
          }
        }
        // send strings to search server
        searchQuery(enteredKeyWords);
        // client wants to index all files in the book directory
        System.out.println("****Thanks for your indexing inquiry****.");
      } else {
        System.err.println("Invalid input.");
        System.out.println("Instruction: please enter only 1 for indexing a file, 2 for " +
            "indexing a folder, 3 for searching");
      }
    }
  }


  private static List<String> getAllFileNames() {
    File folder = new File("./books");
    File[] listOfFiles = folder.listFiles();
    List<String> fileNames = new ArrayList<>();
    assert listOfFiles != null;
    for (int i = 0; i < listOfFiles.length; i++) {
      if (listOfFiles[i].isFile() && listOfFiles[i].getName().endsWith(".txt")) {
        fileNames.add(listOfFiles[i].getName());
      }
    }
    return fileNames;
  }

  private static boolean indexQuery(String fileName) {
    Callable<Object> task = new Callable<Object>() {
      public Object call() throws
          NotBoundException, MalformedURLException, RemoteException {
        getTime();
        return indexFile(fileName);
      }
    };
    Boolean result = (Boolean) executeWithTimeout(task);
    if (result) {
      getTime();
      return true;
    } else {
      System.out.println("****Error(indexQuery): please try later.****");
      return false;
    }
  }

  private static boolean indexFile(String fileName) throws
      NotBoundException, MalformedURLException, RemoteException {
    searchForClientStub = (SearchForClientQuery) Naming
        .lookup("//" + searchServerAddr.toString() + "/searchServerForClient");
    boolean result = searchForClientStub.indexSingleFile("books/" + fileName);
    if (result) {
      System.out.println("Indexing is completed.");
      return true;
    }
    return false;
  }

  private static void searchQuery(String keyWords) {
    Callable<Object> task = new Callable<Object>() {
      public Object call() throws
          NotBoundException, MalformedURLException, RemoteException {
        getTime();
        return searchWords(keyWords);
      }
    };
    Boolean result = (Boolean) executeWithTimeout(task);
    if (result == null) {
      System.err.println("****Exception occured in executeWithTimeout.****");
      JOptionPane.showMessageDialog(null, "Error: Please try search later");
    } else if (result) {
      getTime();
    } else {
      System.out.println("****Error(searchQuery): please try later.****");
      JOptionPane.showMessageDialog(null, "Error(searchQuery): Please try search later");
    }
  }

  private static boolean searchWords(String keyWords) throws
      NotBoundException, MalformedURLException, RemoteException {
    searchForClientStub = (SearchForClientQuery) Naming
        .lookup("//" + searchServerAddr.toString() + "/searchServerForClient");
    String book = searchForClientStub.searchKeyWords(keyWords);
    List<String> bookList = new ArrayList<>();
    bookList.addAll(Arrays.asList(book.split(";")));
    if (bookList == null || bookList.size() == 0) {
      System.out.println("No result found.");
    }
    StringBuffer sb = new StringBuffer();
    System.out.println("Searching result (in frequency order):");
    for (int i = 0; i < bookList.size(); i++) {
      sb.append("  [" + (i + 1) + "] " + bookList.get(i) + "\n");
      System.out.println("  [" + (i + 1) + "] " + bookList.get(i));
    }
    JOptionPane.showMessageDialog(null, "Searching result (in frequency order):\n" + sb);
    System.out.println();
    return true;
  }
}


