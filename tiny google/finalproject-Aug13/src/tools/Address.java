package tools;

import java.io.Serializable;


/**
 * The class Address.
 */
public class Address implements Serializable {

  public String ip;
  public String port;
  private static final long serialVersionUID = 1L;

  /**
   * Instantiates a new Address.
   *
   * @param ip the ip
   * @param port the port
   */
  public Address(String ip, String port) {
    this.ip = ip;
    this.port = port;
  }

  public String toString() {
    if (ip != null || port != null) {
      return new String(ip + ":" + port);
    } else {
      return null;
    }
  }

  /**
   * Gets ip.
   *
   * @return the ip
   */
  public String getIp() {
    return ip;
  }

  /**
   * Sets ip.
   *
   * @param ip the ip
   */
  public void setIp(String ip) {
    this.ip = ip;
  }

  /**
   * Gets port.
   *
   * @return the port
   */
  public String getPort() {
    return port;
  }

  /**
   * Sets port.
   *
   * @param port the port
   */
  public void setPort(String port) {
    this.port = port;
  }
}
