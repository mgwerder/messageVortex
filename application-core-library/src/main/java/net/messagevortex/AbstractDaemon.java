package net.messagevortex;

/**
 * <p>This is an abstract class providing empty hulled methodes for all threadless implementations.</p>
 */
public abstract class AbstractDaemon implements RunningDaemon {

  @Override
  public void startDaemon() {
    // empty starter for all classes without threads
  }

  @Override
  public void stopDaemon() {
    // empty stopper for all classes without threads
  }

  @Override
  public void shutdownDaemon() {
    // empty shutdown for all classes without threads
  }
}
