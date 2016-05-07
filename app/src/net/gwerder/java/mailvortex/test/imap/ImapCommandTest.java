package net.gwerder.java.mailvortex.test.imap;

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

    private final boolean  DO_NOT_TEST_ENCRYPTION=false;
    
    private static final java.util.logging.Logger LOGGER;

    static {
        ImapConnection.setDefaultTimeout(2000);
        ImapClient.setDefaultTimeout(2000);
        LOGGER = MailvortexLogger.getLogger((new Throwable()).getStackTrace()[0].getClassName());
        MailvortexLogger.setGlobalLogLevel(Level.ALL);
    }

    private String[] sendCommand(ImapClient c,String command,String reply) {
        try{ 
            LOGGER.log(Level.INFO,"IMAP C-> "+ImapLine.commandEncoder(command));
            String[] s=c.sendCommand(command);
            for(String v:s) { LOGGER.log(Level.INFO,"IMAP<- C: "+ImapLine.commandEncoder(v)); }; 
            assertTrue("command \""+command+"\" has not been answered properly (expected \""+reply+"\" but got \""+s[s.length-1]+"\")",s[s.length-1].startsWith(reply));
            return s;
        } catch(TimeoutException e) {
            e.printStackTrace();
            fail("got timeout while waiting for reply to command "+command);
        }
        return null;
    }

    @Test
    public void checkSetClientTimeout() {
        try{
            LOGGER.log(Level.INFO,"************************************************************************");
            LOGGER.log(Level.INFO,"Check set client timeout");
            LOGGER.log(Level.INFO,"************************************************************************");
            ImapServer is=new ImapServer(0,false);
            ImapClient ic=new ImapClient("localhost",is.getPort(),false);
            assertTrue("test default Timeout",ImapClient.getDefaultTimeout()==ic.setDefaultTimeout(123));
            assertTrue("test default Timeout",ic.getDefaultTimeout()==123);
            ic.setDefaultTimeout(3600*1000);
            assertTrue("test  Timeout set",ic.getTimeout()==ic.setTimeout(123));
            assertTrue("test  Timeout get",ic.getTimeout()==123);
            ic.setTimeout(3600*1000);
        } catch(Exception e) {
            fail("Exception thrown ("+e+")");
        }
    }
    
    @Test
    public void checkServerTimeout() {
        try{
            LOGGER.log(Level.INFO,"************************************************************************");
            LOGGER.log(Level.INFO,"Check server default timeout");
            LOGGER.log(Level.INFO,"************************************************************************");
            ImapConnection.setDefaultTimeout(300);
            ImapServer is=new ImapServer(0,false);
            ImapClient ic=new ImapClient("localhost",is.getPort(),false);
            ic.sendCommand("a0 IWantATimeout",300);
            ic.setTimeout(3600*1000);
            Thread.sleep(1000);
            fail("Timeout not issued");
        } catch(TimeoutException toe) {
            // Exception reached as planed
        } catch(Exception e) {
            fail("Exception thrown ("+e+")");
        }
        ImapConnection.setDefaultTimeout(10000);
    }
    
    @Test
    public void checkClientTimeout() {
        try{
            LOGGER.log(Level.INFO,"************************************************************************");
            LOGGER.log(Level.INFO,"Check client timeout");
            LOGGER.log(Level.INFO,"************************************************************************");
            ImapServer is=new ImapServer(0,false);
            ImapClient ic=new ImapClient("localhost",is.getPort(),false);
            ic.sendCommand("a0 IWantATimeout",300);
            ic.setTimeout(300);
            Thread.sleep(300);
            ic.sendCommand("a0 IWantATimeout",300);
            Thread.sleep(1000);
            fail("Timeout not issued");
        } catch(TimeoutException toe) {
            // Exception reached as planed
        } catch(Exception e) {
            fail("Exception thrown ("+e+")");
        }
    }
    
    @Test
    public void checkClientDefaultTimeout() {
        try{
            LOGGER.log(Level.INFO,"************************************************************************");
            LOGGER.log(Level.INFO,"Check client default timeout");
            LOGGER.log(Level.INFO,"************************************************************************");
            ImapClient.setDefaultTimeout(300);
            ImapServer is=new ImapServer(0,false);
            ImapClient ic=new ImapClient("localhost",is.getPort(),false);
            Thread.sleep(300);
            ic.sendCommand("a0 IWantATimeout",300);
            Thread.sleep(1000);
            fail("Timeout not issued");
        } catch(TimeoutException toe) {
            // Exception reached as planed
        } catch(Exception e) {
            fail("Exception thrown ("+e+")");
        }
        ImapClient.setDefaultTimeout(10000);
    }
    
    @Test
    public void checkFullLogout() {
        boolean encrypted=false;
        do{
            try{
                ImapServer s=new ImapServer(0,encrypted);
                LOGGER.log(Level.INFO,"************************************************************************");
                LOGGER.log(Level.INFO,"Check full Login Logout ("+encrypted+"/"+s.getName()+")");
                LOGGER.log(Level.INFO,"************************************************************************");
                ImapClient c=new ImapClient("localhost",s.getPort(),encrypted);
                c.setTimeout(2000);
                ImapConnection.setDefaultTimeout(2000);
                String tag=ImapLine.getNextTag();
                assertTrue("command logut failed BYE-check",sendCommand(c,tag+" LOGOUT",tag+" OK")[0].startsWith("* BYE"));
                s.shutdown();
                c.shutdown();
            } catch (Exception toe) {
                fail("exception thrown ("+toe.toString()+") while testing using encryption="+encrypted);
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
                LOGGER.log(Level.INFO,"************************************************************************");
                LOGGER.log(Level.INFO,"Check full Login Logout ("+encrypted+"/"+s.getName()+")");
                LOGGER.log(Level.INFO,"************************************************************************");
                ImapConnection.setDefaultTimeout(2000);
                ImapAuthenticationDummyProxy ap=new ImapAuthenticationDummyProxy();
                ap.addUser("USER","password");
                
                assertFalse("check for fail if user is null",ap.login(null,"a"));
                assertFalse("check for fail if password is null",ap.login("USER",null));
                assertFalse("check for fail if user is unknown",ap.login("USER1","password"));
                assertFalse("check for fail if password is bad",ap.login("USER","password1"));
                assertTrue("check for fail if password is bad",ap.login("USER","password"));
                assertTrue("check for success if username casing does not match",ap.login("User","password"));
                
                s.setAuth(ap);
                ImapClient c=new ImapClient("localhost",s.getPort(),encrypted);
                c.setTimeout(2000);
                assertTrue("check encryption ("+encrypted+"/"+c.isTLS()+")", encrypted==c.isTLS());
                String tag=ImapLine.getNextTag();
                String[] ret=sendCommand(c,tag+" NOOP",tag+" OK");
                tag=ImapLine.getNextTag();
                ret=sendCommand(c,tag+" CAPABILITY",tag+" OK");
                tag=ImapLine.getNextTag();
                if(encrypted) {
                    ret=sendCommand(c,tag+" LOGIN user password",tag+" OK");
                } else {    
                    ret=sendCommand(c,tag+" LOGIN user password",tag+" BAD");
                }
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