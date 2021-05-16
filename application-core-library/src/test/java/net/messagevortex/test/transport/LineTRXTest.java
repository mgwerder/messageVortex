package net.messagevortex.test.transport;

import net.messagevortex.ExtendedSecureRandom;
import net.messagevortex.MessageVortexLogger;
import net.messagevortex.test.GlobalJunitExtension;
import net.messagevortex.test.transport.imap.ImapSSLTest;
import net.messagevortex.transport.AllTrustManager;
import net.messagevortex.transport.ClientConnection;
import net.messagevortex.transport.CustomKeyManager;
import net.messagevortex.transport.ListeningSocketChannel;
import net.messagevortex.transport.SecurityContext;
import net.messagevortex.transport.ServerConnection;
import net.messagevortex.transport.SocketListener;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.parallel.ResourceAccessMode;
import org.junit.jupiter.api.parallel.ResourceLock;
import org.junit.jupiter.api.parallel.Resources;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLServerSocket;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509KeyManager;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;


@ExtendWith(GlobalJunitExtension.class)
public class LineTRXTest {

    private static final ExtendedSecureRandom esr=new ExtendedSecureRandom();
    private static final Logger LOGGER;
    static {
        LOGGER = MessageVortexLogger.getLogger((new Throwable()).getStackTrace()[0].getClassName());
        java.security.Security.addProvider(new org.bouncycastle.jce.provider.BouncyCastleProvider());
        ClientConnection.setDefaultTimeout( 10000 );
    }

    private class Listener implements SocketListener {

        public volatile int numConnects = 0;

        @Override
        public void gotConnect(ServerConnection ac) {
            LOGGER.log(Level.INFO, "listener gotConnection()" );
            numConnects++;
        }
    }

    private class SenderThread extends Thread {

        ServerSocket s;
        String[] sText;
        String[] rText;
        boolean encrypted;

        public SenderThread( String sText, String rText, boolean encrypted ) throws IOException {
            if( encrypted ) {
                try {
                    KeyStore trustStore = KeyStore.getInstance( "JKS" );
                    try(InputStream is=this.getClass().getClassLoader().getResourceAsStream("keystore.jks") ) {
                        trustStore.load(is, "changeme".toCharArray());
                    }
                    TrustManagerFactory trustFactory =  TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
                    trustFactory.init(trustStore);
                    TrustManager[] trustManagers = trustFactory.getTrustManagers();
                    KeyManagerFactory kmf = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
                    kmf.init( trustStore, "changeme".toCharArray() );
                    SSLContext trustContext = SSLContext.getInstance("TLS");
                    trustContext.init(kmf.getKeyManagers(), trustManagers, null);
                    s = trustContext.getServerSocketFactory().createServerSocket(0);
                    ((SSLServerSocket)s).setWantClientAuth( false );
                    ((SSLServerSocket)s).setNeedClientAuth( false );
                } catch (Exception e) {
                    throw new IOException( "got exception while creating socket", e );
                }
            } else {
                s = new ServerSocket(0);
            }
            this.sText = new String[] { sText };
            this.rText = new String[] { rText };
            this.encrypted = encrypted;
        }

        public int getLocalPort() {
            return s.getLocalPort();
        }

        private boolean fail = false;
        public void run() {
            try {
                LOGGER.log(Level.INFO, "  Server Thread got connection");
                Socket so = s.accept();
                for( int i = 0; i<sText.length || i<rText.length; i++ ) {
                    if ( i<rText.length && rText[i] != null ) {
                        LOGGER.log(Level.INFO, "  Server Thread waits for text \"" + rText[i]  + "\"");
                        byte[] b = new byte[ rText[i].getBytes( StandardCharsets.UTF_8 ).length ];
                        int bytesRead=0;
                        while(bytesRead<b.length) {
                            bytesRead+=so.getInputStream().read(b, bytesRead, b.length - bytesRead);
                        }
                        String text = new String( b, StandardCharsets.UTF_8 );
                        if( ! rText[i].equals( text ) ) {
                            // abort test
                            LOGGER.log(Level.WARNING, "  Server Thread got wrong text \"" + text + "\"");
                            fail = true;
                            break;
                        } else {
                            LOGGER.log(Level.INFO, "  Server Thread got correct text \"" + text + "\"");
                        }
                    }
                    if ( i<sText.length && sText[i] != null ) {
                        LOGGER.log(Level.INFO, "  Server Thread sends text \"" + sText[i] + "\"");
                        so.getOutputStream().write(sText[i].getBytes(StandardCharsets.UTF_8));
                        so.setTcpNoDelay( true );
                        so.getOutputStream().flush();
                        so.setTcpNoDelay( false );
                    }
                }
                LOGGER.log(Level.INFO, "  Server Thread closes connection");
                while(true) {
                    try {
                        Thread.sleep(200);
                        break;
                    } catch (InterruptedException ie) {
                        // safe to ignore
                    }
                }
                so.getOutputStream().close();
                so.close();
                s.close();
                LOGGER.log(Level.INFO, "  Server Thread closed connection");
            } catch (IOException ioe) {
                ioe.printStackTrace();
                fail = true;
            }
        }

        public void isFailed() {
            if(fail) {
                Assertions.fail("failure in sender thread");
            }
            try {
                this.join();
            } catch( InterruptedException ie) {
                // safe to ignore
            }
        }
    }

    @Test
    @ResourceLock(value = Resources.SYSTEM_PROPERTIES, mode = ResourceAccessMode.READ_WRITE)
    public void plainConnectionTest() {
        Set<Thread> threadSet = ImapSSLTest.getThreadList();
        LOGGER.log(Level.INFO, "**************************************************************" );
        LOGGER.log(Level.INFO, "*** Plain connection test" );
        LOGGER.log(Level.INFO, "**************************************************************" );
        try {
            LOGGER.log(Level.INFO, "Testing a plain connect");
            // creating a listening Socket which is immediately closing
            SenderThread t = new SenderThread( null, null,false );
            t.start();

            LOGGER.log(Level.INFO, "  creating client connection");
            ClientConnection ss = new ClientConnection( new InetSocketAddress( "127.0.0.1", t.getLocalPort() ), null );

            LOGGER.log(Level.INFO, "  client connecting");
            ss.connect();

            LOGGER.log(Level.INFO, "  initiating client shutdown");
            ss.shutdown();

            LOGGER.log(Level.INFO, "  client shutdown completed");
            t.isFailed();

        } catch(IOException ioe) {
            ioe.printStackTrace();
            Assertions.fail("got IOException while handling the client side");
        }
        Assertions.assertTrue(ImapSSLTest.verifyHangingThreads(threadSet).size() == 0, "error searching for hangig threads");
    }

    @Test
    @ResourceLock(value = Resources.SYSTEM_PROPERTIES, mode = ResourceAccessMode.READ_WRITE)
    public void plainConnectionReadTest() {
        Set<Thread> threadSet = ImapSSLTest.getThreadList();
        LOGGER.log(Level.INFO, "**************************************************************" );
        LOGGER.log(Level.INFO, "*** Plain connection read test" );
        LOGGER.log(Level.INFO, "**************************************************************" );
        try {
            for( final String test: new String[] {"Hello I am your Server", "AEIOUÄÖÜ", "\0\n\r" } ) {
                LOGGER.log(Level.INFO, "Testing a plain connect with \"" + test + "\"" );
                // creating a listening Socket which sends one string
                SenderThread t = new SenderThread( test, null,false );
                t.start();

                LOGGER.log(Level.INFO, "  creating client connection");
                ClientConnection ss = new ClientConnection(new InetSocketAddress("127.0.0.1", t.getLocalPort()), null);

                LOGGER.log(Level.INFO, "  client connecting");
                ss.connect();

                LOGGER.log(Level.INFO, "  client reading");
                String result = ss.read( 1000 );
                LOGGER.log(Level.INFO, "  client got \"" + result +"\"" );
                Assertions.assertTrue(result == null ? false : test.equals(result), "failed to read text (result=" + result + "; expected=" + test + ")");

                LOGGER.log(Level.INFO, "  initiating client shutdown");
                ss.shutdown();

                LOGGER.log(Level.INFO, "  client shutdown completed");
                t.isFailed();

            }
        } catch(Exception ioe) {
            ioe.printStackTrace();
            Assertions.fail("got exception while handling the client side");
        }
        Assertions.assertTrue(ImapSSLTest.verifyHangingThreads(threadSet).size() == 0, "error searching for hangig threads");
    }

    @Test
    @ResourceLock(value = Resources.SYSTEM_PROPERTIES, mode = ResourceAccessMode.READ_WRITE)
    public void plainConnectionWriteTest() {
        Set<Thread> threadSet = ImapSSLTest.getThreadList();
        LOGGER.log(Level.INFO, "**************************************************************" );
        LOGGER.log(Level.INFO, "*** Plain connection write test" );
        LOGGER.log(Level.INFO, "**************************************************************" );
        try {
            for( final String test: new String[] {"Hello I am your Server", "AEIOUÄÖÜ", "\0\n\r" } ) {
                LOGGER.log(Level.INFO, "Testing a plain connect with \"" + test + "\"" );
                // creating a listening Socket which sends one string
                SenderThread t = new SenderThread( null, test, false );
                t.start();

                LOGGER.log(Level.INFO, "  creating client connection");
                ClientConnection ss = new ClientConnection(new InetSocketAddress("127.0.0.1", t.getLocalPort()), null);

                LOGGER.log(Level.INFO, "  client connecting");
                ss.connect();

                LOGGER.log(Level.INFO, "  client writing");
                ss.write( test );

                LOGGER.log(Level.INFO, "  initiating client shutdown");
                ss.shutdown();

                LOGGER.log(Level.INFO, "  client shutdown completed");
                t.isFailed();

            }
        } catch(IOException ioe) {
            ioe.printStackTrace();
            Assertions.fail("got IOException while handling the client side");
        }
        Assertions.assertTrue(ImapSSLTest.verifyHangingThreads(threadSet).size() == 0, "error searching for hangig threads");
    }

    @Test
    @ResourceLock(value = Resources.SYSTEM_PROPERTIES, mode = ResourceAccessMode.READ_WRITE)
    public void plainConnectionReadLineTest() {
        Set<Thread> threadSet = ImapSSLTest.getThreadList();
        LOGGER.log(Level.INFO, "**************************************************************" );
        LOGGER.log(Level.INFO, "*** Plain connection readline test" );
        LOGGER.log(Level.INFO, "**************************************************************" );
        try {
            LOGGER.log(Level.INFO, "Testing a plain connect with " );
            // creating a listening Socket which is immediately closing
            SenderThread t = new SenderThread( "line1\r\nline2\r\n", null,false );
            t.start();

            LOGGER.log(Level.INFO, "  creating client connection");
            ClientConnection ss = new ClientConnection(new InetSocketAddress("127.0.0.1", t.getLocalPort()), null);

            LOGGER.log(Level.INFO, "  client connecting");
            ss.connect();

            String result = ss.readln();
            Assertions.assertTrue(result == null ? false : "line1".equals(result), "failed to read text (result: " + result + ")");

            result = ss.readln();
            Assertions.assertTrue(result == null ? false : "line2".equals(result), "failed to read text (result: " + result + ")");

            LOGGER.log(Level.INFO, "  initiating client shutdown");
            ss.shutdown();

            LOGGER.log(Level.INFO, "  client shutdown completed");
            t.isFailed();
        } catch(Exception ioe) {
            ioe.printStackTrace();
            Assertions.fail("got exception while handling the client side");
        }
        Assertions.assertTrue(ImapSSLTest.verifyHangingThreads(threadSet).size() == 0, "error searching for hangig threads");
    }

    @Test
    @ResourceLock(value = Resources.SYSTEM_PROPERTIES, mode = ResourceAccessMode.READ_WRITE)
    public void encryptedConnectionTest() {
        Set<Thread> threadSet = ImapSSLTest.getThreadList();
        try {
            LOGGER.log(Level.INFO, "Testing a encrypted connect");
            // creating a listening Socket which is immediately closing
            SenderThread t = new SenderThread( null, "\0", true );
            t.start();

            // creating SSL context for client
            KeyStore trustStore = KeyStore.getInstance(KeyStore.getDefaultType());
            try(InputStream is=this.getClass().getClassLoader().getResourceAsStream("keystore.jks") ) {
                trustStore.load(is, "changeme".toCharArray());
            }
            KeyManagerFactory kmf = KeyManagerFactory.getInstance("SunX509");
            kmf.init( trustStore, "changeme".toCharArray() );

            TrustManagerFactory trustFactory =  TrustManagerFactory.getInstance("SunX509");
            trustFactory.init(trustStore);

            SSLContext trustContext = SSLContext.getInstance("TLS");
            trustContext.init( kmf.getKeyManagers(), new TrustManager[] { new AllTrustManager() }, new SecureRandom() );

            SecurityContext sc = new SecurityContext();
            sc.setContext(trustContext);

            LOGGER.log(Level.INFO, "  creating client connection");
            ClientConnection ss = new ClientConnection( new InetSocketAddress( "127.0.0.1", t.getLocalPort() ), sc );
            //ClientConnection ss = new ClientConnection( new InetSocketAddress( "www.gwerder.net", 443 ), trustContext );

            LOGGER.log(Level.INFO, "  client connecting");
            ss.setTimeout( 600000 );
            ss.connect();

            LOGGER.log(Level.INFO, "  doing TLS handshake");
            ss.startTls();
            ss.write( "\0" );

            LOGGER.log(Level.INFO, "  initiating client shutdown");
            ss.shutdown();

            LOGGER.log(Level.INFO, "  client shutdown completed");
            t.isFailed();

        } catch(Exception ioe) {
            ioe.printStackTrace();
            Assertions.fail("got Exception while handling the client side");
        }
        Assertions.assertTrue(ImapSSLTest.verifyHangingThreads(threadSet).size() == 0, "error searching for hangig threads");
    }

    @Test
    @ResourceLock(value = Resources.SYSTEM_PROPERTIES, mode = ResourceAccessMode.READ_WRITE)
    public void encryptedConnectionReadTest() {
        Set<Thread> threadSet = ImapSSLTest.getThreadList();
        LOGGER.log(Level.INFO, "**************************************************************" );
        LOGGER.log(Level.INFO, "*** encrypted connection read test" );
        LOGGER.log(Level.INFO, "**************************************************************" );
        try {
            for( String test: new String[] {"Hello I am your Server", "AEIOUÄÖÜ", "\0\n\r" } ) {
                LOGGER.log(Level.INFO, "Testing a encrypted connect with " + test );
                // creating a listening Socket which is immediately closing
                SenderThread t = new SenderThread( test, "\0\r\n", true );
                t.start();

                // creating SSL context for client
                SSLContext c = SSLContext.getInstance("TLS");
                String ks="keystore.jks";
                InputStream stream = this.getClass().getClassLoader().getResourceAsStream(ks);
                Assertions.assertTrue((stream != null), "Keystore check");
                c.init(new X509KeyManager[] {new CustomKeyManager(ks,"changeme", "mykey3") }, new TrustManager[] {new AllTrustManager()}, esr.getSecureRandom() );

                SecurityContext sc = new SecurityContext();
                sc.setContext(c);

                LOGGER.log(Level.INFO, "  creating client connection");
                ClientConnection ss = new ClientConnection(new InetSocketAddress("127.0.0.1", t.getLocalPort()), sc );

                LOGGER.log(Level.INFO, "  client connecting");
                ss.connect();

                LOGGER.log(Level.INFO, "  doing TLS handshake");
                ss.startTls();

                ss.writeln( "\0" );

                LOGGER.log(Level.INFO, "  Reading server reply");
                String result = ss.read();
                LOGGER.log(Level.INFO, "    result is " + result );
                Assertions.assertTrue(result == null ? false : test.equals(result), "failed to read text (result: " + result + ")");

                LOGGER.log(Level.INFO, "  initiating client shutdown");
                ss.shutdown();

                LOGGER.log(Level.INFO, "  client shutdown completed");
                t.isFailed();
            }
        } catch(Exception ioe) {
            ioe.printStackTrace();
            Assertions.fail("got Exception while handling the client side");
        }
        Assertions.assertTrue(ImapSSLTest.verifyHangingThreads(threadSet).size() == 0, "error searching for hangig threads");
    }

    @Test
    @ResourceLock(value = Resources.SYSTEM_PROPERTIES, mode = ResourceAccessMode.READ_WRITE)
    public void lineReceiverPlainTest() throws InterruptedException {
        Set<Thread> threadSet = ImapSSLTest.getThreadList();
        LOGGER.log(Level.INFO, "**************************************************************" );
        LOGGER.log(Level.INFO, "*** line receiver plain test" );
        LOGGER.log(Level.INFO, "**************************************************************" );
        try {
            Listener l=new Listener();
            ListeningSocketChannel listener = new ListeningSocketChannel( new InetSocketAddress( InetAddress.getLoopbackAddress(),0 ),l);
            SSLContext trustContext = SSLContext.getInstance("TLS");
            trustContext.init(null, new TrustManager[]{new AllTrustManager()}, new SecureRandom());
            SecurityContext context = new SecurityContext();
            context.setContext(trustContext);
            ClientConnection t = new ClientConnection(new InetSocketAddress("127.0.0.1", listener.getPort()), context );
            t.connect();
            Thread.sleep( 50 );
            t.shutdown();
            listener.shutdown();
            Assertions.assertTrue(l.numConnects==1, "illegal number of connects (is: "+l.numConnects+")");
        } catch(IOException|NoSuchAlgorithmException |KeyManagementException ioe) {
            ioe.printStackTrace();
            Assertions.fail("got Exception while handling the client side");
        }
        Assertions.assertTrue(ImapSSLTest.verifyHangingThreads(threadSet).size() == 0, "error searching for hangig threads");
    }

}
