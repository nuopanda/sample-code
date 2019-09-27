package server;

import java.rmi.Remote;
import java.rmi.RemoteException;

/**
 * The interface Search for client query.
 */
public interface SearchForClientQuery extends Remote {

  /**
   * Search key words string.
   *
   * @param keyWords the key words
   * @return the string
   * @throws RemoteException the remote exception
   */
  String searchKeyWords(String keyWords) throws RemoteException;

  /**
   * Index single file boolean.
   *
   * @param file the file
   * @return the boolean
   * @throws RemoteException the remote exception
   */
  boolean indexSingleFile(String file) throws RemoteException;

}
