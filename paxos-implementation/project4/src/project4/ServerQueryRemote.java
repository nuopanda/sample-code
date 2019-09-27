package project4;

import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;


/**
 * The type Server query remote.
 */
// act as proposer
public class ServerQueryRemote extends UnicastRemoteObject implements ServerQuery {

  private static final long serialVersionUID = 1L;
  private Random rand = new Random();
  private static int defaultTimeOutInSec = 5;
  private ConcurrentHashMap<String, String> map = new ConcurrentHashMap<>();
  private static final String[] serverAddrs = {"", "2010", "2020", "2030", "2040", "2050"};
  private static AcceptorQuery acceptorStub;
  private Integer myID;
  private Integer minSequence = 0;


  /**
   * Instantiates a new Query remote.
   *
   * @param myID the my id
   * @throws RemoteException the remote exception
   */
  protected ServerQueryRemote(Integer myID) throws RemoteException {
    super();
    this.myID = myID;
  }

  /**
   * Instantiates a new Query remote.
   *
   * @throws RemoteException the remote exception
   */
  protected ServerQueryRemote() throws RemoteException {
    super();
  }

  public Boolean isAlive() throws RemoteException {
    return true;
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
      System.out.println("time out from Acceptor.");
    } catch (Exception e) {
      // System.err.println("Non-timeout exception found.");
      //e.printStackTrace();
    } finally {
      future.cancel(true);
    }
    return false;
  }


  private void proposerSendPut(String key, String value, List<Integer> promisedList, Integer seq) {
    List<Boolean> commit = new ArrayList<>();
    //System.out.println("send to accetor sequence: " + seq);
    for (int i = 0; i < promisedList.size(); i++) {
      commit.add(doPut(promisedList.get(i), key, value, seq));
      //System.out.println("put serverID: " + promisedList.get(i));
    }
    return;
  }

  private void proposerSendDelete(String key, List<Integer> promisedList, Integer seq) {
    List<Boolean> commit = new ArrayList<>();
    for (int i = 0; i < promisedList.size(); i++) {
      commit.add(doDelete(promisedList.get(i), key, seq));
    }
    return;
  }


  private static Boolean doPut(Integer serverNum, String key, String value, Integer seq) {
    Callable<Object> task = new Callable<Object>() {
      public Object call() throws RemoteException, NotBoundException, MalformedURLException {
        acceptorStub = (AcceptorQuery) Naming
            .lookup("//localhost:" + serverAddrs[serverNum] + "/Acceptor");
        return acceptorStub.putAccept(key, value, seq);
      }
    };
    if ((boolean) executeWithTimeout(task)) {
      return true;
    }
    return false;
  }

  private static Boolean doDelete(Integer serverNum, String key, Integer seq) {
    Callable<Object> task = new Callable<Object>() {
      public Object call() throws RemoteException, NotBoundException, MalformedURLException {
        acceptorStub = (AcceptorQuery) Naming
            .lookup("//localhost:" + serverAddrs[serverNum] + "/Acceptor");
        return acceptorStub.deleteAccept(key, seq);
      }
    };
    if ((boolean) executeWithTimeout(task)) {
      return true;
    }
    return false;
  }


  private List<Integer> proposerSendPrepare(Integer seq) {
    //System.out.println("start to prepare");
    List<Integer> promisedList = new ArrayList<>();
    for (int i = 1; i < serverAddrs.length; i++) {
      // use callable for acceptorPrepare
      // if accept return true, add the acceptor to promisedList
      if (acceptorPromise(i, seq)) {
        promisedList.add(i);
      }
    }
    return promisedList;
  }

  private static Boolean acceptorPromise(Integer serverNum, Integer seq) {
    Callable<Object> task = new Callable<Object>() {
      public Object call() throws RemoteException, NotBoundException, MalformedURLException {
        acceptorStub = (AcceptorQuery) Naming
            .lookup("//localhost:" + serverAddrs[serverNum] + "/Acceptor");
        //System.out.println("connected with " + serverAddrs[serverNum]);
        return acceptorStub.promise(seq);
      }
    };
    Boolean result = (Boolean) executeWithTimeout(task);
    //System.out.println(result);
    return result;
  }


  @Override
  public String put(String key, String value) throws RemoteException {
    // proposer prepare and get promised acceptor list
    List<Integer> promisedList = proposerSendPrepare(minSequence);
    if (promisedList.size() >= 3) {
      //promised acceptors accept the proposed operation
      proposerSendPut(key, value, promisedList, minSequence);
      minSequence++;
      return "added";
    }
    minSequence++;
    return "failed to add";
  }

  @Override
  public String delete(String key) throws RemoteException {
    // proposer prepare and get promised acceptor list
    List<Integer> promisedList = proposerSendPrepare(minSequence);
    if (promisedList.size() >= 3) {
      //promised acceptors accept the proposed operation
      proposerSendDelete(key, promisedList, minSequence);
      minSequence++;
      return "deleted";
    }
    minSequence++;
    return "failed to add";
  }

}