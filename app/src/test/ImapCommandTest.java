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

    private String[] sendCommand(ImapClient c,String command,String reply) {
        try{ 
            System.out.println("IMAP C-> "+command);
            String[] s=c.sendCommand(command+"\r\n");
            for(String v:s) { System.out.print("IMAP<- C: "+v); }; 
            assertTrue("command \""+command+"\" has not been answered properly (expected \""+reply+"\" but got \""+s[s.length-1]+"\")",s[s.length-1].startsWith(reply));
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
                String tag=ImapLine.getNextTag();
                assertTrue("command logut failed BYE-check",sendCommand(c,tag+" LOGOUT",tag+" OK")[0].startsWith("* BYE"));
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
                ImapAuthenticationDummyProxy ap=new ImapAuthenticationDummyProxy();
                ap.addUser("USER","password");
                s.setAuth(ap);
                ImapClient c=new ImapClient("localhost",s.getPort(),encrypted);
                String tag=ImapLine.getNextTag();
                sendCommand(c,tag+" NOOP",tag+" OK");
                tag=ImapLine.getNextTag();
                sendCommand(c,tag+" CAPABILITY",tag+" OK");
                tag=ImapLine.getNextTag();
                sendCommand(c,tag+" LOGIN user password",tag+" OK");
                tag=ImapLine.getNextTag();
                sendCommand(c,tag+" CAPABILITY",tag+" OK");
                tag=ImapLine.getNextTag();
                sendCommand(c,tag+" NOOP",tag+" OK");
                tag=ImapLine.getNextTag();
                sendCommand(c,tag+" LOGOUT",tag+" OK");
                tag=ImapLine.getNextTag();
                s.shutdown();
            } catch (Exception toe) {
                assertTrue("exception thrown ("+toe.toString()+") while testing using encryption="+encrypted,false);
            }
            encrypted=!encrypted;
        } while(!encrypted);
    }
    
}