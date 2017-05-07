package net.gwerder.java.messagevortex.asn1;

import net.gwerder.java.messagevortex.MessageVortexLogger;
import net.gwerder.java.messagevortex.asn1.encryption.Algorithm;
import net.gwerder.java.messagevortex.asn1.encryption.Parameter;

import java.io.*;
import java.util.ArrayDeque;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;

/**
 * This is a class to precalculate keys.
 *
 * It is disabled by default. Enable it by setting a caching file Name. To disable set the name to null.
 * @// TODO: 06.05.2017  make it multi threaded
 */
public class AsymmetricKeyPreCalculator implements Serializable {

    private static final java.util.logging.Logger LOGGER;
    static {
        LOGGER = MessageVortexLogger.getLogger((new Throwable()).getStackTrace()[0].getClassName());
        MessageVortexLogger.setGlobalLogLevel( Level.ALL);
    }

    private static class InternalThread extends Thread {

        private boolean shutdown=false;

        public InternalThread() {
            // This thread may die safely
            setDaemon(true);

            // we start the daemon as soon as we can
            start();
        }

        /***
         * Tells the process to shutdown asap
         */
        public void shutdown() {

        }

        public void run() {
            while(!shutdown) {
                // get a parameter set to be calculated
                // FIXME
                AlgorithmParameter p=null;

                // calculate parameter set (if any)
                if(p!=null) {
                    // calculate key
                    try {
                        AsymmetricKey ak=new AsymmetricKey(p);
                        // put in cache
                        synchronized (cache) {
                            cache.get(p).add(ak);
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

    private static final Map<Map<String,Object>,Queue<AsymmetricKey>> cache=new ConcurrentHashMap<>();

    private static final Map<Map<String,Object>,Integer> cacheSize=new ConcurrentHashMap<>();

    private static InternalThread runner=null;

    private static String filename=null;

    public static AsymmetricKey getPrecomputedAsymmetricKey(Map<String,Object> parameters) {
        synchronized (cache) {
            if(filename==null && runner!=null) {
                if(!runner.isAlive()) {
                    runner=null;
                }
            }
            if(filename==null) {
                // we are shutting down or have been shut down. No services are provided
                return null;
            } else if(cache.get(parameters)==null) {
                // this cache does not yet exist schedule for creation
                cacheSize.put(parameters,1);
                cache.put(parameters,new ArrayDeque<>());
                return null;
            } else if(cache.get(parameters).size()==0) {
                // this cache is too small as it is empty
                cacheSize.put(parameters,cacheSize.get(parameters)+1);
                return null;
            }
            return cache.get(parameters).poll();
        }
    }

    /***
     *
     * @param name
     * @return
     * @throws IllegalThreadStateException if the previous thread has not yet shutdown but a new thread was tried to be started
     */
    public static String setCacheFileName(String name) {
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
        ObjectInputStream f = new ObjectInputStream(new FileInputStream(filename));
        Map<Map<String, Object>, Queue<AsymmetricKey>> lc = (Map<Map<String, Object>, Queue<AsymmetricKey>>)f.readObject();
        Map<Map<String, Object>, Integer> lcc = (Map<Map<String, Object>, Integer>) (f.readObject());
        cache.clear();
        cache.putAll(lc);
        cacheSize.clear();
        cacheSize.putAll(lcc);
        f.close();
    }

    private static void save() throws IOException,ClassNotFoundException {
        ObjectOutputStream f = new ObjectOutputStream(new FileOutputStream(filename));
        f.writeObject(cache);
        f.writeObject(cacheSize);
        f.close();
    }

}
