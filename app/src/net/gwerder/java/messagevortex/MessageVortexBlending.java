package net.gwerder.java.messagevortex;

import net.gwerder.java.messagevortex.routing.operation.RoutingSender;
import net.gwerder.java.messagevortex.transport.TransportReceiver;

import java.io.InputStream;

/**
 * Created by Martin on 01.02.2018.
 */
public class MessageVortexBlending implements TransportReceiver, RoutingSender  {

    private TransportReceiver receiver = null;
    private RoutingSender     sender   = null;


    public MessageVortexBlending( TransportReceiver blend, RoutingSender sender ) {
        receiver = blend;
        this.sender = sender;
    }

    public TransportReceiver setTransportReceiver( TransportReceiver receiver ) {
        TransportReceiver ret = receiver;
        this.receiver = receiver;
        return ret;
    }

    public RoutingSender setRoutingSender( RoutingSender sender ) {
        RoutingSender ret = sender;
        this.sender = sender;
        return ret;
    }

    @Override
    public boolean gotMessage(InputStream is) {
        // extra
        return receiver.gotMessage(is);
    }

    @Override
    public boolean sendMessage(String target, MessageVortex msg) {
        return sender.sendMessage( target, msg );
    }
}
