package net.gwerder.java.mailvortex.routing;

import net.gwerder.java.mailvortex.asn1.Message;

/**
 * Created by martin.gwerder on 03.04.2017.
 */
public interface IncomingMessageRouterListener {

    public void processMessage(Message message);

}
