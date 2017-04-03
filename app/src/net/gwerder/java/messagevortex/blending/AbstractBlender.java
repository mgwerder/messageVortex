package net.gwerder.java.messagevortex.blending;

import net.gwerder.java.messagevortex.accountant.HeaderVerifyer;
import net.gwerder.java.messagevortex.asn1.Message;
import net.gwerder.java.messagevortex.routing.IncomingMessageRouterListener;
import net.gwerder.java.messagevortex.transport.DummyTransport;
import net.gwerder.java.messagevortex.transport.TransportListener;

/**
 * Created by martin.gwerder on 03.04.2017.
 */
abstract public class AbstractBlender implements Blender,TransportListener {

    IncomingMessageRouterListener listener=null;
    HeaderVerifyer verifyer=null;
    DummyTransport transport=new DummyTransport(this);

    public IncomingMessageRouterListener setIncomingMessageListener(IncomingMessageRouterListener listener) {
        IncomingMessageRouterListener old=this.listener;
        this.listener=listener;
        return old;
    }

    public HeaderVerifyer setHeaderVerifyer(HeaderVerifyer verifyer) {
        HeaderVerifyer old=this.verifyer;
        this.verifyer=verifyer;
        return old;

    }

    abstract public boolean blendMessage(Message message);

}
