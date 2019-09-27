package tools;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.ServerSocket;
import java.net.SocketException;
import java.util.Enumeration;

/**
 * The type Server tool.
 */
public class ServerTool {

  /**
   * get the public address of the server
   *
   * @param socket the socket
   * @return the address
   * @throws IOException the io exception
   */
  public static Address getAddress(ServerSocket socket) throws IOException {
    String ip = "";
    try {
      Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
      while (interfaces.hasMoreElements()) {
        NetworkInterface iface = interfaces.nextElement();
        if (iface.isLoopback() || !iface.isUp()) {
          continue;
        }
        Enumeration<InetAddress> addresses = iface.getInetAddresses();
        while (addresses.hasMoreElements()) {
          InetAddress addr = addresses.nextElement();
          ip = addr.getHostAddress();
        }
      }
      String portNum = String.valueOf(socket.getLocalPort());
      //System.out.println("ip :" + ip + " , portNum : " + portNum);
      return new Address(ip, portNum);
    } catch (SocketException e) {
      System.err.println("Socket Exception: get current address error");
    } catch (Exception e) {
      System.err.println("error: get current address");
    } finally {
      socket.close();
    }
    return null;
  }

  /**
   * Get public address for nameServer.NameServer from publicDNS
   *
   * @return the name server address
   */
  public static Address getNameServerAddress() {
    BufferedReader reader = null;
    Address nameServerAddress = null;
    String line;
    try {
      File file = new File("publicDNS.txt");
      reader = new BufferedReader(new FileReader(file));
      while ((line = reader.readLine()) != null) {
        String[] strs = line.split(":");
        String IPaddress = strs[0];
        String port = strs[1];
        if (IPaddress != null && port != null) {
          nameServerAddress = new Address(IPaddress, port);
        }
      }
      if (nameServerAddress != null) {
        return nameServerAddress;
      }
    } catch (FileNotFoundException e) {
      System.err.println("\n  Error: can't find \"publicDNS.txt\".");
    } catch (IOException e) {
      System.err.println("\n  Error: can't read \"publicDNS.txt\".");
    } finally {
      try {
        reader.close();
      } catch (IOException e) {
        System.err.println(e.toString());
      }
    }
    return null;
  }
}
