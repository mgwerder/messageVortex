package net.messagevortex.transport.dummy;

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

import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import net.messagevortex.AbstractDaemon;
import net.messagevortex.Config;
import net.messagevortex.MessageVortex;
import net.messagevortex.MessageVortexLogger;
import net.messagevortex.transport.ByteArrayBuilder;
import net.messagevortex.transport.RandomString;
import net.messagevortex.transport.Transport;
import net.messagevortex.transport.TransportReceiver;

public class DummyTransportTrx extends AbstractDaemon implements Transport {
  
  private static final Logger LOGGER;
  
  static {
    LOGGER = MessageVortexLogger.getLogger((new Throwable()).getStackTrace()[0].getClassName());
  }
  
  static Map<String, String> idReservation;
  static final Object mon = new Object();
  static Map<String, TransportReceiver> endpoints = new HashMap<>();
  private static String name = null;
  private String registeredEndpoint = null;
  
  /**
   * <p>Constructor to set up a dummy endpoint with named id and blender.</p>
   *
   * @param section section containing data to set up endpoint
   * @throws IOException if endpoint id is already defined
   */
  public DummyTransportTrx(String section) throws IOException {
    initCluster();
    LOGGER.log(Level.INFO, "setup of dummy endpoint for section \"" + section + "\"");
    String id = Config.getDefault().getStringValue(section, "transport_id");
    LOGGER.log(Level.INFO, "  id is \"" + id + "\"");
    String blenderName = Config.getDefault().getStringValue(section, "blender");
    LOGGER.log(Level.INFO, "  blender is \"" + blenderName + "\"");
    TransportReceiver blender = MessageVortex.getBlender(blenderName);
    LOGGER.log(Level.INFO, "  blender " + (blender != null ? "found" : "not found"));
    LOGGER.log(Level.INFO, "  initializing transport");
    init(id, blender);
    LOGGER.log(Level.INFO, "setup of dummy endpoint for section \"" + section + "\" done");
  }
  
  /**
   * <p>Sets the name of the cluster instance</p>
   * @param newName the new Name of the instance to connect to.
   */
  public static void setClusterName(String newName) throws IOException {
    synchronized (mon) {
      if (idReservation == null) {
        name = newName;
      } else {
        throw new IOException("Cluster is already initialized");
      }
    }
  }
  
  /**
   * <p>Constructor to set up a dummy endpoint with named id and blender.</p>
   *
   * @param id      ID of the endpoint
   * @param blender blender to be used for received messages
   * @throws IOException if endpoint id is already defined
   */
  public DummyTransportTrx(String id, TransportReceiver blender) throws IOException {
    init(id, blender);
  }
  
  /**
   * Constructor to create an endpoint with a random id.
   *
   * @param blender reference to the respective blender layer
   * @throws IOException if therad problems occure
   */
  public DummyTransportTrx(TransportReceiver blender) throws IOException {
    initCluster();
    synchronized (endpoints) {
      String id = null;
      while (id == null || idReservation.containsKey(id)) {
        id = RandomString.nextString(5, "0123456789abcdef@example.com");
      }
      init(id, blender);
    }
  }
  
  private void initCluster() throws IOException {
    synchronized (mon) {
      if (name == null) {
        // set an instance name
        name = InetAddress.getLocalHost().getHostName();
      }
      
      HazelcastInstance hz = Hazelcast.getOrCreateHazelcastInstance(new com.hazelcast.config.Config(name));
      idReservation = hz.getMap("dummyTransportTrxEndpoints");
    }
  }
  
  private void init(String id, TransportReceiver blender) throws IOException {
    initCluster();
    synchronized (endpoints) {
      if (idReservation.containsKey(id)) {
        throw new IOException("Duplicate transport endpoint identifier (id:" + id + ")");
      }
      String host = InetAddress.getLocalHost().getHostName();
      LOGGER.log(Level.INFO, "Registering " + id + " to node " + host);
      idReservation.put(id, host);
      registeredEndpoint = id;
      endpoints.put(id, blender);
    }
  }
  
  @Override
  public void shutdownDaemon() {
    // deregister endpoint
    synchronized (idReservation) {
      try {
        String hostname = idReservation.remove(registeredEndpoint);
        if (hostname.equals(InetAddress.getLocalHost().getHostName())) {
          LOGGER.log(Level.FINE, "successfully deregistered id " + registeredEndpoint + " from dummy transport");
        } else {
          LOGGER.log(Level.SEVERE, "OUCH... for some reasons this endpoint was registered to a different host (" + hostname + "). It is unclear if your system is still working properly.");
        }
      } catch (UnknownHostException uhe) {
        LOGGER.log(Level.SEVERE, "OUCH... got exception while fetching own host name.", uhe);
      }
    }
    
    super.shutdownDaemon();
  }
  
  /**
   * <p>send a message to another dummy endpoint.</p>
   *
   * <p>FIXME: This only works for local messages</p>
   *
   * @param address the string representation of the target address on the transport layer
   * @param is      the input stream to be sent
   * @throws IOException if requested endpoint id is unknown
   */
  public void sendMessage(final String address, InputStream is) throws IOException {
    TransportReceiver ep = endpoints.get(address);
    if (address == null || ep == null) {
      throw new IOException("recipient address is unknown");
    }
    // convert is to byte array
    ByteArrayBuilder bab = new ByteArrayBuilder();
    int n;
    byte[] buffer = new byte[1024];
    while ((n = is.read(buffer, 0, buffer.length)) != -1) {
      bab.append(buffer, n);
    }
    is.close();
    
    // send byte array as input stream to target
    byte[] arr = bab.toBytes();
    LOGGER.log(Level.INFO, "Dummy transport received " + arr.length + " sized message");
    final InputStream iso = new ByteArrayInputStream(arr);
    synchronized (endpoints) {
      // create new thread for message processing
      new Thread() {
        @Override
        public void run() {
          ep.gotMessage(iso);
        }
      }.start();
    }
  }
  
  /**
   * <p>Remove all Dummy endpoints from the main listing.</p>
   */
  public static void clearDummyEndpoints() {
    synchronized (endpoints) {
      endpoints.clear();
    }
  }
}

