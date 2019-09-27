package nameServer;

import java.rmi.AccessException;
import java.rmi.RemoteException;
import java.rmi.server.RemoteServer;
import java.rmi.server.ServerNotActiveException;
import java.rmi.server.UnicastRemoteObject;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Random;
import tools.Address;

/**
 * The class Name for client remote.
 */
public class NameForClientRemote extends UnicastRemoteObject implements
    NameForClientQuery {

  private static final long serialVersionUID = 1L;

  public NameForClientRemote() throws RemoteException {
    super();
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

  public Address getSearchServerAddr() throws RemoteException {
    String clientAddr = getClientAddress();
    System.out.println("Name Server get query from client:" + clientAddr);
    if (NameServerAbstract.searchServerAddress.size() == 0) {
      System.err.println("No available Search Server.");
      return null;
    }
    Address searchServerAddr = null;
    List<Address> keysAsArray = new ArrayList<Address>(
        NameServerAbstract.searchServerAddress.keySet());
    searchServerAddr = keysAsArray.get(0);
    if (searchServerAddr == null) {
      System.err.println("No available Search Server.");
      return null;
    }
    return searchServerAddr;
  }

}
