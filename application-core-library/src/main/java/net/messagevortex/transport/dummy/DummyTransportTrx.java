package net.messagevortex.transport.dummy;

import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.map.impl.proxy.MapProxyImpl;
import net.messagevortex.AbstractDaemon;
import net.messagevortex.Config;
import net.messagevortex.MessageVortexLogger;
import net.messagevortex.MessageVortexRepository;
import net.messagevortex.transport.ByteArrayBuilder;
import net.messagevortex.transport.RandomString;
import net.messagevortex.transport.Transport;
import net.messagevortex.transport.TransportReceiver;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;

public class DummyTransportTrx extends AbstractDaemon implements Transport {

    private static final Logger LOGGER;

    static {
        LOGGER = MessageVortexLogger.getLogger((new Throwable()).getStackTrace()[0].getClassName());
    }

    private static String defHostname;
    private static Map<String, String> idReservation = null;
    private static final Object mon = new Object();
    private static boolean localMode = false;

    static final Map<String, TransportReceiver> endpoints = new HashMap<>();
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
        TransportReceiver blender = MessageVortexRepository.getBlender("", blenderName);
        LOGGER.log(Level.INFO, "  blender " + (blender != null ? "found" : "not found"));
        LOGGER.log(Level.INFO, "  initializing transport");
        init(id, blender);
        LOGGER.log(Level.INFO, "setup of dummy endpoint for section \"" + section + "\" done");
    }

    /**
     * <p>Sets the name of the cluster instance.</p>
     *
     * @param newName the new Name of the instance to connect to.
     * @throws IOException if the cluster is already initialized
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
     * @throws IOException if thread problems occur
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
            if (idReservation == null) {
                if (name == null || "".equals(name)) {
                    // set an instance name
                    name = InetAddress.getLocalHost().getHostName() + "-" + RandomString.nextString(5);
                    LOGGER.log(Level.INFO, "  Got hazelcast instance name  " + name + " from node");
                }

                HazelcastInstance hz =
                        Hazelcast.getOrCreateHazelcastInstance(new com.hazelcast.config.Config(name));
                if (localMode) {
                    idReservation = new HashMap<>();
                } else {
                    idReservation = hz.getMap("dummyTransportTrxEndpoints");
                }
            }
        }
    }

    /**
     * <p>Set local only mode for dummy transport.</p>
     *
     * @param lm true if local mode should be set
     * @return old state of local mode
     * @throws IOException if cluster is already initialized
     */
    public static boolean setLocalMode(boolean lm) throws IOException {
        boolean old = localMode;
        synchronized (mon) {
            if (idReservation == null) {
                localMode = lm;
            } else {
                throw new IOException("Cluster is already initialized");
            }
        }
        return old;
    }

    private final void init(String id, TransportReceiver blender) throws IOException {
        defHostname = InetAddress.getLocalHost().getHostName();
        initCluster();
        synchronized (endpoints) {
            if (idReservation.containsKey(id)) {
                throw new IOException("Duplicate transport endpoint identifier (id:" + id + ")");
            }
            LOGGER.log(Level.INFO, "Registering " + id + " to node " + defHostname);
            idReservation.put(id, defHostname);
            registeredEndpoint = id;
            endpoints.put(id, blender);
        }
    }

    @Override
    public void shutdownDaemon() {
        // deregister endpoint
        List<String> l = new Vector<>();
        synchronized (idReservation) {
            // Remove all identities
            for (Map.Entry<String, String> e : idReservation.entrySet()) {
                if (e.getValue().equals(defHostname)) {
                    l.add(e.getKey());
                }
            }
            for (String key : l.toArray(new String[0])) {
                String hostname = idReservation.remove(key);
                if (hostname != null && hostname.equals(defHostname)) {
                    LOGGER.log(Level.FINE, "successfully de-registered id " + registeredEndpoint
                            + " from dummy transport");
                } else {
                    LOGGER.log(Level.SEVERE, "OUCH... for some reasons this endpoint was registered to"
                            + " a different host (" + hostname + "). It is unclear if your system is still"
                            + " working properly.");
                }
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
            if(!localMode) {
                if(idReservation!=null) {
                    ((MapProxyImpl) (idReservation)).destroy();
                }
            }
            idReservation = null;
            name = null;
        }
    }
}

