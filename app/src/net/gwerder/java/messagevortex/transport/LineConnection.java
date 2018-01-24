package net.gwerder.java.messagevortex.transport;

import javax.net.ssl.SSLContext;
import java.net.Socket;

/**
 * Created by Martin on 23.01.2018.
 */
public class LineConnection extends Thread {

    private boolean shutdown=false;
    private long timeout = 90*1000;
    private Socket s=null;
    private SSLContext context;
    private ServerAuthenticator auth=null;

    public LineConnection(Socket s, SSLContext context) {
        this.s=s;
        this.context=context;
    }

    public void shutdown() {
        shutdown=true;
    }

    public ServerAuthenticator setAuthenticator(ServerAuthenticator auth) {
        ServerAuthenticator ret=this.auth;
        this.auth=auth;
        return ret;
    }

    public long getTimeout() {
        return timeout;
    }

    public long setTimeout( long timeout ) {
        long ret=this.timeout;
        this.timeout=timeout;
        return ret;
    }
}
