package server;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

/**
 * The type Search for name remote.
 */
public class SearchForNameRemote extends UnicastRemoteObject implements SearchForNameQuery {

  private static final long serialVersionUID = 1L;

  /**
   * Instantiates a new Search for name remote.
   *
   * @throws RemoteException the remote exception
   */
  public SearchForNameRemote() throws RemoteException {
    super();
  }

  public boolean sendHeartBeat() throws RemoteException {
    System.out.println("Sent heart beat to Name Server.");
    return true;
  }


}
