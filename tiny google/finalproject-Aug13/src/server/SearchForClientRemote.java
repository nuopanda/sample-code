package server;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;


/**
 * The type Search for client remote.
 */
public class SearchForClientRemote extends UnicastRemoteObject implements
    SearchForClientQuery {

  private static final long serialVersionUID = 1L;

  /**
   * Instantiates a new Search for client remote.
   *
   * @throws RemoteException the remote exception
   */
  public SearchForClientRemote() throws RemoteException {
    super();
  }


  public boolean indexSingleFile(String file) throws RemoteException {
    return SearchServer.indexFile(file);
  }

  public String searchKeyWords(String keyWords) throws RemoteException {
    return SearchServer.searchWord(keyWords);
  }


}
