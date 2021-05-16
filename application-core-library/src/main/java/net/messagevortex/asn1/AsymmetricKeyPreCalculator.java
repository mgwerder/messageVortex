package net.messagevortex.asn1;

import net.messagevortex.MessageVortex;
import net.messagevortex.MessageVortexLogger;
import net.messagevortex.asn1.encryption.AlgorithmType;
import net.messagevortex.asn1.encryption.Mode;
import net.messagevortex.asn1.encryption.Padding;
import net.messagevortex.asn1.encryption.Parameter;
import picocli.CommandLine;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Callable;
import java.util.concurrent.LinkedTransferQueue;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;

/**
 * <p>This is a class to precalculate keys.</p>
 *
 * <p>It is disabled by default. Enable it by setting a caching file Name.
 * To disable set the name to null.</p>
 */
@CommandLine.Command(
        name = "keycache",
        aliases = {"kc", "cache"},
        description = "Handle the asymmetric key cache",
        mixinStandardHelpOptions = true
)
public class AsymmetricKeyPreCalculator implements Serializable, Callable<Integer> {

    public static final String DEFAULT_CACHE_FILENAME = "AsymmetricKey.cache";

    public static final long serialVersionUID = 100000000031L;

    private static final String TMP_PREFIX = "Precalc";

    private static final java.util.logging.Logger LOGGER;
    private static final boolean DISABLE_CACHE = false;
    @CommandLine.Option(names = {"--stopIfFull"},
            description = "stop the cache calculation if the cache is full")
    private static boolean stopIfFull = false;
    private static final AsymmetricKeyCache cache = new AsymmetricKeyCache();
    private static double dequeueProbability = 1.0;
    private static File tempdir = null;
    private static long lastSaved = 0;
    private static boolean firstWarning = true;
    private static volatile InternalThread runner = null;
    @CommandLine.Option(names = {"--cacheFileName"},
            description = "filename of the cache file")
    private static String filename = null;
    private static int incrementor = 128;
    /* number of threads to use */
    private static int numThreads = Math.max(2, Runtime.getRuntime().availableProcessors() - 1);
    private static final ThreadPoolExecutor pool;

    static {
        LOGGER = MessageVortexLogger.getLogger((new Throwable()).getStackTrace()[0].getClassName());
    }

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

    static {
        Runtime.getRuntime().addShutdownHook(new Thread() {
            public void run() {
                try {
                    // force saving of cache on shutdown
                    lastSaved = -1;
                    LOGGER.log(Level.INFO, "Storing cache state");
                    save();
                    InternalThread it = runner;
                    if (it != null) {
                        LOGGER.log(Level.INFO, "Shutdown cache runner");
                        it.shutdown();
                    }
                    LOGGER.log(Level.INFO, "Shutdown hook complete");
                } catch (IOException | RuntimeException ioe) {
                    LOGGER.log(Level.WARNING, "Error while writing cache", ioe);
                }
            }
        });
    }

    @CommandLine.Option(names = {"--element"},
            description = "the affected element")
    private int elementIndex = -1;
    @CommandLine.Option(names = {"--value"},
            description = "number of elements for a key (requires --element)")
    private int value = -1;

    /**
     * Worker thread class to calculate a new specific asymmetric key.
     */
    private static class CalculationThread extends Thread {

        private final AlgorithmParameter param;

        /**
         * <p>Create a worker thread for the spicified set of parameters.</p>
         *
         * @param param the parameter set for the key to be generated
         */
        public CalculationThread(AlgorithmParameter param) {
            this.param = new AlgorithmParameter(param);
        }

        /**
         * <p>Runner for the key calculation.</p>
         */
        public void run() {
            LOGGER.log(Level.FINE, "precalculating key " + param + "");
            try {
                long start = System.currentTimeMillis();
                AsymmetricKey ak = new AsymmetricKey(new AlgorithmParameter(param), false);
                cache.setCalcTime(new AlgorithmParameter(param), System.currentTimeMillis() - start);
                cache.push(ak);
            } catch (IOException ioe) {
                LOGGER.log(Level.SEVERE, "got unexpected exception", ioe);
            }
        }
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
                t = File.createTempFile("MessageVortex" + TMP_PREFIX, ".keydir");
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

    /***
     * <p>retrieves a precomputed key from the cache.</p>
     *
     * @param parameters the parameters reflecting the requested key
     * @return the requested key
     */
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
                    LOGGER.log(Level.FINE, "added new key type to cache (" + ap + ")");
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

    static double getDequeueProbability() {
        return dequeueProbability;
    }

    static double setDequeueProbability(double newProbability) {
        if (newProbability > 1 || newProbability < 0) {
            throw new IllegalArgumentException("probablitiy must be in interval [0,1]");
        }
        double ret = getDequeueProbability();
        dequeueProbability = newProbability;
        return ret;
    }

    /**
     * <p>Set the maximum number of working threads for the cache pre-calculator.</p>
     *
     * @param newNumThreads the number of threads used for pre-calculation
     * @return the previously set number of threads
     */
    public static int setNumThreads(int newNumThreads) {
        int old = getNumThreads();
        numThreads = newNumThreads;
        pool.setMaximumPoolSize(numThreads);
        return old;
    }

    /**
     * <p>Get the number of maximum threads used for cache pre-calculation.</p>
     *
     * @return the currently set number of threads
     */
    public static int getNumThreads() {
        return numThreads;
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

    /**
     * <p>Gets the currently set filename for key pre-calculation.</p>
     *
     * @return the currently set filename
     */
    public static String getCacheFileName() {
        return filename;
    }

    /***
     * <p>Set name of cache file.</p>
     *
     * <p>If set to null the pre-calculator is disabled.</p>
     *
     * @param name                         file name of the cache file
     * @return String representing the previously set name
     * @throws IllegalThreadStateException if the previous thread has not yet shutdown but a new
     *                                     thread was tried to be started
     */
    public static String setCacheFileName(String name) {
        if ("".equals(filename)) {
            filename = DEFAULT_CACHE_FILENAME;
        }

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

    /***
     * <p>Commandline handler to pre-populate the key cache.</p>
     */
    @CommandLine.Command(name = "run", description = "pre-populates the cache")
    public static void fillCache() {
        try {
            if (filename == null || "".equals(filename)) {
                filename = DEFAULT_CACHE_FILENAME;
            }
            setCacheFileName(filename);
            stopIfFull = true;
            startOrStopThread();
            runner.join();
        } catch (InterruptedException ie) {
            LOGGER.log(Level.WARNING, "Got unexpected exception", ie);
        }
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
                    LOGGER.log(Level.FINE, "error loading cache file (will be recreated)", e);
                }
                // start runner
                runner = new InternalThread(stopIfFull);
            }
        }

    }

    private static void load(String inFile, boolean merge) throws IOException {
        LOGGER.log(Level.INFO, "loading cache from " + inFile);
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
        if (filename != null) {
            if("".equals(filename)) {
                filename=DEFAULT_CACHE_FILENAME;
            }
            try {
                synchronized (cache) {
                    cache.store(filename + ".tmp");
                    lastSaved = System.currentTimeMillis();
                    if (tempdir != null && cache.getCacheFillGrade() >= 0.999) {
                        // move file as temp file and clear cache
                        String fn = File.createTempFile("MessageVortex" + TMP_PREFIX, ".key").getAbsolutePath();
                        LOGGER.log(Level.INFO, "stored chunk to file \"" + fn + "\" to pick up");
                        Files.move(Paths.get(filename + ".tmp"), Paths.get(fn),
                                StandardCopyOption.REPLACE_EXISTING);
                        cache.clear();
                        if (stopIfFull) {
                            setCacheFileName(null);
                        }
                    } else {
                        LOGGER.log(Level.INFO, "stored cache to "
                                + Paths.get(filename).getFileName().toString());
                        Files.move(Paths.get(filename + ".tmp"), Paths.get(filename),
                                StandardCopyOption.REPLACE_EXISTING);
                    }
                }
            } catch (Exception e) {
                LOGGER.log(Level.WARNING, "Exception while storing file", e);
                throw e;
            }
        } else {
            LOGGER.log(Level.WARNING, "Cache not saved to disk (no filename for caching set)");
        }
    }

    @Override
    public Integer call() throws IOException {
        new AsymmetricKeyPreCalculator(true);
        if (cache.isEmpty()) {
            try {
                if (filename == null) {
                    filename = DEFAULT_CACHE_FILENAME;
                }
                load(filename, true);
            } catch (IOException ioe) {
                throw new IOException("unable to load existing asymmetric key cache file"
                        + "... aborting execution", ioe);
            }
            cache.clear();
            cache.showStats();
        }
        if (value != -1) {
            // setting the target value of the specified element
        } else {
            // just start the cache
            setCacheFileName(tempdir.getAbsolutePath() + "/precalcCache.cache");
        }
        try {
            runner.join();
        } catch (InterruptedException ie) {
            throw new IOException("Exception while waiting for cache runner to finish", ie);
        }
        return 0;
    }

    /***
     * <p>Command line helper to set the maximum cache size of a cached item.</p>
     *
     * @throws IOException if the specified file is not fond or there was an error when reading
     */
    @CommandLine.Command(name = "set", description = "sets the size of a specific cache element")
    public void setCacheSize() throws IOException {
        LOGGER.log(Level.INFO, "SET called for element " + elementIndex);
        if (elementIndex <= 0 || value <= 0) {
            LOGGER.log(Level.SEVERE, "SET requires a valid element (" + elementIndex
                    + ") and an valid value to be set (" + value + ")");
            System.exit(MessageVortex.ARGUMENT_FAIL);
        } else {
            setCacheSize(elementIndex, value);
        }
        setCacheFileName(null);
        System.exit(0);
    }

    /***
     * <p>Set the maximum cache size of a cached item.</p>
     *
     * @param index the index of the cached item
     * @param size the size to be set
     * @throws IOException if the specified file is not fond or there was an error when reading
     */
    public void setCacheSize(int index, int size) throws IOException {
        LOGGER.log(Level.INFO, "SET called for element " + index + " and size " + size);
        if (cache.isEmpty()) {
            try {
                LOGGER.log(Level.INFO, "loading cache " + filename);
                load(filename, true);
            } catch (IOException ioe) {
                throw new IOException("unable to load existing asymmetric key cache file"
                        + "... aborting execution", ioe);
            }
        }
        LOGGER.log(Level.INFO, "chaning cache size");
        cache.setCacheSize(elementIndex, value);
        cache.showStats();
        LOGGER.log(Level.INFO, "storing cache " + filename);
        cache.store(filename);
    }

    /***
     * <p>Command line helper to remove a cached item type from the cache.</p>
     *
     * @throws IOException if the specified file is not fond or there was an error when reading
     */
    @CommandLine.Command(name = "remove", description = "removes a specific cache element")
    public void removeCacheElement() throws IOException {
        LOGGER.log(Level.INFO, "removing element " + elementIndex);
        if (elementIndex <= 0) {
            LOGGER.log(Level.SEVERE, "REMOVE requires a valid element (" + elementIndex + ")");
            System.exit(MessageVortex.ARGUMENT_FAIL);
        } else {
            removeCacheElement(elementIndex);
        }
        setCacheFileName(null);
        System.exit(0);
    }

    /***
     * <p>Remove a the specified item type from the cache.</p>
     *
     * @param index the index of the element to be removed
     * @throws IOException if the specified file is not fond or there was an error when reading
     */
    public void removeCacheElement(int index) throws IOException {
        if (cache.isEmpty()) {
            try {
                LOGGER.log(Level.INFO, "loading cache " + filename);
                load(filename, true);
            } catch (IOException ioe) {
                throw new IOException("unable to load existing asymmetric key cache file"
                        + "... aborting execution", ioe);
            }
        }
        LOGGER.log(Level.INFO, "removing element");
        cache.removeCacheElement(elementIndex);
        cache.showStats();
        LOGGER.log(Level.INFO, "storing cache " + filename);
        cache.store(filename);
    }

    /***
     * <p>Command line helper to list cached items.</p>
     *
     * @throws IOException if the specified file is not fond or there was an error when reading
     */
    @CommandLine.Command(name = "list", description = "Lists all elements of the cache")
    public void listCache() throws IOException {
        if (cache.isEmpty()) {
            try {
                LOGGER.log(Level.INFO, "loading cache " + filename);
                load(filename, true);
            } catch (IOException ioe) {
                throw new IOException("unable to load existing asymmetric key cache file"
                        + "... aborting execution", ioe);
            }
        }
        cache.showStats();
        setCacheFileName(null);
        System.exit(0);
    }

    private static class InternalThread extends Thread {

        private static int counter = 0;

        private volatile boolean shutdown = false;
        private final boolean stopIfFull;

        InternalThread(boolean stopIfFull) {
            this.stopIfFull = stopIfFull;
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

            // wait maximum 3 seconds for shutdown then abort key calculation
            try {
                pool.awaitTermination(3, TimeUnit.SECONDS);
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
                    if (!stopIfFull) {
                        try {
                            LOGGER.log(Level.INFO, "cache is idle (" + String.format("%2.3f",
                                    cache.getCacheFillGrade() * 100) + "%) ... sleeping for a short while "
                                    + "and waiting for requests");
                            Thread.sleep(10000);
                        } catch (InterruptedException ie) {
                            Thread.currentThread().interrupt();
                        }
                    } else if (cache.getCacheFillGrade() >= 0.999) {
                        LOGGER.log(Level.INFO, "cache is full(" + String.format("%2.3f",
                                cache.getCacheFillGrade() * 100) + "%) ... shutting down");
                        shutdown = true;
                    }
                }

            }

        }

        private boolean mergePrecalculatedKeys() {
            boolean ret = false;
            // get list of files to merge
            String targetFile;  // fileThatYouWantToFilter
            List<File> listOfFiles = new ArrayList<>();
            File[] fl = (new File(System.getProperty("java.io.tmpdir"))).listFiles();
            for (File tfile : fl == null ? new File[0] : fl) {
                if (tfile.isFile()) {
                    targetFile = tfile.getName();
                    if (targetFile.startsWith("MessageVortex" + TMP_PREFIX) && targetFile.endsWith(".key")) {
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
            return new CalculationThread(param);
        }
    }
}
