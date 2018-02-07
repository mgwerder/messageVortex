package net.gwerder.java.messagevortex.test.imap;

import net.gwerder.java.messagevortex.MessageVortexLogger;
import net.gwerder.java.messagevortex.transport.imap.ImapBlankLineException;
import net.gwerder.java.messagevortex.transport.imap.ImapException;
import net.gwerder.java.messagevortex.transport.imap.ImapLine;
import net.gwerder.java.messagevortex.transport.imap.ImapNullLineException;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.logging.Level;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * Tests for {@link net.gwerder.java.messagevortex.imap.ImapLine}.
 *
 * @author martin@gwerder.net (Martin GWERDER)
 */
@RunWith(JUnit4.class)
public class ImapLineTest {

    private static final java.util.logging.Logger LOGGER;

    static {
        LOGGER = MessageVortexLogger.getLogger((new Throwable()).getStackTrace()[0].getClassName());
        MessageVortexLogger.setGlobalLogLevel(Level.ALL);
    }

    @Test
    public void charlistHelpers() {
        assertTrue("Error testing charlistbuilder space"," ".equals(ImapLine.charlistBuilder(32,32)));
        assertTrue("Error testing charlistbuilder digits","0123456789".equals(ImapLine.charlistBuilder(48,57)));
        assertTrue("Error testing charlistdifferencer digits","0123568".equals(ImapLine.charlistDifferencer("0123456789","479")));
        assertTrue("Error testing charlistbuilder range1",ImapLine.charlistBuilder(-1,32)==null);
        assertTrue("Error testing charlistbuilder range2",ImapLine.charlistBuilder(1,0)==null);
        assertTrue("Error testing charlistbuilder range3",ImapLine.charlistBuilder(1,260)==null);
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
            LOGGER.log(Level.WARNING,"Unexpected Exception",ble);
            fail("Blank Line Exception rised");
        } catch (ImapException ie) {
            LOGGER.log(Level.WARNING,"Unexpected Exception",ie);
            fail("ImapException rised"+ie.getMessage()+"("+ie.getStackTrace()[0].getFileName()+":"+ie.getStackTrace()[0].getLineNumber()+")");
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
            LOGGER.log(Level.WARNING,"Unexpected Exception",ble);
            fail("Blank Line Exception rised");
        } catch (ImapException ie) {
            LOGGER.log(Level.WARNING,"Unexpected Exception",ie);
            fail("ImapException rised"+ie.getMessage()+"("+ie.getStackTrace()[0].getFileName()+":"+ie.getStackTrace()[0].getLineNumber()+")");
       }
    }

    @Test
    public void blankLine() {
        try{
            new ImapLine(null,"",null);
            fail("Blank Line Exception not rised");
        } catch(ImapNullLineException ble) {
            LOGGER.log(Level.WARNING,"Expected Exception",ble);
            assertTrue("Blank Line Exception rised",true);
        } catch (ImapException ie) {
            LOGGER.log(Level.WARNING,"Unexpected Exception",ie);
            fail("Imap Exception rised (should have been ImaplBlankLineException");
        }
    }

    @Test
    public void tagOnly() {
        try{
            new ImapLine(null,"a",null);
            fail("ImapException not rised");
        } catch(ImapBlankLineException ble) {
            LOGGER.log(Level.WARNING,"Unexpected Exception",ble);
            fail("Blank Line Exception rised");
        } catch (ImapException ie) {
            LOGGER.log(Level.WARNING,"Expected Exception",ie);
            assertTrue("ImapException rised",true);
        }
    }

    @Test
    public void blankLineStream() {
        try{
            InputStream i=new ByteArrayInputStream("".getBytes(Charset.defaultCharset()));
            new ImapLine(null,"",i);
            fail("Blank Line Exception not rised");
        } catch(ImapNullLineException ble) {
            // Null Line Exception rised -- this is expected
            assertTrue("Got expected exception",true);
        } catch(ImapBlankLineException ble) {
            LOGGER.log(Level.WARNING,"Unexpected Exception",ble);
            fail("Blank Line Exception rised");
        } catch (ImapException ie) {
            LOGGER.log(Level.WARNING,"Unexpected Exception",ie);
            fail("Imap Exception rised (should have been ImaplBlankLineException)");
        }
    }

    @Test
    public void nonBlankLineNullStream() {
        try{
            new ImapLine(null,"",null);
            fail("Null Line Exception not rised");
        } catch(ImapNullLineException ble) {
            assertTrue("Got expected exception",true);
        } catch(ImapBlankLineException ble) {
            LOGGER.log(Level.WARNING,"Unexpected Exception",ble);
            fail("Blank Line Exception rised");
        } catch (ImapException ie) {
            LOGGER.log(Level.WARNING,"Unexpected Exception",ie);
            fail("Imap Exception rised (should have been ImaplBlankLineException but is \""+ie.toString()+"\")");
        }
    }

    @Test
    public void nonBlankLineNullStream2() {
        try{
            new ImapLine(null,"\r\n",null);
            fail("Blank Line Exception not rised");
        } catch(ImapBlankLineException ble) {
            assertTrue("Got expected exception",true);
        } catch(ImapNullLineException ble) {
            LOGGER.log(Level.WARNING,"Unexpected Exception",ble);
            fail("Null Line Exception rised");
        } catch (ImapException ie) {
            LOGGER.log(Level.WARNING,"Unexpected Exception",ie);
            fail("Imap Exception rised (should have been ImaplBlankLineException)");
        }
    }

    @Test
    public void nonBlankLineStream() {
        try{
            InputStream i=new ByteArrayInputStream("a b".getBytes(Charset.defaultCharset()));
            ImapLine il=new ImapLine(null,"",i);
            assertTrue("Returned tag should be \"a\"","a".equals(il.getTag()));
            assertTrue("Returned command should be \"b\" but is infact \""+il.getCommand()+"\"","b".equals(il.getCommand()));
            il=new ImapLine(null,"a5 logout",i);
            assertTrue("Returned tag should be \"a5\"","a5".equals(il.getTag()));
            assertTrue("Returned command should be \"logout\" but is infact \""+il.getCommand()+"\"","logout".equals(il.getCommand()));
        } catch(ImapBlankLineException ble) {
            LOGGER.log(Level.WARNING,"Unexpected Exception",ble);
            fail("Blank Line Exception rised");
        } catch (ImapException ie) {
            LOGGER.log(Level.WARNING,"Unexpected Exception",ie);
            fail("Imap Exception rised (no exception expected)");
        }
    }

    @Test
    public void nullLine() {
        try{
            InputStream i=new ByteArrayInputStream("a b".getBytes(Charset.defaultCharset()));
            new ImapLine(null,null,i);
        } catch(ImapBlankLineException ble) {
            LOGGER.log(Level.WARNING,"Unexpected Exception",ble);
            fail("Should not reach this point as an Null Line exception should be rised");
        } catch(ImapNullLineException ble) {
            assertTrue("Blank Line Exception rised",true);
        } catch (ImapException ie) {
            LOGGER.log(Level.WARNING,"Unexpected Exception",ie);
            fail("Imap Exception rised");
        }
    }

    @Test
    public void badLine1() {
        try{
            InputStream i=new ByteArrayInputStream("+".getBytes(Charset.defaultCharset()));
            new ImapLine(null,null,i);
            fail("Should not reach this point as an exception should be rised");
        } catch(ImapNullLineException ble) {
            LOGGER.log(Level.WARNING,"Unexpected Exception",ble);
            fail("Blank Line Exception rised");
        } catch (ImapException ie) {
            // all OK this exception is expected
        }
    }

    @Test
    public void badLine2() {
        try{
            InputStream i=new ByteArrayInputStream("a +".getBytes(Charset.defaultCharset()));
            ImapLine il=new ImapLine(null,null,i);
            fail("Should not reach this point as an exception should be rised got ["+il.getTag()+"] ["+il.getCommand()+"]");
        } catch(ImapNullLineException ble) {
            LOGGER.log(Level.WARNING,"Unexpected Exception",ble);
            fail("Blank Line Exception rised");
        } catch (ImapException ie) {
            // all OK this exception is expected
        }
    }

    @Test
    public void goodLine() {
        try{
            InputStream i=new ByteArrayInputStream("a OK\r\n".getBytes(Charset.defaultCharset()));
            new ImapLine(null,null,i);
            assertTrue("Should  reach this point",true);
        } catch(ImapNullLineException ble) {
            LOGGER.log(Level.WARNING,"Unexpected Exception",ble);
            fail("Blank Line Exception rised");
        } catch (ImapException ie) {
            LOGGER.log(Level.WARNING,"Unexpected Exception",ie);
            fail("ImapException rised");
        }
    }

    @Test
    public void nullStringNonBlankLineStream() {
        try{
            InputStream i=new ByteArrayInputStream("a b".getBytes(Charset.defaultCharset()));
            ImapLine il=new ImapLine(null,null,i);
            assertTrue("Returned tag should be \"a\"","a".equals(il.getTag()));
            assertTrue("Returned command should be \"b\" but is infact \""+il.getCommand()+"\"","b".equals(il.getCommand()));
        } catch(ImapBlankLineException ble) {
            LOGGER.log(Level.WARNING,"Unexpected Exception",ble);
            fail("Blank Line Exception rised");
        } catch (ImapException ie) {
            LOGGER.log(Level.WARNING,"Unexpected Exception",ie);
            fail("Imap Exception rised (no exception expected)");
        }
    }

    @Test
    public void lineSpacing() {
        try{
            InputStream i=new ByteArrayInputStream(" b".getBytes(Charset.defaultCharset()));
            ImapLine il=new ImapLine(null,"a",i);
            assertTrue("ImapException not rised",true);
            assertTrue("Returned tag should be \"a\"","a".equals(il.getTag()));
            assertTrue("Returned command should be \"b\" but is infact \""+il.getCommand()+"\"","b".equals(il.getCommand()));
        } catch(ImapBlankLineException ble) {
            LOGGER.log(Level.WARNING,"Unexpected Exception",ble);
            fail("ImapBlankLineException rised (no exception expected)");
        } catch (ImapException ie) {
            LOGGER.log(Level.WARNING,"Unexpected Exception",ie);
            fail("Imap Exception rised");
        }
    }
}
