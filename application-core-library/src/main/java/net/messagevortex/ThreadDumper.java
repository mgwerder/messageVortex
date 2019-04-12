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

public class ThreadDumper {

  private static final Logger LOGGER;

  static {
    LOGGER = Logger.getLogger((new Throwable()).getStackTrace()[0].getClassName());
  }

  private static final DateFormat df;

  static {
    df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm'Z'");
    TimeZone tz = TimeZone.getTimeZone("UTC");
    df.setTimeZone(tz);
  }

  private static final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1, new ThreadDumperThreadFactory());

  private static ThreadGroup rootTG = null;

  private static class ThreadDumperRunner implements Runnable {
    @Override
    public void run() {
      try {
        LOGGER.log(Level.INFO, "dumping threads");
        for (String s : getThreadDump(false).split(System.lineSeparator())) {
          LOGGER.log(Level.INFO, s);
        }
        LOGGER.log(Level.INFO, "threads dumped");
      } catch (Exception e) {
        LOGGER.log(Level.SEVERE, "Error while executing thread dump", e );
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


  public ThreadDumper(long interval) {
    LOGGER.log(Level.INFO, "added thread dumper scheduler");
    synchronized (scheduler) {
      Runnable r = new ThreadDumperRunner();
      scheduler.scheduleAtFixedRate(r, interval, interval, TimeUnit.SECONDS);
    }
  }

  public static String getThreadDump(boolean dumpDaemon) {
    StringBuilder tdump = new StringBuilder();
    ThreadMXBean threadMXBean = ManagementFactory.getThreadMXBean();
    ThreadInfo[] threadInfos = threadMXBean.getThreadInfo(threadMXBean.getAllThreadIds(), 100);
    tdump.append("======================================================================" + System.lineSeparator());
    tdump.append("==== Thread Dump " + df.format(new Date()) + "                               =====" + System.lineSeparator());
    tdump.append("======================================================================" + System.lineSeparator());
    for (ThreadInfo threadInfo : threadInfos) {
      Thread.State state = threadInfo.getThreadState();
      Thread t = getThread( threadInfo.getThreadId() );
      if (dumpDaemon || !t.isDaemon()) {
        tdump.append("\"" + threadInfo.getThreadName() + "\"" + System.lineSeparator());
        tdump.append("   java.lang.Thread.State: " + state + (t.isDaemon()?" DAEMON":"") + System.lineSeparator());
        final StackTraceElement[] stackTraceElements = threadInfo.getStackTrace();
        for (final StackTraceElement stackTraceElement : stackTraceElements) {
          tdump.append("        at " + stackTraceElement + System.lineSeparator());
        }
        tdump.append("======================================================================" + System.lineSeparator());
      }
    }
    return tdump.toString();
  }

  static Thread getThread(long id) {
    final ThreadMXBean thbean = ManagementFactory.getThreadMXBean( );
    ThreadGroup root;
    if (rootTG != null) {

      root = rootTG;
    } else {
      root = Thread.currentThread().getThreadGroup();
      while (root.getParent() != null) {
        root = root.getParent();
      }
      rootTG = root;
    }
    int nAlloc = thbean.getThreadCount( );
    int n = 0;
    Thread[] threads;
    do {
      nAlloc *= 2;
      threads = new Thread[ nAlloc ];
      n = root.enumerate( threads, true );
    } while ( n == nAlloc );

    for ( Thread thread : Arrays.copyOf( threads, n ) )
      if ( thread.getId( ) == id )
        return thread;
    return null;
  }

}
