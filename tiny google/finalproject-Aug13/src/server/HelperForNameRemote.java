package server;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

/**
 * The type Helper for name remote.
 */
public class HelperForNameRemote extends UnicastRemoteObject implements HelperForNameQuery {
  private static final long serialVersionUID = 1L;

  public HelperForNameRemote() throws RemoteException {
    super();
  }

  public boolean sendHeartBeat() throws RemoteException {
    System.out.println("Sent heart beat to Name Server.");
    return true;
  }

}
