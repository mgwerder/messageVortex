package net.gwerder.java.messagevortex.transport;

import net.gwerder.java.messagevortex.MessageVortexLogger;

import javax.net.ssl.SSLEngine;
import javax.net.ssl.SSLSession;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.util.logging.Logger;

public class ClientConnection extends AbstractConnection {

    private static final Logger LOGGER;
    static {
        LOGGER = MessageVortexLogger.getLogger((new Throwable()).getStackTrace()[0].getClassName());
    }

    public ClientConnection(InetSocketAddress socketAddress, SecurityContext context) throws IOException  {
        super(socketAddress);

        if( context != null ) {
            SSLEngine engine = context.getContext().createSSLEngine( getHostName(), getPort() );
            engine.setUseClientMode( true );
            setSSLEngine( engine );
            SSLSession session = engine.getSession();
            outboundEncryptedData = ByteBuffer.allocate(session.getPacketBufferSize());
            inboundEncryptedData = ByteBuffer.allocate(session.getPacketBufferSize());
        } else {
            // use 2 K of initial buffer if SSL is not supported
            outboundEncryptedData = ByteBuffer.allocate(2048);
            inboundEncryptedData = ByteBuffer.allocate(2048);
        }
        inboundEncryptedData.flip();
    }

    @Override
    public void shutdown() throws IOException {
        super.shutdown();
        closeConnection();
    }

}
