package net.gwerder.java.messagevortex.transport;

/**
 * Created by Martin on 10.03.2018.
 */
public interface SocketListener {

    // FIXME check connection type
    void gotConnect( AbstractConnection ac );
}
