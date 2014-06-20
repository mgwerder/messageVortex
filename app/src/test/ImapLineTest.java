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
public class ImapLineTest {    
    
    @Test
    public void charlistHelpers() {
        assertTrue("Error testing chrlistbuilder space"," ".equals(ImapLine.charlistBuilder(32,32)));
        assertTrue("Error testing chrlistbuilder digits","0123456789".equals(ImapLine.charlistBuilder(48,57)));
        assertTrue("Error testing chrlistdifferencer digits","0123568".equals(ImapLine.charlistDifferencer("0123456789","479")));
    }
    
    @Test
    public void getAStringNormal() {
        try{
            ImapLine il=new ImapLine(null,"a b astring1 astring2 3ttti",null);
            String s=il.getAString();
            assertTrue("Error getting astring1 (got \""+s+"\")","astring1".equals(s));
            int skip=il.skipSP(-1);
            assertTrue("Error skipping spaces ("+il.getContext()+"; skip="+skip+")",skip==1);
            s=il.getAString();
            assertTrue("Error getting astring2 (got \""+s+"\")","astring2".equals(s));
            assertTrue("Error skipping spaces",il.skipSP(-1)==1);
            s=il.getAString();
            assertTrue("Error getting \"3ttti\" (got \""+s+"\")","3ttti".equals(s));
        } catch(ImapBlankLineException ble) {
            assertTrue("Blank Line Exception rised",false);
            System.out.flush();ble.printStackTrace();System.out.flush();
        } catch (ImapException ie) {
            assertTrue("ImapException rised"+ie.getMessage()+"("+ie.getStackTrace()[0].getFileName()+":"+ie.getStackTrace()[0].getLineNumber()+")",false);
            System.out.flush();System.err.flush();System.out.println("## ImapExceptionRised in getAStringNormal");ie.printStackTrace();System.out.flush();
        }
    }
    
    @Test
    public void getAStringUgly1() {
        try{
            ImapLine il=new ImapLine(null,"a5 login \"user\\\"\" {4}\r\npass t\r\n",null);
            assertTrue("Error getting tag (got \""+il.getTag()+"\")","a5".equals(il.getTag()));
            assertTrue("Error getting command (got \""+il.getCommand()+"\")","login".equals(il.getCommand()));
            String s=il.getAString();
            assertTrue("Error getting user (got \""+s+"\" at "+il.getContext()+")","user\"".equals(s));
            int skip=il.skipSP(-1);
            assertTrue("Error skipping spaces ("+il.getContext()+"; skip="+skip+")",skip==1);
            s=il.getAString();
            assertTrue("Error getting password (got \""+s+"\")","pass".equals(s));
            assertTrue("Error skipping spaces",il.skipSP(-1)==1);
            s=il.getAString();
            assertTrue("Error getting \"t\" (got \""+s+"\")","t".equals(s));
            assertTrue("Error skipping EOL",il.skipCRLF());
        } catch(ImapBlankLineException ble) {
            assertTrue("Blank Line Exception rised",false);
            System.out.flush();ble.printStackTrace();System.out.flush();
        } catch (ImapException ie) {
            assertTrue("ImapException rised"+ie.getMessage()+"("+ie.getStackTrace()[0].getFileName()+":"+ie.getStackTrace()[0].getLineNumber()+")",false);
            System.out.flush();System.err.flush();System.out.println("## ImapExceptionRised in getAStringNormal");ie.printStackTrace();System.out.flush();
        }
    }
    
    @Test
    public void blankLine() {
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
    public void tagOnly() {
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
    public void blankLineStream() {
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
    public void nonBlankLineNullStream() {
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
    public void nonBlankLineStream() {
        try{
            InputStream i=new ByteArrayInputStream("a b".getBytes());
            ImapLine il=new ImapLine(null,"",i);
            assertTrue("Returned tag should be \"a\"","a".equals(il.getTag()));
            assertTrue("Returned command should be \"b\" but is infact \""+il.getCommand()+"\"","b".equals(il.getCommand()));
            il=new ImapLine(null,"a5 logout",i);
            assertTrue("Returned tag should be \"a5\"","a5".equals(il.getTag()));
            assertTrue("Returned command should be \"logout\" but is infact \""+il.getCommand()+"\"","logout".equals(il.getCommand()));
        } catch(ImapBlankLineException ble) {
            assertTrue("Blank Line Exception rised",false);
        } catch (ImapException ie) {
            assertTrue("Imap Exception rised (no exception expected)",false);
        }
    }
    
    @Test
    public void nullLine() {
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
    public void nullStringNonBlankLineStream() {
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
    public void lineSpacing() {
        try{
            InputStream i=new ByteArrayInputStream(" b".getBytes());
            ImapLine il=new ImapLine(null,"a",i);
            assertTrue("ImapException not rised",true);
            assertTrue("Returned tag should be \"a\"","a".equals(il.getTag()));
            assertTrue("Returned command should be \"b\" but is infact \""+il.getCommand()+"\"","b".equals(il.getCommand()));
        } catch(ImapBlankLineException ble) {
            assertTrue("ImapBlankLineException rised (no exception expected)",false);
            ble.printStackTrace();
            System.out.flush();ble.printStackTrace();System.out.flush();
        } catch (ImapException ie) {
            assertTrue("Imap Exception rised",false);
            System.out.flush();ie.printStackTrace();System.out.flush();
        }
    }
}    