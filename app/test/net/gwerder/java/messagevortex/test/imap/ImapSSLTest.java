package net.gwerder.java.messagevortex.test.imap;
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
import net.gwerder.java.messagevortex.transport.imap.ImapClient;
import net.gwerder.java.messagevortex.transport.imap.ImapLine;
import net.gwerder.java.messagevortex.transport.imap.ImapServer;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import javax.net.ssl.*;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.security.KeyStore;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;

import static net.gwerder.java.messagevortex.test.transport.SMTPTransportSenderTest.CRLF;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;


/**
 * Tests for {@link net.gwerder.java.messagevortex.MessageVortex}.
 *
 * @author martin@gwerder.net (Martin GWERDER)
 */
@RunWith(JUnit4.class)
public class ImapSSLTest {

    private static final java.util.logging.Logger LOGGER;
    private static final ExtendedSecureRandom esr=new ExtendedSecureRandom();

    static {
        LOGGER = MessageVortexLogger.getLogger((new Throwable()).getStackTrace()[0].getClassName());
        MessageVortexLogger.setGlobalLogLevel(Level.ALL);
    }

    @Test
    public void testInitalSSLClient() {
        try{
            LOGGER.log(Level.INFO,"************************************************************************");
            LOGGER.log(Level.INFO,"Testing SSL handshake by client");
            LOGGER.log(Level.INFO,"************************************************************************");

            String ks="keystore.jks";
            assertTrue("Keystore check",(new File(ks)).exists());
            KeyStore keyStore = KeyStore.getInstance(KeyStore.getDefaultType());
            FileInputStream f = new FileInputStream(ks);
            keyStore.load( f, "changeme".toCharArray() );
            f.close();
            TrustManagerFactory tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
            tmf.init(keyStore);
            final SSLContext context = SSLContext.getInstance("TLS");
            KeyManagerFactory kmf = KeyManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
            kmf.init(keyStore,"changeme".toCharArray());
            //context.init(null, tmf.getTrustManagers(), null);
            //context.init(new X509KeyManager[] {new CustomKeyManager(ks,"changeme", "mykey3") }, new TrustManager[] {new AllTrustManager()}, esr.getSecureRandom() );
            context.init( kmf.getKeyManagers(), tmf.getTrustManagers(), esr.getSecureRandom() );
            SSLContext.setDefault(context);

            Set<String> suppCiphers=new HashSet<>();
            String[] arr=((SSLServerSocketFactory) SSLServerSocketFactory.getDefault()).getSupportedCipherSuites();
            LOGGER.log(Level.FINE,"Detecting supported cipher suites");
            Set<Thread> threadSet = getThreadList();
            for(int i=0; i<arr.length; i++) {
                boolean supported=true;
                ServerSocket serverSocket=null;
                try{
                    serverSocket = SSLServerSocketFactory.getDefault().createServerSocket(0);
                    ((SSLServerSocket)serverSocket).setEnabledCipherSuites(new String[] {arr[i]});
                    SocketDeblocker t=new SocketDeblocker(serverSocket.getLocalPort(),30);
                    t.start();
                    SSLSocket s=(SSLSocket)serverSocket.accept();
                    s.setSoTimeout( 1000 );
                    s.close();
                    serverSocket.close();
                    serverSocket=null;
                    t.shutdown();
                    LOGGER.log(Level.INFO,"Cipher suite \""+arr[i]+"\" seems to be supported");
                } catch(SSLException e) {
                    LOGGER.log(Level.INFO,"Cipher suite \""+arr[i]+"\" seems to be unsupported",e);
                    supported=false;
                    try{
                        serverSocket.close();
                    } catch(Exception e2) {
                        LOGGER.log(Level.FINEST,"cleanup failed (never mind)",e2);
                    }
                    serverSocket=null;
                }
                if(supported) {
                    suppCiphers.add(arr[i]);
                }
            }
            final ServerSocket ss=SSLServerSocketFactory.getDefault().createServerSocket(0);
            ((SSLServerSocket)(ss)).setEnabledCipherSuites(suppCiphers.toArray(new String[suppCiphers.size()]));
            (new Thread() {
                public void run() {
                    try{
                        SSLContext.setDefault( context );
                        LOGGER.log(Level.INFO,"pseudoserver waiting for connect");
                        Socket s=ss.accept();
                        LOGGER.log(Level.INFO,"pseudoserver waiting for command");
                        s.getInputStream().skip(9);
                        LOGGER.log(Level.INFO,"pseudoserver sending reply");
                        s.getOutputStream().write("a1 OK\r\n".getBytes(Charset.defaultCharset()));
                        LOGGER.log(Level.INFO,"pseudoserver closing");
                        s.close();
                        ss.close();
                    } catch(IOException ioe) {
                        LOGGER.log(Level.WARNING,"Unexpected Exception",ioe);
                        fail("Exception risen in server ("+ioe+") while communicating");
                    }
                }
            }).start();

            ImapClient ic =new ImapClient(new InetSocketAddress( "localhost", ss.getLocalPort() ), new SecurityContext( context,SecurityRequirement.UNTRUSTED_SSLTLS ) );
            ic.setTimeout(1000);
            ic.connect();
            ic.sendCommand("a1 test");
            assertTrue("check client socket state",ic.isTLS());
            ic.shutdown();

            // Self test
            Thread.sleep(700);
            assertTrue("error searching for hangig threads",verifyHangingThreads(threadSet).size()==0);
        } catch(Exception ioe) {
            LOGGER.log(Level.WARNING,"Unexpected Exception",ioe);
            fail("Exception rised  in client("+ioe+") while communicating");
        } finally {
            MessageVortexLogger.flush();
        }
    }

    protected static Set<Thread> getThreadList() {
        Set<Thread> cThread = Thread.getAllStackTraces().keySet();
        ArrayList<Thread> al=new ArrayList<>();
        for(Thread t:cThread) {
            if( ! t.isAlive() || t.isDaemon() || t.getState() == Thread.State.TERMINATED ) {
                al.add(t);
            }
        }
        cThread.removeAll(al);
        return cThread;
    }

    protected static Set<Thread> verifyHangingThreads(Set<Thread> pThread) {
        Set<Thread> cThread = Thread.getAllStackTraces().keySet();
        cThread.removeAll(pThread);
        ArrayList<Thread> al=new ArrayList<>();
        for(Thread t:cThread) {
            if( t.isAlive() && ! t.isDaemon() && t.getState() != Thread.State.TERMINATED && ! pThread.contains(t) ) {
                LOGGER.log( Level.SEVERE, "Error got new thread " + t.getName() );
            } else {
                al.add(t);
            }
        }
        cThread.removeAll(al);
        return cThread;
    }

    @Test
    public void testInitalSSLServer() {
        try{
            LOGGER.log(Level.INFO,"************************************************************************");
            LOGGER.log(Level.INFO,"Testing SSL handshake by server");
            LOGGER.log(Level.INFO,"************************************************************************");
            LOGGER.log(Level.INFO,"setting up server");
            Set<Thread> threadSet = getThreadList();

            final SSLContext context=SSLContext.getInstance("TLS");
            String ks="keystore.jks";
            assertTrue("Keystore check",(new File(ks)).exists());
            context.init(new X509KeyManager[] {new CustomKeyManager(ks,"changeme", "mykey3") }, new TrustManager[] {new AllTrustManager()}, esr.getSecureRandom() );
            SSLContext.setDefault(context);

            SecurityContext secContext = new SecurityContext( context,SecurityRequirement.UNTRUSTED_SSLTLS );
            ImapServer is=new ImapServer(0, secContext );
            is.setTimeout(4000);
            LOGGER.log(Level.INFO,"setting up pseudo client");
            Socket s=SSLSocketFactory.getDefault().createSocket(InetAddress.getByName("localhost"),is.getPort());
            s.setSoTimeout(4000);
            LOGGER.log(Level.INFO,"sending command to  port "+is.getPort() );
            s.getOutputStream().write("a1 capability\r\n".getBytes(StandardCharsets.UTF_8));
            s.getOutputStream().flush();
            LOGGER.log( Level.INFO,"sent... waiting for reply" );
            StringBuilder sb = new StringBuilder();
            int start=0;
            while( !sb.toString().endsWith( "a1 OK" + CRLF ) ) {
                byte[] b=new byte[1];
                int numread=s.getInputStream().read(b,0,b.length);
                if(numread>0) {
                    start+=numread;
                    sb.append( (char)(b[0]) );
                    LOGGER.log( Level.INFO,"got "+start+" bytes ("+sb.toString()+")" );
                }
            }
            LOGGER.log(Level.INFO,"got sequence \""+sb.toString()+"\"");
            s.close();
            is.shutdown();
            assertTrue("error searching for hangig threads",verifyHangingThreads(threadSet).size()==0);
        } catch(Exception ioe) {
            LOGGER.log(Level.WARNING,"Unexpected Exception",ioe);
            fail("Exception rised  in client("+ioe+") while communicating");
        }
    }

    @Test
    public void testInitalSSLBoth() {
        try{
            LOGGER.log(Level.INFO,"************************************************************************");
            LOGGER.log(Level.INFO,"Testing initial SSL handshake for both components");
            LOGGER.log(Level.INFO,"************************************************************************");
            Set<Thread> threadSet = getThreadList();
            final SSLContext context = SSLContext.getInstance("TLS");
            String ks="keystore.jks";
            assertTrue("Keystore check",(new File(ks)).exists());
            context.init(new X509KeyManager[] {new CustomKeyManager(ks,"changeme", "mykey3") }, new TrustManager[] {new AllTrustManager()}, esr.getSecureRandom() );
            ImapServer is=new ImapServer(0,new SecurityContext(context,SecurityRequirement.UNTRUSTED_SSLTLS));
            is.setTimeout(5000);
            ImapClient ic=new ImapClient( new InetSocketAddress( "localhost", is.getPort() ), new SecurityContext( context,SecurityRequirement.UNTRUSTED_SSLTLS) );
            ic.setTimeout(5000);
            ImapClient.setDefaultTimeout(300);
            ic.connect();
            String[] s=ic.sendCommand("a1 capability");
            for(String v:s) {
                LOGGER.log(Level.INFO,"IMAP<- C: "+ImapLine.commandEncoder(v));
            }
            LOGGER.log(Level.INFO,"closing server");
            is.shutdown();
            LOGGER.log(Level.INFO,"closing client");
            ic.shutdown();
            LOGGER.log(Level.INFO,"done");
            Thread.sleep(300);
            assertTrue("error searching for hangig threads",verifyHangingThreads(threadSet).size()==0);
        } catch(Exception ioe) {
            LOGGER.log(Level.WARNING,"Unexpected Exception",ioe);
            ioe.printStackTrace();
            fail("Exception rised  in client("+ioe+") while communicating");
        }
    }
}
