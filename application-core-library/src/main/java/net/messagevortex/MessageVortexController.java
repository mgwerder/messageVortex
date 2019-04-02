package net.messagevortex;

import sun.misc.Signal;
import sun.misc.SignalHandler;

import java.util.logging.Level;

public class MessageVortexController  implements SignalHandler, Runnable {

  private static final java.util.logging.Logger LOGGER;

  static {
    LOGGER = MessageVortexLogger.getLogger((new Throwable()).getStackTrace()[0].getClassName());
  }

  private boolean shutdown = false;
  private Object runningLock = new Object();
  private Thread runner = new Thread(this);

  public MessageVortexController() {
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

  public void run() {
    boolean  shutdown = false;
    while( !shutdown ){
      try {
        Thread.sleep(100);
      } catch(InterruptedException ie) {
        LOGGER.log(Level.FINEST, "ignoring interrupted exception while waiting for action", ie);
      }
      synchronized(runningLock) {
        shutdown = this.shutdown;
      }
    }
  }

  public void waitForShutdown() {
    while( runner.isAlive() ){
      try {
        runner.join();
      } catch(InterruptedException ie) {
        LOGGER.log(Level.FINEST, "ignoring interrupted exception while waiting for shutdown", ie);
      }
    }
  }


  public void shutdown() {
    synchronized(runningLock) {
      shutdown = true;
    }
    waitForShutdown();
  }



}
