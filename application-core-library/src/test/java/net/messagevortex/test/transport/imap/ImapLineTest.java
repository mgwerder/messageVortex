package net.messagevortex.test.transport.imap;

import net.messagevortex.MessageVortexLogger;
import net.messagevortex.test.GlobalJunitExtension;
import net.messagevortex.transport.imap.ImapBlankLineException;
import net.messagevortex.transport.imap.ImapException;
import net.messagevortex.transport.imap.ImapLine;
import net.messagevortex.transport.imap.ImapNullLineException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.logging.Level;

/**
 * Tests for {@link net.messagevortex.transport.imap.ImapLine}.
 *
 * @author martin@gwerder.net (Martin GWERDER)
 */
@ExtendWith(GlobalJunitExtension.class)
public class ImapLineTest {

    private static final java.util.logging.Logger LOGGER;

    static {
        LOGGER = MessageVortexLogger.getLogger((new Throwable()).getStackTrace()[0].getClassName());
    }

    @Test
    public void charlistHelpers() {
        Assertions.assertTrue(" ".equals(ImapLine.charlistBuilder(32,32)), "Error testing charlistbuilder space");
        Assertions.assertTrue("0123456789".equals(ImapLine.charlistBuilder(48,57)), "Error testing charlistbuilder digits");
        Assertions.assertTrue("0123568".equals(ImapLine.charlistDifferencer("0123456789","479")), "Error testing charlistdifferencer digits");
        Assertions.assertTrue(ImapLine.charlistBuilder(-1,32)==null, "Error testing charlistbuilder range1");
        Assertions.assertTrue(ImapLine.charlistBuilder(1,0)==null, "Error testing charlistbuilder range2");
        Assertions.assertTrue(ImapLine.charlistBuilder(1,260)==null, "Error testing charlistbuilder range3");
    }

    @Test
    public void getAStringNormal() {
        try{
            ImapLine il=new ImapLine(null,"a b astring1 astring2 3ttti",null);
            String s=il.getAString();
            Assertions.assertTrue("astring1".equals(s), "Error getting astring1 (got \""+s+"\")");
            int skip=il.skipWhitespace(-1);
            Assertions.assertTrue(skip==1, "Error skipping spaces ("+il.getContext()+"; skip="+skip+")");
            s=il.getAString();
            Assertions.assertTrue("astring2".equals(s), "Error getting astring2 (got \""+s+"\")");
            Assertions.assertTrue(il.skipWhitespace(-1)==1, "Error skipping spaces");
            s=il.getAString();
            Assertions.assertTrue("3ttti".equals(s), "Error getting \"3ttti\" (got \""+s+"\")");
        } catch(ImapBlankLineException ble) {
            LOGGER.log(Level.WARNING,"Unexpected Exception",ble);
            Assertions.fail("Blank Line Exception rised");
        } catch (ImapException ie) {
            LOGGER.log(Level.WARNING,"Unexpected Exception",ie);
            Assertions.fail("ImapException rised"+ie.getMessage()+"("+ie.getStackTrace()[0].getFileName()+":"+ie.getStackTrace()[0].getLineNumber()+")");
        }
    }

    @Test
    public void getAStringUgly1() {
        try{
            ImapLine il=new ImapLine(null,"a5 login \"user\\\"\" {4}\r\npass t\r\n",null);
            Assertions.assertTrue("a5".equals(il.getTag()), "Error getting tag (got \""+il.getTag()+"\")");
            Assertions.assertTrue("login".equals(il.getCommand()), "Error getting command (got \""+il.getCommand()+"\")");
            String s=il.getAString();
            Assertions.assertTrue("user\"".equals(s), "Error getting user (got \""+s+"\" at "+il.getContext()+")");
            int skip=il.skipWhitespace(-1);
            Assertions.assertTrue(skip==1, "Error skipping spaces ("+il.getContext()+"; skip="+skip+")");
            s=il.getAString();
            Assertions.assertTrue("pass".equals(s), "Error getting password (got \""+s+"\")");
            Assertions.assertTrue(il.skipWhitespace(-1)==1, "Error skipping spaces");
            s=il.getAString();
            Assertions.assertTrue("t".equals(s), "Error getting \"t\" (got \""+s+"\")");
            Assertions.assertTrue(il.skipLineEnd(), "Error skipping EOL");
        } catch(ImapBlankLineException ble) {
            LOGGER.log(Level.WARNING,"Unexpected Exception",ble);
            Assertions.fail("Blank Line Exception rised");
        } catch (ImapException ie) {
            LOGGER.log(Level.WARNING,"Unexpected Exception",ie);
            Assertions.fail("ImapException rised"+ie.getMessage()+"("+ie.getStackTrace()[0].getFileName()+":"+ie.getStackTrace()[0].getLineNumber()+")");
       }
    }

    @Test
    public void blankLine() {
        try{
            new ImapLine(null,"",null);
            Assertions.fail("Blank Line Exception not rised");
        } catch(ImapNullLineException ble) {
            LOGGER.log(Level.WARNING,"Expected Exception",ble);
            Assertions.assertTrue(true, "Blank Line Exception rised");
        } catch (ImapException ie) {
            LOGGER.log(Level.WARNING,"Unexpected Exception",ie);
            Assertions.fail("Imap Exception rised (should have been ImaplBlankLineException");
        }
    }

    @Test
    public void tagOnly() {
        try{
            new ImapLine(null,"a",null);
            Assertions.fail("ImapException not rised");
        } catch(ImapBlankLineException ble) {
            LOGGER.log(Level.WARNING,"Unexpected Exception",ble);
            Assertions.fail("Blank Line Exception rised");
        } catch (ImapException ie) {
            LOGGER.log(Level.WARNING,"Expected Exception",ie);
            Assertions.assertTrue(true, "ImapException rised");
        }
    }

    @Test
    public void blankLineStream() {
        try{
            InputStream i=new ByteArrayInputStream("".getBytes(Charset.defaultCharset()));
            new ImapLine(null,"",i);
            Assertions.fail("Blank Line Exception not rised");
        } catch(ImapNullLineException ble) {
            // Null Line Exception rised -- this is expected
            Assertions.assertTrue(true, "Got expected exception");
        } catch(ImapBlankLineException ble) {
            LOGGER.log(Level.WARNING,"Unexpected Exception",ble);
            Assertions.fail("Blank Line Exception rised");
        } catch (ImapException ie) {
            LOGGER.log(Level.WARNING,"Unexpected Exception",ie);
            Assertions.fail("Imap Exception rised (should have been ImaplBlankLineException)");
        }
    }

    @Test
    public void nonBlankLineNullStream() {
        try{
            new ImapLine(null,"",null);
            Assertions.fail("Null Line Exception not rised");
        } catch(ImapNullLineException ble) {
            Assertions.assertTrue(true, "Got expected exception");
        } catch(ImapBlankLineException ble) {
            LOGGER.log(Level.WARNING,"Unexpected Exception",ble);
            Assertions.fail("Blank Line Exception rised");
        } catch (ImapException ie) {
            LOGGER.log(Level.WARNING,"Unexpected Exception",ie);
            Assertions.fail("Imap Exception rised (should have been ImaplBlankLineException but is \""+ ie +"\")");
        }
    }

    @Test
    public void nonBlankLineNullStream2() {
        try{
            new ImapLine(null,"\r\n",null);
            Assertions.fail("Blank Line Exception not rised");
        } catch(ImapBlankLineException ble) {
            Assertions.assertTrue(true, "Got expected exception");
        } catch(ImapNullLineException ble) {
            LOGGER.log(Level.WARNING,"Unexpected Exception",ble);
            Assertions.fail("Null Line Exception rised");
        } catch (ImapException ie) {
            LOGGER.log(Level.WARNING,"Unexpected Exception",ie);
            Assertions.fail("Imap Exception rised (should have been ImaplBlankLineException)");
        }
    }

    @Test
    public void nonBlankLineStream() {
        try{
            InputStream i=new ByteArrayInputStream("a b".getBytes(Charset.defaultCharset()));
            ImapLine il=new ImapLine(null,"",i);
            Assertions.assertTrue("a".equals(il.getTag()), "Returned tag should be \"a\"");
            Assertions.assertTrue("b".equals(il.getCommand()), "Returned command should be \"b\" but is infact \""+il.getCommand()+"\"");
            il=new ImapLine(null,"a5 logout",i);
            Assertions.assertTrue("a5".equals(il.getTag()), "Returned tag should be \"a5\"");
            Assertions.assertTrue("logout".equals(il.getCommand()), "Returned command should be \"logout\" but is infact \""+il.getCommand()+"\"");
        } catch(ImapBlankLineException ble) {
            LOGGER.log(Level.WARNING,"Unexpected Exception",ble);
            Assertions.fail("Blank Line Exception rised");
        } catch (ImapException ie) {
            LOGGER.log(Level.WARNING,"Unexpected Exception",ie);
            Assertions.fail("Imap Exception rised (no exception expected)");
        }
    }

    @Test
    public void nullLine() {
        try{
            InputStream i=new ByteArrayInputStream("a b".getBytes(Charset.defaultCharset()));
            new ImapLine(null,null,i);
        } catch(ImapBlankLineException ble) {
            LOGGER.log(Level.WARNING,"Unexpected Exception",ble);
            Assertions.fail("Should not reach this point as an Null Line exception should be rised");
        } catch(ImapNullLineException ble) {
            Assertions.assertTrue(true, "Blank Line Exception rised");
        } catch (ImapException ie) {
            LOGGER.log(Level.WARNING,"Unexpected Exception",ie);
            Assertions.fail("Imap Exception rised");
        }
    }

    @Test
    public void badLine1() {
        try{
            InputStream i=new ByteArrayInputStream("+".getBytes(Charset.defaultCharset()));
            new ImapLine(null,null,i);
            Assertions.fail("Should not reach this point as an exception should be rised");
        } catch(ImapNullLineException ble) {
            LOGGER.log(Level.WARNING,"Unexpected Exception",ble);
            Assertions.fail("Blank Line Exception rised");
        } catch (ImapException ie) {
            // all OK this exception is expected
        }
    }

    @Test
    public void badLine2() {
        try{
            InputStream i=new ByteArrayInputStream("a +".getBytes(Charset.defaultCharset()));
            ImapLine il=new ImapLine(null,null,i);
            Assertions.fail("Should not reach this point as an exception should be rised got ["+il.getTag()+"] ["+il.getCommand()+"]");
        } catch(ImapNullLineException ble) {
            LOGGER.log(Level.WARNING,"Unexpected Exception",ble);
            Assertions.fail("Blank Line Exception rised");
        } catch (ImapException ie) {
            // all OK this exception is expected
        }
    }

    @Test
    public void goodLine() {
        try{
            InputStream i=new ByteArrayInputStream("a OK\r\n".getBytes(Charset.defaultCharset()));
            new ImapLine(null,null,i);
            Assertions.assertTrue(true, "Should  reach this point");
        } catch(ImapNullLineException ble) {
            LOGGER.log(Level.WARNING,"Unexpected Exception",ble);
            Assertions.fail("Blank Line Exception rised");
        } catch (ImapException ie) {
            LOGGER.log(Level.WARNING,"Unexpected Exception",ie);
            Assertions.fail("ImapException rised");
        }
    }

    @Test
    public void nullStringNonBlankLineStream() {
        try{
            InputStream i=new ByteArrayInputStream("a b".getBytes(Charset.defaultCharset()));
            ImapLine il=new ImapLine(null,null,i);
            Assertions.assertTrue("a".equals(il.getTag()), "Returned tag should be \"a\"");
            Assertions.assertTrue("b".equals(il.getCommand()), "Returned command should be \"b\" but is infact \""+il.getCommand()+"\"");
        } catch(ImapBlankLineException ble) {
            LOGGER.log(Level.WARNING,"Unexpected Exception",ble);
            Assertions.fail("Blank Line Exception rised");
        } catch (ImapException ie) {
            LOGGER.log(Level.WARNING,"Unexpected Exception",ie);
            Assertions.fail("Imap Exception rised (no exception expected)");
        }
    }

    @Test
    public void lineSpacing() {
        try{
            InputStream i=new ByteArrayInputStream(" b".getBytes(Charset.defaultCharset()));
            ImapLine il=new ImapLine(null,"a",i);
            Assertions.assertTrue(true, "ImapException not rised");
            Assertions.assertTrue("a".equals(il.getTag()), "Returned tag should be \"a\"");
            Assertions.assertTrue("b".equals(il.getCommand()), "Returned command should be \"b\" but is infact \""+il.getCommand()+"\"");
        } catch(ImapBlankLineException ble) {
            LOGGER.log(Level.WARNING,"Unexpected Exception",ble);
            Assertions.fail("ImapBlankLineException rised (no exception expected)");
        } catch (ImapException ie) {
            LOGGER.log(Level.WARNING,"Unexpected Exception",ie);
            Assertions.fail("Imap Exception rised");
        }
    }
}
