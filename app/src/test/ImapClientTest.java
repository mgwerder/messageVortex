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
    

    // FIXME test broken imapClient always returns immediately
    @Ignore
    @Test
    public void ImapClientTimeoutTest() {
        DeadSocket ds=new DeadSocket(0,-1);
        ImapClient ic =new ImapClient("localhost",ds.getPort(),false);
        ic.setTimeout(30000);
        ds.shutdown();
    }

}    