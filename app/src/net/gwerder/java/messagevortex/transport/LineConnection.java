package net.gwerder.java.messagevortex.transport;
// ************************************************************************************
// * Copyright (c) 2018 Martin Gwerder (martin@gwerder.net)
// *
// * Permission is hereby granted, free of charge, to any person obtaining a copy
// * of this software and associated documentation files (the "Software"), to deal
// * in the Software without restriction, including without limitation the rights
// * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
// * copies of the Software, and to permit persons to whom the Software is
// * furnished to do so, subject to the following conditions:
// *
// * The above copyright notice and this permission notice shall be included in all
// * copies or substantial portions of the Software.
// *
// * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
// * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
// * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
// * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
// * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
// * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
// * SOFTWARE.
// ************************************************************************************

import net.gwerder.java.messagevortex.MessageVortexLogger;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.TimeoutException;
import java.util.logging.Level;

import static java.nio.charset.StandardCharsets.UTF_8;

/**
 * Open clients connections.
 */
public abstract class LineConnection extends Thread implements StoppableThread {

    static {
        java.security.Security.addProvider(new org.bouncycastle.jce.provider.BouncyCastleProvider());
    }

    public static final String CRLF= "\r\n";

    static final java.util.logging.Logger LOGGER;
    static {
        LOGGER = MessageVortexLogger.getLogger((new Throwable()).getStackTrace()[0].getClassName());
        MessageVortexLogger.setGlobalLogLevel( Level.ALL);
    }

    private Object lock=new Object();

    private String protocol = "unknown";

    boolean shutdown=false;
    private int timeout = 90*1000;
    private Socket s=null;
    private SSLContext context;
    private ServerAuthenticator auth=null;
    private TransportReceiver receiver=null;
    private Byte buffer = null;
    private SecurityRequirement encrypted;

    private Set<String> supportedCiphers = new HashSet<>();

    private boolean isTLS=false;

    public LineConnection( Socket s, SSLContext context, Set<String> supportedCiphers, SecurityRequirement encrypted ) throws IOException  {
        this.encrypted=encrypted;
        setSocket( s );
        this.context = context;
        if( supportedCiphers != null ) {
            this.supportedCiphers.addAll( supportedCiphers );
        }
        if( encrypted == SecurityRequirement.SSLTLS || encrypted == SecurityRequirement.UNTRUSTED_SSLTLS ) {
            if( s != null ) {
                startTLS();
            } else {
                isTLS=true;
            }
        }
        if( s != null ) {
            LOGGER.log(Level.INFO, "Got connection from client (" + s.getPort() + "/" + isTLS + ")");
        }
    }

    public LineConnection(SSLContext context,TransportReceiver receiver, SecurityRequirement encrypted) throws IOException {
        this.context = context;
        this.receiver=receiver;
        setSocket(null);
        isTLS = encrypted == SecurityRequirement.SSLTLS || encrypted == SecurityRequirement.UNTRUSTED_SSLTLS;
    }

    public LineConnection createConnection(Socket s) throws IOException {
        throw new UnsupportedOperationException("createConnection must be overloaded");
    }

    public SecurityRequirement getSecurityRequirement() {
        return encrypted;
    }

    public TransportReceiver getReceiver() {
        return receiver;
    }

    public SSLContext getSSLContext() {
        return context;
    }

    public String setProtocol( String protocol ) {
        String ret = this.protocol;
        this.protocol=protocol;
        return ret;
    }

    public String getProtocol() {
        return protocol;
    }



    public  final void setSocket(Socket s) throws IOException {
        this.s=s;
        if( this.s != null ) {
            this.s.setSoTimeout( (int)getTimeout() );
        }
    }

    public void shutdown() {
        shutdown = true;
    }

    public void waitShutdown() {
        // wait for shutdown
        while (getState() != State.TERMINATED) {
            try {
                this.join();
            } catch (InterruptedException ie) {
                // safe to ignore due to loop
            }
        }
    }

    public boolean isShutdown() {
        return getState() != State.TERMINATED;
    }

    public ServerAuthenticator setAuthenticator(ServerAuthenticator auth) {
        ServerAuthenticator ret=this.auth;
        this.auth=auth;
        return ret;
    }

    public int getTimeout() {
        return timeout;
    }

    public int setTimeout( int timeout ) {
        int ret=this.timeout;
        this.timeout=timeout;
        return ret;
    }

    public void startTLS() throws IOException {
        synchronized (lock) {
            if( s != null ) {
                LOGGER.log(Level.INFO, "Starting server TLS");
                setSocket( ((SSLSocketFactory) SSLSocketFactory.getDefault()).createSocket(s,
                        s.getInetAddress().getHostAddress(),
                        s.getPort(),
                        true) );
                s.setSoTimeout( (int)getTimeout() );
                ((SSLSocket)(s)).setUseClientMode(false);
                ((SSLSocket)(s)).startHandshake();
                isTLS = true;
                LOGGER.log(Level.INFO, "Server TLS completed successfully");
            } else {
                LOGGER.log( Level.WARNING, "Skipped starttls", new Object[] { this} );
            }
        }
    }

    public boolean isTLS() {
        return isTLS;
    }

    public String read( boolean addCRLF ) throws IOException {
        synchronized( lock ) {
            String txt=null;
            long start=new Date().getTime();
            while(new Date().getTime()<start+timeout && txt==null) {
                        try {
                            StringBuilder sb = new StringBuilder();
                            if( buffer != null ) {
                                sb.append( buffer );
                                buffer = null;
                            }
                            s.setSoTimeout((int)(getTimeout()));
                            InputStream is = s.getInputStream();
                            long maxTimeMillis = System.currentTimeMillis() + timeout;
                            while (System.currentTimeMillis() < maxTimeMillis && ! sb.toString().endsWith( "\n" ) && ! shutdown ) {
                                byte[] b = new byte[1];
                                try {
                                    LOGGER.log(Level.INFO, "trying to read char");
                                    int readResult = is.read(b, 0, 1);
                                    if (readResult == 1) {
                                        sb.append((char)(b[0]));
                                        LOGGER.log(Level.INFO, "got char " + (char)(b[0])+" ("+sb.toString()+")");
                                    }
                                } catch(SocketTimeoutException ste) {
                                    // safe to ignore as this is expected on timeout
                                }
                            }
                            if ( System.currentTimeMillis() >= maxTimeMillis ) {
                                throw new IOException( "error while reading socket",new TimeoutException( "Timeout while waiting for line" ) );
                            }
                            txt = sb.toString();
                } catch (SocketTimeoutException to) {
                    txt=null;
                }
            }
            if(txt== null){
                throw new SocketTimeoutException();
            }
            if( ! addCRLF ) {
                // strip off crlf
                if( txt.endsWith( "\n") ) {
                    txt = txt.substring( 0, txt.length()-2 );
                }
                if( txt.endsWith( "\r") ) {
                    txt = txt.substring( 0, txt.length()-2 );
                }
            }
            LOGGER.log(Level.INFO,"C:"+txt);
            return txt;
        }
    }

    public void run() {
        try {
            while (!isShutdown()) {
                String line = read( false );
                gotLine(line);
            }
        } catch(IOException ioe) {
            LOGGER.log( Level.WARNING, "Got Exception while communicating",ioe );
        }
        shutdown();
    }

    protected abstract void gotLine(String line) throws IOException ;

    public Set<String> getSupportedCiphers() {
        return new HashSet<>(supportedCiphers);
    }

    public void write(String txt) throws IOException  {
        synchronized( lock ) {
            OutputStream os = s.getOutputStream();
            os.write( txt.getBytes( UTF_8 ) );
            LOGGER.log(Level.INFO,"S:"+txt);
            os.flush();
        }
    }

    public void write(String[] txt) throws IOException  {
        synchronized( lock ) {
            OutputStream os = s.getOutputStream();
            for(String l : txt) {
                os.write( l.getBytes( UTF_8 ) );
                LOGGER.log(Level.INFO, "S:" + l);
            }
            os.flush();
        }
    }

}
