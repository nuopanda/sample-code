package nameServer;

import java.rmi.Remote;
import java.rmi.RemoteException;
import tools.Address;

/**
 * The interface Name for client query.
 */
public interface NameForClientQuery extends Remote {

  /**
   * Gets search server addr.
   *
   * @return the search server addr
   * @throws RemoteException the remote exception
   */
  public Address getSearchServerAddr() throws RemoteException;

}
