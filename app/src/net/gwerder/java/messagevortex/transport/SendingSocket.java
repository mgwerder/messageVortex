package net.gwerder.java.messagevortex.transport;


import net.gwerder.java.messagevortex.MessageVortexLogger;

import javax.net.ssl.SSLSession;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.channels.spi.SelectorProvider;
import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;

public class SendingSocket extends AbstractConnection {

    private static final Logger LOGGER;
    static {
        LOGGER = MessageVortexLogger.getLogger((new Throwable()).getStackTrace()[0].getClassName());
    }
    private Selector          selector;

    public SendingSocket( InetSocketAddress localAddress, SecurityContext context ) throws IOException {
        super( localAddress );

        if( context!=null && context.getContext()!=null ) {
            setSSLEngine(context.getContext().createSSLEngine());
            SSLSession dummySession = getEngine().getSession();
            outboundAppData = ByteBuffer.allocate(dummySession.getApplicationBufferSize());
            outboundEncryptedData = ByteBuffer.allocate(dummySession.getPacketBufferSize());
            inboundAppData = ByteBuffer.allocate(dummySession.getApplicationBufferSize());
            inboundEncryptedData = ByteBuffer.allocate(dummySession.getPacketBufferSize());
            dummySession.invalidate();
        } else {
            outboundAppData = ByteBuffer.allocate( 2048 );
            outboundEncryptedData = ByteBuffer.allocate( 2048 );
            inboundAppData = ByteBuffer.allocate( 2048 );
            inboundEncryptedData = ByteBuffer.allocate( 2048 );
        }

        selector = SelectorProvider.provider().openSelector();
        ServerSocketChannel serverSocketChannel = ServerSocketChannel.open();
        serverSocketChannel.configureBlocking(false);
        serverSocketChannel.socket().bind( localAddress );
        serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);
    }

    public void start() throws IOException {
        LOGGER.log( Level.INFO, "waiting for inbound connects");
        while( ! isShutdown() ) {
            selector.select();
            Iterator<SelectionKey> selectedKeys = selector.selectedKeys().iterator();
            while (selectedKeys.hasNext()) {
                SelectionKey key = selectedKeys.next();
                selectedKeys.remove();
                if (!key.isValid()) {
                    continue;
                }
                if (key.isAcceptable()) {
                    accept(key);
                } else if (key.isReadable()) {
                    setSocketChannel( (SocketChannel) key.channel() );
                    // FIXME incomplete
                    // read();
                }
            }
        }
        LOGGER.log( Level.INFO, "Listening socket closed");
    }

    public void stop() throws IOException {
        LOGGER.log( Level.INFO, "Closing listener");

        // shutting down connection
        shutdown();

        // shutting down threads
        selector.wakeup();
    }

    private void accept(SelectionKey key) throws IOException {
        LOGGER.log( Level.INFO, "got new connection");
        SocketChannel socketChannel = ((ServerSocketChannel) key.channel()).accept();
        socketChannel.configureBlocking(false);

        getEngine().setUseClientMode(false);
        getEngine().beginHandshake();
        startTLS();
        if (isTLS()) {
            socketChannel.register(selector, SelectionKey.OP_READ, getEngine() );
        } else {
            socketChannel.close();
            LOGGER.log( Level.WARNING, "Socket closed due to illegal handshake" );
        }
    }

}
