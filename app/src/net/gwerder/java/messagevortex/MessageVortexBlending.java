package net.gwerder.java.messagevortex;

import net.gwerder.java.messagevortex.transport.TransportReceiver;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import java.io.InputStream;

/**
 * Created by Martin on 01.02.2018.
 */
public class MessageVortexBlending implements TransportReceiver {

    @Override
    public void gotMessage(InputStream is) {
        throw new NotImplementedException(); // FIXME
    }
}
