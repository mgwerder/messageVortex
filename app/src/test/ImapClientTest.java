package net.gwerder.java.mailvortex.test;

import net.gwerder.java.mailvortex.imap.*;

import org.junit.Test;
import org.junit.Ignore;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.junit.Assert;
import java.util.concurrent.TimeoutException;
import static org.junit.Assert.*;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.net.ServerSocket;
import javax.net.SocketFactory;
import java.net.InetAddress;

/**
 * Tests for {@link net.gwerder.java.mailvortex.imap.ImapClient}.
 *
 * @author martin@gwerder.net (Martin GWERDER)
 */
@RunWith(JUnit4.class)
public class ImapClientTest {

    private class DeadSocket implements Runnable {
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
    
    private static  class ImapCommandIWantATimeout extends ImapCommand {
        static void init() {
            ImapCommand.registerCommand(new ImapCommandIWantATimeout());
        }
    
        public String[] processCommand(ImapLine line) {
            do{
                try{
                    Thread.sleep(10000000);
                }catch(InterruptedException ie) {}    
            }while(true);    
        }
    
        public String[] getCommandIdentifier() {
            return new String[] {"IWantATimeout"};
        }
    
    }    

   
    // FIXME test broken imapClient always returns immediately
    @Test
    public void ImapClientTimeoutTest() {
        DeadSocket ds=new DeadSocket(0,-1);
        ImapClient ic =new ImapClient("localhost",ds.getPort(),false);
        assertTrue("No timeoutException was raised",ic.isTLS()==false);
        long start=System.currentTimeMillis();
        ImapCommandIWantATimeout.init();
        try{
            ic.setTimeout(1000);
            System.out.println("Sending IWantATiomeout");for(String s:ic.sendCommand("a0 IWantATimeout",300)) System.out.println("Reply was: "+s);
            assertTrue("No timeoutException was raised",false);
        } catch(TimeoutException te) {
            long el=(System.currentTimeMillis()-start);
            assertTrue("Did not wait until end of timeout was reached (just "+el+")",el>=300);
            assertFalse("Did wait too long",el>=1000);
        }
        try{
            ic.setTimeout(100);
            System.out.println("Sending IWantATiomeout");for(String s:ic.sendCommand("a1 IWantATimeout",300)) System.out.println("Reply was: "+s);
            assertTrue("No timeoutException was raised",false);
        } catch(TimeoutException te) {
            long el=(System.currentTimeMillis()-start);
            assertTrue("Did not wait until end of timeout was reached (just "+el+")",el>=300);
            assertFalse("Did wait too long",el>=1000);
        }
        ImapCommand.deregisterCommand("IWantATimeout");
        ic.shutdown();
        ds.shutdown();
    }

}    