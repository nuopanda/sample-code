package project4;



/**
 * The type Server thread.
 */
public class ServerThread implements Runnable {

  private int id;
  private boolean exit;
  private boolean restart;
  Thread t;

  /**
   * Instantiates a new Server thread.
   *
   * @param id the id
   */
  public ServerThread(Integer id) {
    this.id = id;
    t = new Thread(this);
    exit = false;
    t.start();

  }

  public void run() {
    Server server = new Server(id);
    while (!exit) {
      try {
        Thread.sleep(2000);
      } catch (InterruptedException e) {
        System.err.println("Thread is interrupted");
      }
    }
    try {
      server.exit();
    } catch (Exception e) {
      System.err.println("error exit server");
    }
    System.out.println("server " + id + " stopped.");

  }

  /**
   * Stop.
   */
  public void stop() {
    exit = true;
    System.out.println("server" + this.id + " is exiting");
  }

}


