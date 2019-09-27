package nameServer;

import java.io.IOException;
import java.net.ServerSocket;
import tools.ServerTool;


/**
 * The type Name server.
 */
public class NameServer extends NameServerAbstract {


  public NameServer() {
    super();
  }

  /**
   * The entry point of application.
   *
   * @param args the input arguments
   */
  public static void main(String[] args) {
    NameServer nameServer = new NameServer();
    try {
      socket = new ServerSocket(0);
      nameServerAddr = ServerTool.getAddress(socket);
      System.out.println(nameServerAddr.toString());
      nameServer.addToPublicDNS();
      nameForServerQueryAddr = "//" + nameServerAddr + "/NameServerForServer";
      nameForClientQueryAddr = "//" + nameServerAddr + "/NameServerForClient";
      nameServer.remoteInterfaceRegistration();
      nameServer.checkServerHeartBeat();
      nameServer.setWriteToLocalTimer();
    } catch (IOException e) {
      System.err.println("Name Server error: " + e.toString());
    }
  }
}
