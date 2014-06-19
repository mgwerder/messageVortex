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
    
    @Test
    public void imapBlankLine() {
        try{
            new ImapLine(null,"",null);
            assertTrue("Blank Line Exception not rised",true);
        } catch(ImapBlankLineException ble) {
            assertTrue("Blank Line Exception rised",true);
        } catch (ImapException ie) {
            assertTrue("Imap Exception rised (should have been ImaplBlankLineException",false);
        }
    }
    
    @Test
    public void imapTagOnly() {
        try{
            new ImapLine(null,"a",null);
            assertTrue("ImapException not rised",false);
        } catch(ImapBlankLineException ble) {
            assertTrue("Blank Line Exception rised",false);
        } catch (ImapException ie) {
            assertTrue("ImapException rised",true);
        }
    }
    
    @Test
    public void imapBlankLineStream() {
        try{
            InputStream i=new ByteArrayInputStream("".getBytes());
            new ImapLine(null,"",i);
            assertTrue("Blank Line Exception not rised",false);
        } catch(ImapBlankLineException ble) {
            assertTrue("Blank Line Exception rised",true);
        } catch (ImapException ie) {
            assertTrue("Imap Exception rised (should have been ImaplBlankLineException)",false);
        }
    }
    
    @Test
    public void imapNonBlankLineNullStream() {
        try{
            new ImapLine(null,"",null);
            assertTrue("Blank Line Exception not rised",true);
        } catch(ImapBlankLineException ble) {
            assertTrue("Blank Line Exception rised",true);
        } catch (ImapException ie) {
            assertTrue("Imap Exception rised (should have been ImaplBlankLineException)",false);
        }
    }
    
    @Test
    public void imapNonBlankLineStream() {
        try{
            InputStream i=new ByteArrayInputStream("a b".getBytes());
            ImapLine il=new ImapLine(null,"",i);
            assertTrue("Returned tag should be \"a\"","a".equals(il.getTag()));
            assertTrue("Returned command should be \"b\" but is infact \""+il.getCommand()+"\"","b".equals(il.getCommand()));
        } catch(ImapBlankLineException ble) {
            assertTrue("Blank Line Exception rised",false);
        } catch (ImapException ie) {
            assertTrue("Imap Exception rised (no exception expected)",false);
        }
    }
    
    @Test
    public void imapNullLine() {
        try{
            InputStream i=new ByteArrayInputStream("a b".getBytes());
            ImapLine il=new ImapLine(null,null,null);
            assertTrue("Should not reach this point as an exception should be rised",false);
        } catch(ImapBlankLineException ble) {
            assertTrue("Blank Line Exception rised",false);
        } catch (ImapException ie) {
            assertTrue("Imap Exception rised",true);
        }
    }
    
    @Test
    public void imapNullStringNonBlankLineStream() {
        try{
            InputStream i=new ByteArrayInputStream("a b".getBytes());
            ImapLine il=new ImapLine(null,null,i);
            assertTrue("Returned tag should be \"a\"","a".equals(il.getTag()));
            assertTrue("Returned command should be \"b\" but is infact \""+il.getCommand()+"\"","b".equals(il.getCommand()));
        } catch(ImapBlankLineException ble) {
            assertTrue("Blank Line Exception rised",false);
        } catch (ImapException ie) {
            assertTrue("Imap Exception rised (no exception expected)",false);
        }
    }
    
    @Test
    public void imapLineSpacing() {
        try{
            InputStream i=new ByteArrayInputStream(" b".getBytes());
            ImapLine il=new ImapLine(null,"a",i);
            assertTrue("ImapException not rised",true);
        } catch(ImapBlankLineException ble) {
            assertTrue("ImapBlankLineException rised (no exception expected)",false);
        } catch (ImapException ie) {
            assertTrue("Imap Exception rised",false);
        }
    }

}