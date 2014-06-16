package net.gwerder.java.mailvortex.test;

import net.gwerder.java.mailvortex.imap.*;

import org.junit.Test;
import org.junit.Ignore;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.junit.Assert;
import java.util.concurrent.TimeoutException;
import static org.junit.Assert.*;

/**
 * Tests for {@link net.gwerder.java.mailvortex.imap.ImapCommand}.
 *
 * @author martin@gwerder.net (Martin GWERDER)
 */
@RunWith(JUnit4.class)
public class ImapCommandTest {

    /***
     * @known.bug Port should be tested first (Problem when testing subsequent)
     ***/
    private static int port = 200;
    private static Object lock= new Object();
    
    private static int getFreePort() {
        synchronized(lock) {
            port++;
        }    
        return port;
    }

    private String[] sendCommand(ImapClient c,String command) {
        try{ 
            System.out.println("IMAP C-> "+command);
            String[] s=c.sendCommand(command);
            for(String v:s) { System.out.print("IMAP<- C: "+v); }; 
            assertFalse("Logout has not been answered properly",s[0].startsWith((command.split(" "))[0]+" BYE"));
            return s;
        } catch(TimeoutException e) {e.printStackTrace();}
        return null;
    }

    @Test
    public void checkPortAllocator() {
        for(int i=0;i<100;i++) {
            assertTrue("Port allocation does not guarantee uniqueness",getFreePort()!=getFreePort());
        }
    }    

    @Test
    public void checkFullLogout() {
        boolean encrypted=false;
        int lastport=0;
        do{
            try{
                int port = getFreePort();
                assertTrue("Ports did not differ for tests",port!=lastport);
                ImapServer s=new ImapServer(0,encrypted);
                ImapClient c=new ImapClient("localhost",s.getPort(),encrypted);
                sendCommand(c,ImapLine.getNextTag()+" LOGOUT");
                s.shutdown();
                lastport=port;
            } catch (Exception toe) {
                assertTrue("exception thrown ("+toe.toString()+") while testing using encryption="+encrypted,false);
            }
            encrypted=!encrypted;
        } while(!encrypted);
    }

}