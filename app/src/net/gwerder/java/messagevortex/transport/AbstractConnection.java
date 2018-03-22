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

    protected final String CRLF = "\r\n";

    private static final Logger LOGGER;
    static {
        LOGGER = MessageVortexLogger.getLogger((new Throwable()).getStackTrace()[0].getClassName());
    }

    private static long defaultTimeout = 10*1000;
    private        long timeout        = defaultTimeout;

    private ByteBuffer outboundEncryptedData   = (ByteBuffer)ByteBuffer.allocate(1024).clear();
    private ByteBuffer inboundEncryptedData    = (ByteBuffer)ByteBuffer.allocate(1024).clear().flip();
    private ByteBuffer outboundAppData         = (ByteBuffer)ByteBuffer.allocate(1024).clear();
    private ByteBuffer inboundAppData          = (ByteBuffer)ByteBuffer.allocate(1024).clear().flip();

    private String    protocol                = null;
    private boolean   isClient                = false;

    private   ExecutorService   executor      = Executors.newSingleThreadExecutor();
    private   SecurityContext   context       = null;
    private   boolean           isTLS         = false;
    private   SocketChannel     socketChannel = null;
    private   InetSocketAddress remoteAddress = null;
    private   SSLEngine         engine        = null;

    private volatile boolean shutdown = false;

    public AbstractConnection( InetSocketAddress remoteAddress, SecurityContext context ) {
        this.remoteAddress = remoteAddress;
        setSecurityContext( context );
    }

    public AbstractConnection( SocketChannel sock, SecurityContext context ) throws IOException {
        if( sock!=null ) {
            setSocketChannel(sock);
        }
        setSecurityContext( context );
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
        if( s!= null ) {
            try {
                s.configureBlocking(false);
                this.remoteAddress = (InetSocketAddress) s.getRemoteAddress();
            } catch(IOException ioe){
                LOGGER.log(Level.SEVERE, "got unexpected exception", ioe );
            }
        }
        return ret;
    }

    public SocketChannel getSocketChannel() throws IOException {
        if( socketChannel==null || !socketChannel.isConnected() ) {
            throw new IOException( "socket is unexpectedly not connected" );
        }
        return this.socketChannel;
    }

    public SecurityContext setSecurityContext( SecurityContext context ) {
        SecurityContext ret=this.context;
        this.context = context;
        return ret;
    }

    public SecurityContext getSecurityContext() {
        return context;
    }

    protected SSLEngine getEngine() {
        return engine;
    }

    protected SSLEngine setEngine( SSLEngine engine ) {
        SSLEngine ret= this.engine;
        this.engine = engine;
        return ret;
    }

    public void connect() throws IOException {
        if(socketChannel == null) {
            socketChannel = SocketChannel.open();
            socketChannel.configureBlocking(false);
            LOGGER.log(Level.INFO, "connecting socket channel to " + remoteAddress);
        }

        socketChannel.connect( remoteAddress );
        isClient=true;
        // wait for conection to complete handshake
        while (!socketChannel.finishConnect()) {
            try {
                Thread.sleep(10);
            } catch( InterruptedException ie ) {
                // safe to ignore
            }

        }

        if( !isTLS() && context!=null && ( context.getRequirement()==SecurityRequirement.SSLTLS || context.getRequirement()==SecurityRequirement.UNTRUSTED_SSLTLS ) ) {
            startTLS();
        } else if( context!=null && ( context.getRequirement()==SecurityRequirement.SSLTLS || context.getRequirement()==SecurityRequirement.UNTRUSTED_SSLTLS ) ) {
            throw new IOException( "unable to start required handshake due to missing SSLContext" );
        }

        // make sure that socket channel is connected
        if( !socketChannel.isConnected() ) {
            throw new IOException( "socket is unexpectedly not connected" );
        }

    }

    public void startTLS() throws IOException {
        startTLS( getTimeout() );
    }

    public void startTLS( long timeout ) throws IOException {
        if( getEngine() == null && getSecurityContext().getContext()!=null ) {
            // init engine if not yet done
            InetSocketAddress ia=(InetSocketAddress)socketChannel.getLocalAddress();
            setEngine( getSecurityContext().getContext().createSSLEngine( ia.getHostName(),ia.getPort() ) );
            getEngine().setUseClientMode( isClient );
        } else if( getEngine() == null ) {
            throw new IOException( "no securtity context available but startTLS called" );
        }

        // set connection to TLS mode
        isTLS = true;

        // init starttls and fill buffers
        do_handshake( timeout );
    }

    public String setProtocol( String protocol ) {
        String ret=getProtocol();
        this.protocol=protocol;
        return ret;
    }

    public String getProtocol() {
        return protocol;
    }

    protected void do_handshake( long timeout ) throws IOException {
        if (getEngine() == null) {
            throw new IOException("No SSL context available");
        }
        if (!getSocketChannel().isConnected()) {
            throw new IOException("No SSL connection possible on unconnected socket");
        }

        // initiate handshake in engine
        getEngine().beginHandshake();

        processEngineRequirements( timeout );
    }

    private int readRawSocket( long timeout ) throws IOException {
        // read bytes from socket
        boolean loopOnce = inboundEncryptedData.remaining()>0;
        inboundEncryptedData.compact();
        try {
            LOGGER.log( Level.INFO, "starting reading raw socket (loopOnce is "+loopOnce+")" );
            int bytesRead = 0;
            long start = System.currentTimeMillis();
            while (bytesRead == 0 && timeout - (System.currentTimeMillis() - start) > 0) {
                bytesRead = getSocketChannel().read( inboundEncryptedData );
                LOGGER.log( Level.INFO, "tried to read (got "+bytesRead+")" );
                if (bytesRead == 0) {
                    if (loopOnce) {
                        // abort if precondition might be fulfilled already
                        LOGGER.log( Level.INFO, "done reading raw socket (loop once gave no new input)" );
                        break;
                    } else {
                        try {
                            Thread.sleep(50);
                        } catch (InterruptedException ie) {
                            // safe to ignore
                        }
                    }
                }
            }
            LOGGER.log( Level.INFO, "done reading raw socket (took "+ (System.currentTimeMillis()-start) + "ms; read "+bytesRead+" bytes)" );
            return bytesRead;
        } finally {
            inboundEncryptedData.flip();
        }
    }

    private int writeRawSocket( long timeout ) throws IOException {
        outboundEncryptedData.flip();
        try{
            long start = System.currentTimeMillis();
            int bytesWritten = 0;
            while (outboundEncryptedData.hasRemaining()  && timeout - (System.currentTimeMillis() - start) > 0 ) {
                bytesWritten+=getSocketChannel().write(outboundEncryptedData);
            }
            return bytesWritten;
        } finally {
            outboundEncryptedData.compact();
        }
    }

    private void processEngineRequirements( long timeout ) throws IOException {
        if (getEngine() == null) {
            throw new IOException("No SSL context available");
        }
        if (!getSocketChannel().isConnected()) {
            throw new IOException("No SSL connection possible on unconnected socket");
        }

        // start timeout counter
        long start = System.currentTimeMillis();

        // get handshake status
        HandshakeStatus handshakeStatus = getEngine().getHandshakeStatus();

        LOGGER.log( Level.INFO, "status (1) outboundEncryptedData Buffer "+outboundEncryptedData+"" );
        LOGGER.log( Level.INFO, "status (1) outboundAppData Buffer "+outboundAppData );
        LOGGER.log( Level.INFO, "status (1) inboundEncryptedData Buffer "+inboundEncryptedData );
        LOGGER.log( Level.INFO, "status (1) inboundAppData Buffer "+inboundAppData );

        while ( handshakeStatus != SSLEngineResult.HandshakeStatus.FINISHED && handshakeStatus != NOT_HANDSHAKING && timeout - ( System.currentTimeMillis() - start ) > 0 ) {

            // Checking handshake status of SSL engine
            LOGGER.log( Level.INFO, "relooping (handshake status is "+handshakeStatus+")" );
            switch (handshakeStatus) {
                case NEED_UNWRAP:
                    LOGGER.log( Level.INFO, "doing unwrap (reading)" );

                    int bytesRead = readRawSocket( timeout - ( System.currentTimeMillis()-start ) );

                    LOGGER.log( Level.INFO, "doing unwrap ("+inboundEncryptedData.remaining()+")" );

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
                    } catch (SSLException sslException) {
                        LOGGER.log( Level.WARNING, "A problem was encountered while processing the data that caused the SSLEngine to abort. Will try to properly close connection...", sslException);
                        getEngine().closeOutbound();
                        handshakeStatus = getEngine().getHandshakeStatus();
                        break;
                    } finally {
                        inboundAppData.flip();
                    }
                    handshakeStatus = getEngine().getHandshakeStatus();
                    switch( res.getStatus() ) {
                        case OK:
                            LOGGER.log( Level.INFO, "unwrap done (OK)" );
                            break;
                        case CLOSED:
                            if (getEngine().isOutboundDone()) {
                                throw new IOException( "engine reports closed status" );
                            } else {
                                getEngine().closeOutbound();
                                handshakeStatus = getEngine().getHandshakeStatus();
                                LOGGER.log( Level.INFO, "unwrap done (CLOSED)" );
                                break;
                            }
                        case BUFFER_UNDERFLOW:
                            inboundEncryptedData = handleBufferUnderflow(getEngine(), inboundEncryptedData);
                            inboundEncryptedData.flip();
                            handshakeStatus = getEngine().getHandshakeStatus();
                            LOGGER.log( Level.INFO, "unwrap done (UNDERFLOW; inboundEncryptedData Buffer "+inboundEncryptedData.remaining()+"/"+inboundEncryptedData.capacity()+")" );
                            break;
                        case BUFFER_OVERFLOW:
                            inboundAppData = enlargeApplicationBuffer( getEngine(), inboundAppData);
                            handshakeStatus = getEngine().getHandshakeStatus();
                            inboundAppData.flip();
                            LOGGER.log( Level.INFO, "unwrap done (OVERFLOW; inboundAppData Buffer "+inboundAppData.remaining()+"/"+inboundAppData.capacity()+")" );
                            break;
                        default:
                            throw new IllegalStateException( "Invalid SSL status: " + res.getStatus() );
                    }
                    break;
                case NEED_WRAP:
                    LOGGER.log( Level.INFO, "doing wrap (wrapping)" );

                    outboundAppData.flip();
                    SSLEngineResult result = null;
                    try {
                        LOGGER.log( Level.INFO, "outboundAppData (flipped)="+outboundAppData);
                        LOGGER.log( Level.INFO, "outboundEncryptedData="+outboundEncryptedData);
                        result = getEngine().wrap( outboundAppData, outboundEncryptedData );
                        handshakeStatus = result.getHandshakeStatus();
                        LOGGER.log( Level.INFO, "outboundAppData (flipped)="+outboundAppData);
                        LOGGER.log( Level.INFO, "outboundEncryptedData="+outboundEncryptedData+"/"+result );
                    } catch (SSLException se) {
                        LOGGER.log( Level.WARNING, "Exception while reading data", se );
                        getEngine().closeOutbound();
                        handshakeStatus = getEngine().getHandshakeStatus();
                        break;
                    } finally {
                        outboundAppData.compact();
                    }
                    outboundEncryptedData.flip();
                    LOGGER.log( Level.INFO, "doing wrap (outboundEncryptedData is "+outboundEncryptedData.remaining()+"/"+outboundEncryptedData.capacity()+")" );
                    outboundEncryptedData.compact();

                    switch (result.getStatus()) {
                        case OK :
                            writeRawSocket( timeout - ( System.currentTimeMillis()-start ) );
                            outboundEncryptedData.flip();
                            LOGGER.log( Level.INFO, "wrap done with status OK (remaining bytes:"+outboundEncryptedData.remaining()+")");
                            outboundEncryptedData.compact();
                            handshakeStatus = result.getHandshakeStatus();
                            break;
                        case CLOSED:
                            try {
                                writeRawSocket( timeout - ( System.currentTimeMillis()-start ) );
                            } catch( IOException e) {
                                LOGGER.log( Level.WARNING, "Failed to close channel");
                                handshakeStatus = result.getHandshakeStatus();
                            }
                            break;
                        case BUFFER_UNDERFLOW:
                            LOGGER.log( Level.SEVERE, "Buffer underflow should not happen", new SSLException("unknown reason for buffer underflow") );
                            handshakeStatus = result.getHandshakeStatus();
                            break;
                        case BUFFER_OVERFLOW:
                            outboundEncryptedData.flip();
                            outboundEncryptedData = enlargePacketBuffer(getEngine(), outboundEncryptedData);
                            handshakeStatus = result.getHandshakeStatus();
                            outboundEncryptedData.flip();
                            LOGGER.log( Level.INFO, "wrap done (OVERFLOW; outboundEncryptedData Buffer "+outboundEncryptedData.remaining()+"/"+outboundEncryptedData.capacity()+")" );
                            outboundEncryptedData.compact();
                            break;
                        default:
                            throw new IllegalStateException( "Invalid SSL status: " + result.getStatus() );
                    }
                    break;
                case NEED_TASK:
                    LOGGER.log( Level.INFO, "running tasks" );
                    Runnable task = getEngine().getDelegatedTask() ;
                    do {
                        if( task != null ) {
                            LOGGER.log(Level.INFO, "running task " + task);
                            Thread t = new Thread(task);
                            t.start();
                            try {
                                t.join();
                            } catch (InterruptedException ie) {
                                // safe to ignore
                            }
                        } else {
                            try {
                                Thread.sleep( 20 );
                            } catch (InterruptedException ie) {
                                // safe to ignore
                            }
                        }
                        task = getEngine().getDelegatedTask();

                    } while ( task != null );

                    LOGGER.log( Level.INFO, "running tasks done" );
                    handshakeStatus = getEngine().getHandshakeStatus();
                    break;
                case FINISHED:
                    LOGGER.log( Level.INFO, "TLS handshake success" );
                    break;
                case NOT_HANDSHAKING:
                    break;
                default:
                    // just a default catcher (should not be reached)
                    throw new IllegalStateException( "Invalid SSL status: " + handshakeStatus );
            }
            outboundEncryptedData.flip();
            LOGGER.log( Level.INFO, "status outboundEncryptedData Buffer "+outboundEncryptedData.remaining()+"/"+outboundEncryptedData.capacity()+"" );
            outboundEncryptedData.compact();
            outboundAppData.flip();
            LOGGER.log( Level.INFO, "status outboundAppData Buffer "+outboundAppData.remaining()+"/"+outboundAppData.capacity()+"" );
            outboundAppData.compact();
            LOGGER.log( Level.INFO, "status inboundEncryptedData Buffer "+inboundEncryptedData.remaining()+"/"+inboundEncryptedData.capacity()+"" );
            LOGGER.log( Level.INFO, "status inboundAppData Buffer "+inboundAppData.remaining()+"/"+inboundAppData.capacity()+"" );
        }

        if( timeout - ( System.currentTimeMillis() - start ) <= 0 && handshakeStatus != SSLEngineResult.HandshakeStatus.FINISHED ) {
            throw new IOException( "SSL/TLS handshake abborted due to timeout" );
        } else {
            LOGGER.log( Level.INFO, "******* HANDSHAKE SUCCESS" );
        }
    }

    protected void do_teardown( long timeout ) throws IOException {
        long start = System.currentTimeMillis();
        if( isTLS() ) {
            // we are done with the engine
            LOGGER.log( Level.INFO, "starting TLS teardown" );
            getEngine().closeOutbound();
            outboundAppData.clear();

            while (!getEngine().isOutboundDone()) {
                // Get close message
                outboundAppData.flip();
                SSLEngineResult res = getEngine().wrap(outboundAppData, outboundEncryptedData);
                outboundAppData.compact();

                switch (res.getStatus()) {
                    case OK:
                        break;
                    case CLOSED:
                        break;
                    case BUFFER_UNDERFLOW:
                        outboundAppData = handleBufferUnderflow( getEngine(), outboundAppData );
                        break;
                    case BUFFER_OVERFLOW:
                        outboundEncryptedData = enlargePacketBuffer( getEngine(), outboundEncryptedData);
                        break;
                    default:
                        throw new IllegalStateException("Invalid SSL status: " + res.getStatus());
                }

                // Send close message
                writeRawSocket( timeout - ( System.currentTimeMillis() - start ) );
            }
        }
        LOGGER.log( Level.INFO, "TLS teardown done" );
    }

    public boolean isTLS() {
        return isTLS;
    }

    public static long setDefaultTimeout( long timeout ) {
        long ret = defaultTimeout;
        defaultTimeout = timeout;
        return ret;
    }

    public static long getDefaultTimeout() {
        return defaultTimeout;
    }

    public long setTimeout( long timeout ) {
        long ret = this.timeout;
        this.timeout = timeout;
        return ret;
    }

    public long getTimeout() {
        return this.timeout;
    }

    private void writeSocket( String message, long timeout ) throws IOException {
        long start = System.currentTimeMillis();
        // add message to output buffer and prepare for reading
        int msgSize = message.getBytes(StandardCharsets.UTF_8).length;
        if( outboundAppData.limit()< msgSize ) {
            outboundAppData = enlargeBuffer( outboundAppData, outboundAppData.capacity() + msgSize );
        }
        outboundAppData.put( message.getBytes( StandardCharsets.UTF_8 ) );
        outboundAppData.flip();
        while (outboundAppData.remaining() > 0 && start + timeout > System.currentTimeMillis() ) {
            outboundAppData.compact();
            if( isTLS() ) {
                outboundAppData.flip();
                SSLEngineResult result = getEngine().wrap(outboundAppData, outboundEncryptedData);
                outboundAppData.compact();
                switch (result.getStatus()) {
                    case OK:
                        writeRawSocket( timeout - ( System.currentTimeMillis() - start ) );
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
                outboundEncryptedData.put( outboundAppData );
                outboundAppData.compact();
                writeRawSocket( timeout - ( System.currentTimeMillis() - start ) );
            }
            outboundAppData.flip();
        }
        outboundAppData.compact();
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
            bytesRead = readRawSocket( timeout - ( System.currentTimeMillis() - start ) );

            if( bytesRead == 0 ) {
                LOGGER.log( Level.INFO, "sleeping due to missing data (" + inboundEncryptedData.remaining() +")" );
                try {
                    Thread.sleep(50);
                } catch( InterruptedException ie ) {
                    // safe to ignore as we do not rely on timing here
                }
            }
            if( bytesRead > 0  ) {
                if (isTLS()) {
                    bytesRead = -inboundAppData.remaining();
                    inboundAppData.compact();
                    SSLEngineResult result = getEngine().unwrap(inboundEncryptedData, inboundAppData);
                    inboundAppData.flip();
                    bytesRead += inboundAppData.remaining();
                    LOGGER.log(Level.INFO, "decryption done (occupied buffer space: " + inboundAppData.remaining() + "; counter: " + bytesRead + ")");
                    if (getEngine().getHandshakeStatus() != HandshakeStatus.FINISHED && getEngine().getHandshakeStatus() != HandshakeStatus.NOT_HANDSHAKING) {
                        LOGGER.log(Level.WARNING, "Handshake status is " + getEngine().getHandshakeStatus());
                        processEngineRequirements(timeout - (System.currentTimeMillis() - start));
                    }
                    switch (result.getStatus()) {
                        case OK:
                            break;
                        case BUFFER_OVERFLOW:
                            inboundAppData = enlargeApplicationBuffer(getEngine(), inboundAppData);
                            inboundAppData.flip();
                            break;
                        case BUFFER_UNDERFLOW:
                            inboundEncryptedData = handleBufferUnderflow(getEngine(), inboundEncryptedData);
                            inboundEncryptedData.flip();
                            break;
                        case CLOSED:
                            closeConnection();
                            break;
                        default:
                            throw new IllegalStateException("Invalid SSL status: " + result.getStatus());
                    }
                } else {
                    inboundAppData.compact();
                    if (inboundAppData.limit() < bytesRead) {
                        inboundAppData.flip();
                        inboundAppData = enlargeBuffer(inboundAppData, inboundEncryptedData.remaining() + inboundAppData.capacity());
                    }
                    inboundAppData.put( inboundEncryptedData );
                    inboundAppData.flip();
                }

                if (bytesRead > 0) {
                    totBytesRead += bytesRead;
                    LOGGER.log(Level.INFO, "got bytes (" + bytesRead + " bytes; occupied buffer space: " + inboundEncryptedData.remaining() + ")");
                }

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
            LOGGER.log(Level.INFO, "Loop aborted due to data (" + totBytesRead + " bytes; occupied buffer space: "+inboundAppData.remaining()+")");
        } else {
            LOGGER.log(Level.INFO, "Loop aborted due to UNKNOWN");
        }

        if (bytesRead < 0) {
            LOGGER.log( Level.INFO, "doing shudown due to closed connection" );
            // if remote has closed channel do local shutdown
            shutdown();
        }

        // assert inboundAppData.remaining()>=totBytesRead: "found size missmatch  (" + inboundAppData +"; " + totBytesRead + ")";

        return totBytesRead;
    }

    public void writeln( String message ) throws IOException {
        writeln( message + "\r\n",getTimeout() );
    }

    public void writeln( String message, long timeout ) throws IOException {
        write( message + "\r\n", timeout );
    }

    public void write( String message ) throws IOException {
        write( message, getTimeout() );
    }

    public void write( String message,long timeout ) throws IOException {
        writeSocket( message, getTimeout() );
    }

    public String read() throws IOException {
        return read( getTimeout() );
    }

    public String read( long timeout ) throws IOException {
        int numBytes=readSocket( timeout );
        if( numBytes > 0 ) {
            byte[] b = new byte[numBytes];
            inboundAppData.get(b);
            inboundAppData.compact().flip();
            return new String(b, StandardCharsets.UTF_8);
        } else {
            return "";
        }
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
            if( inboundAppData.hasRemaining() ) {
                ret.append((char) inboundAppData.get());
            } else {
                try {
                    Thread.sleep(40);
                } catch( InterruptedException ie ) {
                    // safe to ignore due to loop
                }
            }
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
        ByteBuffer nBuffer;
        if( sessionProposedCapacity > buffer.capacity() ) {
            nBuffer = ByteBuffer.allocate(sessionProposedCapacity);
        } else {
            nBuffer = ByteBuffer.allocate(buffer.capacity() * 2);
        }
        nBuffer.put( buffer );
        return nBuffer;
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
        if( socketChannel != null ) {
            socketChannel.close();
        }
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