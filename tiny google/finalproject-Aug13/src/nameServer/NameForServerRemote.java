package nameServer;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import tools.Address;

/**
 * The class Name for server remote.
 */
public class NameForServerRemote extends UnicastRemoteObject implements
    NameForServerQuery {

  private static final long serialVersionUID = 1L;


  public NameForServerRemote() throws RemoteException {
    super();
  }


  public boolean SearchServerRegistration(Address addr) throws RemoteException {
    System.out.println("Name Server get query from search server:" + addr.toString());
    NameServerAbstract.searchServerAddress.put(addr, true);
    System.out.println("Search Server " + addr.toString() + " is registered.");
    System.out
        .println("Available Search Server : " + NameServerAbstract.searchServerAddress.size());
    System.out
        .println("Available helper Server : " + NameServerAbstract.helperServerAddress.size());
    return true;
  }

  public boolean HelperServerRegistration(Address addr) throws RemoteException {
    System.out.println("Name Server get query from helper server:" + addr.toString());
    NameServerAbstract.helperServerAddress.put(addr, true);
    System.out.println("Helper Server " + addr.toString() + " is registered.");
    System.out
        .println("Available Search Server : " + NameServerAbstract.searchServerAddress.size());
    System.out
        .println("Available helper Server : " + NameServerAbstract.helperServerAddress.size());
    return true;
  }

  public String getHelperServerAddr() throws RemoteException {
    String availableHelperServer = "";
    // convert helper address list to string, divided by "A" (ip:port"A")
    for (Address addr : NameServerAbstract.helperServerAddress.keySet()) {
      String currAddr = addr.toString();
      availableHelperServer += currAddr + "A";
    }
    availableHelperServer.substring(0, availableHelperServer.length() - 1);
    return availableHelperServer;
  }

  public boolean NameServerHeartBeat() throws RemoteException {
    System.out.println("sending heart beat of Name Server to stand by...");
    return true;
  }
}
