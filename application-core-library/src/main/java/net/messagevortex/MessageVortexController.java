package net.messagevortex;

import java.util.logging.Level;

import sun.misc.Signal;
import sun.misc.SignalHandler;

public class MessageVortexController implements SignalHandler, Runnable {

  private static final java.util.logging.Logger LOGGER;

  static {
    LOGGER = MessageVortexLogger.getLogger((new Throwable()).getStackTrace()[0].getClassName());
  }

  private boolean shutdown = false;
  private Object runningLock = new Object();
  private Thread runner = new Thread(this);

  public MessageVortexController() {
    runner.setName( "MessageVortexShutdownController");
    runner.start();
  }

  @Override
  public void handle(Signal signal) {
    if ("INT".equals(signal.getName())) {
      LOGGER.log(Level.INFO, "Received SIGINT signal. Will teardown.");

      shutdown();
    } else {
      LOGGER.log(Level.WARNING, "Received unthandled signal SIG" + signal.getName() + ". IGNORING");
    }
  }

  /***
   * <p>Thread runner.</p>
   *
   * <p>Do not call this methode</p>
   * FIXME: move to private class
   */
  public void run() {
    boolean shutdown = false;
    while (!shutdown) {
      try {
        Thread.sleep(100);
      } catch (InterruptedException ie) {
        LOGGER.log(Level.FINEST, "ignoring interrupted exception while waiting for action", ie);
      }
      synchronized (runningLock) {
        shutdown = this.shutdown;
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
    }
    waitForShutdown();
  }


}
