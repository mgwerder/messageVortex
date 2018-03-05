package net.gwerder.java.messagevortex.transport;

import net.gwerder.java.messagevortex.MessageVortexLogger;

import javax.net.ssl.SSLEngine;
import javax.net.ssl.SSLEngineResult;
import javax.net.ssl.SSLEngineResult.HandshakeStatus;
import javax.net.ssl.SSLException;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;

import static javax.net.ssl.SSLEngineResult.HandshakeStatus.NOT_HANDSHAKING;

/**
 * Abstract functions for creating a TLS channel socket.
 *
 * @author <a href="mailto:martin+messagevortex@gwerder.net">Martin Gwerder</a>
 */
public abstract class AbstractConnection {

    private static final Logger LOGGER;
    static {
        LOGGER = MessageVortexLogger.getLogger((new Throwable()).getStackTrace()[0].getClassName());
    }

    private static int defaultTimeout = 10*1000;
    private        int timeout        = defaultTimeout;

    protected ByteBuffer        outboundEncryptedData;
    protected ByteBuffer        inboundEncryptedData;
    protected ByteBuffer        outboundAppData       = ByteBuffer.allocate(1024);
    protected ByteBuffer        inboundAppData        = (ByteBuffer)ByteBuffer.allocate(1024).clear().flip();

    private   ExecutorService   executor      = Executors.newSingleThreadExecutor();
    private   SSLEngine         engine        = null;
    private   boolean           isTLS         = false;
    private   SocketChannel     socketChannel = null;
    private   InetSocketAddress remoteAddress;

    private boolean shutdown = false;

    protected AbstractConnection( InetSocketAddress remoteAddress ) {
        this.remoteAddress = remoteAddress;
    }

    public String getHostName() {
        return remoteAddress.getHostName();
    }

    public int getPort() {
        return remoteAddress.getPort();
    }

    protected SocketChannel setSocketChannel( SocketChannel s ) {
        SocketChannel ret = this.socketChannel;
        this.socketChannel = s;
        return ret;
    }

    protected SocketChannel getSocketChannel() {
        return this.socketChannel;
    }

    protected SSLEngine setSSLEngine( SSLEngine engine ) {
        SSLEngine ret=this.engine;
        this.engine = engine;
        return ret;
    }

    public void connect() throws IOException {
        SocketChannel socketChannel = SocketChannel.open();
        socketChannel.configureBlocking( false );
        socketChannel.connect( remoteAddress );

        // wait for conection to complete handshake
        while (!socketChannel.finishConnect()) {
            try {
                Thread.sleep(10);
            } catch( InterruptedException ie ) {
                // safe to ignore
            }
        }

        // make sure that socket channel is connected
        if( !socketChannel.isConnected() ) {
            throw new IOException( "socket is unexpectedly not connected" );
        }

        setSocketChannel( socketChannel );
    }

    public void startTLS() throws IOException {
        startTLS( getTimeout() );
    }

    public void startTLS( long timeout ) throws IOException {
        // set connection to TLS mode
        isTLS = true;

        // init starttls and fill buffers
        do_handshake( timeout );
    }

    public void do_handshake( long timeout ) throws IOException {
        if( getEngine() == null ) {
            throw new IOException( "No SSL context available" ) ;
        }
        if( ! getSocketChannel().isConnected() ) {
            throw new IOException( "No SSL connection possible on unconnected socket" ) ;
        }

        // start timeout counter
        long start = System.currentTimeMillis();

        // initiate handshake in engine
        getEngine().beginHandshake();

        // prepare buffers
        // inboundEncryptedData read mode
        // outboundEncryptedData write mode
        // inboundAppData read mode
        // outboundAppData write mode

        // get handshake status
        HandshakeStatus handshakeStatus = getEngine().getHandshakeStatus();

        while ( handshakeStatus != SSLEngineResult.HandshakeStatus.FINISHED && handshakeStatus != NOT_HANDSHAKING && timeout - ( System.currentTimeMillis() - start ) > 0 ) {

            // Checking handshake status of SSL engine
            switch (handshakeStatus) {
                case NEED_UNWRAP:
                    LOGGER.log( Level.WARNING, "doing unwrap" );

                    // read bytes from socket
                    boolean loopOnce = inboundEncryptedData.remaining()>0;
                    inboundEncryptedData.compact();
                    int bytesRead = 0;
                    while( bytesRead==0 && timeout - ( System.currentTimeMillis() - start ) > 0 ) {
                        bytesRead = getSocketChannel().read( inboundEncryptedData );
                        if( bytesRead == 0 ) {
                            try {
                                Thread.sleep(50);
                            } catch (InterruptedException ie) {
                                // safe to ignore
                            }
                            if( loopOnce ) {
                                // abort if precondition might be fullfilled already
                                bytesRead=1;
                            }
                        }
                    }
                    inboundEncryptedData.flip();

                    if ( bytesRead < 0) {
                        if ( getEngine().isInboundDone() && getEngine().isOutboundDone() ) {
                            throw new IOException( "cannot starttls. Socket was at least partially closed or the SSL engine encoutered an error while handshaking" );
                        }
                        try {
                            getEngine().closeInbound();
                        } catch (SSLException e) {
                            LOGGER.log( Level.WARNING, "Exception while closing inbound", e );
                        }
                        getEngine().closeOutbound();
                        handshakeStatus = getEngine().getHandshakeStatus();
                        break;
                    }

                    inboundAppData.compact();
                    SSLEngineResult res;
                    try {
                        res = getEngine().unwrap( inboundEncryptedData, inboundAppData );
                        handshakeStatus = res.getHandshakeStatus();
                    } catch (SSLException sslException) {
                        LOGGER.log( Level.WARNING, "A problem was encountered while processing the data that caused the SSLEngine to abort. Will try to properly close connection...", sslException);
                        getEngine().closeOutbound();
                        handshakeStatus = getEngine().getHandshakeStatus();
                        break;
                    } finally {
                        inboundAppData.flip();
                    }
                    switch( res.getStatus() ) {
                        case OK:
                            break;
                        case CLOSED:
                            if (engine.isOutboundDone()) {
                                throw new IOException( "engine reports closed status" );
                            } else {
                                engine.closeOutbound();
                                handshakeStatus = engine.getHandshakeStatus();
                                break;
                            }
                        case BUFFER_UNDERFLOW:
                            ByteBuffer buf = handleBufferUnderflow(engine, inboundEncryptedData);
                            buf.put( inboundEncryptedData );
                            inboundEncryptedData = buf;
                            inboundEncryptedData.flip();
                            break;
                        case BUFFER_OVERFLOW:
                            ByteBuffer tBuf = enlargeApplicationBuffer(engine, inboundAppData);
                            tBuf.put( inboundAppData );
                            inboundAppData = tBuf;
                            inboundAppData.flip();
                            break;
                        default:
                            throw new IllegalStateException( "Invalid SSL status: " + res.getStatus() );
                    }
                    break;
                case NEED_WRAP:
                    LOGGER.log( Level.WARNING, "doing wrap" );

                    outboundAppData.flip();
                    SSLEngineResult result;
                    try {
                        result = engine.wrap( outboundAppData, outboundEncryptedData );
                        handshakeStatus = result.getHandshakeStatus();
                    } catch (SSLException se) {
                        LOGGER.log( Level.WARNING, "Exception while reading data", se );
                        engine.closeOutbound();
                        handshakeStatus = engine.getHandshakeStatus();
                        break;
                    } finally {
                        outboundAppData.compact();
                    }

                    switch (result.getStatus()) {
                        case OK :
                        case CLOSED:
                            try {
                                outboundEncryptedData.flip();
                                while (outboundEncryptedData.hasRemaining()) {
                                    getSocketChannel().write(outboundEncryptedData);
                                }
                            } catch( IOException e) {
                                LOGGER.log( Level.WARNING, "Failed to close channel");
                                handshakeStatus = engine.getHandshakeStatus();
                            } finally {
                                outboundEncryptedData.compact();
                            }
                            break;
                        case BUFFER_UNDERFLOW:
                            LOGGER.log( Level.SEVERE, "Buffer underflow should not happen", new SSLException("unknown reason for buffer underflow") );
                        case BUFFER_OVERFLOW:
                            outboundEncryptedData = enlargePacketBuffer(getEngine(), outboundEncryptedData);
                            break;
                        default:
                            throw new IllegalStateException( "Invalid SSL status: " + result.getStatus() );
                    }
                    break;
                case NEED_TASK:
                    LOGGER.log( Level.WARNING, "running tasks" );
                    Runnable task;
                    while ( ( task = getEngine().getDelegatedTask() ) != null ) {
                        LOGGER.log( Level.WARNING, "running task " + task );
                        Thread t = new Thread(task);
                        t.start();
                    }
                    LOGGER.log( Level.WARNING, "running tasks done" );
                    handshakeStatus = getEngine().getHandshakeStatus();
                    break;
                case FINISHED:
                    LOGGER.log( Level.WARNING, "TLS handshake success" );
                    break;
                case NOT_HANDSHAKING:
                    break;
                default:
                    // just a default catcher (should not be reached)
                    throw new IllegalStateException( "Invalid SSL status: " + handshakeStatus );
            }
        }

        if( timeout - ( System.currentTimeMillis() - start ) <= 0 && handshakeStatus != SSLEngineResult.HandshakeStatus.FINISHED ) {
            throw new IOException( "SSL/TLS handshake abborted due to timeout" );
        } else {
            LOGGER.log( Level.INFO, "******* HANDSHAKE SUCCESS" );
        }
    }

    protected void do_teardown( long timeout ) throws IOException {
        if( isTLS() ) {
            // we are done with the engine
            getEngine().closeOutbound();
            outboundAppData.clear().flip();

            while (!engine.isOutboundDone()) {
                // Get close message
                SSLEngineResult res = engine.wrap(outboundAppData, outboundEncryptedData);

                switch (res.getStatus()) {
                    case OK:
                        break;
                    case CLOSED:
                        break;
                    case BUFFER_UNDERFLOW:
                        ByteBuffer buf = handleBufferUnderflow(engine, inboundEncryptedData);
                        buf.put(inboundEncryptedData);
                        inboundEncryptedData = buf;
                        inboundEncryptedData.flip();
                        break;
                    case BUFFER_OVERFLOW:
                        ByteBuffer tBuf = enlargeApplicationBuffer(engine, inboundAppData);
                        tBuf.put(inboundAppData);
                        inboundAppData = tBuf;
                        inboundAppData.flip();
                        break;
                    default:
                        throw new IllegalStateException("Invalid SSL status: " + res.getStatus());
                }

                // Send close message
                outboundEncryptedData.flip();
                while (outboundEncryptedData.hasRemaining()) {
                    int num = socketChannel.write(outboundEncryptedData);
                    if (num == 0) {
                        try {
                            Thread.sleep(100);
                        } catch (InterruptedException ie) {
                            LOGGER.log(Level.WARNING, "got unexpected InterruptedException");
                        }
                    }
                }
                outboundEncryptedData.compact();
            }
        }
    }

    public boolean isTLS() {
        return isTLS;
    }

    public static int setDefaultTimeout( int timeout ) {
        int ret = defaultTimeout;
        defaultTimeout = timeout;
        return ret;
    }

    public static int getDefaultTimeout() {
        return defaultTimeout;
    }

    public int setTimeout( int timeout ) {
        int ret = this.timeout;
        this.timeout = timeout;
        return ret;
    }

    public int getTimeout() {
        return this.timeout;
    }

    protected SSLEngine getEngine() {
        return engine;
    }

    private void writeSocket( String message, long timeout ) throws IOException {
        long start = System.currentTimeMillis();
        // add message to output buffer and prepare for reading
        int msgSize = message.getBytes(StandardCharsets.UTF_8).length;
        if( outboundAppData.limit()< msgSize ) {
            outboundAppData = enlargeBuffer( outboundAppData, outboundAppData.capacity() + msgSize );
        }
        outboundAppData.put( message.getBytes( StandardCharsets.UTF_8 ) );
        while (outboundAppData.hasRemaining() && start + timeout > System.currentTimeMillis() ) {
            // Handle outbound messages larger than 16KB.
            if( isTLS() ) {
                outboundEncryptedData.clear();
                SSLEngineResult result = getEngine().wrap(outboundAppData, outboundEncryptedData);
                switch (result.getStatus()) {
                    case OK:
                        outboundEncryptedData.flip();
                        while (outboundEncryptedData.hasRemaining()) {
                            getSocketChannel().write(outboundEncryptedData);
                        }
                        outboundEncryptedData.compact();
                        break;
                    case BUFFER_OVERFLOW:
                        outboundEncryptedData = enlargePacketBuffer(getEngine(), outboundEncryptedData);
                        break;
                    case BUFFER_UNDERFLOW:
                        throw new SSLException("Buffer underflow occured after a wrap. I don't think we should ever get here.");
                    case CLOSED:
                        closeConnection();
                        return;
                    default:
                        throw new IllegalStateException("Invalid SSL status: " + result.getStatus());
                }
            } else {
                // send all bytes as is
                outboundAppData.flip();
                while( outboundAppData.hasRemaining() ) {
                    getSocketChannel().write( outboundAppData );
                }
                outboundAppData.compact();
            }
        }
        if( start + timeout <= System.currentTimeMillis() ){
            throw new IOException( "Timeout reached while writing" );
        }
    }

    /***
     *
     * @param timeout  &lt;0 timeout means read only available bytes &gt;0 means wait for bytes up until
     * @return
     * @throws IOException
     */
    private int readSocket( long timeout ) throws IOException  {

        int bytesRead;
        int totBytesRead = 0;

        long start = System.currentTimeMillis();
        boolean timeoutReached;
        do {
            inboundEncryptedData.compact();
            bytesRead = getSocketChannel().read( inboundEncryptedData );
            inboundEncryptedData.flip();

            if (bytesRead > 0) {
                totBytesRead += bytesRead;
            }

            if( totBytesRead == 0 ) {
                LOGGER.log( Level.INFO, "sleeping due to missing data (" + inboundEncryptedData.remaining() +")" );
                try {
                    Thread.sleep(50);
                } catch( InterruptedException ie ) {
                    // safe to ignore as we do not rely on timing here
                }
            }
            if (isTLS()) {
                inboundAppData.compact();
                SSLEngineResult result = getEngine().unwrap(inboundEncryptedData, inboundAppData);
                inboundAppData.flip();
                if( getEngine().getHandshakeStatus() != HandshakeStatus.FINISHED ) {
                    do_handshake( timeout - (System.currentTimeMillis() - start) );
                }
                switch (result.getStatus()) {
                    case OK:
                        break;
                    case BUFFER_OVERFLOW:
                        inboundAppData = enlargeApplicationBuffer(getEngine(), inboundAppData);
                        break;
                    case BUFFER_UNDERFLOW:
                        inboundEncryptedData = handleBufferUnderflow(getEngine(), inboundEncryptedData);
                        break;
                    case CLOSED:
                        closeConnection();
                    default:
                        throw new IllegalStateException("Invalid SSL status: " + result.getStatus());
                }
            } else {
                inboundAppData.compact();
                if (inboundAppData.limit() < bytesRead ) {
                    inboundAppData = enlargeBuffer( inboundAppData, inboundEncryptedData.remaining()+inboundAppData.capacity() );
                }
                while (inboundEncryptedData.hasRemaining()) {
                    inboundAppData.put( inboundEncryptedData.get() );
                }
                inboundAppData.flip();
            }

            timeoutReached = System.currentTimeMillis()-start > timeout;
        } while( totBytesRead == 0 && !timeoutReached && timeout > 0 );

        if( bytesRead < 0 ) {
            LOGGER.log( Level.INFO, "Loop aborted due to closed connection" );
        } else if( System.currentTimeMillis()-start > timeout ) {
            LOGGER.log( Level.INFO, "Loop aborted due to timeout" );
        } else if( timeout <= 0 ) {
            LOGGER.log(Level.INFO, "Loop aborted due to single round wish");
        } else if( totBytesRead > 0 ) {
            LOGGER.log(Level.INFO, "Loop aborted due to data (" + totBytesRead + " bytes)");
        } else {
            LOGGER.log(Level.INFO, "Loop aborted due to UNKNOWN");
        }

        if (bytesRead < 0) {
            // if remote has closed channel do local shutdown
            shutdown();
        }

        assert inboundAppData.remaining()>=totBytesRead: "found size missmatch  (" + inboundAppData +"; " + totBytesRead + ")";

        return totBytesRead;
    }

    public void writeln( String message ) throws IOException {
        write( message + "\r\n" );
    }

    public void write( String message ) throws IOException {
        writeSocket( message, getTimeout() );
    }

    public String read() throws IOException {
        return read( getTimeout() );
    }

    public String read( long timeout ) throws IOException {
        // FIXME read and return entire buffer
        int numBytes=readSocket( timeout );
        byte[] b = new byte[ numBytes ];
        inboundAppData.get( b );
        inboundAppData.compact().flip();
        return new String( b, StandardCharsets.UTF_8 );
    }

    public String readln() throws IOException {
        return readln( getTimeout() );
    }

    public String readln( long timeout ) throws IOException {
        StringBuilder ret = new StringBuilder();
        long start = System.currentTimeMillis();
        while( ! ret.toString().endsWith( "\r\n" ) && timeout - (System.currentTimeMillis() - start) > 0 ) {
            if( ! inboundAppData.hasRemaining() ) {
                readSocket( timeout - (System.currentTimeMillis() - start));
            }
            ret.append( (char)inboundAppData.get() );
        }

        if( ! ret.toString().endsWith( "\r\n" ) ) {
            inboundAppData.rewind();
            inboundAppData.compact().flip();
            return null;
        } else {
            inboundAppData.compact().flip();
            return ret.toString().substring(0, ret.length() - 2);
        }
    }

    protected ByteBuffer enlargePacketBuffer( SSLEngine engine, ByteBuffer buffer ) {
        return enlargeBuffer( buffer, engine.getSession().getPacketBufferSize() );
    }

    protected ByteBuffer enlargeApplicationBuffer(SSLEngine engine, ByteBuffer buffer) {
        return enlargeBuffer(buffer, engine.getSession().getApplicationBufferSize());
    }

    protected ByteBuffer enlargeBuffer(ByteBuffer buffer, int sessionProposedCapacity) {
        if( sessionProposedCapacity > buffer.capacity() ) {
            buffer = ByteBuffer.allocate(sessionProposedCapacity);
        } else {
            buffer = ByteBuffer.allocate(buffer.capacity() * 2);
        }
        return buffer;
    }

    protected ByteBuffer handleBufferUnderflow(SSLEngine engine, ByteBuffer buffer) {
        if( engine.getSession().getPacketBufferSize() <= buffer.limit() ) {
            return buffer;
        } else {
            ByteBuffer replaceBuffer = enlargePacketBuffer(engine, buffer);
            buffer.flip();
            replaceBuffer.put(buffer);
            return replaceBuffer;
        }
    }

    protected void closeConnection() throws IOException  {
        if( getEngine()!=null ) {
            do_teardown( 1000 );
        }
        socketChannel.close();
    }

    protected void handleEndOfStream() throws IOException  {
        if( getEngine() != null ) {
            try {
                getEngine().closeInbound();
            } catch( IOException ioe ) {
                LOGGER.log( Level.WARNING, "channel already closed without TLS closure",ioe );
            }
        }
        closeConnection();
    }

    public void shutdown() throws IOException {
        shutdown = true;
        executor.shutdown();
    }

    public boolean isShutdown() {
        return shutdown;
    }

}
