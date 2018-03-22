package net.gwerder.java.messagevortex.transport;

import net.gwerder.java.messagevortex.MessageVortexLogger;

import javax.net.ssl.SSLEngine;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.SocketChannel;
import java.util.logging.Logger;

public class ClientConnection extends AbstractConnection {

    private static final Logger LOGGER;
    static {
        LOGGER = MessageVortexLogger.getLogger((new Throwable()).getStackTrace()[0].getClassName());
    }

    public ClientConnection(SocketChannel channel, SecurityContext context ) throws IOException {
        super( channel, context );
        initConnection();
    }

    public ClientConnection(InetSocketAddress socketAddress, SecurityContext context) throws IOException  {
        super( socketAddress, context );
        initConnection();
    }

    private void initConnection() {
        if( getSecurityContext() != null && getSecurityContext().getContext()!=null ) {
            SSLEngine engine = getSecurityContext().getContext().createSSLEngine( getHostName(), getPort() );
            engine.setUseClientMode( true );
            setEngine( engine );
        }
    }

    @Override
    public void shutdown() throws IOException {
        super.shutdown();
        closeConnection();
    }

}
