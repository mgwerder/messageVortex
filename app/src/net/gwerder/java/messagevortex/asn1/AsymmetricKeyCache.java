package net.gwerder.java.messagevortex.asn1;
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

import net.gwerder.java.messagevortex.MessageVortexLogger;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.SecureRandom;
import java.util.*;
import java.util.logging.Level;

/**
 * The key cache supporting AsymmetricKey.
 *
 * Created by martin.gwerder on 04.01.2018.
 */
public class AsymmetricKeyCache implements Serializable {

    public static final long serialVersionUID = 100000000081L;

    public static final SecureRandom esr=new SecureRandom();

    private static final java.util.logging.Logger LOGGER;
    static {
        LOGGER = MessageVortexLogger.getLogger((new Throwable()).getStackTrace()[0].getClassName());
        MessageVortexLogger.setGlobalLogLevel( Level.ALL);
    }

    private Map<AlgorithmParameter,CacheElement> cache=new HashMap<>();

    private static class CacheElement implements Serializable{

        public static final long serialVersionUID = 100000000080L;

        private static final int MAX_NUMBER_OF_CALC_TIMES =   100;
        private static final int MAX_CACHE_SIZE           = 40000;

        private int maxSize = 1;
        private long averageCalcTime=100;
        private int numberOfCalcTimes=0;
        private Queue<AsymmetricKey> cache=new ArrayDeque<AsymmetricKey>();

        public int getMaxSize() {
            return maxSize;
        }

        public AsymmetricKey pull() {
            synchronized(cache) {
                if (cache.size() > 0) {
                    return cache.poll();
                } else {
                    maxSize++;
                    return null;
                }
            }
        }

        public AsymmetricKey peek() {
            synchronized(cache) {
                if (cache.size() > 0) {
                    return cache.peek();
                } else {
                    return null;
                }
            }
        }

        public void push(AsymmetricKey key) {
            synchronized (cache) {
                cache.add(key);
            }
        }

        public void setCalcTime(long millis) {
            synchronized(cache) {
                averageCalcTime = (averageCalcTime * numberOfCalcTimes + millis) / (numberOfCalcTimes + 1);
                numberOfCalcTimes = Math.min(numberOfCalcTimes, MAX_NUMBER_OF_CALC_TIMES);
            }
        }

        public double getAverageCalcTime() {
            return averageCalcTime;
        }

        public double getCacheFillTime() {
            return Math.max(0,maxSize-cache.size())*getAverageCalcTime();
        }

        public int size() {
            synchronized(cache) {
                return cache.size();
            }
        }

        public void clearCache() {
            synchronized(cache) {
                cache.clear();
            }
        }

        public void merge(CacheElement element) {
            synchronized(cache) {
                cache.addAll(element.cache);
                maxSize=Math.max(maxSize,element.maxSize);
                if((numberOfCalcTimes+element.numberOfCalcTimes)>0) {
                    averageCalcTime = (averageCalcTime * numberOfCalcTimes + element.averageCalcTime * element.numberOfCalcTimes) / (numberOfCalcTimes + element.numberOfCalcTimes);
                }
                numberOfCalcTimes=Math.max(numberOfCalcTimes+element.numberOfCalcTimes,MAX_NUMBER_OF_CALC_TIMES);
            }
        }

        private void writeObject(ObjectOutputStream out) throws IOException {
            synchronized(cache) {
                out.writeObject(maxSize);
                out.writeObject(averageCalcTime);
                out.writeObject(numberOfCalcTimes);
                out.writeObject(cache.size());
                for (AsymmetricKey ak : cache) {
                    out.writeObject(ak);
                }
            }
        }

        private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
            maxSize = (Integer)in.readObject();
            averageCalcTime = (long)( in.readObject() );
            numberOfCalcTimes = (Integer)in.readObject();
            int i   = (Integer)in.readObject();
            cache=new ArrayDeque<>( i );
            for ( int j = 0; j < i; j++ ) {
                cache.add( (AsymmetricKey)in.readObject() );
            }
        }

        public void requestCacheIncrease() {
            maxSize=Math.min(MAX_CACHE_SIZE,Math.max((int)(maxSize*1.1),maxSize+1));
        }

    }

    public void store(String filename) throws IOException {
        Path p = Paths.get(filename);
        try (
                ObjectOutputStream os=new ObjectOutputStream(Files.newOutputStream( p ) );
        ) {
            os.writeObject(this);
        }
        LOGGER.log(Level.INFO, "stored cache to file \""+filename+"\"");
        showStats();
    }

    public void load(String filename) throws IOException {
        Path p = Paths.get(filename);
        try (ObjectInputStream is=new ObjectInputStream(Files.newInputStream(p)) ){
            load(is, false);
        }
        LOGGER.log(Level.INFO, "loaded cache from file \""+filename+"\"");
        showStats();
    }

    public void merge(String filename) throws IOException {
        Path p = Paths.get(filename);
        try( ObjectInputStream is=new ObjectInputStream( Files.newInputStream( p ) ) ) {
            load( is, true );
        } catch(ClassCastException cce) {
            throw new IOException( "Error deserializing file \"" + filename + "\"", cce );
        }
    }

    public void setCalcTime( AlgorithmParameter ap, long millis ) {
        CacheElement ce=cache.get( ap );
        if( ce != null ) {
            ce.setCalcTime( millis );
        }
    }

    private void load(ObjectInputStream f,boolean merge) throws IOException {
        synchronized (cache) {
            if (!merge) {
                cache.clear();
            }
            try {
                @SuppressWarnings("unchecked")
                AsymmetricKeyCache tc = (AsymmetricKeyCache) f.readObject();
                if (!merge) {
                    synchronized (cache) {
                        cache.clear();
                        cache.putAll(tc.cache);
                    }
                } else {
                    for (Map.Entry<AlgorithmParameter, CacheElement> ce : tc.cache.entrySet()) {
                        CacheElement t = cache.get(ce.getKey());
                        if (t == null) {
                            cache.put(ce.getKey(), ce.getValue());
                        } else {
                            t.merge(ce.getValue());
                        }
                    }
                }
            } catch(ClassNotFoundException cnfe) {
                throw new IOException("got unexpected exception when deserializing", cnfe);
            }

        }
    }


    public AsymmetricKey pull(AlgorithmParameter parameter) {
        CacheElement ce=cache.get(parameter);
        if(ce==null) {
            ce=new CacheElement();
            cache.put(parameter,ce);
        }
        AsymmetricKey ret=ce.pull();
        return ret;
    }

    public AsymmetricKey peek(AlgorithmParameter parameter) {
        CacheElement ce=cache.get(parameter);
        if(ce==null) {
            ce=new CacheElement();
            cache.put(parameter,ce);
        }
        AsymmetricKey ret=ce.peek();
        return ret;
    }

    public void push(AsymmetricKey key) {
        AlgorithmParameter ap=key.getAlgorithmParameter();
        synchronized(cache) {
            CacheElement ce = cache.get(ap);
            if(ce==null) {
                ce=new CacheElement();
                cache.put(ap,ce);
            }
            ce.push(key);
        }
    }

    public void requestCacheIncrease(AlgorithmParameter parameter) {
        synchronized(cache) {
            CacheElement ce = cache.get(parameter);
            if(ce==null) {
                ce=new CacheElement();
                cache.put(parameter,ce);
            }
            ce.requestCacheIncrease();
        }
    }

    public AlgorithmParameter getSpeculativeParameter() {

        // build a sorted list of total duration
        long l=0;
        Map<Long,AlgorithmParameter> hm=new TreeMap<>();
        synchronized(cache) {
            for(Map.Entry<AlgorithmParameter,CacheElement> me:cache.entrySet()) {
                long ft=(long)(me.getValue().getCacheFillTime());
                l+=ft;
                if(ft>0) {
                    hm.put(l,me.getKey());
                }
            }
        }

        // if all caches are full return no parameter
        if(l==0 || hm.size()==0) {
            return null;
        }

        // elect weighted element acording to number and estimated calc time

        long e;
        synchronized(esr) {
            e=esr.nextLong()%(l+1);
        }


        // get element and return
        for(Map.Entry<Long,AlgorithmParameter> me:hm.entrySet()) {
            if(me.getKey()>=e) {
                return me.getValue();
            }
        }

        return null;
    }

    public double getLowestCacheSize() {
        double lowest=0;
        for (Map.Entry<AlgorithmParameter, CacheElement> e : cache.entrySet()) {
            lowest=Math.min(lowest,(double)(e.getValue().size())/e.getValue().getMaxSize());
        };
        return lowest;
    }

    public double getCacheFillGrade() {
        int maxSize=0;
        int currSize=0;
        for (Map.Entry<AlgorithmParameter, CacheElement> e : cache.entrySet()) {
            maxSize+=e.getValue().getMaxSize();
            currSize+=Math.min(e.getValue().size(),e.getValue().getMaxSize());
        }
        if(maxSize==0) return 1.0;
        return (0.0+currSize)/maxSize;
    }

    public void clear() {
        synchronized (cache) {
            for(CacheElement ce:cache.values()) {
                ce.clearCache();
            }
        }
    }

    public boolean isEmpty() {
        double size=0;
        for (Map.Entry<AlgorithmParameter, CacheElement> e : cache.entrySet()) {
            size+=(double)(e.getValue().size())/e.getValue().getMaxSize();
        }
        return (int)(size)==0;
    }

    private void writeObject(ObjectOutputStream out) throws IOException {
        synchronized (cache) {
            out.writeInt(cache.size());
            for(Map.Entry<AlgorithmParameter,CacheElement> me:cache.entrySet()) {
                out.writeObject(me.getKey());
                out.writeObject(me.getValue());
            }
        }
    }

    private void readObject(ObjectInputStream in) throws IOException {
        try{
            int i=in.readInt();
            cache=new HashMap<>( i );
            for( int j = 0; j < i ; j++ ) {
                cache.put( (AlgorithmParameter)in.readObject(), (CacheElement)in.readObject() );
            }
        }catch( ClassNotFoundException cnfe ) {
            throw new IOException( "Exception while reading cache file", cnfe );
        }
    }

    public static String percentBar(double percent, int size) {
        StringBuilder sb=new StringBuilder();
        sb.append( '|' );
        for(int i=1;i<Math.min(size,percent*size);i++) {
            sb.append( '#' );
        }
        while(sb.length()<size) {
            sb.append( '.' );
        }
        sb.append( '|' );
        return sb.toString();
    }

    public void showStats() {
        final String sepLine = "-----------------------------------------------------------";
        synchronized( cache ) {
            LOGGER.log( Level.INFO, sepLine );
            LOGGER.log( Level.INFO, "| cache stats " );
            LOGGER.log( Level.INFO, sepLine );
            int sum = 0;
            int tot = 0;
            for (Map.Entry<AlgorithmParameter,CacheElement> e : cache.entrySet()) {
                CacheElement ce=e.getValue();
                long s = ce.size();
                long ms = ce.getMaxSize();
                LOGGER.log(Level.INFO, "| " + String.format( "%4s", s ) + "/" + String.format( "%4s", ms )+" "+percentBar((double)(s)/ms, 20 ) + " " + e.getKey() );
                sum += s;
                tot += ms;

            }
            LOGGER.log(Level.INFO, sepLine );
            LOGGER.log(Level.INFO, "| Total: " + sum + "/" + tot + "" );
            LOGGER.log(Level.INFO, sepLine );
        }
    }



}
