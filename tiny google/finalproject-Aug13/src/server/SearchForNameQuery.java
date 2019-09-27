package server;

import java.rmi.Remote;
import java.rmi.RemoteException;

/**
 * The interface Search for name query.
 */
public interface SearchForNameQuery extends Remote {

  /**
   * Send heart beat boolean.
   *
   * @return the boolean
   * @throws RemoteException the remote exception
   */
  boolean sendHeartBeat() throws RemoteException;
}



