package net.gwerder.java.messagevortex.transport.imap;
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

import net.gwerder.java.messagevortex.ExtendedSecureRandom;
import net.gwerder.java.messagevortex.MessageVortexLogger;
import net.gwerder.java.messagevortex.transport.*;

import javax.net.ssl.TrustManager;
import javax.net.ssl.X509KeyManager;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.security.GeneralSecurityException;
import java.util.Set;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.logging.Level;
import java.util.logging.Logger;


public class ImapServer extends ListeningSocketChannel implements StoppableThread, SocketListener {

    private static final Logger LOGGER;
    private static int id = 1;
    private long gcLastRun=0;
    private SecurityContext context = null;

    static {
        LOGGER = MessageVortexLogger.getLogger((new Throwable()).getStackTrace()[0].getClassName());
    }

    Set<ImapConnection> conn=new ConcurrentSkipListSet<>();
    private Thread runner=null;
    private ImapAuthenticationProxy auth=null;
    private static long defaultTimeout = 10000;
    private long timeout = defaultTimeout;

    public ImapServer( SecurityContext secContext ) throws IOException {
        this( ( secContext.getRequirement() == SecurityRequirement.UNTRUSTED_SSLTLS || secContext.getRequirement() == SecurityRequirement.SSLTLS)?993:143, secContext );
    }

    public ImapServer( int port, SecurityContext enc ) throws IOException {
        super( new InetSocketAddress( "0.0.0.0", port ),null );
        setSocketListener( this );
        setSecurityContext( enc );
        java.security.Security.addProvider(new org.bouncycastle.jce.provider.BouncyCastleProvider());

        setProtocol( "imap" );

        setName( "IMAPlisten-"+(id++) );

        // Determine valid cyphers
        String ks="keystore.jks";
        try{
            // FIXME always installs all trust manager ... should be done selectively
            enc.getContext().init(new X509KeyManager[] {new CustomKeyManager(ks,"changeme", "mykey3") }, new TrustManager[] {new AllTrustManager()}, ExtendedSecureRandom.getSecureRandom() );
        } catch(GeneralSecurityException gse) {
            throw new IOException("Error initializing security context for connection",gse);
        }
    }

    public ImapAuthenticationProxy setAuth(ImapAuthenticationProxy ap) {
        ImapAuthenticationProxy old=auth;
        auth=ap;
        return old;
    }

    public SecurityContext setSecurityContext( SecurityContext context ){
        SecurityContext ret=this.context;
        this.context= context;
        return ret;
    }

    @Override
    public void gotConnect(ServerConnection ac) {
        try {
            LOGGER.log( Level.INFO, "got new connection" );
            ac.setSecurityContext( context );
            ac.setTimeout(getTimeout());
            if (context != null && (context.getRequirement() == SecurityRequirement.SSLTLS || context.getRequirement() == SecurityRequirement.UNTRUSTED_SSLTLS)) {
                LOGGER.log( Level.INFO, "starting handshake" );
                ac.startTLS();
            }
            LOGGER.log( Level.INFO, "inbound connection ready for use" );
            // FIXME handle incomming imap connection
            ac.shutdown();
        } catch(IOException ioe ) {
            LOGGER.log( Level.WARNING, "got exception while initial Handshake", ioe );
        }
    }

    @Override
    public boolean isShutdown() {
        return false;
    }

    public long getTimeout() {
        return timeout;
    }

    public long setTimeout( long timeout ) {
        long ret = this.timeout;
        this.timeout = timeout;
        return ret;
    }
}


