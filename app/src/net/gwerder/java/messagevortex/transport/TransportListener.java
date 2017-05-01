package net.gwerder.java.messagevortex.transport;

import java.io.InputStream;

/**
 * Interface for all blending layers listening to transport layer messages
 */
public interface TransportListener {

    /***
     * This Method is called by the Transport layer if a possible vmessage has arrived.
     *
     * The message (if any) is decoded, verified and (if successful) passed on to the routing layer in a separate
     * thread (@see IncommingMessageRouterListener).
     *
     * @param is the InputStream containing a possible message
     */
    void gotMessage(InputStream is);


}
