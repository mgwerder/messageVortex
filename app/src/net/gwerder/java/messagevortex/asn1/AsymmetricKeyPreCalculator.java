package net.gwerder.java.messagevortex.asn1;

import net.gwerder.java.messagevortex.MessageVortexLogger;
import net.gwerder.java.messagevortex.asn1.encryption.AlgorithmType;
import net.gwerder.java.messagevortex.asn1.encryption.Mode;
import net.gwerder.java.messagevortex.asn1.encryption.Padding;
import net.gwerder.java.messagevortex.asn1.encryption.Parameter;

import java.io.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;

/**
 * This is a class to precalculate keys.
 *
 * It is disabled by default. Enable it by setting a caching file Name. To disable set the name to null.
 * @TODO: 06.05.2017  make it multi threaded
 */
public class AsymmetricKeyPreCalculator implements Serializable {

    private static final java.util.logging.Logger LOGGER;
    static {
        LOGGER = MessageVortexLogger.getLogger((new Throwable()).getStackTrace()[0].getClassName());
        MessageVortexLogger.setGlobalLogLevel( Level.ALL);
    }
    private static long lastSaved = 0;

    private static int numThreads=4;

    private static class InternalThread extends Thread {

        private static int counter=0;

        private boolean shutdown=false;

        public InternalThread() {
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
        public void shutdown() {

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
                        if(maxSize.intValue()>e.getValue().size() && tfiller<filler) {
                            p=e.getKey();
                            filler=tfiller;
                        }
                    }
                }

                // calculate parameter set (if any)
                if(p!=null) {
                    try {
                        final AlgorithmParameter param=p;
                        // prepare thread list
                        List<Thread> tl=new ArrayList<>();
                        for(int i=0;i<numThreads;i++) {
                            tl.add(new Thread() {
                                public void run() {
                                    LOGGER.log(Level.FINE,"precalculating key "+param.toString()+"");
                                    try{
                                        AsymmetricKey ak=new AsymmetricKey(param,false);

                                        // put in cache
                                        synchronized (cache) {
                                            Queue<AsymmetricKey> q=cache.get(param);
                                            assert q!=null;
                                            assert ak!=null;
                                            q.add(ak);
                                        }
                                    } catch(IOException ioe) {
                                        LOGGER.log(Level.SEVERE,"got unexpected exception",ioe);
                                    }
                                }

                            });
                        }

                        // wait for all threads to finish
                        for(Thread t:tl) {
                            try {
                                t.join();
                            }catch(InterruptedException ie) {
                                LOGGER.log(Level.SEVERE,"got unexpected exception",ie);
                            }
                        }

                        // store cache
                        save();
                    } catch(IOException|ClassNotFoundException ioe) {
                        LOGGER.log(Level.INFO,"exception while storing file",ioe);
                    }
                }

                // sleep 10
                if(p==null) {
                    try{
                        Thread.sleep(10000);
                    } catch(InterruptedException ie) {
                        // ignore it as we do not care
                    }
                }
            }

        }

    }

    private static final Map<AlgorithmParameter,Queue<AsymmetricKey>> cache=new ConcurrentHashMap<>();

    private static final Map<AlgorithmParameter,Integer> cacheSize=new ConcurrentHashMap<>();

    private static InternalThread runner=null;

    private static String filename=null;

    public static AsymmetricKey getPrecomputedAsymmetricKey(AlgorithmParameter parameters) {
        parameters=prepareParameters(parameters);

        synchronized (cache) {
            if(filename==null && runner!=null) {
                if(!runner.isAlive()) {
                    runner=null;
                }
            }
            if(filename==null) {
                // we are shutting down or have been shut down. No services are provided
                LOGGER.log(Level.INFO, "cache disabled .. no key offered");
                return null;
            } else if(cache.get(parameters)==null) {
                // this cache does not yet exist schedule for creation
                synchronized(cache) {
                    cacheSize.put(parameters, 1);
                    cache.put(parameters, new ArrayDeque<AsymmetricKey>());
                }
                LOGGER.log(Level.FINE, "added new key type to cache");
                return null;
            } else if(cache.get(parameters).size()==0) {
                // this cache is too small as it is empty increase up to 100 storage places
                synchronized(cache) {
                    cacheSize.put(parameters, Math.min(400, cacheSize.get(parameters) + 1));
                }
                LOGGER.log(Level.FINE, "cache underrun ... increased key type "+parameters.get(Parameter.ALGORITHM)+"/"+parameters.get(Parameter.KEYSIZE)+" cache to "+cacheSize.get(parameters));
                return null;
            }
            LOGGER.log(Level.FINE, "cache offered precalculated key");
            return cache.get(parameters).poll();
        }
    }

    private static AlgorithmParameter prepareParameters(AlgorithmParameter ap) {
        AlgorithmParameter ret=ap.clone();

        // clear IV parameters as they are not relevant
        ap.put(Parameter.IV,null);

        // make sure that padding and mode are on default
        ap.put(Parameter.PADDING, Padding.getDefault(AlgorithmType.ASYMMETRIC).toString());
        ap.put(Parameter.MODE, Mode.getDefault(AlgorithmType.ASYMMETRIC).toString());

        return ap;
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
        if(filename==null && runner!=null) {
            if(!runner.isAlive()) {
                runner=null;
            }
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
                } catch (IOException | ClassNotFoundException e) {
                    LOGGER.log(Level.INFO, "error loading cache file (will be recreated)", e);
                }
                // start runner
                runner = new InternalThread();
            }
        }

    }

    private static void load() throws IOException,ClassNotFoundException {
        ObjectInputStream f=null;
        try{
            synchronized (cache) {
                f = new ObjectInputStream(new FileInputStream(filename));
                Map<AlgorithmParameter, Queue<AsymmetricKey>> lc = (Map<AlgorithmParameter, Queue<AsymmetricKey>>) f.readObject();
                Map<AlgorithmParameter, Integer> lcc = (Map<AlgorithmParameter, Integer>) (f.readObject());
                synchronized(cache) {
                    cache.clear();
                    cache.putAll(lc);
                    cacheSize.clear();
                    cacheSize.putAll(lcc);
                }
                showStats();
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
        synchronized(cache) {
            LOGGER.log(Level.INFO, "-----------------------------------------------------------");
            LOGGER.log(Level.INFO, "| cache stats");
            LOGGER.log(Level.INFO, "-----------------------------------------------------------");
            int sum = 0;
            int tot = 0;
            for (Map.Entry<AlgorithmParameter, Queue<AsymmetricKey>> q : cache.entrySet()) {
                LOGGER.log(Level.INFO, "| " + q.getKey().toString() + ": " + q.getValue().size() + "/" + cacheSize.get(q.getKey()));
                sum += q.getValue().size();
                tot += cacheSize.get(q.getKey());

            }
            LOGGER.log(Level.INFO, "-----------------------------------------------------------");
            LOGGER.log(Level.INFO, "| Total: "+sum+"/"+tot);
            LOGGER.log(Level.INFO, "-----------------------------------------------------------");
        }
    }

    private static void save() throws IOException,ClassNotFoundException {
        // do not allow saving more often than every minute
        if(lastSaved+60000>System.currentTimeMillis()) {
            return;
        }

        // store data
        ObjectOutputStream f=null;
        try {
            synchronized (cache) {
                f = new ObjectOutputStream(new FileOutputStream(filename));
                f.writeObject(cache);
                f.writeObject(cacheSize);
                lastSaved=System.currentTimeMillis();
                showStats();
            }
        } catch(Exception e) {
            throw e;
        } finally {
            if(f!=null) {
                f.close();
            }
        }
    }

}
