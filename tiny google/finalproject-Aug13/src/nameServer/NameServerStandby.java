package nameServer;

import java.io.IOException;
import java.net.ServerSocket;
import java.rmi.Naming;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentHashMap;
import tools.Address;
import tools.ServerTool;
import tools.WriteToLocalTool;

/**
 * The type Name server standby.
 */
public class NameServerStandby extends NameServerAbstract {

  private final static SimpleDateFormat sdf = new SimpleDateFormat("yyyy-mm-dd.hh.MM.ss.ms");
  protected static Address originalNameServerAddr;
  private static final int STAND_BY_CHECK_FREQUENCY = 10000;
  protected static NameForServerQuery nameToNameStub;


  public NameServerStandby() {
    super();
  }

  /**
   * The entry point of application.
   *
   * @param args the input arguments
   */
  public static void main(String[] args) {
    NameServerStandby nameServerStandBy = new NameServerStandby();
    System.out
        .println("NameServerStandby will take over if the original Name Server fails.");
    originalNameServerAddr = ServerTool.getNameServerAddress();
    System.out.println("Name Server's address is " + originalNameServerAddr.toString());
    if (originalNameServerAddr == null) {
      return;
    }
    nameServerStandBy.checkOriginalNameServer();
  }

  public static void getTime() {
    long sendTime = System.currentTimeMillis();
    Date sendDate = new Date(sendTime);
    System.out.println(sdf.format(sendDate));
  }
  /**
   * Check original name server.
   */
  public void checkOriginalNameServer() {
    Timer standbyTimer = new Timer();
    standbyTimer.schedule(new TimerTask() {
      @Override
      public void run() {
        try {
          getTime();
          nameToNameStub = (NameForServerQuery) Naming
              .lookup("//" + originalNameServerAddr.toString() + "/NameServerForServer");
          nameToNameStub.NameServerHeartBeat();
        } catch (Exception e) {
          System.out.println(
              "Failed to get heart beat from Name Server :" + originalNameServerAddr.toString());
          try {
            socket = new ServerSocket(0);
            nameServerAddr = ServerTool.getAddress(socket);
            System.out
                .println("Name Server Stand by takes the role :" + nameServerAddr.toString());
            addToPublicDNS();
            nameForServerQueryAddr = "//" + nameServerAddr + "/NameServerForServer";
            nameForClientQueryAddr = "//" + nameServerAddr + "/NameServerForClient";
            remoteInterfaceRegistration();
            copyServerFromLocal();
            checkServerHeartBeat();
            setWriteToLocalTimer();
          } catch (IOException ee) {
            System.err.println("Name Server error: " + ee.toString());
          }
        }
      }
    }, 0, STAND_BY_CHECK_FREQUENCY);
  }


//  @SuppressWarnings("unchecked")
//  private void copyServerFromLocal() {
//    searchServerAddress = (ConcurrentHashMap<Address, Boolean>) WriteToLocalTool
//        .readObjectFromLocal(SEARCH_SERVER_REGISTRATION_LOCAL_PATH);
//    helperServerAddress = (ConcurrentHashMap<Address, Boolean>) WriteToLocalTool
//        .readObjectFromLocal(HELPER_SERVER_REGISTRATION_LOCAL_PATH);
//    System.out
//        .println("Available search Server : " + NameServerAbstract.searchServerAddress.size());
//    System.out
//        .println("Available helper Server : " + NameServerAbstract.helperServerAddress.size());
//  }

}


