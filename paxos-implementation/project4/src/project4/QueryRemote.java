package project4;


import java.net.MalformedURLException;
import java.rmi.AccessException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.server.RemoteServer;
import java.rmi.server.ServerNotActiveException;
import java.rmi.server.UnicastRemoteObject;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Random;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;


/**
 * The class QueryRemote.
 */
public class QueryRemote extends UnicastRemoteObject implements Query {

  private static final long serialVersionUID = 1L;
  private final static SimpleDateFormat sdf = new SimpleDateFormat("yyyy-mm-dd.hh.MM.ss.ms");
  private Random rand = new Random();
  private static final String[] serverAddrs = {"","2010", "2020", "2030", "2040", "2050"};
  private static int[] alive = {0, 0, 0, 0, 0};
  private static int defaultTimeOutInSec = 5;
  private static ServerQuery serverStub;
  private static AcceptorQuery accpetorStub;

  /**
   * Instantiates a new Query remote.
   *
   * @throws RemoteException the remote exception
   */
  protected QueryRemote() throws RemoteException {
    super();
  }

  /**
   * Gets time.
   */
  public static void getTime() {
    long sendTime = System.currentTimeMillis();
    Date sendDate = new Date(sendTime);
    System.out.println(sdf.format(sendDate));
  }

  private String getClientAddress() throws AccessException {
    String hostName = null;
    try {
      hostName = RemoteServer.getClientHost();
    } catch (ServerNotActiveException e) {
      // if no remote host is currently executing this method,
      // then is localhost, and the access should be granted.
    }
    if (hostName == null) {
      throw new AccessException("Can not get remote host address.");
    }
    return hostName;
  }

  /**
   * Execute with Timeout.
   */
  private static Object executeWithTimeout(Callable<Object> task) {
    ExecutorService executor = Executors.newCachedThreadPool();
    Future<Object> future = executor.submit(task);
    try {
      return future.get(defaultTimeOutInSec, TimeUnit.SECONDS);
    } catch (TimeoutException e) {
      System.out.println("time out");
    } catch (Exception e) {
//      System.err.println("Non-timeout exception found.");
//      e.printStackTrace();
    } finally {
      future.cancel(true);
    }
    return false;
  }

  private static Boolean checkAlive(Integer serverNo) {
    Callable<Object> task = new Callable<Object>() {
      public Object call() throws RemoteException, NotBoundException, MalformedURLException {
        serverStub = (ServerQuery) Naming
            .lookup("//localhost:" + serverAddrs[serverNo] + "/ServerQuery");
        return serverStub.isAlive();
      }
    };
    if ((boolean) executeWithTimeout(task)) {
      return true;
    }
    return false;
  }

  // this is a simplified election, just to check which proposer has the largest serverNo
  private static Integer election(Integer serverNo) {
    int runningMaxIndex = -1;
    for (int i = 1; i < serverAddrs.length; i++) {
      if (checkAlive(i)) {
        runningMaxIndex = i;
      }
    }
    return runningMaxIndex;
  }


  private static String ProposePut(Integer leader, String key, String value) {
    Callable<Object> task = new Callable<Object>() {
      public Object call() throws RemoteException, NotBoundException, MalformedURLException {
        serverStub = (ServerQuery) Naming
            .lookup("//localhost:" + serverAddrs[leader] + "/ServerQuery");
        return serverStub.put(key, value);
      }
    };
    return (String) executeWithTimeout(task);
  }


  @Override
  public String put(String key, String value, Integer serverNo) throws RemoteException {
    System.out.println("--------------------------------------------------------");
    getTime();
    System.out.println(
        "Received from client " + getClientAddress() + ": put key: " + key + ", value: " + value);
    // call proposer
    Integer leader = election(serverNo);
    System.out.println("election completed: leader is Server " + leader);
    // ask leader to propose and do the operation
    String result = ProposePut(leader, key, value);
    getTime();
    if (result.equals("added")) {
      System.out.println("Sent to client: key: " + key + ", value: " + value + " has been added");
    } else {
      System.out
          .println("Sent to client: key: " + key + ", value: " + value + " failed to be added");
    }
    return result;
  }

  @Override
  public String get(String key, Integer serverNo) throws RemoteException {
    System.out.println("--------------------------------------------------------");
    getTime();
    System.out.println("Received from client " + getClientAddress() + ": get key: " + key);
    String result = ProposeGet(serverNo, key);
    getTime();
    System.out.println("Sent to client: key: " + key + ", value: " + result);
    return result;
  }


  private static String ProposeGet(Integer serverNo, String key) {
    Callable<Object> task = new Callable<Object>() {
      public Object call() throws RemoteException, NotBoundException, MalformedURLException {
        accpetorStub = (AcceptorQuery) Naming
            .lookup("//localhost:" + serverAddrs[serverNo] + "/Acceptor");
        return accpetorStub.get(key);
      }
    };
    return (String) executeWithTimeout(task);
  }


  @Override
  public String delete(String key, Integer serverNo) throws RemoteException {
    System.out.println("--------------------------------------------------------");
    getTime();
    System.out.println("Received from client " + getClientAddress() + ": delete key: " + key);
    // modify below
    // call proposer
    Integer leader = election(serverNo);
    System.out.println("election completed: leader is Server " + leader);
    String result = ProposeDelete(leader, key);
    // ask leader to propose and do the operation
    getTime();
    if (result.equals("deleted")) {
      System.out.println("Sent to client: key: " + key + " has been deleted");
    } else {
      System.out.println("Sent to client: key: " + key + " failed to be deleted");
    }
    return result;
  }


  private static String ProposeDelete(Integer leader, String key) {
    Callable<Object> task = new Callable<Object>() {
      public Object call() throws RemoteException, NotBoundException, MalformedURLException {
        serverStub = (ServerQuery) Naming
            .lookup("//localhost:" + serverAddrs[leader] + "/ServerQuery");
        return serverStub.delete(key);
      }
    };
    return (String) executeWithTimeout(task);
  }


  @Override
  public String invalidQuery(String line) throws RemoteException {
    try {
      Thread.sleep(rand.nextInt(6000));
    } catch (InterruptedException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
    getClientAddress();
    getTime();
    System.out.println(
        "received malformed request of length " + line.length() + " from " + getClientAddress());
    getTime();
    System.out.println("Sent to client: " + line + " is invalid ");
    return "invalid";
  }
}
