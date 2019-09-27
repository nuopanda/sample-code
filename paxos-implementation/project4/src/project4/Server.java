package project4;

import java.io.Serializable;
import java.rmi.Naming;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;


/**
 * The type Server.
 */
public class Server implements Serializable {

  private Integer portNum;
  private String addr = "//localhost:";
  private String serverAddr = null;
  private String serverQueryAddr = null;
  private String acceptorQueryAddr = null;
  private Integer port = null;
  private Registry registry = null;

  /**
   * Instantiates a new Server.
   *
   * @param portNum the port num
   */
  public Server(Integer portNum) {
    this.portNum = portNum;
    switch (portNum) {
      case 1:
        port = 2010;
        serverAddr = addr + "2010/Server";
        serverQueryAddr = addr + "2010/ServerQuery";
        acceptorQueryAddr = addr + "2010/Acceptor";
        break;
      case 2:
        port = 2020;
        serverAddr = addr + "2020/Server";
        serverQueryAddr = addr + "2020/ServerQuery";
        acceptorQueryAddr = addr + "2020/Acceptor";
        break;
      case 3:
        port = 2030;
        serverAddr = addr + "2030/Server";
        serverQueryAddr = addr + "2030/ServerQuery";
        acceptorQueryAddr = addr + "2030/Acceptor";
        break;
      case 4:
        port = 2040;
        serverAddr = addr + "2040/Server";
        serverQueryAddr = addr + "2040/ServerQuery";
        acceptorQueryAddr = addr + "2040/Acceptor";
        break;
      case 5:
        port = 2050;
        serverAddr = addr + "2050/Server";
        serverQueryAddr = addr + "2050/ServerQuery";
        acceptorQueryAddr = addr + "2050/Acceptor";
        break;
      default:
        System.err.println(
            "Instruction: input Server No. (1-5)");
        System.exit(1);
    }
    try {
      registry = LocateRegistry.createRegistry(port);
      Query stub = new QueryRemote();
      ServerQuery serverStub = new ServerQueryRemote(portNum);
      AcceptorQuery acceptorStub = new AcceptorQueryRemote(portNum);
      Naming.bind(serverAddr, stub);
      Naming.bind(serverQueryAddr, serverStub);
      Naming.bind(acceptorQueryAddr, acceptorStub);
      System.out.println("Server " + port + " is ready. Address: " + serverAddr);
    } catch (
        Exception e) {
      //System.err.println("Server " + port + " exception: " + e.toString());
      //e.printStackTrace();
    }
  }

  /**
   * Exit.
   *
   * @throws RemoteException the remote exception
   */
  public void exit() throws RemoteException {
    try {
      Naming.unbind(serverAddr);
      Naming.unbind(serverQueryAddr);
      Naming.unbind(acceptorQueryAddr);
      UnicastRemoteObject.unexportObject(registry, true);
      //System.out.println("exiting server " + portNum);
    } catch (Exception e) {
    }
  }
}
