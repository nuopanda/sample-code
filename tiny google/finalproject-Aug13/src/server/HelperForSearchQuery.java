package server;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.concurrent.ConcurrentHashMap;

/**
 * The interface Helper for search query.
 */
public interface HelperForSearchQuery extends Remote {

  /**
   * Do helper task concurrent hash map.
   *
   * @param workLoad the work load
   * @return the concurrent hash map
   * @throws RemoteException the remote exception
   */
  ConcurrentHashMap<String, Integer> doHelperTask(String workLoad) throws RemoteException;

}

