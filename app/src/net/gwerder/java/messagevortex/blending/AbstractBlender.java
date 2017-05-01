package net.gwerder.java.messagevortex.blending;

import net.gwerder.java.messagevortex.accountant.HeaderVerifyer;
import net.gwerder.java.messagevortex.asn1.BlendingSpec;
import net.gwerder.java.messagevortex.asn1.VortexMessage;
import net.gwerder.java.messagevortex.routing.IncomingMessageRouterListener;
import net.gwerder.java.messagevortex.transport.TransportListener;

/**
 * Abstract blender class unifying required interfaces and offers basic functionality.
 */
public abstract class AbstractBlender implements Blender,TransportListener {

    IncomingMessageRouterListener listener=null;
    HeaderVerifyer verifyer=null;

    /***
     * Sets the listener for incomming messages to the routing listener.
     * @param listener the listening routing layer
     * @return the previously set listener
     */
    public IncomingMessageRouterListener setIncomingMessageListener(IncomingMessageRouterListener listener) {
        IncomingMessageRouterListener old=this.listener;
        this.listener=listener;
        return old;
    }

    /***
     * Sets the header verifier for the blending layer.
     *
     * @param verifyer the verifier to be set
     * @return the previously set header verifier
     */
    public HeaderVerifyer setHeaderVerifyer(HeaderVerifyer verifyer) {
        HeaderVerifyer old=this.verifyer;
        this.verifyer=verifyer;
        return old;

    }

    public abstract boolean blendMessage(BlendingSpec target,VortexMessage message);

}
