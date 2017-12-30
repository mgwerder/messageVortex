package net.gwerder.java.messagevortex.asn1;

import net.gwerder.java.messagevortex.MessageVortexLogger;
import net.gwerder.java.messagevortex.asn1.encryption.AlgorithmType;
import net.gwerder.java.messagevortex.asn1.encryption.Mode;
import net.gwerder.java.messagevortex.asn1.encryption.Padding;
import net.gwerder.java.messagevortex.asn1.encryption.Parameter;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;

/**
 * This is a class to precalculate keys.
 *
 * It is disabled by default. Enable it by setting a caching file Name. To disable set the name to null.
 */
class AsymmetricKeyPreCalculator implements Serializable {

    public static final long serialVersionUID = 100000000031L;

    private static final java.util.logging.Logger LOGGER;
    static {
        LOGGER = MessageVortexLogger.getLogger((new Throwable()).getStackTrace()[0].getClassName());
        MessageVortexLogger.setGlobalLogLevel( Level.ALL);
    }


    private static final boolean DISABLE_CACHE=false;
    private static double dequeueProbability = 1.0;

    private static long lastSaved = 0;
    private static List<LastCalculated> log=new ArrayList<>();
    private static boolean firstWarning=true;
    private static final Map<AlgorithmParameter,Queue<AsymmetricKey>> cache=new ConcurrentHashMap<>();
    private static final Map<AlgorithmParameter,Integer> cacheSize=new ConcurrentHashMap<>();
    private static InternalThread runner=null;
    private static String filename=null;

    /* nuber of threads to use */
    private static int numThreads=Math.max(2,Runtime.getRuntime().availableProcessors()-1);
    private static ExecutorService pool = Executors.newFixedThreadPool(numThreads);

    static {
        Runtime.getRuntime().addShutdownHook(new Thread() {
            public void run() {
                try{
                    // force saving of cache on shutdown
                    lastSaved=-1;
                    save();
                } catch(IOException |RuntimeException|ClassNotFoundException ioe) {
                    LOGGER.log(Level.WARNING,"Error while writing cache",ioe);
                }
            }
        });
    }

    private static class LastCalculated {
        private String msg;
        private Date lastStored=new Date();
        private int num=1;

        public LastCalculated(String msg) {
            this.msg=msg;
        }

        public String toString() {
            SimpleDateFormat sortableFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
            String ret = "[" + sortableFormat.format(lastStored) + "] " + msg;
            if (num > 1) {
                ret += " (" + num + ")";
            }
            return ret;
        }

        public String getMessage() {
            return msg;
        }

        public Date getTimestamp() {
            return lastStored;
        }

        public Date setTimestamp(Date newTimestamp) {
            Date ret=lastStored;
            if(newTimestamp==null) {
                lastStored = new Date();
            } else {
                lastStored=newTimestamp;
            }
            return ret;
        }

        public int getNumberOfCalls() {
            return num;
        }

        public int incrementNumberOfCalls() {
            return ++num;
        }
    }

    private static class InternalThread extends Thread {

        private static int counter=0;

        private boolean shutdown=false;

        InternalThread() {
            // This thread may die safely
            setDaemon(true);

            // ... and should run at very low priority
            setPriority(MIN_PRIORITY);

            // we start the daemon as soon as we can
            setName("AsymmetricKey cache manager "+(counter++));
            start();
            LOGGER.log(Level.INFO,"cache manager \""+getName()+"\" started");
        }

        /***
         * Tells the process to shutdown asap
         */
        void shutdown() {
            pool.shutdown();

            // wait maximum 60 seconds for shutdown then abbort key calculation
            try{
                pool.awaitTermination(60,TimeUnit.SECONDS);
                pool.shutdownNow();
            } catch (InterruptedException ie) {
                pool.shutdownNow();
                Thread.currentThread().interrupt();
            }
            shutdown=true;
        }

        public void run() {

            while(!shutdown) {
                // get a parameter set to be calculated
                AlgorithmParameter p=null;
                double filler=1;
                synchronized(cache) {
                    for(Map.Entry<AlgorithmParameter,Queue<AsymmetricKey>> e:cache.entrySet()) {
                        Integer i=cacheSize.get(e.getKey());
                        if(i==null) {
                            i=1;
                        }
                        double tfiller=(0.0+e.getValue().size())/(0.0+i);
                        Integer maxSize=cacheSize.get(e.getKey());
                        assert maxSize!=null;
                        Queue<AsymmetricKey> q=e.getValue();
                        assert q!=null;
                        if(maxSize>e.getValue().size() && tfiller<filler) {
                            p=e.getKey();
                            filler=tfiller;
                        }
                    }
                }

                // calculate parameter set (if any)
                if(p!=null) {
                    calculateKey(p);
                } else {
                    try {
                        Thread.sleep(10000);
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                    }
                }

            }

        }

        private void calculateKey(AlgorithmParameter p) {
            try {
                // prepare thread list
                for (int i = 0; i < numThreads; i++) {
                    Thread t=runCalculatorThread(p);
                    t.setName("cache precalculation thread");
                    t.setPriority(Thread.MIN_PRIORITY);
                    pool.execute(t);

                }

                // Wait a while for existing tasks to terminate
                pool.awaitTermination(60, TimeUnit.SECONDS);

                // store cache
                save();
            } catch (IOException | ClassNotFoundException ioe) {
                LOGGER.log(Level.INFO, "exception while storing file", ioe);
            } catch (InterruptedException ie) {
                // (Re-)Cancel if current thread also interrupted
                pool.shutdownNow();
                // Preserve interrupt status
                Thread.currentThread().interrupt();
            }
        }

        private void waitForCalculationThread(AlgorithmParameter p,Thread t) {
            try {
                t.join();
                attachLog("added precomputed key " + p.toString() + " to cache");
            } catch (InterruptedException ie) {
                LOGGER.log(Level.SEVERE, "got unexpected exception", ie);
                Thread.currentThread().interrupt();
            }
        }

        private Thread runCalculatorThread(final AlgorithmParameter param) {
            return new Thread() {
                public void run() {
                    LOGGER.log(Level.FINE, "precalculating key " + param.toString() + "");
                    try {
                        AsymmetricKey ak = new AsymmetricKey(param.clone(), false);

                        // put in cache
                        synchronized (cache) {
                            Queue<AsymmetricKey> q = cache.get(param);
                            assert q != null;
                            assert ak != null;
                            q.add(ak);
                        }
                    } catch (IOException ioe) {
                        LOGGER.log(Level.SEVERE, "got unexpected exception", ioe);
                    }
                }

            };
        }
    }

    private AsymmetricKeyPreCalculator() {
        // just a dummy to hide the default constructor
    }

    private static void attachLog(String msg) {
        synchronized(log) {
            if(!log.isEmpty() && log.get(log.size()-1).getMessage().equals(msg)) {
                log.get(log.size()-1).incrementNumberOfCalls();
                log.get(log.size()-1).setTimestamp(null);
            } else {
                while(log.size()>10) {
                    log.remove(0);
                }
                log.add(log.size(),new LastCalculated(msg));
            }
        }
    }


    public static AsymmetricKey getPrecomputedAsymmetricKey(AlgorithmParameter parameters) {

        AlgorithmParameter ap=prepareParameters(parameters);

        synchronized (cache) {
            if (filename == null && runner != null && !runner.isAlive()) {
                runner = null;
            }
            synchronized (cache) {
                if(DISABLE_CACHE) {
                    if(firstWarning) {
                        LOGGER.log(Level.WARNING, "Cache is disabled");
                        firstWarning=false;
                    }
                    return null;
                } else if (filename == null) {
                    // we are shutting down or have been shut down. No services are provided
                    LOGGER.log(Level.INFO, "cache disabled .. no key offered");
                    return null;
                } else if (cache.get(ap) == null) {
                    // this cache does not yet exist schedule for creation
                    cacheSize.put(ap, 1);
                    cache.put(ap, new ArrayDeque<AsymmetricKey>());
                    LOGGER.log(Level.FINE, "added new key type to cache");
                    return null;
                } else if (cache.get(ap).isEmpty()) {
                    // this cache is too small as it is empty increase up to 100 storage places
                    cacheSize.put(ap, Math.min(400, cacheSize.get(ap) + 1));
                    LOGGER.log(Level.FINE, "cache underrun ... increased key type " + ap.get(Parameter.ALGORITHM) + "/" + ap.get(Parameter.KEYSIZE) + " cache to " + cacheSize.get(ap));
                    return null;
                }
            }
            LOGGER.log(Level.FINE, "cache offered precalculated key");
            if(Math.random()<dequeueProbability) {
                return cache.get(ap).poll();
            } else {
                return cache.get(ap).peek();
            }
        }
    }

    public static double getDequeueProbability() {
        return dequeueProbability;
    }

    public static double setDequeueProbability(double newProbability) {
        if(newProbability>1 || newProbability<0) {
            throw new IllegalArgumentException("probablitiy must be in interval [0,1]");
        }
        double ret=getDequeueProbability();
        dequeueProbability=newProbability;
        return ret;
    }

    private static AlgorithmParameter prepareParameters(AlgorithmParameter ap) {
        AlgorithmParameter ret=ap.clone();

        // clear IV parameters as they are not relevant
        ret.put(Parameter.IV,null);

        // make sure that padding and mode are on default
        ret.put(Parameter.PADDING, Padding.getDefault(AlgorithmType.ASYMMETRIC).toString());
        ret.put(Parameter.MODE, Mode.getDefault(AlgorithmType.ASYMMETRIC).toString());

        return ret;
    }

    /***
     *
     * @param name
     * @return
     * @throws IllegalThreadStateException if the previous thread has not yet shutdown but a new thread was tried to be started
     */
    public static String setCacheFileName(String name) {
        // if the same name is set again do nothing
        if(filename!=null && filename.equals(name)) {
            return filename;
        }

        // check if there is a dead runner
        if(filename==null && runner!=null && !runner.isAlive()) {
            runner=null;
        }

        // abort if there is a dying runner
        if(filename==null && name!=null && runner!=null) {
            throw new IllegalThreadStateException("Thread is still shutting down... try again later");
        }

        // set runner name
        String ret=filename;
        filename=name;
        startOrStopThread();
        return ret;
    }

    private static void startOrStopThread() {
        // check if we have to start or stop
        synchronized (cache) {
            if (filename == null && runner != null) {
                runner.shutdown();
            }

            if(runner!=null && !runner.isAlive()) {
                runner=null;
            }

            if (filename != null && runner == null) {
                // load cache
                try {
                    load();
                } catch (IOException | ClassNotFoundException|ExceptionInInitializerError e) {
                    LOGGER.log(Level.INFO, "error loading cache file (will be recreated)", e);
                }
                // start runner
                runner = new InternalThread();
            }
        }

    }

    private static void load() throws IOException,ClassNotFoundException {
        ObjectInputStream f=null;
        try {
            synchronized (cache) {
                f = new ObjectInputStream(new FileInputStream(filename));
                Map<AlgorithmParameter, Queue<AsymmetricKey>> lc = new HashMap<>();

                // number of tuples
                int i = (int) f.readObject();

                // get tupples
                for (int j = 0; j < i; j++) {
                    @SuppressWarnings("unchecked")
                    AlgorithmParameter ap = (AlgorithmParameter) f.readObject();
                    @SuppressWarnings("unchecked")
                    Queue<AsymmetricKey> q = (Queue<AsymmetricKey>) f.readObject();
                    lc.put(prepareParameters(ap), q);
                }

                // Loading list of algs
                Map<AlgorithmParameter, Integer> lcc = new HashMap<>();
                i = (int) f.readObject();
                for (int j = 0; j < i; j++) {
                    @SuppressWarnings("unchecked")
                    AlgorithmParameter ap = (AlgorithmParameter) f.readObject();
                    @SuppressWarnings("unchecked")
                    int num = (Integer) f.readObject();
                    lcc.put(prepareParameters(ap), num);
                }

                // building new cache
                synchronized (cache) {
                    cache.clear();
                    for (Map.Entry<AlgorithmParameter, Queue<AsymmetricKey>> e : lc.entrySet()) {
                        AlgorithmParameter p = prepareParameters(e.getKey());
                        Queue<AsymmetricKey> q = cache.get(p);
                        if (q == null) {
                            cache.put(p, e.getValue());
                        } else {
                            q.addAll(e.getValue());
                        }
                    }
                    cacheSize.clear();
                    for (Map.Entry<AlgorithmParameter, Integer> e : lcc.entrySet()) {
                        AlgorithmParameter p = prepareParameters(e.getKey());
                        Integer i1 = cacheSize.get(p);
                        if (i1 == null) {
                            cacheSize.put(p, e.getValue());
                        } else {
                            cacheSize.put(p, Math.max(i1, e.getValue()));
                        }
                    }
                }
                showStats();
            }
        } catch(ClassCastException e) {
            LOGGER.log(Level.WARNING, "error casting file ... restarting",e);
        } catch(Exception e) {
            throw e;
        } finally {
            if(f!=null) {
                f.close();
            }
        }
    }

    private static void save() throws IOException,ClassNotFoundException {
        // do not allow saving more often than every minute
        synchronized(runner) {
            if (lastSaved + 60000 > System.currentTimeMillis()) {
                return;
            }
        }

        // store data
        ObjectOutputStream f=null;
        try {
            synchronized (cache) {
                f = new ObjectOutputStream(new FileOutputStream(filename+".tmp"));
                synchronized(cache) {

                    // Number of tuples
                    f.writeObject(cache.size());

                    // write tuples
                    for (Map.Entry<AlgorithmParameter, Queue<AsymmetricKey>> e : cache.entrySet()) {
                        f.writeObject(e.getKey());
                        f.writeObject(e.getValue());
                    }
                }
                synchronized(cacheSize) {
                    f.writeObject(cacheSize.size());
                    for (Map.Entry<AlgorithmParameter, Integer> e : cacheSize.entrySet()) {
                        f.writeObject(e.getKey());
                        f.writeObject(e.getValue());
                    }
                }
                lastSaved=System.currentTimeMillis();
                showStats();
                f.close();
                f=null;
                Files.move(Paths.get(filename+".tmp"),Paths.get(filename), StandardCopyOption.REPLACE_EXISTING);
            }
        } catch(Exception e) {
            throw e;
        } finally {
            if(f!=null) {
                f.close();
            }
        }
    }

    private static void showStats() {
        final String sepLine="-----------------------------------------------------------";
        synchronized(cache) {
            LOGGER.log(Level.INFO, sepLine);
            LOGGER.log(Level.INFO, "| cache stats");
            LOGGER.log(Level.INFO, sepLine);
            int sum = 0;
            int tot = 0;
            for (Map.Entry<AlgorithmParameter, Queue<AsymmetricKey>> q : new TreeMap<AlgorithmParameter,Queue<AsymmetricKey>>(cache).entrySet()) {
                LOGGER.log(Level.INFO, "| " + q.getKey().toString() + ": " + q.getValue().size() + "/" + cacheSize.get(q.getKey()));
                sum += q.getValue().size();
                tot += cacheSize.get(q.getKey());

            }
            LOGGER.log(Level.INFO, sepLine);
            LOGGER.log(Level.INFO, "| Total: "+sum+"/"+tot);
            synchronized(log) {
                if(!log.isEmpty()) {
                    LOGGER.log(Level.INFO, sepLine);
                    for(LastCalculated l:log) {
                        LOGGER.log(Level.INFO, "| "+l.toString());
                    }
                }
            }
            LOGGER.log(Level.INFO, sepLine);
        }
    }

}
