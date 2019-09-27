package project4;

import java.rmi.Remote;
import java.rmi.RemoteException;


/**
 * The interface Acceptor query.
 */
public interface AcceptorQuery extends Remote {


  /**
   * Promise boolean.
   *
   * @param seq the seq
   * @return the boolean
   * @throws RemoteException the remote exception
   */
  public Boolean promise(Integer seq) throws RemoteException;

  /**
   * Put accept boolean.
   *
   * @param key the key
   * @param value the value
   * @param sequence the sequence
   * @return the boolean
   * @throws RemoteException the remote exception
   */
  public Boolean putAccept(String key, String value, Integer sequence) throws RemoteException;

  /**
   * Delete accept boolean.
   *
   * @param key the key
   * @param sequence the sequence
   * @return the boolean
   * @throws RemoteException the remote exception
   */
  public Boolean deleteAccept(String key, Integer sequence) throws RemoteException;

  /**
   * Alive boolean.
   *
   * @return the boolean
   * @throws RemoteException the remote exception
   */
  public Boolean alive() throws RemoteException;

  /**
   * Get string.
   *
   * @param key the key
   * @return the string
   * @throws RemoteException the remote exception
   */
  public String get(String key) throws RemoteException;

  /**
   * Populate my map.
   *
   * @throws RemoteException the remote exception
   */
  public void populateMyMap() throws RemoteException;

  /**
   * Populate map.
   *
   * @param serverNum the server num
   * @throws RemoteException the remote exception
   */
  public void populateMap(Integer serverNum) throws RemoteException;

  /**
   * Restart put boolean.
   *
   * @param key the key
   * @param value the value
   * @return the boolean
   * @throws RemoteException the remote exception
   */
  public Boolean restartPut(String key, String value) throws RemoteException;

}