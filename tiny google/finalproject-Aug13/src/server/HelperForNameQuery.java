package server;

import java.rmi.Remote;
import java.rmi.RemoteException;

/**
 * The interface Helper for name query.
 */
public interface HelperForNameQuery extends Remote {

  /**
   * Send heart beat boolean.
   *
   * @return the boolean
   * @throws RemoteException the remote exception
   */
  boolean sendHeartBeat() throws RemoteException;
}
