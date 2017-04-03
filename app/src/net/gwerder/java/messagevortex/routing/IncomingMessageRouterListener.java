package net.gwerder.java.messagevortex.routing;

import net.gwerder.java.messagevortex.asn1.Message;

/**
 * Created by martin.gwerder on 03.04.2017.
 */
public interface IncomingMessageRouterListener {

    public void processMessage(Message message);

}
