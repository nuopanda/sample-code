package nameServer;

import java.io.FileWriter;
import java.io.IOException;
import java.net.ServerSocket;
import java.rmi.Naming;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentHashMap;
import server.HelperForNameQuery;
import server.SearchForNameQuery;
import tools.Address;
import tools.WriteToLocalTool;

public class NameServerAbstract {

  protected static Address nameServerAddr;
  protected static String nameForClientQueryAddr = null;
  protected static String nameForServerQueryAddr = null;
  protected static Registry registry = null;
  protected static ServerSocket socket;
  protected static ConcurrentHashMap<Address, Boolean> searchServerAddress;
  protected static ConcurrentHashMap<Address, Boolean> helperServerAddress;
  protected static final Integer CHECK_HEART_BEAT_FREQUENCY = 15000;
  protected static final Integer WRITE_FREQUENCY = 15000;
  protected static SearchForNameQuery searchForNameStub;
  protected static HelperForNameQuery helperForNameStub;
  protected static final String SEARCH_SERVER_REGISTRATION_LOCAL_PATH = "SearchServerAddress.txt";
  protected static final String HELPER_SERVER_REGISTRATION_LOCAL_PATH = "HelperServerAddress.txt";

  public NameServerAbstract() {
    searchServerAddress = new ConcurrentHashMap<>();
    helperServerAddress = new ConcurrentHashMap<>();
  }


  /**
   * Add to public dns.
   */
  protected void addToPublicDNS() {
    try {
      FileWriter fw = new FileWriter("publicDNS.txt", true);
      fw.write(nameServerAddr.ip + ":" + nameServerAddr.port);
      fw.write("\n");
      fw.flush();
    } catch (IOException e) {
      System.err.println(
          "Error: failed to write Name Server's address to publicDNS.txt");
    }
  }

  /**
   * Remote interface registration.
   */
  protected void remoteInterfaceRegistration() {
    try {
      registry = LocateRegistry.createRegistry(Integer.parseInt(nameServerAddr.getPort()));
      NameForClientQuery clientStub = new NameForClientRemote();
      NameForServerQuery serverStub = new NameForServerRemote();
      Naming.bind(nameForClientQueryAddr, clientStub);
      Naming.bind(nameForServerQueryAddr, serverStub);
      System.out.println("nameServer RMI is ready.");
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  /**
   * Check server heart beat.
   */
  protected void checkServerHeartBeat() {
    Timer timer = new Timer();
    timer.schedule(new TimerTask() {
      @Override
      public void run() {
        System.out.println("Name Server is checking search servers' heart beats...");
        for (Address addr : searchServerAddress.keySet()) {
          System.out.println("add:" + addr.toString());
          try {
            searchForNameStub = (SearchForNameQuery) Naming
                .lookup("//" + addr.toString() + "/searchServerForName");
            searchForNameStub.sendHeartBeat();
          } catch (Exception e) {
            //e.printStackTrace();
            System.out.println("Search Server :" + addr.toString()
                + " is removed from registration list.");
            searchServerAddress.remove(addr);
          }
        }
        System.out.println("Name Server is checking helper servers' heart beats...");
        for (Address addr : helperServerAddress.keySet()) {
          try {
            helperForNameStub = (HelperForNameQuery) Naming
                .lookup("//" + addr.toString() + "/helperServerForName");
            helperForNameStub.sendHeartBeat();
          } catch (Exception e) {
            System.out.println("Helper Server :" + addr.toString()
                + " is removed from registration list.");
            helperServerAddress.remove(addr);
          }
        }
        System.out
            .println("Available search Server : " + NameServerAbstract.searchServerAddress.size());
        System.out
            .println("Available helper Server : " + NameServerAbstract.helperServerAddress.size());
      }
    }, 0, CHECK_HEART_BEAT_FREQUENCY);
  }


  /**
   * Sets write to local timer.
   *
   * @throws IOException the io exception
   */
  protected void setWriteToLocalTimer() throws IOException {
    Timer writeTimer = new Timer();
    writeTimer.schedule(new TimerTask() {
      @Override
      public void run() {
        try {
          WriteToLocalTool.writeObjectToLocal(searchServerAddress,
              SEARCH_SERVER_REGISTRATION_LOCAL_PATH);
          WriteToLocalTool.writeObjectToLocal(helperServerAddress,
              HELPER_SERVER_REGISTRATION_LOCAL_PATH);
          System.out.println("Writing Servers on local is completed!");
        } catch (IOException e) {
          System.err
              .println("Failed to write onto local :" + e.toString());
        }
      }
    }, 0, WRITE_FREQUENCY);
  }

  @SuppressWarnings("unchecked")
  protected void copyServerFromLocal() {
    searchServerAddress = (ConcurrentHashMap<Address, Boolean>) WriteToLocalTool
        .readObjectFromLocal(SEARCH_SERVER_REGISTRATION_LOCAL_PATH);
    helperServerAddress = (ConcurrentHashMap<Address, Boolean>) WriteToLocalTool
        .readObjectFromLocal(HELPER_SERVER_REGISTRATION_LOCAL_PATH);
    System.out
        .println("Available search Server : " + NameServerAbstract.searchServerAddress.size());
    System.out
        .println("Available helper Server : " + NameServerAbstract.helperServerAddress.size());
  }
}
