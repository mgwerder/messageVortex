package net.messagevortex;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Timer;
import java.util.TimerTask;
import java.util.logging.Level;

import static java.nio.charset.StandardCharsets.UTF_8;

public class MessageVortexController {
  
  private static final java.util.logging.Logger LOGGER;
  
  static {
    LOGGER = MessageVortexLogger.getLogger((new Throwable()).getStackTrace()[0].getClassName());
  }
  
  private static final Object runningLock = new Object();
  private static final int port = 8743;
  private static final ControllerRunner runner = new ControllerRunner();
  private Timer timer = null;
  
  private static class ControllerRunner implements Runnable {
    
    private boolean shutdown = false;
    private Thread runner = null;
    
    public Thread getThread() {
      return runner;
    }
    
    public void setThread(Thread t) {
      runner = t;
    }
    
    /***
     * <p>Thread runner.</p>
     *
     * <p>Do not call this method</p>
     */
    public void run() {
      boolean shutdown = false;
      while (!shutdown) {
        try {
          ServerSocket serverConnection = new ServerSocket(port);
          LOGGER.log(Level.INFO, "MessageVortex controller waits for command");
          Socket s = serverConnection.accept();
          
          // read exactly one line and close socket
          String command = new BufferedReader(
                  new InputStreamReader(s.getInputStream(), UTF_8)
          ).readLine();
          LOGGER.log(Level.INFO, "MessageVortex controller got command \"" + command + "\"");
          
          if (command == null) {
            LOGGER.log(Level.INFO, "MessageVortex controller skips empty command line");
            s.getOutputStream().write("ERROR got empty command\r\n".getBytes(UTF_8));
          } else if (command.toLowerCase().startsWith("shutdown")) {
            LOGGER.log(Level.INFO, "MessageVortex controller executes shutdown command");
            shutdown = true;
            s.getOutputStream().write("OK\r\n".getBytes(UTF_8));
          } else if (command.toLowerCase().startsWith("status")) {
            LOGGER.log(Level.INFO, "MessageVortex controller executes status command");
            s.getOutputStream().write("OK\r\n## Status for all components are green\r\n"
                    .getBytes(UTF_8));
          } else {
            LOGGER.log(Level.WARNING, "MessageVortex controller got illegal command \""
                    + command + "\"");
            s.getOutputStream().write("ERROR unknown command\r\n".getBytes(UTF_8));
          }
          s.close();
          
        } catch (IOException ioe) {
          LOGGER.log(Level.FINE, "Exception while listening/processing controller connection", ioe);
        }
        synchronized (runningLock) {
          shutdown = this.shutdown || shutdown;
        }
      }
    }
    
    /***
     * <p>Wait for a previously initiated shutdown.</p>
     */
    public void waitForShutdown() {
      while (runner.isAlive()) {
        try {
          runner.join();
        } catch (InterruptedException ie) {
          LOGGER.log(Level.FINEST, "ignoring interrupted exception while waiting for shutdown", ie);
        }
      }
    }
    
    
    /***
     * <p>Shutdown Controller and wait for termination.</p>
     */
    public void shutdown() {
      synchronized (runningLock) {
        shutdown = true;
        
        //wake up thread
        try {
          Socket s = new Socket(InetAddress.getLoopbackAddress().getHostAddress(), port);
          s.getOutputStream().write("shutdown\r\n".getBytes(UTF_8));
          s.close();
        } catch (IOException ioe) {
          LOGGER.log(Level.INFO, "IOException while waking up thread for shutdown", ioe);
        }
      }
      waitForShutdown();
    }
    
  }
  
  /***
   * <p>Creates a new vortex controller listening on localhost only.</p>
   */
  public MessageVortexController() {
    Thread t = new Thread(runner);
    t.setName("MessageVortexShutdownController:" + port);
    runner.setThread(t);
    t.start();
  }
  
  /***
   * <p>Wait for shutdown of the runner.</p>
   */
  public void waitForShutdown() {
    runner.waitForShutdown();
  }
  
  /***
   * <p>Sets the timeout when the controller should shutdown.</p>
   *
   * @param milliSeconds the time in milliseconds
   */
  public synchronized void setTimeout(long milliSeconds) {
    if (milliSeconds < 0) {
      return;
    }
    LOGGER.log(Level.INFO, "MessageVortex controller sets timeout to " + milliSeconds / 1000
            + " s");
    if (timer != null) {
      timer.cancel();
    }
    timer = new Timer(true);
    timer.schedule(new TimerTask() {
      @Override
      public void run() {
        runner.shutdown();
      }
    }, milliSeconds);
  }
  
}
