package net.messagevortex;

import java.lang.management.ManagementFactory;
import java.lang.management.ThreadInfo;
import java.lang.management.ThreadMXBean;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.TimeZone;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * <p>Helper class to debug hanging or uncleared threads.</p>
 */
public class ThreadDumper {

  private static final String CRLF = System.lineSeparator();

  private static final Logger LOGGER;

  static {
    LOGGER = MessageVortexLogger.getLogger((new Throwable()).getStackTrace()[0].getClassName());
  }

  private static final DateFormat df;

  static {
    df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm'Z'");
    TimeZone tz = TimeZone.getTimeZone("UTC");
    df.setTimeZone(tz);
  }

  private static final ScheduledExecutorService scheduler
      = Executors.newScheduledThreadPool(1, new ThreadDumperThreadFactory());

  private static volatile ThreadGroup rootTG = null;

  private static class ThreadDumperRunner implements Runnable {
    @Override
    public void run() {
      try {
        LOGGER.log(Level.INFO, "dumping threads");
        for (String s : getThreadDump(false).split(CRLF)) {
          LOGGER.log(Level.INFO, s);
        }
        LOGGER.log(Level.INFO, "threads dumped");
      } catch (Exception e) {
        LOGGER.log(Level.SEVERE, "Error while executing thread dump", e);
      }
    }
  }

  private static class ThreadDumperThreadFactory implements ThreadFactory {
    public Thread newThread(Runnable r) {
      Thread t = new Thread(r, "ThreadDumper");
      t.setDaemon(true);
      return t;
    }
  }

  /***
   * <p>Dump all running threads on a regular base.</p>
   *
   * @param interval interval in seconds
   */
  public ThreadDumper(long interval) {
    LOGGER.log(Level.INFO, "added thread dumper scheduler");
    Runnable r = new ThreadDumperRunner();
    scheduler.scheduleAtFixedRate(r, interval, interval, TimeUnit.SECONDS);
  }

  /***
   * <p>Get a string dump all running threads.</p>
   *
   * @param dumpDaemon true if daemon processes should be dumped too
   * @return the requested dump
   */
  public static String getThreadDump(boolean dumpDaemon) {
    StringBuilder tdump = new StringBuilder();

    // Build header of table dump
    tdump.append("======================================================================")
        .append(CRLF);
    synchronized (df) {
      tdump.append("==== Thread Dump ").append(df.format(new Date()))
          .append("                               =====").append(CRLF);
    }
    tdump.append("======================================================================")
        .append(CRLF);

    // obtain list of running threads
    ThreadMXBean tbean = ManagementFactory.getThreadMXBean();
    ThreadInfo[] threadInfos = tbean.getThreadInfo(tbean.getAllThreadIds(), 100);

    // Dump information for each thread
    for (ThreadInfo threadInfo : threadInfos) {
      Thread.State state = threadInfo.getThreadState();
      Thread t = getThread(threadInfo.getThreadId());
      if (dumpDaemon || !t.isDaemon()) {
        tdump.append('"').append(threadInfo.getThreadName()).append('"').append(CRLF);
        tdump.append("   java.lang.Thread.State: ").append(state)
            .append(t.isDaemon() ? " [DAEMON]" : " [normal]")
            .append(CRLF);
        final StackTraceElement[] stackTraceElements = threadInfo.getStackTrace();
        for (final StackTraceElement stackTraceElement : stackTraceElements) {
          tdump.append("        at ").append(stackTraceElement).append(CRLF);
        }
        tdump.append("======================================================================")
            .append(CRLF);
      }
    }
    return tdump.toString();
  }

  private static Thread getThread(long id) {
    final ThreadMXBean thbean = ManagementFactory.getThreadMXBean();
    ThreadGroup root = rootTG;
    if (root == null) {
      root = Thread.currentThread().getThreadGroup();
      while (root.getParent() != null) {
        root = root.getParent();
      }
      rootTG = root;
    }
    int nalloc = thbean.getThreadCount();
    int n = 0;
    Thread[] threads;
    do {
      nalloc *= 2;
      threads = new Thread[nalloc];
      n = root.enumerate(threads, true);
    } while (n == nalloc);

    for (Thread thread : Arrays.copyOf(threads, n)) {
      if (thread.getId() == id) {
        return thread;
      }
    }
    return null;
  }

}
