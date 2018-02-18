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

import javax.net.ServerSocketFactory;
import javax.net.SocketFactory;
import javax.net.ssl.*;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.security.GeneralSecurityException;
import java.util.*;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.logging.Level;
import java.util.logging.Logger;


public class ImapServer extends LineReceiver implements StoppableThread {

    private static final Logger LOGGER;
    private static int id = 1;
    private long gcLastRun=0;

    static {
        LOGGER = MessageVortexLogger.getLogger((new Throwable()).getStackTrace()[0].getClassName());
    }

    Set<ImapConnection> conn=new ConcurrentSkipListSet<>();
    private Thread runner=null;
    private ImapAuthenticationProxy auth=null;

    public ImapServer(boolean encrypted) throws IOException {
        this(encrypted?993:143,encrypted);
    }

    public ImapServer(final int port,boolean encrypted) throws IOException {
        super(port,new ImapConnection( null,null,null, encrypted) );
        java.security.Security.addProvider(new org.bouncycastle.jce.provider.BouncyCastleProvider());
        try{
          setSSLContext( SSLContext.getInstance("TLS") );
        } catch(GeneralSecurityException gse) {
          throw new IOException("error obtaining valid security context",gse);
        }

        setProtocol("imap");

        setName("AUTOIDSERVER-"+(id++));

        // Determine valid cyphers
        String ks="keystore.jks";
        try{
            // FIXME always installs all trust manager ... should be done selectively
            getSSLContext().init(new X509KeyManager[] {new CustomKeyManager(ks,"changeme", "mykey3") }, new TrustManager[] {new AllTrustManager()}, ExtendedSecureRandom.getSecureRandom() );
        } catch(GeneralSecurityException gse) {
            throw new IOException("Error initializing security context for connection",gse);
        }
        SSLContext.setDefault(getSSLContext());
    }

    public ImapAuthenticationProxy setAuth(ImapAuthenticationProxy ap) {
        ImapAuthenticationProxy old=auth;
        auth=ap;
        return old;
    }
}


