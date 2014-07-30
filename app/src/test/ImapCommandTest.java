package net.gwerder.java.mailvortex.test;

import net.gwerder.java.mailvortex.imap.*;
import net.gwerder.java.mailvortex.*;
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

/**
 * Tests for {@link net.gwerder.java.mailvortex.imap.ImapCommand}.
 *
 * @author martin@gwerder.net (Martin GWERDER)
 */
@RunWith(JUnit4.class)
public class ImapCommandTest {

    private final boolean  DO_NOT_TEST_ENCRYPTION=true;
    
    private static final java.util.logging.Logger LOGGER;

    static {
        ImapConnection.setDefaultTimeout(10000);
        ImapClient.setDefaultTimeout(10000);
        MailvortexLogger.setGlobalLogLevel(Level.FINER);
        LOGGER = MailvortexLogger.getLogger((new Throwable()).getStackTrace()[0].getClassName());
    }

    private String[] sendCommand(ImapClient c,String command,String reply) {
        try{ 
            LOGGER.log(Level.FINE,"IMAP C-> "+ImapLine.commandEncoder(command));
            String[] s=c.sendCommand(command);
            for(String v:s) { LOGGER.log(Level.FINE,"IMAP<- C: "+ImapLine.commandEncoder(v)); }; 
            assertTrue("command \""+command+"\" has not been answered properly (expected \""+reply+"\" but got \""+s[s.length-1]+"\")",s[s.length-1].startsWith(reply));
            return s;
        } catch(TimeoutException e) {
            e.printStackTrace();
            fail("got timeout while waiting for reply to command "+command);
        }
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
                c.shutdown();
            } catch (Exception toe) {
                assertTrue("exception thrown ("+toe.toString()+") while testing using encryption="+encrypted,false);
            }
            encrypted=!encrypted;
        } while(encrypted && !DO_NOT_TEST_ENCRYPTION);
    }
    
    @Test
    public void checkFullLoginLogout() {
        boolean encrypted=false;
        do{
            try{
                ImapServer s=new ImapServer(0,encrypted);
                ImapConnection.setDefaultTimeout(10000);
                ImapAuthenticationDummyProxy ap=new ImapAuthenticationDummyProxy();
                ap.addUser("USER","password");
                s.setAuth(ap);
                ImapClient c=new ImapClient("localhost",s.getPort(),encrypted);
                c.setTimeout(10000);
                assertTrue("check encryption ("+encrypted+"/"+c.isTLS()+")", encrypted==c.isTLS());
                String tag=ImapLine.getNextTag();
                String[] ret=sendCommand(c,tag+" NOOP",tag+" OK");
                tag=ImapLine.getNextTag();
                ret=sendCommand(c,tag+" CAPABILITY",tag+" OK");
                tag=ImapLine.getNextTag();
                ret=sendCommand(c,tag+" LOGIN user password",tag+" OK");
                tag=ImapLine.getNextTag();
                ret=sendCommand(c,tag+" CAPABILITY",tag+" OK");
                tag=ImapLine.getNextTag();
                ret=sendCommand(c,tag+" NOOP",tag+" OK");
                tag=ImapLine.getNextTag();
                ret=sendCommand(c,tag+" LOGOUT",tag+" OK");
                s.shutdown();
                c.shutdown();
            } catch (Exception toe) {
                fail("exception thrown ("+toe.toString()+") while testing using encryption="+encrypted+" at "+toe.getStackTrace()[0]);
            }
            encrypted=!encrypted;
        } while(encrypted && !DO_NOT_TEST_ENCRYPTION);
    }
    
}