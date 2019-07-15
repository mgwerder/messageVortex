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

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.SecureRandom;
import java.util.ArrayDeque;
import java.util.HashMap;
import java.util.Map;
import java.util.Queue;
import java.util.TreeMap;
import java.util.logging.Level;

import net.messagevortex.MessageVortexLogger;

/**
 * <p>The key cache supporting AsymmetricKey.</p>
 */
public class AsymmetricKeyCache implements Serializable {

  public static final long serialVersionUID = 100000000081L;

  public static final SecureRandom esr = new SecureRandom();

  private static final java.util.logging.Logger LOGGER;

  static {
    LOGGER = MessageVortexLogger.getLogger((new Throwable()).getStackTrace()[0].getClassName());
    MessageVortexLogger.setGlobalLogLevel(Level.ALL);
  }

  private Map<AlgorithmParameter, CacheElement> cache = new TreeMap<>();

  private static class CacheElement implements Serializable {

    public static final long serialVersionUID = 100000000080L;

    private static final int MAX_NUMBER_OF_CALC_TIMES = 100;
    private static final int MAX_CACHE_SIZE = 40000;

    private int maxSize = 1;
    private long averageCalcTime = 100;
    private int numberOfCalcTimes = 0;
    private Queue<AsymmetricKey> cache = new ArrayDeque<>();

    /***
     * <p>Gets the maximum size of the cache element.</p>
     *
     * @return the currently set maximum size
     */
    public int getMaxSize() {
      return maxSize;
    }

    /***
     * <p>Sets the maximum size of the cache to the specified value.</p>
     *
     * @param size the new maximum size to be set
     * @return the previously set maximum size
     */
    public int setMaxSize(int size) {
      int ret = maxSize;
      maxSize = size;
      return ret;
    }

    /***
     * <p>Gets the topmost cache element from the element queue.</p>
     *
     * @return the topmost element from the cache queue
     */
    public AsymmetricKey pull() {
      synchronized (cache) {
        if (cache.size() > 0) {
          return cache.poll();
        } else {
          maxSize++;
          return null;
        }
      }
    }

    /***
     * <p>Gets the topmost element from the cache queue without removing it.</p>
     *
     * @return the topmost element from the cache queue
     */
    public AsymmetricKey peek() {
      synchronized (cache) {
        if (cache.size() > 0) {
          return cache.peek();
        } else {
          return null;
        }
      }
    }

    /***
     * <p>Adds a new precaculated key to the cache queue.</p>
     * @param key the key to be added.
     */
    public void push(AsymmetricKey key) {
      synchronized (cache) {
        cache.add(key);
      }
    }

    /***
     * <p>Adds the time of calculation of the last key for stats purposes.</p>
     * @param millis the required time in milliseconds
     */
    public void setCalcTime(long millis) {
      synchronized (cache) {
        averageCalcTime = (averageCalcTime * numberOfCalcTimes + millis) / (numberOfCalcTimes + 1);
        numberOfCalcTimes = Math.min(numberOfCalcTimes, MAX_NUMBER_OF_CALC_TIMES);
      }
    }

    /***
     * <p>Gets the average time to calculate this type of key.</p>
     *
     * @return the average time to calculate a new cache element of this type
     */
    public double getAverageCalcTime() {
      return averageCalcTime;
    }

    /***
     * <p>Cacluates the time required to fill this cache based on its current fill.</p>
     *
     * @return the calculated time required in milliseconds
     */
    public double getCacheFillTime() {
      double avg = getAverageCalcTime();
      if (avg <= 0) {
        avg = 10;
      }
      return Math.max(0, maxSize - cache.size()) * avg;
    }

    /***
     * <p>Gets the number of elements in the cache.</p>
     *
     * @return the current number of elements in the cache queue
     */
    public int size() {
      synchronized (cache) {
        return cache.size();
      }
    }

    /***
     * <p>Clears all elements from the cache queue.</p>
     */
    public void clearCache() {
      synchronized (cache) {
        cache.clear();
      }
    }

    /***
     * <p>Merges two cache queues.</p>
     *
     * @param element the cache queue to be merged into the current one
     */
    public void merge(CacheElement element) {
      synchronized (cache) {
        cache.addAll(element.cache);
        maxSize = Math.max(maxSize, element.maxSize);
        if ((numberOfCalcTimes + element.numberOfCalcTimes) > 0) {
          averageCalcTime = (averageCalcTime * numberOfCalcTimes + element.averageCalcTime
                  * element.numberOfCalcTimes) / (numberOfCalcTimes + element.numberOfCalcTimes);
        }
        numberOfCalcTimes = Math.max(numberOfCalcTimes + element.numberOfCalcTimes,
                MAX_NUMBER_OF_CALC_TIMES);
      }
    }

    /***
     * <p>Serializer for the cache queue including the cache elements and stats.</p>
     *
     * @param out the writer to be used
     * @throws IOException if the writer does not accept the object (e.g., permission denied or
     *                     disk full)
     */
    private void writeObject(ObjectOutputStream out) throws IOException {
      synchronized (cache) {
        out.writeObject(maxSize);
        out.writeObject(averageCalcTime);
        out.writeObject(numberOfCalcTimes);
        out.writeObject(cache.size());
        for (AsymmetricKey ak : cache) {
          out.writeObject(ak);
        }
      }
    }

    /***
     * <p>Deserializer for the cache queue.</p>
     *
     * @param in the reader to be used
     * @throws IOException if a disk error occures when reading (e.g., corrupted filesystem)
     * @throws ClassNotFoundException if deserialization fails
     */
    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
      maxSize = (Integer) in.readObject();
      averageCalcTime = (long) (in.readObject());
      numberOfCalcTimes = (Integer) in.readObject();
      int i = (Integer) in.readObject();
      cache = new ArrayDeque<>(i);
      for (int j = 0; j < i; j++) {
        cache.add((AsymmetricKey) in.readObject());
      }
    }

    /***
     * <p>Convenience function to request a cache increase in case of an empty cache.</p>
     */
    public void requestCacheIncrease() {
      maxSize = Math.min(MAX_CACHE_SIZE, Math.max((int) (maxSize * 1.1), maxSize + 1));
    }

  }

  /***
   * <p>Stores the cache to the specified filename for later usage.</p>
   *
   * @param filename     the filename to store to (will replace an existing file)
   * @throws IOException if writing of file fails
   */
  public void store(String filename) throws IOException {
    Path p = Paths.get(filename);
    try (
            ObjectOutputStream os = new ObjectOutputStream(Files.newOutputStream(p));
    ) {
      os.writeObject(this);
    }
    LOGGER.log(Level.INFO, "stored cache to file \"" + filename + "\"");
    showStats();
  }

  /***
   * <p>Loads the cache from the specified filename.</p>
   *
   * @param filename     the filename to read from
   * @throws IOException if reading of file fails
   */
  public void load(String filename) throws IOException {
    Path p = Paths.get(filename);
    try (ObjectInputStream is = new ObjectInputStream(Files.newInputStream(p))) {
      load(is, false);
    }
    LOGGER.log(Level.INFO, "loaded cache from file \"" + filename + "\"");
    showStats();
  }

  private void load(ObjectInputStream f, boolean merge) throws IOException {
    synchronized (cache) {
      if (!merge) {
        cache.clear();
      }
      try {
        @SuppressWarnings("unchecked")
        AsymmetricKeyCache tc = (AsymmetricKeyCache) f.readObject();
        if (!merge) {
          cache.clear();
          cache.putAll(tc.cache);
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
      } catch (ClassNotFoundException cnfe) {
        throw new IOException("got unexpected exception when deserializing", cnfe);
      }

    }
  }

  /***
   * <p>Adds the keys in the specified file to the cache.</p>
   *
   * @param filename     the filename of the cache to merge
   * @throws IOException if reading of the file fails
   */
  public void merge(String filename) throws IOException {
    Path p = Paths.get(filename);
    try (ObjectInputStream is = new ObjectInputStream(Files.newInputStream(p))) {
      load(is, true);
    } catch (ClassCastException cce) {
      throw new IOException("Error deserializing file \"" + filename + "\"", cce);
    }
  }

  /***
   * <p>Sets the time for a calculation with the specified parameter.</p>
   *
   * @param ap      parameter set
   * @param millis  the time in milliseconds it takes on average to calculate the key
   */
  public void setCalcTime(AlgorithmParameter ap, long millis) {
    CacheElement ce = cache.get(ap);
    if (ce != null) {
      ce.setCalcTime(millis);
    }
  }

  private AlgorithmParameter getCacheElementByIndex(int index) {
    synchronized (cache) {
      int i = 0;
      for (Map.Entry<AlgorithmParameter, CacheElement> e : cache.entrySet()) {
        i++;
        if (i == index) {
          return e.getKey();
        }
      }
      return null;
    }
  }

  /***
   * <p>Gets a precalculated key from the cache.</p>
   *
   * @param parameter the parameter set requested
   * @return the key or null if no such key is available in the cache
   */
  public AsymmetricKey pull(AlgorithmParameter parameter) {
    synchronized (cache) {
      CacheElement ce = cache.get(parameter);
      if (ce == null) {
        ce = new CacheElement();
        cache.put(parameter, ce);
      }
      AsymmetricKey ret = ce.pull();
      return ret;
    }
  }

  /***
   * <p>Gets a precalculated key from the cache without removing it.</p>
   *
   * @param parameter the parameter set requested
   * @return the key or null if no such key is available in the cache
   */
  public AsymmetricKey peek(AlgorithmParameter parameter) {
    synchronized (cache) {
      CacheElement ce = cache.get(parameter);
      if (ce == null) {
        ce = new CacheElement();
        cache.put(parameter, ce);
      }
      AsymmetricKey ret = ce.peek();
      return ret;
    }
  }

  /***
   * <p>store a precalculated key into the cache.</p>
   *
   * @param key the key to be stored
   */
  public void push(AsymmetricKey key) {
    AlgorithmParameter ap = key.getAlgorithmParameter();
    synchronized (cache) {
      CacheElement ce = cache.get(ap);
      if (ce == null) {
        ce = new CacheElement();
        cache.put(ap, ce);
      }
      ce.push(key);
    }
  }

  /***
   * <p>Increase the cache size for the specified parameter set.</p>
   *
   * @param parameter the parameter set to be increased
   */
  public void requestCacheIncrease(AlgorithmParameter parameter) {
    synchronized (cache) {
      CacheElement ce = cache.get(parameter);
      if (ce == null) {
        ce = new CacheElement();
        cache.put(parameter, ce);
      }
      ce.requestCacheIncrease();
    }
  }

  /***
   * <p>Gets a set of parameter which should be calculated next.</p>
   *
   * @return the parameter set
   */
  public AlgorithmParameter getSpeculativeParameter() {
    // build a sorted list of total duration
    long l = 0;
    Map<Long, AlgorithmParameter> hm = new TreeMap<>();
    synchronized (cache) {
      for (Map.Entry<AlgorithmParameter, CacheElement> me : cache.entrySet()) {
        long ft = (long) (me.getValue().getCacheFillTime());
        // make sure that all key sizes have a minimum time

        l += ft;
        if (ft > 0) {
          hm.put(l, me.getKey());
        }
      }
    }

    // if all caches are full return no parameter
    if (l == 0 || hm.size() == 0) {
      return null;
    }

    // elect weighted element acording to number and estimated calc time
    long e;
    synchronized (esr) {
      e = Math.abs(esr.nextLong() % (l + 1));
    }


    // get element and return
    for (Map.Entry<Long, AlgorithmParameter> me : hm.entrySet()) {
      if (me.getKey() >= e) {
        return me.getValue();
      }
    }

    return null;
  }


  /***
   * <p>Get the size of the lowest cache in fraction of percent.</p>
   *
   * @return the fill state of the lowest cache (bounds 0..1)
   */
  public double getLowestCacheSize() {
    double lowest = 0;
    for (Map.Entry<AlgorithmParameter, CacheElement> e : cache.entrySet()) {
      lowest = Math.min(lowest, (double) (e.getValue().size()) / e.getValue().getMaxSize());
    }
    ;
    return lowest;
  }

  /***
   * <p>Get the total cache fill grade in percent.</p>
   *
   * @return the fill state of cache (bounds 0..1)
   */
  public double getCacheFillGrade() {
    int maxSize = 0;
    int currSize = 0;
    for (Map.Entry<AlgorithmParameter, CacheElement> e : cache.entrySet()) {
      maxSize += e.getValue().getMaxSize();
      currSize += Math.min(e.getValue().size(), e.getValue().getMaxSize());
    }
    if (maxSize == 0) {
      return 1.0;
    } else {
      return (0.0 + currSize) / maxSize;
    }
  }

  /***
   * <p>Remove all elements from the cache.</p>
   */
  public void clear() {
    synchronized (cache) {
      for (CacheElement ce : cache.values()) {
        ce.clearCache();
      }
    }
  }

  /***
   * <p>Check if the cache is empty.</p>
   *
   * @return true if the cache is empty
   */
  public boolean isEmpty() {
    double size = 0;
    for (Map.Entry<AlgorithmParameter, CacheElement> e : cache.entrySet()) {
      size += (double) (e.getValue().size()) / e.getValue().getMaxSize();
    }
    return (int) (size) == 0;
  }

  private void writeObject(ObjectOutputStream out) throws IOException {
    synchronized (cache) {
      out.writeInt(cache.size());
      for (Map.Entry<AlgorithmParameter, CacheElement> me : cache.entrySet()) {
        out.writeObject(me.getKey());
        out.writeObject(me.getValue());
      }
    }
  }

  private void readObject(ObjectInputStream in) throws IOException {
    try {
      int i = in.readInt();
      cache = new HashMap<>(i);
      for (int j = 0; j < i; j++) {
        cache.put((AlgorithmParameter) in.readObject(), (CacheElement) in.readObject());
      }
    } catch (ClassNotFoundException cnfe) {
      throw new IOException("Exception while reading cache file", cnfe);
    }
  }


  private static String percentBar(double percent, int size) {
    StringBuilder sb = new StringBuilder();
    sb.append('|');
    for (int i = 1; i < Math.min(size, percent * size); i++) {
      sb.append('#');
    }
    while (sb.length() < size) {
      sb.append('.');
    }
    sb.append('|');
    return sb.toString();
  }

  /***
   * <p>Set the expected size of the cache.</p>
   * @param index index of the cache to be set
   * @param value the new size of the specified cache
   * @throws IOException if the index is not known to the cache
   */
  public void setCacheSize(int index, int value) throws IOException {
    synchronized (cache) {
      AlgorithmParameter ap = getCacheElementByIndex(index);
      if (ap == null) {
        throw new IOException("cache element " + index + " not found");
      }
      CacheElement ce = cache.get(ap);
      ce.setMaxSize(value);
    }
  }

  /***
   * <p>Remove the specified key type from cache.</p>
   * @param index the index of the cache to be removed
   * @throws IOException if the index does not belong to a known element
   */
  public void removeCacheElement(int index) throws IOException {
    synchronized (cache) {
      AlgorithmParameter ap = getCacheElementByIndex(index);
      if (ap == null) {
        throw new IOException("cache element " + index + " not found");
      }
      cache.remove(ap);
    }
  }

  /***
   * <p>Dumps cache stats to the logger.</p>
   */
  public void showStats() {
    final String sepLine = "-----------------------------------------------------------";
    synchronized (cache) {
      LOGGER.log(Level.INFO, sepLine);
      LOGGER.log(Level.INFO, "| cache stats ");
      LOGGER.log(Level.INFO, sepLine);
      int sum = 0;
      int tot = 0;
      int i = 0;
      for (Map.Entry<AlgorithmParameter, CacheElement> e : cache.entrySet()) {
        i++;
        CacheElement ce = e.getValue();
        long s = ce.size();
        long ms = ce.getMaxSize();
        LOGGER.log(Level.INFO, "|" + String.format("%2s", i) + ") " + String.format("%5s", s)
                + "/" + String.format("%5s", ms) + " "
                + percentBar((double) (s) / ms, 20) + " " + e.getKey());
        sum += s;
        tot += ms;

      }
      LOGGER.log(Level.INFO, sepLine);
      LOGGER.log(Level.INFO, "| Total: " + sum + "/" + tot + "");
      LOGGER.log(Level.INFO, sepLine);
    }
  }

}
