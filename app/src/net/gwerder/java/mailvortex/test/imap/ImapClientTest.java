package net.gwerder.java.mailvortex.test.imap;

import net.gwerder.java.mailvortex.imap.*;
import net.gwerder.java.mailvortex.MailvortexLogger;
import java.util.logging.Logger;
import java.util.logging.Level;

import org.junit.Test;
import org.junit.Ignore;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.junit.Assert;
import java.util.concurrent.TimeoutException;
import static org.junit.Assert.*;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.IOException;
import java.net.ServerSocket;
import javax.net.SocketFactory;
import java.net.InetAddress;
import java.security.NoSuchAlgorithmException;
import java.security.KeyManagementException;
import java.security.GeneralSecurityException;

/**
 * Tests for {@link net.gwerder.java.mailvortex.imap.ImapClient}.
 *
 * @author martin@gwerder.net (Martin GWERDER)
 */
@RunWith(JUnit4.class)
public class ImapClientTest {

    private static final java.util.logging.Logger LOGGER;

    static {
        ImapConnection.setDefaultTimeout(2000);
        ImapClient.setDefaultTimeout(2000);
        LOGGER = MailvortexLogger.getLogger((new Throwable()).getStackTrace()[0].getClassName());
        MailvortexLogger.setGlobalLogLevel(Level.ALL);
    }

    private static class DeadSocket implements Runnable {
        private boolean shutdown=false;
        private Thread runner=new Thread(this,"Dead socket (init)");
        
        private ServerSocket ss;
        private int counter;
        
        public DeadSocket(int port, int counter) {
            this.counter=counter;
            try{
                ss=new ServerSocket(port,20,InetAddress.getByName("localhost"));
            } catch(Exception e) {}
            runner.setName("DeadSocket (port "+ss.getLocalPort());
            runner.setDaemon(true);
            runner.start();
        }
        
        public void shutdown() {
            // initiate shutdown of runner
            shutdown=true;
            
            // wakeup runner if necesary
            try{
                SocketFactory.getDefault().createSocket("localhost",ss.getLocalPort());
            } catch(Exception e) {}
        
            // Shutdown runner task
            boolean endshutdown=false;
            try{
                runner.join();
            } catch(InterruptedException ie) { }
        }
        
        public int getPort() { return ss.getLocalPort();}
         
        public void run() {
            while(!shutdown) {
                try{
                    ss.accept();
                } catch(Exception sorry) {
                    assertTrue("Exception should not be rised",false);
                }    
                counter--;
                if(counter==0) shutdown=true;
            }
        }        
    }
    
    private static class ImapCommandIWantATimeout extends ImapCommand {
    
        public void init() {
            ImapCommand.registerCommand(this);
        }
    
        public String[] processCommand(ImapLine line) {
            do{
                try{
                    Thread.sleep(100000);
                }catch(InterruptedException ie) {}    
            }while(true);    
        }
    
        public String[] getCommandIdentifier() {
            return new String[] {"IWantATimeout"};
        }
    
        public String[] getCapabilities() {
            return new String[] {};
        }
    }    

    @Test
    public void ImapClientEncryptedTest1() {
        try{
            LOGGER.log(Level.INFO,"************************************************************************");
            LOGGER.log(Level.INFO,"IMAP Client Encrypted Test");
            LOGGER.log(Level.INFO,"************************************************************************");
            ImapServer is =new ImapServer(0,true); 
            ImapConnection.setDefaultTimeout(1000);
            ImapClient ic =new ImapClient("localhost",is.getPort(),true);
            ic.setTimeout(1000);
            assertTrue("TLS is not as expected",ic.isTLS()==true);
        } catch(IOException e) {
            fail("IOException while creating server");
        }
    }
        
    @Test
    public void ImapClientTimeoutTest() {
        LOGGER.log(Level.INFO,"************************************************************************");
        LOGGER.log(Level.INFO,"IMAP Client Timeout Test");
        LOGGER.log(Level.INFO,"************************************************************************");
        DeadSocket ds=new DeadSocket(0,-1);
        ImapClient ic =new ImapClient("localhost",ds.getPort(),false);
        assertTrue("TLS is not as expected",ic.isTLS()==false);
        long start=System.currentTimeMillis();
        (new ImapCommandIWantATimeout()).init();
        try{
            ic.setTimeout(2000);
            System.out.println("Sending IWantATimeout");for(String s:ic.sendCommand("a0 IWantATimeout",300)) System.out.println("Reply was: "+s);
            fail("No timeoutException was raised");
        } catch(TimeoutException te) {
            long el=(System.currentTimeMillis()-start);
            assertTrue("Did not wait until end of timeout was reached (just "+el+")",el>=300);
            assertFalse("Did wait too long",el>2100);
        }
        try{
            ic.setTimeout(100);
            System.out.println("Sending IWantATimeout");for(String s:ic.sendCommand("a1 IWantATimeout",300)) System.out.println("Reply was: "+s);
            fail("No timeoutException was raised");
        } catch(TimeoutException te) {
            long el=(System.currentTimeMillis()-start);
            assertTrue("Did not wait until end of timeout was reached (just "+el+")",el>=300);
            assertFalse("Did wait too long",el>1000);
            // assertTrue("Connection was not terminated",ic.isTerminated());
        }
        ImapCommand.deregisterCommand("IWantATimeout");
        ic.shutdown();
        ds.shutdown();
    }

}    