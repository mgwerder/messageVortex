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

/**
 * Tests for {@link net.gwerder.java.mailvortex.imap.ImapCommand}.
 *
 * @author martin@gwerder.net (Martin GWERDER)
 */
@RunWith(JUnit4.class)
public class ImapCommandTest {
    
    static {
        ImapConnection.setDefaultTimeout(1000);
        ImapClient.setDefaultTimeout(1000);
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
    public void checkFullLogout() {
        boolean encrypted=false;
        do{
            try{
                ImapServer s=new ImapServer(0,encrypted);
                ImapClient c=new ImapClient("localhost",s.getPort(),encrypted);
                sendCommand(c,ImapLine.getNextTag()+" LOGOUT");
                s.shutdown();
            } catch (Exception toe) {
                assertTrue("exception thrown ("+toe.toString()+") while testing using encryption="+encrypted,false);
            }
            encrypted=!encrypted;
        } while(!encrypted);
    }
    
    @Test
    public void checkFullLoginLogout() {
        boolean encrypted=false;
        do{
            try{
                ImapServer s=new ImapServer(0,encrypted);
                ImapClient c=new ImapClient("localhost",s.getPort(),encrypted);
                sendCommand(c,ImapLine.getNextTag()+" NOOP");
                sendCommand(c,ImapLine.getNextTag()+" CAPABILITY");
                sendCommand(c,ImapLine.getNextTag()+" LOGIN user password");
                sendCommand(c,ImapLine.getNextTag()+" CAPABILITY");
                sendCommand(c,ImapLine.getNextTag()+" NOOP");
                sendCommand(c,ImapLine.getNextTag()+" LOGOUT");
                s.shutdown();
            } catch (Exception toe) {
                assertTrue("exception thrown ("+toe.toString()+") while testing using encryption="+encrypted,false);
            }
            encrypted=!encrypted;
        } while(!encrypted);
    }
    
}