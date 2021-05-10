package net.messagevortex.transport;

import net.messagevortex.MessageVortexLogger;
import net.messagevortex.transport.imap.ImapLine;

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
import java.util.concurrent.TimeoutException;
import java.util.logging.Level;
import java.util.logging.Logger;

import static javax.net.ssl.SSLEngineResult.HandshakeStatus.NOT_HANDSHAKING;

/**
 * Abstract functions for creating a TLS channel socket.
 *
 * @author <a href="mailto:martin+messagevortex@gwerder.net">Martin Gwerder</a>
 */
public abstract class AbstractConnection {

    protected static final String CRLF = "\r\n";

    private static final Logger LOGGER;

    static {
        LOGGER = MessageVortexLogger.getLogger((new Throwable()).getStackTrace()[0].getClassName());
    }

    private static long defaultTimeout = 10 * 1000;
    private long timeout = defaultTimeout;

    private ByteBuffer outboundEncryptedData = (ByteBuffer) ByteBuffer.allocate(1024).clear();
    private ByteBuffer inboundEncryptedData = (ByteBuffer) ByteBuffer.allocate(1024).clear().flip();
    private ByteBuffer outboundAppData = (ByteBuffer) ByteBuffer.allocate(1024).clear();
    private ByteBuffer inboundAppData = (ByteBuffer) ByteBuffer.allocate(1024).clear().flip();

    private String protocol = null;
    private boolean isClient;

    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private SecurityContext context = null;
    private boolean isTls = false;
    private SocketChannel socketChannel = null;
    private InetSocketAddress remoteAddress = null;
    private SSLEngine engine = null;

    private volatile boolean shutdownAbstractConnection = false;

    public AbstractConnection(InetSocketAddress remoteAddress, SecurityContext context) {
        this.remoteAddress = remoteAddress;
        setSecurityContext(context);
    }

    /***
     * <p>This copy constructor enables duplication of a connection.</p>
     *
     * @param ac A connection to be copied
     */
    public AbstractConnection(AbstractConnection ac) {
        if (ac != null) {
            setSecurityContext(ac.getSecurityContext());
            setEngine(ac.getEngine());
            this.isTls = ac.isTls;
            this.socketChannel = ac.socketChannel;
            this.remoteAddress = ac.remoteAddress;
            this.protocol = ac.protocol;
            this.isClient = ac.isClient;
            this.timeout = ac.timeout;
            this.outboundEncryptedData = ac.outboundEncryptedData;
            this.outboundAppData = ac.outboundAppData;
            this.inboundEncryptedData = ac.inboundEncryptedData;
            this.inboundAppData = ac.inboundAppData;
        }
    }

    /***
     * <p>Create a connection with the given context.</p>
     *
     * @param sock          the channel to connect to
     * @param context       the predefined security context
     * @param isClient      true if the connection is a client connection
     */
    public AbstractConnection(SocketChannel sock, SecurityContext context, boolean isClient) {
        this.isClient = isClient;
        if (sock != null) {
            setSocketChannel(sock);
        }
        setSecurityContext(context);
    }

    /***
     * <p>Create a connection with the given context.</p>
     *
     * @param sock          the channel to connect to
     * @param context       the predefined security context
     */
    public AbstractConnection(SocketChannel sock, SecurityContext context) {
        this(sock, context, true);
    }

    /***
     * <p>Get the hostname of the remote host.</p>
     *
     * @return the hostname
     */
    public String getHostName() {
        if (remoteAddress == null) {
            return null;
        } else {
            return remoteAddress.getHostName();
        }
    }

    /***
     * <p>Gets the port of the remote host.</p>
     *
     * @return the remote port number (if known; otherwise -1)
     */
    public int getPort() {
        if (remoteAddress == null) {
            return -1;
        } else {
            return remoteAddress.getPort();
        }
    }

    protected final SocketChannel setSocketChannel(SocketChannel s) {
        SocketChannel ret = this.socketChannel;
        this.socketChannel = s;
        if (s != null) {
            try {
                s.configureBlocking(false);
                this.remoteAddress = (InetSocketAddress) s.getRemoteAddress();
            } catch (IOException ioe) {
                LOGGER.log(Level.SEVERE, "got unexpected exception", ioe);
            }
        }
        return ret;
    }

    /***
     * <p>Returns the socket channel in use for this connection.</p>
     *
     * @return the socket channel of this connection
     * @throws IOException if socket is not connected
     */
    public SocketChannel getSocketChannel() throws IOException {
        if (socketChannel == null || !socketChannel.isConnected()) {
            throw new IOException("socket is unexpectedly not connected (" + socketChannel + ")");
        }
        return this.socketChannel;
    }

    /***
     * <p>Sets the security context to be used with the socket channel.</p>
     *
     * @param context the security context to be used
     * @return the previously set security context
     */
    public final SecurityContext setSecurityContext(SecurityContext context) {
        SecurityContext ret = this.context;
        this.context = context;
        return ret;
    }

    /***
     * <p>Gets the security context used with the socket channel.</p>
     *
     * @return the security context
     */
    public SecurityContext getSecurityContext() {
        return context;
    }

    protected SSLEngine getEngine() {
        return engine;
    }

    protected final SSLEngine setEngine(SSLEngine engine) {
        SSLEngine ret = this.engine;
        this.engine = engine;
        return ret;
    }

    /***
     * <p>Connects to the remote host with respective security context.</p>
     *
     * @throws IOException if connecting fails
     */
    public void connect() throws IOException {
        if (socketChannel == null) {
            socketChannel = SocketChannel.open();
            socketChannel.configureBlocking(false);
            LOGGER.log(Level.INFO, "connecting socket channel to " + remoteAddress);
        }

        socketChannel.connect(remoteAddress);
        isClient = true;
        // wait for connection to complete handshake
        while (!socketChannel.finishConnect()) {
            try {
                Thread.sleep(10);
            } catch (InterruptedException ie) {
                // safe to ignore
            }

        }

        if (!isTls() && context != null && (context.getRequirement() == SecurityRequirement.SSLTLS
                || context.getRequirement() == SecurityRequirement.UNTRUSTED_SSLTLS)) {
            startTls();
        } else if (context != null && (context.getRequirement() == SecurityRequirement.SSLTLS
                || context.getRequirement() == SecurityRequirement.UNTRUSTED_SSLTLS)) {
            throw new IOException("unable to start required handshake due to missing SSLContext");
        }

        // make sure that socket channel is connected
        if (!socketChannel.isConnected()) {
            throw new IOException("socket is unexpectedly not connected");
        }

    }

    /***
     * <p>Make a TLS handshake on the connection with the default timeout.</p>
     *
     * @throws IOException if handshake fails
     */
    public void startTls() throws IOException {
        startTls(getTimeout());
    }

    /***
     * <p>Make a TLS handshake on the connection with a specified timeout.</p>
     *
     * @param timeout       the timeout in milliseconds
     * @throws IOException if handshake fails
     */
    public void startTls(long timeout) throws IOException {
        if (isTls()) {
            LOGGER.log(Level.WARNING, "refused to renegotiate TLS (already encrypted)");
            return;
        }
        if (getEngine() == null && getSecurityContext() != null
                && getSecurityContext().getContext() != null) {
            // init engine if not yet done
            InetSocketAddress ia = (InetSocketAddress) socketChannel.getLocalAddress();
            LOGGER.log(Level.FINE, "created SSLEngine for " + ia
                    + " (name=" + ia.getHostName() + "; client=" + isClient + ")");
            setEngine(getSecurityContext().getContext().createSSLEngine(ia.getHostName(), ia.getPort()));
            getEngine().setUseClientMode(isClient);
        } else if (getEngine() == null) {
            throw new IOException("no security context available but startTls called");
        }

        LOGGER.log(Level.FINE, "starting TLS handshake (clientMode=" + isClient + ")");

        // set connection to TLS mode
        isTls = true;

        // init starttls and fill buffers
        do_handshake(timeout);
    }

    /***
     * <p>Sets the protocol to be used (mainly for logger messages).</p>
     *
     * @param protocol the protocol name or abbreviation
     * @return the previously set protocol name
     */
    public String setProtocol(String protocol) {
        String ret = getProtocol();
        this.protocol = protocol;
        return ret;
    }

    /***
     * <p>Gets the protocol name used.</p>
     *
     * @return the protocol name
     */
    public String getProtocol() {
        return protocol;
    }

    protected void do_handshake(long timeout) throws IOException {
        if (getEngine() == null) {
            throw new IOException("No SSL context available");
        }
        if (!getSocketChannel().isConnected()) {
            throw new IOException("No SSL connection possible on unconnected socket");
        }

        // initiate handshake in engine
        getEngine().beginHandshake();

        processEngineRequirements(timeout);
    }

    private int readRawSocket(long timeout) throws IOException {
        // read bytes from socket
        boolean loopOnce = inboundEncryptedData.remaining() > 0;
        int bytesRead = 0;
        inboundEncryptedData.compact();
        try {
            LOGGER.log(Level.FINE, "starting reading raw socket (loopOnce is " + loopOnce + ")");
            long start = System.currentTimeMillis();
            while (!shutdownAbstractConnection && bytesRead == 0
                    && timeout - (System.currentTimeMillis() - start) > 0) {
                bytesRead = getSocketChannel().read(inboundEncryptedData);
                LOGGER.log(Level.INFO, "tried to read (got " + bytesRead + ")");
                if (bytesRead == 0) {
                    if (loopOnce) {
                        // abort if precondition might be fulfilled already
                        LOGGER.log(Level.INFO, "done reading raw socket (loop once gave no new input)");
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
            LOGGER.log(Level.FINE, "done reading raw socket (took "
                    + (System.currentTimeMillis() - start) + "ms; read " + bytesRead + " bytes)");
        } catch (IOException ioe) {
            inboundEncryptedData.flip();
            LOGGER.log(Level.WARNING, "got exception while reading (propagated)", ioe);
            throw ioe;
        }
        inboundEncryptedData.flip();
        LOGGER.log(Level.FINE, "reverting inbound buffer (new size is "
                + inboundEncryptedData.remaining() + ")");
        return bytesRead;
    }

    private int writeRawSocket(long timeout) throws IOException {
        outboundEncryptedData.flip();
        LOGGER.log(Level.INFO, "trying to send outboundEncryptedData Buffer "
                + outboundEncryptedData.remaining() + "");
        try {
            long start = System.currentTimeMillis();
            int bytesWritten = 0;
            while (outboundEncryptedData.hasRemaining()
                    && timeout - (System.currentTimeMillis() - start) > 0) {
                bytesWritten += getSocketChannel().write(outboundEncryptedData);
            }
            return bytesWritten;
        } finally {
            LOGGER.log(Level.FINE, "sending outboundEncryptedData Buffer done ("
                    + outboundEncryptedData.remaining() + ")");
            outboundEncryptedData.compact();
        }
    }

    private void processEngineRequirements(long timeout) throws IOException {
        if (getEngine() == null) {
            throw new IOException("No SSL context available");
        }
        if (!getSocketChannel().isConnected()) {
            throw new IOException("No SSL connection possible on unconnected socket");
        }

        LOGGER.log(Level.FINER, "status (1) outboundEncryptedData Buffer "
                + outboundEncryptedData + "");
        LOGGER.log(Level.FINER, "status (1) outboundAppData Buffer " + outboundAppData);
        LOGGER.log(Level.FINER, "status (1) inboundEncryptedData Buffer " + inboundEncryptedData);
        LOGGER.log(Level.FINER, "status (1) inboundAppData Buffer " + inboundAppData);

        // start timeout counter
        long start = System.currentTimeMillis();

        // get handshake status
        HandshakeStatus handshakeStatus = getEngine().getHandshakeStatus();

        while (!shutdownAbstractConnection && handshakeStatus != HandshakeStatus.FINISHED
                && handshakeStatus != NOT_HANDSHAKING
                && timeout - (System.currentTimeMillis() - start) > 0 && getEngine() != null) {

            // Checking handshake status of SSL engine
            LOGGER.log(Level.FINE, "re-looping (handshake status is " + handshakeStatus + ")");
            switch (handshakeStatus) {
                case NEED_UNWRAP:
                    LOGGER.log(Level.FINE, "doing unwrap (reading; buffer is "
                            + inboundEncryptedData.remaining() + ")");
                    int bytesRead = readRawSocket(timeout - (System.currentTimeMillis() - start));

                    LOGGER.log(Level.FINE, "doing unwrap (" + inboundEncryptedData.remaining() + ")");

                    if (bytesRead < 0) {
                        if (getEngine().isInboundDone() && getEngine().isOutboundDone()) {
                            throw new IOException("cannot starttls. Socket was at least partially closed or the "
                                    + "SSL engine encountered an error while handshaking");
                        }
                        try {
                            getEngine().closeInbound();
                        } catch (SSLException e) {
                            LOGGER.log(Level.WARNING, "Exception while closing inbound", e);
                        }
                        getEngine().closeOutbound();
                        handshakeStatus = getEngine().getHandshakeStatus();
                        break;
                    }

                    inboundAppData.compact();
                    SSLEngineResult res;
                    try {
                        res = getEngine().unwrap(inboundEncryptedData, inboundAppData);
                    } catch (SSLException sslException) {
                        LOGGER.log(Level.WARNING, "A problem was encountered while processing the data "
                                        + "that caused the SSLEngine to abort. Will try to properly close "
                                        + "connection...",
                                sslException);
                        getEngine().closeOutbound();
                        handshakeStatus = getEngine().getHandshakeStatus();
                        break;
                    } finally {
                        inboundAppData.flip();
                    }
                    handshakeStatus = getEngine().getHandshakeStatus();
                    switch (res.getStatus()) {
                        case OK:
                            LOGGER.log(Level.FINE, "unwrap done (OK;inboundAppData="
                                    + inboundAppData.remaining() + ";inboundEncryptedData="
                                    + inboundEncryptedData.remaining() + ")");
                            break;
                        case CLOSED:
                            if (getEngine().isOutboundDone()) {
                                throw new IOException("engine reports closed status");
                            } else {
                                getEngine().closeOutbound();
                                handshakeStatus = getEngine().getHandshakeStatus();
                                LOGGER.log(Level.FINE, "unwrap done (CLOSED)");
                                isTls = false;
                                setEngine(null);
                                break;
                            }
                        case BUFFER_UNDERFLOW:
                            inboundEncryptedData = handleBufferUnderflow(getEngine(), inboundEncryptedData);
                            handshakeStatus = getEngine().getHandshakeStatus();
                            LOGGER.log(Level.FINE, "unwrap done (UNDERFLOW; inboundEncryptedData Buffer "
                                    + inboundEncryptedData.remaining() + "/" + inboundEncryptedData.capacity()
                                    + ")");
                            break;
                        case BUFFER_OVERFLOW:
                            inboundAppData = enlargeApplicationBuffer(getEngine(), inboundAppData);
                            handshakeStatus = getEngine().getHandshakeStatus();
                            LOGGER.log(Level.FINE, "unwrap done (OVERFLOW; inboundAppData Buffer "
                                    + inboundAppData.remaining() + "/" + inboundAppData.capacity() + ")");
                            break;
                        default:
                            throw new IllegalStateException("Invalid SSL status: " + res.getStatus());
                    }
                    break;
                case NEED_WRAP:
                    LOGGER.log(Level.FINE, "doing wrap (wrapping)");

                    outboundAppData.flip();
                    SSLEngineResult result = null;
                    try {
                        LOGGER.log(Level.FINER, "outboundAppData (flipped)=" + outboundAppData);
                        LOGGER.log(Level.FINER, "outboundEncryptedData=" + outboundEncryptedData);
                        result = getEngine().wrap(outboundAppData, outboundEncryptedData);
                        handshakeStatus = result.getHandshakeStatus();
                        LOGGER.log(Level.FINER, "outboundAppData (flipped)=" + outboundAppData);
                        LOGGER.log(Level.FINER, "outboundEncryptedData=" + outboundEncryptedData
                                + "/" + result);
                    } catch (SSLException se) {
                        LOGGER.log(Level.WARNING, "Exception while reading data", se);
                        getEngine().closeOutbound();
                        handshakeStatus = getEngine().getHandshakeStatus();
                        break;
                    } finally {
                        outboundAppData.compact();
                    }
                    outboundEncryptedData.flip();
                    LOGGER.log(Level.FINE, "doing wrap (outboundEncryptedData is "
                            + outboundEncryptedData.remaining() + "/"
                            + outboundEncryptedData.capacity() + ")");
                    outboundEncryptedData.compact();

                    switch (result.getStatus()) {
                        case OK:
                            writeRawSocket(timeout - (System.currentTimeMillis() - start));
                            outboundEncryptedData.flip();
                            LOGGER.log(Level.FINE, "wrap done with status OK (remaining bytes:"
                                    + outboundEncryptedData.remaining() + ")");
                            outboundEncryptedData.compact();
                            handshakeStatus = result.getHandshakeStatus();
                            break;
                        case CLOSED:
                            try {
                                writeRawSocket(timeout - (System.currentTimeMillis() - start));
                            } catch (IOException e) {
                                LOGGER.log( Level.FINE, "Failed to close channel", e );
                            }
                            isTls = false;
                            setEngine(null);
                            handshakeStatus = result.getHandshakeStatus();
                            break;
                        case BUFFER_UNDERFLOW:
                            LOGGER.log(Level.SEVERE, "Buffer underflow should not happen",
                                    new SSLException("unknown reason for buffer underflow"));
                            handshakeStatus = result.getHandshakeStatus();
                            break;
                        case BUFFER_OVERFLOW:
                            outboundEncryptedData.flip();
                            outboundEncryptedData = enlargePacketBuffer(getEngine(), outboundEncryptedData);
                            LOGGER.log(Level.FINE, "wrap done (OVERFLOW; outboundEncryptedData Buffer "
                                    + outboundEncryptedData.remaining() + "/"
                                    + outboundEncryptedData.capacity() + ")");
                            outboundEncryptedData.compact();
                            handshakeStatus = result.getHandshakeStatus();
                            break;
                        default:
                            throw new IllegalStateException("Invalid SSL status: " + result.getStatus());
                    }
                    break;
                case NEED_TASK:
                    LOGGER.log(Level.FINE, "running tasks");
                    Runnable task = getEngine().getDelegatedTask();
                    do {
                        if (task != null) {
                            LOGGER.log(Level.FINE, "running task " + task);
                            Thread t = new Thread(task);
                            t.start();
                            try {
                                t.join();
                            } catch (InterruptedException ie) {
                                // safe to ignore
                            }
                        } else {
                            try {
                                Thread.sleep(20);
                            } catch (InterruptedException ie) {
                                // safe to ignore
                            }
                        }
                        task = getEngine().getDelegatedTask();

                    } while (task != null);

                    LOGGER.log(Level.FINE, "running tasks done");
                    handshakeStatus = getEngine().getHandshakeStatus();
                    break;
                case FINISHED:
                    LOGGER.log(Level.FINE, "TLS handshake success");
                    break;
                case NOT_HANDSHAKING:
                    break;
                default:
                    // just a default catcher (should not be reached)
                    throw new IllegalStateException("Invalid SSL status: " + handshakeStatus);
            }
            outboundEncryptedData.flip();
            LOGGER.log(Level.FINE, "status outboundEncryptedData Buffer "
                    + outboundEncryptedData.remaining() + "/" + outboundEncryptedData.capacity() + "");
            outboundEncryptedData.compact();
            outboundAppData.flip();
            LOGGER.log(Level.FINE, "status outboundAppData Buffer " + outboundAppData.remaining()
                    + "/" + outboundAppData.capacity() + "");
            outboundAppData.compact();
            LOGGER.log(Level.FINE, "status inboundEncryptedData Buffer "
                    + inboundEncryptedData.remaining() + "/" + inboundEncryptedData.capacity() + "");
            LOGGER.log(Level.FINE, "status inboundAppData Buffer " + inboundAppData.remaining()
                    + "/" + inboundAppData.capacity() + "");
        }

        long elapsed = timeout - (System.currentTimeMillis() - start);
        if (!shutdownAbstractConnection && elapsed <= 0
                && handshakeStatus != SSLEngineResult.HandshakeStatus.FINISHED) {
            throw new IOException("SSL/TLS handshake aborted due to timeout (current timeout:"
                    + (timeout / 1000) + "s ;elapsed:" + (elapsed / 1000) + "s)");
        } else {
            LOGGER.log(Level.FINE, "******* HANDSHAKE SUCCESS");
        }
    }

    protected void do_teardown(long timeout) throws IOException {
        long start = System.currentTimeMillis();
        if (isTls()) {
            // we are done with the engine
            LOGGER.log(Level.FINE, "starting TLS teardown");
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
                        outboundAppData.flip();
                        outboundAppData = handleBufferUnderflow(getEngine(), outboundAppData);
                        outboundAppData.compact();
                        break;
                    case BUFFER_OVERFLOW:
                        outboundEncryptedData.flip();
                        outboundEncryptedData = enlargePacketBuffer(getEngine(), outboundEncryptedData);
                        outboundEncryptedData.compact();
                        break;
                    default:
                        throw new IllegalStateException("Invalid SSL status: " + res.getStatus());
                }

                // Send close message
                writeRawSocket(timeout - (System.currentTimeMillis() - start));
            }
        }
        LOGGER.log(Level.FINE, "TLS teardown done");
    }

    /***
     * <p>returns true if a TLS handshake has been successfully done.</p>
     *
     * @return the TLS state
     */
    public boolean isTls() {
        return isTls;
    }

    /***
     * <p>Sets the default timeout for all connections not having an own timeout.</p>
     *
     * @param timeout the timeout in milliseconds
     * @return the previously set timeout
     */
    public static long setDefaultTimeout(long timeout) {
        long ret = defaultTimeout;
        defaultTimeout = timeout;
        return ret;
    }

    /***
     * <p>Gets the default timeout for all connections not having an own timeout.</p>
     *
     * @return the previously set timeout
     */
    public static long getDefaultTimeout() {
        return defaultTimeout;
    }

    /***
     * <p>Sets the default timeout for this connection.</p>
     *
     * @param timeout the timeout in milliseconds
     * @return the previously set timeout
     */
    public long setTimeout(long timeout) {
        long ret = this.timeout;
        this.timeout = timeout;
        return ret;
    }

    /***
     * <p>Gets the default timeout for this connection.</p>
     *
     * @return the  timeout in milliseconds
     */
    public long getTimeout() {
        return this.timeout;
    }

    private void writeSocket(String message, long timeout) throws IOException {
        final long start = System.currentTimeMillis();
        // add message to output buffer and prepare for reading
        int msgSize = (message == null ? 0 : message.getBytes(StandardCharsets.UTF_8).length);
        if (outboundAppData.limit() < msgSize) {
            outboundAppData = enlargeBuffer(outboundAppData, outboundAppData.capacity() + msgSize);
        }
        if (message != null) {
            outboundAppData.put(message.getBytes(StandardCharsets.UTF_8));
        }
        outboundAppData.flip();
        while (!shutdownAbstractConnection && outboundAppData.remaining() > 0
                && start + timeout > System.currentTimeMillis()) {
            outboundAppData.compact();
            if (isTls()) {
                outboundAppData.flip();
                SSLEngineResult result = getEngine().wrap(outboundAppData, outboundEncryptedData);
                outboundAppData.compact();
                switch (result.getStatus()) {
                    case OK:
                        writeRawSocket(timeout - (System.currentTimeMillis() - start));
                        break;
                    case BUFFER_OVERFLOW:
                        outboundAppData.flip();
                        outboundEncryptedData = enlargePacketBuffer(getEngine(), outboundEncryptedData);
                        outboundAppData.compact();
                        break;
                    case BUFFER_UNDERFLOW:
                        throw new SSLException("Buffer underflow occurred after a wrap. "
                                + "I don't think we should ever get here.");
                    case CLOSED:
                        closeConnection();
                        return;
                    default:
                        throw new IllegalStateException("Invalid SSL status: " + result.getStatus());
                }
            } else {
                // send all bytes as is
                outboundAppData.flip();
                outboundEncryptedData.put(outboundAppData);
                outboundAppData.compact();
                writeRawSocket(timeout - (System.currentTimeMillis() - start));
            }
            outboundAppData.flip();
        }
        outboundAppData.compact();
        if (start + timeout <= System.currentTimeMillis()) {
            throw new IOException("Timeout reached while writing");
        }
    }

    /***
     * <p>FIXME.</p>
     *
     * @param timeout  &lt;0 timeout means read only available bytes;
     *                 &gt;0 means wait for bytes up until
     * @return FIXME
     * @throws IOException FIXME
     */
    private int readSocket(long timeout) throws IOException, TimeoutException {

        int bytesRead;
        int totBytesRead = 0;

        long start = System.currentTimeMillis();
        boolean timeoutReached;
        do {
            try {
                bytesRead = readRawSocket(timeout - (System.currentTimeMillis() - start));
            } catch (IOException ioe) {
                bytesRead = 0;
                if (timeout <= (System.currentTimeMillis() - start)) {
                    // timeout has been reached
                    LOGGER.log(Level.FINE, "timeout has been reached while waiting for input");
                    TimeoutException e = new TimeoutException("Timeout while reading");
                    e.addSuppressed(ioe);
                    throw e;
                } else {
                    throw ioe;
                }
            }

            if (bytesRead == 0) {
                LOGGER.log(Level.FINE, "sleeping due to missing data ("
                        + inboundEncryptedData.remaining() + ")");
                try {
                    Thread.sleep(50);
                } catch (InterruptedException ie) {
                    // safe to ignore as we do not rely on timing here
                }
            }
            if (inboundEncryptedData.remaining() > 0) {
                if (isTls()) {
                    bytesRead = -inboundAppData.remaining();
                    inboundAppData.compact();
                    LOGGER.log(Level.FINE, "decrypting (occupied inbound buffer space: "
                            + inboundEncryptedData.remaining() + "; counter: " + bytesRead
                            + ")", new Object[]{inboundEncryptedData, inboundAppData});
                    final SSLEngineResult result = getEngine().unwrap(inboundEncryptedData, inboundAppData);
                    inboundAppData.flip();
                    bytesRead += inboundAppData.remaining();
                    LOGGER.log(Level.FINE, "decryption done (occupied buffer space: "
                            + inboundAppData.remaining() + "; counter: " + bytesRead + ")");
                    if (getEngine().getHandshakeStatus() != HandshakeStatus.FINISHED
                            && getEngine().getHandshakeStatus() != HandshakeStatus.NOT_HANDSHAKING) {
                        LOGGER.log(Level.FINE, "Handshake status is " + getEngine().getHandshakeStatus());
                        processEngineRequirements(timeout - (System.currentTimeMillis() - start));
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
                            break;
                        default:
                            throw new IllegalStateException("Invalid SSL status: " + result.getStatus());
                    }
                } else {
                    inboundAppData.compact();
                    if (inboundAppData.limit() < bytesRead) {
                        inboundAppData.flip();
                        inboundAppData = enlargeBuffer(inboundAppData,
                                inboundEncryptedData.remaining() + inboundAppData.capacity());
                        inboundAppData.compact();
                    }
                    inboundAppData.put(inboundEncryptedData);
                    inboundAppData.flip();
                }

                if (bytesRead > 0) {
                    totBytesRead += bytesRead;
                    LOGGER.log(Level.FINE, "got bytes (" + bytesRead + " bytes; occupied buffer space: "
                            + inboundEncryptedData.remaining() + ")");
                }

            }

            timeoutReached = System.currentTimeMillis() - start > timeout;
        } while (!shutdownAbstractConnection && bytesRead >= 0 && totBytesRead == 0
                && !timeoutReached && timeout > 0);

        if (bytesRead < 0) {
            LOGGER.log(Level.FINE, "Loop aborted due to closed connection");
        } else if (System.currentTimeMillis() - start > timeout) {
            LOGGER.log(Level.FINE, "Loop aborted due to timeout");
        } else if (timeout <= 0) {
            LOGGER.log(Level.INFO, "Loop aborted due to single round wish");
        } else if (totBytesRead > 0) {
            LOGGER.log(Level.FINE, "Loop aborted due to data (" + totBytesRead
                    + " bytes; occupied buffer space: " + inboundAppData.remaining() + ")");
        } else {
            LOGGER.log(Level.FINE, "Loop aborted due to UNKNOWN");
        }

        if (bytesRead < 0) {
            LOGGER.log(Level.FINE, "doing shutdown due to closed connection");
            // if remote has closed channel do local shutdown
            shutdown();
        }

        return totBytesRead;
    }

    public void writeln(String message) throws IOException {
        writeln(message, getTimeout());
    }

    public void writeln(String message, long timeout) throws IOException {
        write(message + "\r\n", timeout);
    }

    public void write(String message) throws IOException {
        write(message, getTimeout());
    }

    /***
     * <p>Write a message string to the peer partner.</p>
     *
     * @param message the message string to be sent
     * @param timeout the timeout in milliseconds
     * @throws IOException if communication or encryption fails
     */
    public void write(String message, long timeout) throws IOException {
        LOGGER.log(Level.FINE, "writing message with write ("
                + ImapLine.commandEncoder(message) + "; size "
                + (message != null ? message.length() : -1) + ")");
        writeSocket(message, getTimeout());
    }

    public String read() throws IOException, TimeoutException {
        return read(getTimeout());
    }

    /***
     * <p>Read a string from the socket channel.</p>
     *
     * @param timeout the timeout to be applied before unblocking
     * @return the string read
     *
     * @throws IOException if decryption fails or host is unexpectedly disconnected
     * @throws TimeoutException if reaching a timeout while reading
     */
    public String read(long timeout) throws IOException, TimeoutException {
        int numBytes = readSocket(timeout);
        if (numBytes > 0) {
            byte[] b = new byte[numBytes];
            inboundAppData.get(b);
            inboundAppData.compact().flip();
            return new String(b, StandardCharsets.UTF_8);
        } else {
            return "";
        }
    }

    public String readln() throws IOException, TimeoutException {
        return readln(getTimeout());
    }


    /***
     * <p>Read a string up until CRLF from the socket channel.</p>
     *
     * @param timeout the timeout to be applied before unblocking
     * @return the string read
     *
     * @throws IOException if decryption fails or host is unexpectedly disconnected
     * @throws TimeoutException if reaching a timeout while reading
     */
    public String readln(long timeout) throws IOException, TimeoutException {
        StringBuilder ret = new StringBuilder();
        long start = System.currentTimeMillis();
        try {
            while (!shutdownAbstractConnection && !ret.toString().endsWith("\r\n")
                    && timeout - (System.currentTimeMillis() - start) > 0) {
                if (!inboundAppData.hasRemaining()) {
                    readSocket(timeout - (System.currentTimeMillis() - start));
                }
                if (inboundAppData.hasRemaining()) {
                    ret.append((char) inboundAppData.get());
                } else {
                    try {
                        Thread.sleep(40);
                    } catch (InterruptedException ie) {
                        // safe to ignore due to loop
                    }
                }
            }
        } catch (IOException ioe) {
            if (timeout <= (System.currentTimeMillis() - start)) {
                // timeout has been reached
                LOGGER.log(Level.INFO, "timeout has been reached while waiting for input");
            } else {
                throw ioe;
            }
        }

        if (!ret.toString().endsWith("\r\n")) {
            inboundAppData.rewind();
            inboundAppData.compact().flip();
            return null;
        } else {
            LOGGER.log(Level.FINE, "got line from readline ("
                    + ret.substring(0, ret.length() - 2) + "; size "
                    + ret.length() + ")");
            inboundAppData.compact().flip();
            return ret.substring(0, ret.length() - 2);
        }
    }

    protected ByteBuffer enlargePacketBuffer(SSLEngine engine, ByteBuffer buffer) {
        return enlargeBuffer(buffer, engine.getSession().getPacketBufferSize());
    }

    protected ByteBuffer enlargeApplicationBuffer(SSLEngine engine, ByteBuffer buffer) {
        return enlargeBuffer(buffer, engine.getSession().getApplicationBufferSize());
    }

    protected ByteBuffer enlargeBuffer(ByteBuffer buffer, int sessionProposedCapacity) {
        ByteBuffer newBuffer;
        if (sessionProposedCapacity > buffer.capacity()) {
            newBuffer = ByteBuffer.allocate(sessionProposedCapacity);
        } else {
            newBuffer = ByteBuffer.allocate(buffer.capacity() * 2);
        }
        newBuffer.put(buffer);
        newBuffer.flip();
        return newBuffer;
    }

    protected ByteBuffer handleBufferUnderflow(SSLEngine engine, ByteBuffer buffer) {
        if (engine.getSession().getPacketBufferSize() <= buffer.limit()) {
            return buffer;
        } else {
            return enlargePacketBuffer(engine, buffer);
        }
    }

    protected void closeConnection() throws IOException {
        if (getEngine() != null && isTls()) {
            do_teardown(1000);
        }
        if (socketChannel != null) {
            socketChannel.close();
        }
    }

    protected void handleEndOfStream() throws IOException {
        if (getEngine() != null) {
            try {
                getEngine().closeInbound();
            } catch (IOException ioe) {
                LOGGER.log(Level.WARNING, "channel already closed without TLS closure", ioe);
            }
        }
        closeConnection();
    }

    public void shutdown() throws IOException {
        shutdownAbstractConnection = true;
        executor.shutdown();
    }

    public boolean isShutdown() {
        return shutdownAbstractConnection;
    }

}
