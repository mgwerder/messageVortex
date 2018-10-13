package net.messagevortex.asn1;

// ************************************************************************************
// * Copyright (c) 2018 Martin Gwerder (martin@gwerder.net)
// *
// * Permission is hereby granted, free of charge, to any person obtaining a copy
// * of this software and associated documentation files (the "Software"), to deal
// * in the Software without restriction, including without limitation the rights
// * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
// * copies of the Software, and to permit persons to whom the Software is
// * furnished to do so, subject to the following conditions:
// *
// * The above copyright notice and this permission notice shall be included in all
// * copies or substantial portions of the Software.
// *
// * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
// * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
// * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
// * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
// * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
// * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
// * SOFTWARE.
// ************************************************************************************

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedTransferQueue;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import net.messagevortex.MessageVortexLogger;
import net.messagevortex.asn1.encryption.AlgorithmType;
import net.messagevortex.asn1.encryption.Mode;
import net.messagevortex.asn1.encryption.Padding;
import net.messagevortex.asn1.encryption.Parameter;

/**
 * <p></p>This is a class to precalculate keys.</p>
 *
 * <p>It is disabled by default. Enable it by setting a caching file Name.
 * To disable set the name to null.</p>
 */
class AsymmetricKeyPreCalculator implements Serializable {

  public static final long serialVersionUID = 100000000031L;

  private static final String TMP_PREFIX = "MessageVortexPrecalc";

  private static final java.util.logging.Logger LOGGER;

  static {
    LOGGER = MessageVortexLogger.getLogger((new Throwable()).getStackTrace()[0].getClassName());
  }

  private static AsymmetricKeyCache cache = new AsymmetricKeyCache();


  private static final boolean DISABLE_CACHE = false;
  private static double dequeueProbability = 1.0;

  private static File tempdir = null;

  private static long lastSaved = 0;
  private static boolean firstWarning = true;
  private static InternalThread runner = null;
  private static String filename = null;

  private static int incrementor = 128;

  static {
    Runtime.getRuntime().addShutdownHook(new Thread() {
      public void run() {
        try {
          // force saving of cache on shutdown
          lastSaved = -1;
          save();
          runner.shutdown();
        } catch (IOException | RuntimeException ioe) {
          LOGGER.log(Level.WARNING, "Error while writing cache", ioe);
        }
      }
    });
  }


  private static class InternalThread extends Thread {

    private static int counter = 0;

    private volatile boolean shutdown = false;

    InternalThread() {
      // This thread may die safely
      setDaemon(true);

      // ... and should run at very low priority
      setPriority(MIN_PRIORITY);

      // we start the daemon as soon as we can
      setName(TMP_PREFIX + " manager " + (counter++));
      start();
      LOGGER.log(Level.INFO, "cache manager \"" + getName() + "\" started");
    }

    /***
     * <p>Tells the process to shutdown asap.</p>
     */
    void shutdown() {
      pool.shutdown();

      // wait maximum 60 seconds for shutdown then abbort key calculation
      try {
        pool.awaitTermination(180, TimeUnit.SECONDS);
        pool.shutdownNow();
      } catch (InterruptedException ie) {
        pool.shutdownNow();
        Thread.currentThread().interrupt();
      }
      shutdown = true;
    }

    public void run() {
      pool.allowCoreThreadTimeOut(true);
      while (!shutdown) {
        // get a parameter set to be calculated
        AlgorithmParameter p = cache.getSpeculativeParameter();

        // calculate parameter set (if any)
        if (p != null) {
          // calculate key
          calculateKey(p);

          // merge precalculated keys (if applicable)
          if (tempdir == null && mergePrecalculatedKeys()) {
            try {
              save();
            } catch (IOException e) {
              LOGGER.log(Level.WARNING, "Error saving cache (1)", e);
            }
          } else if (tempdir != null) {
            try {
              save();
            } catch (IOException e) {
              LOGGER.log(Level.WARNING, "Error saving cache (2)", e);
            }
          }
        } else {
          try {
            LOGGER.log(Level.INFO, "cache is idle (" + String.format("%f2.3",
                    cache.getCacheFillGrade() * 100) + "%) ... sleeping for a short while "
                    + "and waiting for requests");
            Thread.sleep(10000);
          } catch (InterruptedException ie) {
            Thread.currentThread().interrupt();
          }
        }

      }

    }

    private boolean mergePrecalculatedKeys() {
      boolean ret = false;
      // get list of files to merge
      String targetFile;  // fileThatYouWantToFilter
      List<File> listOfFiles = new ArrayList<>();
      for (File tfile : (new File(System.getProperty("java.io.tmpdir"))).listFiles()) {
        if (tfile.isFile()) {
          targetFile = tfile.getName();
          if (targetFile.startsWith(TMP_PREFIX) && targetFile.endsWith(".key")) {
            listOfFiles.add(tfile);
          }
        }
      }
      // add keys to cache
      for (File f : listOfFiles) {
        try {
          //  merge only if one key cache is below 40%
          double lowest = cache.getLowestCacheSize();
          if (lowest < 0.4) {
            load(f.getAbsolutePath(), true);
            ret = f.delete();
          } else {
            // abort as soon as lowest cache element is above 40%
            return ret;
          }
        } catch (IOException e) {
          LOGGER.log(Level.WARNING, "Error merging file " + f.getAbsolutePath(), e);
        }
        ret &= f.delete();
      }
      return ret;
    }

    private void calculateKey(AlgorithmParameter p) {
      try {
        // prepare thread
        Thread t = runCalculatorThread(p);
        t.setName(TMP_PREFIX + " precalculation");
        t.setPriority(Thread.MIN_PRIORITY);
        t.setDaemon(true);
        pool.execute(t);
        LOGGER.log(Level.FINE, "Added key precalculator for " + p + " (pool size:"
                + pool.getQueue().size() + "; thread count (min/current/max):"
                + pool.getCorePoolSize() + "/" + pool.getActiveCount() + "/"
                + pool.getMaximumPoolSize() + ")");

        // Wait a while for existing tasks to terminate
        if (pool.getQueue().size() > Math.max(incrementor * 2, numThreads)) {
          pool.awaitTermination(10, TimeUnit.SECONDS);
          if (tempdir != null) {
            cache.showStats();
            LOGGER.log(Level.INFO, "|Running threads " + pool.getActiveCount() + " of "
                    + pool.getQueue().size());
          }

          // calculate new incrementor
          if (pool.getQueue().size() > incrementor
                  && pool.getQueue().size() < incrementor * 2 && tempdir != null) {
            incrementor = Math.max(1, incrementor / 2);
            LOGGER.log(Level.INFO, "lowered incrementor to " + incrementor);
          } else if (pool.getQueue().size() < numThreads && tempdir != null) {
            incrementor = incrementor * 2;
            LOGGER.log(Level.INFO, "raised incrementor to " + incrementor);
          }

          // store cache
          save();
        }

      } catch (IOException ioe) {
        LOGGER.log(Level.INFO, "exception while storing file", ioe);
      } catch (InterruptedException ie) {
        // (Re-)Cancel if current thread also interrupted
        pool.shutdownNow();
        // Preserve interrupt status
        Thread.currentThread().interrupt();
      }
    }

    private Thread runCalculatorThread(final AlgorithmParameter param) {
      return new Thread() {
        public void run() {
          LOGGER.log(Level.FINE, "precalculating key " + param + "");
          try {
            long start = System.currentTimeMillis();
            AsymmetricKey ak = new AsymmetricKey(new AlgorithmParameter(param), false);
            cache.setCalcTime(new AlgorithmParameter(param), System.currentTimeMillis() - start);

            // put in cache
            assert ak != null;
            cache.push(ak);
          } catch (IOException ioe) {
            LOGGER.log(Level.SEVERE, "got unexpected exception", ioe);
          }
        }

      };
    }
  }

  /* nuber of threads to use */
  private static int numThreads = Math.max(2, Runtime.getRuntime().availableProcessors() - 1);
  private static ThreadPoolExecutor pool;

  static {
    BlockingQueue<Runnable> queue = new LinkedTransferQueue<Runnable>() {
      @Override
      public boolean offer(Runnable e) {
        return tryTransfer(e);
      }
    };
    ThreadFactory factory = new ThreadFactory() {
      public Thread newThread(Runnable r) {
        Thread t = new Thread(r);
        t.setName(TMP_PREFIX + " worker");
        t.setDaemon(true);
        return t;
      }
    };

    pool = new ThreadPoolExecutor(1, numThreads, 1, TimeUnit.SECONDS, queue, factory);
    pool.setRejectedExecutionHandler(new RejectedExecutionHandler() {
      @Override
      public void rejectedExecution(Runnable r, ThreadPoolExecutor executor) {
        try {
          executor.getQueue().put(r);
        } catch (InterruptedException e) {
          Thread.currentThread().interrupt();
        }
      }
    });
  }

  private AsymmetricKeyPreCalculator() {
    // just a dummy to hide the default constructor
    this(false);
  }

  private AsymmetricKeyPreCalculator(boolean detached) {
    File t = null;
    if (detached) {
      try {
        // create temporary file
        t = File.createTempFile(TMP_PREFIX, ".keydir");
      } catch (IOException ioe) {
        LOGGER.log(Level.WARNING, "Unable to create temp file", ioe);
      } finally {
        tempdir = t;
      }
      try {
        // remove tempfile
        if (tempdir == null) {
          throw new IOException("failed to create temp file");
        }
        if (!(tempdir.delete())) {
          throw new IOException("Could not delete temp file: " + tempdir.getAbsolutePath());
        }
        // create directory instead
        if (!(tempdir.mkdir())) {
          throw new IOException("Could not create temp directory: " + tempdir.getAbsolutePath());
        }
        // make sure that the directory is deleted on exit
        tempdir.deleteOnExit();
      } catch (IOException ioe) {
        LOGGER.log(Level.WARNING, "Unable to create temp dir", ioe);
      }
    } else {
      tempdir = t;
    }
  }

  public static AsymmetricKey getPrecomputedAsymmetricKey(AlgorithmParameter parameters) {

    AlgorithmParameter ap = prepareParameters(parameters);

    synchronized (cache) {
      if (filename == null && runner != null && !runner.isAlive()) {
        runner = null;
      }
      synchronized (cache) {
        if (DISABLE_CACHE) {
          if (firstWarning) {
            LOGGER.log(Level.WARNING, "Cache is disabled");
            firstWarning = false;
          }
          return null;
        } else if (filename == null) {
          // we are shutting down or have been shut down. No services are provided
          LOGGER.log(Level.INFO, "cache disabled .. no key offered");
          return null;
        } else if (cache.peek(ap) == null) {
          // this cache does not yet exist schedule for creation
          // is done by peeking first time
          cache.requestCacheIncrease(ap);
          LOGGER.log(Level.FINE, "added new key type to cache ");
          return null;
        }
      }
      LOGGER.log(Level.FINE, "cache offered precalculated key");
      if (Math.random() < dequeueProbability) {
        return cache.pull(ap);
      } else {
        return cache.peek(ap);
      }
    }
  }

  public static double getDequeueProbability() {
    return dequeueProbability;
  }

  public static double setDequeueProbability(double newProbability) {
    if (newProbability > 1 || newProbability < 0) {
      throw new IllegalArgumentException("probablitiy must be in interval [0,1]");
    }
    double ret = getDequeueProbability();
    dequeueProbability = newProbability;
    return ret;
  }

  private static AlgorithmParameter prepareParameters(AlgorithmParameter ap) {
    AlgorithmParameter ret = new AlgorithmParameter(ap);

    // clear IV parameters as they are not relevant
    ret.put(Parameter.IV, null);

    // make sure that padding and mode are on default
    ret.put(Parameter.PADDING, Padding.getDefault(AlgorithmType.ASYMMETRIC).toString());
    ret.put(Parameter.MODE, Mode.getDefault(AlgorithmType.ASYMMETRIC).toString());

    return ret;
  }

  /***
   * <p>Set name of cache file.</p>
   *
   * <p>If set to null the Precalculator is disabled.</p>
   *
   * @param name                         file name of the cache file
   * @return                             String representing the previously set name
   * @throws IllegalThreadStateException if the previous thread has not yet shutdown but a new
   *                                     thread was tried to be started
   */
  public static String setCacheFileName(String name) {
    // if the same name is set again do nothing
    if (filename != null && filename.equals(name)) {
      return filename;
    }

    // check if there is a dead runner
    if (filename == null && runner != null && !runner.isAlive()) {
      runner = null;
    }

    // abort if there is a dying runner
    if (filename == null && name != null && runner != null) {
      throw new IllegalThreadStateException("Thread is still shutting down... try again later");
    }

    // set runner name
    String ret = filename;
    filename = name;
    startOrStopThread();
    return ret;
  }

  private static void startOrStopThread() {
    // check if we have to start or stop
    synchronized (cache) {
      if (filename == null && runner != null) {
        runner.shutdown();
      }

      if (runner != null && !runner.isAlive()) {
        runner = null;
      }

      if (filename != null && runner == null) {
        // load cache
        try {
          if (cache.isEmpty()) {
            load(filename, false);
          }
        } catch (IOException | ExceptionInInitializerError e) {
          LOGGER.log(Level.INFO, "error loading cache file (will be recreated)", e);
        }
        // start runner
        runner = new InternalThread();
      }
    }

  }

  private static void load(String inFile, boolean merge) throws IOException {
    if (!merge) {
      cache.load(inFile);
    } else {
      cache.merge(inFile);
    }
  }

  private static void save() throws IOException {
    // do not allow saving more often than every minute
    if (runner != null) {
      synchronized (runner) {
        if (lastSaved + 60000 > System.currentTimeMillis()) {
          return;
        }
      }
    }

    // store data
    try {
      synchronized (cache) {
        cache.store(filename + ".tmp");
        lastSaved = System.currentTimeMillis();
        if (tempdir != null && cache.getCacheFillGrade() > 0.1) {
          // move file as temp file and clear cache
          String fn = File.createTempFile(TMP_PREFIX, ".key").getAbsolutePath();
          LOGGER.log(Level.INFO, "stored chunk to file \"" + fn + "\" to pick up");
          Files.move(Paths.get(filename + ".tmp"), Paths.get(fn),
                  StandardCopyOption.REPLACE_EXISTING);
          cache.clear();
        } else {
          LOGGER.log(Level.INFO, "stored cache");
          Files.move(Paths.get(filename + ".tmp"), Paths.get(filename),
                  StandardCopyOption.REPLACE_EXISTING);
        }
      }
    } catch (Exception e) {
      LOGGER.log(Level.WARNING, "Exception while storing file", e);
      throw e;
    }
  }

  public static void main(String[] args)
          throws InterruptedException, ClassNotFoundException, IOException {
    MessageVortexLogger.setGlobalLogLevel(Level.ALL);
    new AsymmetricKeyPreCalculator(true);
    if (cache.isEmpty()) {
      try {
        load("AsymmetricKey.cache", true);
      } catch (IOException ioe) {
        throw new IOException("unable to load existing asymmetric key cache file"
                              + "... aborting execution", ioe);
      }
      cache.clear();
      cache.showStats();
    }
    setCacheFileName(tempdir.getAbsolutePath() + "/precalcCache.cache");
    runner.join();
  }
}