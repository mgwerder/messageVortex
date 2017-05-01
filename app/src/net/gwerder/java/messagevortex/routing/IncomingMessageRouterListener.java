package net.gwerder.java.messagevortex.routing;

import net.gwerder.java.messagevortex.asn1.VortexMessage;

/**
 * Created by martin.gwerder on 03.04.2017.
 */
public interface IncomingMessageRouterListener {

    /***
     * This method is called by the blending layer when an incomming message has
     * been received and the verifier acknowledged its processing.
     *
     * @param message the message received
     */
    public void processMessage(VortexMessage message);

}
