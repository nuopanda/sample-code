package project4;

import java.rmi.Remote;
import java.rmi.RemoteException;


/**
 * The interface Server query.
 */
public interface ServerQuery extends Remote {


  /**
   * Is alive boolean.
   *
   * @return the boolean
   * @throws RemoteException the remote exception
   */
  public Boolean isAlive() throws RemoteException;


  /**
   * Put operation.
   *
   * @param key the key
   * @param value the value
   * @return the string
   * @throws RemoteException the remote exception
   */
  public String put(String key, String value) throws RemoteException;


  /**
   * Delete operation.
   *
   * @param key the key
   * @return the string
   * @throws RemoteException the remote exception
   */
  public String delete(String key) throws RemoteException;

}