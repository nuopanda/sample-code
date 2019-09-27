package project4;


import java.io.FileWriter;
import java.rmi.Naming;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;


/**
 * The type Acceptor query remote.
 */
public class AcceptorQueryRemote extends UnicastRemoteObject implements AcceptorQuery {

  private static final long serialVersionUID = 1L;
  private ConcurrentHashMap<String, String> map = new ConcurrentHashMap<>();
  private Integer currID;
  private Integer currSeq = null;
  private static FileWriter fw = null;
  private final static SimpleDateFormat sdf = new SimpleDateFormat("yyyy-mm-dd.hh.MM.ss.ms");
  private static final String[] serverAddrs = {"", "2010", "2020", "2030", "2040", "2050"};
  private static AcceptorQuery acceptorStub;

  /**
   * Instantiates a new Query remote.
   *
   * @param myID the my id
   * @throws RemoteException the remote exception
   */
  protected AcceptorQueryRemote(Integer myID) throws RemoteException {
    super();
    this.currID = myID;
    try {
      fw = new FileWriter("Server" + currID + "Log.txt", true);
//      fw.write(getTime() + "start server " + currID);
//      fw.write("\n");
//      fw.flush();
    } catch (Exception e) {
      System.out.println("Error opening log file server closing");
      e.printStackTrace();
    }
  }

  @Override
  public void populateMyMap() throws RemoteException {
    int serverNum = 1;
    while (serverNum < serverAddrs.length) {
      if (serverNum == this.currID) {
        serverNum++;
        continue;
      }
      try {
        acceptorStub = (AcceptorQuery) Naming
            .lookup("//localhost:" + serverAddrs[serverNum] + "/Acceptor");
        acceptorStub.populateMap(currID);
        break;
      } catch (Exception e) {
        serverNum++;
      }
    }
  }

  /**
   * Gets time.
   *
   * @return the time
   */
  public static String getTime() {
    long sendTime = System.currentTimeMillis();
    Date sendDate = new Date(sendTime);
    return sdf.format(sendDate);
  }

  @Override
  public Boolean alive() throws RemoteException {
    //System.out.println("isAlive");
    return true;

  }

  @Override
  public Boolean promise(Integer seq) throws RemoteException {
    //System.out.println("proposer sequence: " + seq);
    //System.out.println("curr sequence:" + currSeq);
    if (currSeq == null || seq >= currSeq) {
      currSeq = seq;
      try {
        fw = new FileWriter("Server" + currID + "Log.txt", true);
        fw.write(getTime() + ":promised\n");
        fw.flush();
      } catch (Exception e) {
        System.err.println("error: file writer");
        System.err.println(e.toString());
      }
      return true;
    }
    try {
      fw = new FileWriter("Server" + currID + "Log.txt", true);
      fw.write(getTime() + ":not promised");
      fw.flush();
    } catch (Exception e) {
      System.err.println("error: file writer");
      System.err.println(e.toString());
    }
    return false;
  }

  @Override
  public Boolean putAccept(String key, String value, Integer seq) throws RemoteException {
    //System.out.println("acceptor received sequence : " + seq + "curSeq: " + currSeq);
    if (seq.equals(currSeq)) {
      this.map.put(key, value);
      try {
        fw = new FileWriter("Server" + currID + "Log.txt", true);
        fw.write(getTime() + ":put key: " + key + ", value: " + value);
        fw.write("\n");
        fw.flush();
      } catch (Exception e) {
        System.err.println("error: file writer");
        System.err.println(e.toString());
      }
      return true;
    }
    return false;
  }

  @Override
  public Boolean deleteAccept(String key, Integer seq) throws RemoteException {
    if (seq.equals(currSeq)) {
      this.map.remove(key);
      try {
        fw = new FileWriter("Server" + currID + "Log.txt", true);
        fw.write(getTime() + ":delete key: " + key);
        fw.write("\n");
        fw.flush();
      } catch (Exception e) {
        System.err.println("error: file writer");
        System.err.println(e.toString());
      }
      return true;
    }
    return false;
  }

  @Override
  public Boolean restartPut(String key, String value) throws RemoteException {
    this.map.put(key, value);
    try {
      fw = new FileWriter("Server" + currID + "Log.txt", true);
      fw.write(getTime() + ":put key: " + key + ", value: " + value);
      fw.write("\n");
      fw.flush();
    } catch (Exception e) {
      System.err.println("error: file writer");
      System.err.println(e.toString());
    }
    return true;
  }

  @Override
  public String get(String key) throws RemoteException {
    // mock a timeout
//    try {
//      Thread.sleep(rand.nextInt(6000));
//    } catch (InterruptedException e) {
//      // TODO Auto-generated catch block
//      e.printStackTrace();
//    }
    String result = this.map.containsKey(key) ? this.map.get(key) : "not found the key";
    try {
      fw = new FileWriter("Server" + currID + "Log.txt", true);
      fw.write(getTime() + ":get key: " + key + ", value :" + result);
      fw.write("\n");
      fw.flush();
    } catch (Exception e) {
      System.err.println("error: file writer");
      System.err.println(e.toString());
    }
    return result;
  }

  @Override
  public void populateMap(Integer serverNum) throws RemoteException {
    try {
      acceptorStub = (AcceptorQuery) Naming
          .lookup("//localhost:" + serverAddrs[serverNum] + "/Acceptor");
      //System.out.println("connected with " + serverAddrs[serverNum]);
      for (Map.Entry<String, String> entry : map.entrySet()) {
        acceptorStub.restartPut(entry.getKey(), entry.getValue());
      }
    } catch (Exception e) {
      System.err.println("populate map error");
//      System.err.println(e.toString());
    }
  }
}


