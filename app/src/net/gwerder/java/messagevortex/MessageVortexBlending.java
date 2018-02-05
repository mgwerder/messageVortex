package net.gwerder.java.messagevortex;

import net.gwerder.java.messagevortex.blending.Blender;
import net.gwerder.java.messagevortex.routing.operation.RoutingSender;
import net.gwerder.java.messagevortex.transport.TransportReceiver;

import java.io.InputStream;

/**
 * Created by Martin on 01.02.2018.
 */
public class MessageVortexBlending implements TransportReceiver, RoutingSender  {

    private TransportReceiver receiver = null;
    private RoutingSender     sender   = null;
    private Blender           blender  = null;


    public MessageVortexBlending( TransportReceiver blend, RoutingSender sender ) {
        receiver = blend;
        this.sender = sender;
    }

    public TransportReceiver setTransportReceiver( TransportReceiver receiver ) {
        TransportReceiver ret = receiver;
        this.receiver = receiver;
        return ret;
    }

    public TransportReceiver getTransportReceiver() {
        return receiver;
    }

    public RoutingSender setRoutingSender( RoutingSender sender ) {
        RoutingSender ret = sender;
        this.sender = sender;
        return ret;
    }

    public RoutingSender getRoutingSender() {
        return sender;
    }

    public Blender setBlender( Blender blender ) {
        Blender ret = blender;
        this.blender = blender;
        return ret;
    }

    public Blender getBlender() {
        return blender;
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
