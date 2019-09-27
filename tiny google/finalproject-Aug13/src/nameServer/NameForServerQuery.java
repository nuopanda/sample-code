package nameServer;

import java.rmi.Remote;
import java.rmi.RemoteException;
import tools.Address;

/**
 * The interface Name for server query.
 */
public interface NameForServerQuery extends Remote {

  /**
   * Search server registration boolean.
   *
   * @param addr the addr
   * @return the boolean
   * @throws RemoteException the remote exception
   */
  public boolean SearchServerRegistration(Address addr) throws RemoteException;

  /**
   * Helper server registration boolean.
   *
   * @param addr the addr
   * @return the boolean
   * @throws RemoteException the remote exception
   */
  public boolean HelperServerRegistration(Address addr) throws RemoteException;

  /**
   * Gets helper server addr.
   *
   * @return the helper server addr
   * @throws RemoteException the remote exception
   */
  public String getHelperServerAddr() throws RemoteException;

  /**
   * Name server heart beat boolean.
   *
   * @return the boolean
   * @throws RemoteException the remote exception
   */
  public boolean NameServerHeartBeat() throws RemoteException;
}
