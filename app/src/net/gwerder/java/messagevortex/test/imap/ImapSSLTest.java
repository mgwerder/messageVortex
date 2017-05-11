package net.gwerder.java.messagevortex.test.imap;

import net.gwerder.java.messagevortex.ExtendedSecureRandom;
import net.gwerder.java.messagevortex.MessageVortexLogger;
import net.gwerder.java.messagevortex.imap.*;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import javax.net.ssl.*;
import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.Charset;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;


/**
 * Tests for {@link net.gwerder.java.messagevortex.MailVortex}.
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
            final SSLContext context=SSLContext.getInstance("TLS");
            String ks="keystore.jks";
            assertTrue("Keystore check",(new File(ks)).exists());
            context.init(new X509KeyManager[] {new CustomKeyManager(ks,"changeme", "mykey3") }, new TrustManager[] {new AllTrustManager()}, esr.getSecureRandom() );
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
                    s.close();
                    serverSocket.close();
                    serverSocket=null;
                    t.shutdown();
                    LOGGER.log(Level.INFO,"Cipher suite \""+arr[i]+"\" seems to be supported");
                } catch(SSLException e) {
                    LOGGER.log(Level.FINER,"Cipher suite \""+arr[i]+"\" seems to be unsupported",e);
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
            ((SSLServerSocket)(ss)).setEnabledCipherSuites(suppCiphers.toArray(new String[0]));
            (new Thread() {
                public void run() {
                    try{
                        SSLContext.setDefault(context);
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
                        fail("Exception rised in server ("+ioe+") while communicating");
                    }
                }
            }).start();

            ImapClient ic =new ImapClient("localhost",ss.getLocalPort(),true);
            ic.setTimeout(2000);
            ic.sendCommand("a1 test");
            assertTrue("check client socket state",ic.isTLS());
            ic.shutdown();

            // Selftest
            // This selftest ought to be removed Socket s=SSLSocketFactory.getDefault().createSocket(InetAddress.getByString("localhost"),ss.getLocalPort());
            assertTrue("error searching for hangig threads",verifyHangingThreads(threadSet).size()==0);
        } catch(Exception ioe) {
            LOGGER.log(Level.WARNING,"Unexpected Exception",ioe);
            fail("Exception rised  in client("+ioe+") while communicating");
        }
    }

    protected static Set<Thread> getThreadList() {
        return Thread.getAllStackTraces().keySet();
    }

    protected static Set<Thread> verifyHangingThreads(Set<Thread> pThread) {
        Set<Thread> cThread = Thread.getAllStackTraces().keySet();
        cThread.removeAll(pThread);
        for(Thread t:cThread) {
            LOGGER.log(Level.INFO,"Error got new thread "+t.getName());
        }
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
            ImapServer is=new ImapServer(0,true);
            LOGGER.log(Level.INFO,"setting up pseudo client");
            final Socket s=SSLSocketFactory.getDefault().createSocket(InetAddress.getByName("localhost"),is.getPort());
            s.setSoTimeout(500);
            LOGGER.log(Level.INFO,"sending command");
            s.getOutputStream().write("a1 capability\r\n".getBytes(Charset.defaultCharset()));
            byte b[]=new byte[7];
            int start=0;
            int len=b.length;
            int numread=0;
            while(len>0) {
                numread=s.getInputStream().read(b,start,len);
                if(numread>0) {
                    start+=numread;
                    len-=numread;
                    numread=0;
                }
            }
            LOGGER.log(Level.INFO,"got sequence \""+(new String(b,java.nio.charset.Charset.defaultCharset()))+"\"");
            LOGGER.log(Level.INFO,"done");
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
            ImapServer is=new ImapServer(0,true);
            ImapClient ic=new ImapClient("localhost",is.getPort(),true);
            ImapClient.setDefaultTimeout(300);
            String[] s=ic.sendCommand("a1 capability\r\n");
            for(String v:s) {
                LOGGER.log(Level.INFO,"IMAP<- C: "+ImapLine.commandEncoder(v));
            }
            LOGGER.log(Level.INFO,"closing server");
            is.shutdown();
            LOGGER.log(Level.INFO,"closing client");
            ic.shutdown();
            LOGGER.log(Level.INFO,"done");
            assertTrue("error searching for hangig threads",verifyHangingThreads(threadSet).size()==0);
        } catch(Exception ioe) {
            LOGGER.log(Level.WARNING,"Unexpected Exception",ioe);
            fail("Exception rised  in client("+ioe+") while communicating");
        }
    }
}
