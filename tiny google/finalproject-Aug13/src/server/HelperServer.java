package server;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.net.MalformedURLException;
import java.net.ServerSocket;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import nameServer.NameForServerQuery;
import tools.Address;
import tools.ServerTool;

/**
 * The type Helper server.
 */
public class HelperServer {

  protected static Address nameServerAddr;
  protected static ServerSocket socket;
  protected static Address helperServerAddr;
  protected static String helperForNameQueryAddr = null;
  protected static String helperForServerQueryAddr = null;
  protected static Registry registry = null;
  protected static NameForServerQuery nameForHelperStub;
  private static int DEFAULT_TIME_OUT = 200;


  /**
   * Remote interface registration.
   */
  protected static void remoteInterfaceRegistration() {
    try {
      registry = LocateRegistry.createRegistry(Integer.parseInt(helperServerAddr.getPort()));
      HelperForNameQuery helperStub = new HelperForNameRemote();
      HelperForSearchQuery helperSearchStub = new HelperForSearchRemote();
      Naming.bind(helperForNameQueryAddr, helperStub);
      Naming.bind(helperForServerQueryAddr, helperSearchStub);
      System.out.println("Helper Server RMI is ready.");
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  private static Object executeWithTimeout(Callable<Object> task) {
    ExecutorService executor = Executors.newCachedThreadPool();
    Future<Object> future = executor.submit(task);
    try {
      return future.get(DEFAULT_TIME_OUT, TimeUnit.SECONDS);
    } catch (TimeoutException e) {
      System.out.println("time out.");
    } catch (Exception e) {
    } finally {
      future.cancel(true);
    }
    return null;
  }

  /**
   * Do task concurrent hash map.
   *
   * @param workLoad the work load
   * @return the concurrent hash map
   */
  @SuppressWarnings("unchecked")
  public static ConcurrentHashMap<String, Integer> doTask(String workLoad) {
    Callable<Object> task = () -> helperServerDoTask(workLoad);
    ConcurrentHashMap<String, Integer> result = (ConcurrentHashMap<String, Integer>) executeWithTimeout(
        task);
    if (result == null) {
      System.err.println("Error: Helper Server failed to execute thread!");
    }
    return result;
  }


  private static ConcurrentHashMap<String, Integer> helperServerDoTask(String workLoad)
      throws IOException {
    String[] list = workLoad.split(":");
    HelperTaskWrapper task = new HelperTaskWrapper(list[0], Long.parseLong(list[1]),
        Long.parseLong(list[2]));
    System.out.println("Helper server received a workload from Search Server.");
    ConcurrentHashMap<String, Integer> countByWordTable = mapper(task);
    System.out.println("Helper server finished mapper task of file: " + task.getTaskFilename());
    return countByWordTable;
  }


  /**
   * Mapper task on a helper.
   *
   * @param helperTask
   * @return the concurrent hash map
   * @throws IOException the io exception
   */
  public static ConcurrentHashMap<String, Integer> mapper(HelperTaskWrapper helperTask)
      throws IOException {
    String filePath = helperTask.getFilePath();
    if (filePath == null || !new File(filePath).exists()) {
      System.err.println("Invalid file path!");
      return null;
    }
    RandomAccessFile file = new RandomAccessFile(filePath, "rw");
    ConcurrentHashMap<String, Integer> wordCountTable = new ConcurrentHashMap<>();
    long start = helperTask.getStart(), end = helperTask.getEnd();
    StringBuffer sb = new StringBuffer();
    for (long i = start; i <= end; i++) {
      file.seek(i);
      char curChar = (char) file.read();
      if (Character.isLetterOrDigit(curChar) || curChar == '-') {
        sb.append(curChar);
      } else {
        String word = sb.toString().toLowerCase();
        wordCountTable.put(word, wordCountTable.getOrDefault(word, 0) + 1);
        sb = new StringBuffer();
        continue;
      }
    }
    file.close();
    return wordCountTable;
  }


  /**
   * The entry point of application.
   *
   * @param args the input arguments
   * @throws RemoteException the remote exception
   * @throws NotBoundException the not bound exception
   * @throws MalformedURLException the malformed url exception
   */
  public static void main(String[] args)
      throws RemoteException, NotBoundException, MalformedURLException {
    try {
      socket = new ServerSocket(0);
      helperServerAddr = ServerTool.getAddress(socket);
      System.out.println("Helper server is starting: " + helperServerAddr.toString());
    } catch (Exception e) {
      System.err.println("Error: socket " + e.toString());
    }
    helperForNameQueryAddr = "//" + helperServerAddr.toString() + "/helperServerForName";
    helperForServerQueryAddr = "//" + helperServerAddr.toString() + "/helperServerForSearch";
    // get Name Server address
    nameServerAddr = ServerTool.getNameServerAddress();
    System.out.println("Found Name Server's address :" + nameServerAddr.toString());
    if (nameServerAddr == null) {
      System.err.println(
          "Error: No available Name Server. \nPlease check if name server is operating...");
      System.exit(1);
    }
    // call Name Server remote interface, register to Name Server
    nameForHelperStub = (NameForServerQuery) Naming
        .lookup("//" + nameServerAddr.toString() + "/NameServerForServer");
    nameForHelperStub.HelperServerRegistration(helperServerAddr);
    System.out.println("Sent registration to Name Server.");
    remoteInterfaceRegistration();
  }
}
