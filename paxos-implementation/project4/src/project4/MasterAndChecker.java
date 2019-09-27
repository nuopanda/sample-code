package project4;

import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Random;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * The type Heartbeat checker.
 */
public class MasterAndChecker {

  private static final long serialVersionUID = 1L;
  private final static SimpleDateFormat sdf = new SimpleDateFormat("yyyy-mm-dd.hh.MM.ss.ms");
  private static final String[] serverAddrs = {"", "2010", "2020", "2030", "2040", "2050"};
  private static int defaultTimeOutInSec = 8;
  private static AcceptorQuery acceptorStub;


  /**
   * Gets time.
   */
  public static void getTime() {
    long sendTime = System.currentTimeMillis();
    Date sendDate = new Date(sendTime);
    System.out.println(sdf.format(sendDate));
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

    } finally {
      future.cancel(true);
    }
    return false;
  }

  private static Boolean checkAlive(Integer serverNo) {
    Callable<Object> task = new Callable<Object>() {
      public Object call() throws RemoteException, NotBoundException, MalformedURLException {
        acceptorStub = (AcceptorQuery) Naming
            .lookup("//localhost:" + serverAddrs[serverNo] + "/Acceptor");
        return acceptorStub.alive();
      }
    };
    if ((boolean) executeWithTimeout(task)) {
      return true;
    }
    return false;
  }

  // this is a simplified election, just to check which proposer has the largest serverNo
  private static List<Integer> getRestartList() {
    List<Integer> restartList = new ArrayList<>();
    for (int i = 1; i < serverAddrs.length; i++) {
      //System.out.println("checking server: " + i);
      if (!checkAlive(i)) {
        restartList.add(i);
      }
    }
    System.out.println("restart list: ");
    for (int j = 0; j < restartList.size(); j++) {
      int serverID = restartList.get(j);
      System.out.println("server " + serverID);
    }
    return restartList;
  }

  private static void reStart(List<Integer> restartList) {
    for (int i = 0; i < restartList.size(); i++) {
      ServerThread restartServer = new ServerThread(restartList.get(i));
      try {
        Thread.sleep(5000);
      } catch (Exception e) {
        System.err.println("thread sleep error");
      }
      try {
        acceptorStub = (AcceptorQuery) Naming
            .lookup("//localhost:" + serverAddrs[restartList.get(i)] + "/Acceptor");
        acceptorStub.populateMyMap();
      } catch (Exception e) {
        System.err.println("restart error");
        e.printStackTrace();
      }
      getTime();
      System.out.println("server " + restartList.get(i) + " restarted");
    }
  }

  /**
   * The entry point of application.
   *
   * @param args the input arguments
   */
  public static void main(String[] args) {
    MasterAndChecker checker = new MasterAndChecker();
    ServerThread serverT1 = new ServerThread(1);
    ServerThread serverT2 = new ServerThread(2);
    ServerThread serverT3 = new ServerThread(3);
    ServerThread serverT4 = new ServerThread(4);
    ServerThread serverT5 = new ServerThread(5);

    Random rand = new Random();
    while (true) {
      int stop = rand.nextInt(6);
      try {
        Thread.sleep(25000);
        if (stop == 1) {
          serverT1.stop();
        } else if (stop == 2) {
          serverT2.stop();
        } else if (stop == 3) {
          serverT3.stop();
        } else if (stop == 4) {
          serverT4.stop();
        } else if (stop == 5) {
          serverT5.stop();
        }
        Thread.sleep(5000);
        List<Integer> restartList = getRestartList();
        reStart(restartList);
        Thread.sleep(10000);
      } catch (InterruptedException e) {
        System.out.println("Caught:" + e);
      }
    }
  }
}


