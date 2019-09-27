package server;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.concurrent.ConcurrentHashMap;

/**
 * The type Helper for search remote.
 */
public class HelperForSearchRemote extends UnicastRemoteObject implements HelperForSearchQuery {
  private static final long serialVersionUID = 1L;

  /**
   * Instantiates a new Helper for search remote.
   *
   * @throws RemoteException the remote exception
   */
  public HelperForSearchRemote() throws RemoteException {
    super();
  }

  public ConcurrentHashMap<String, Integer> doHelperTask(String workLoad) throws RemoteException {
    return HelperServer.doTask(workLoad);
  }
}
