package net.messagevortex;

public interface RunningDaemon {
  /**
   * <p>Initializes and starts all threads required to run the daemon.</p>
   */
  void startDaemon();

  /**
   * <p>Stopps all daemon threads and frees all temporary resources.</p>
   */
  void stopDaemon();

  /**
   * <p>Shuts this class down.</p>
   *
   * <p>This frees all resources and ends all threads for an application or layer shutdown.
   * It is not possible to call start() after running shutdown().</p>
   */
  void shutdownDaemon();
}
