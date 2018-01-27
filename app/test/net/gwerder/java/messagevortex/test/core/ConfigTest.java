package net.gwerder.java.messagevortex.test.core;

import net.gwerder.java.messagevortex.Config;
import net.gwerder.java.messagevortex.MessageVortexConfig;
import net.gwerder.java.messagevortex.MessageVortexLogger;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.util.logging.Level;

import static org.junit.Assert.*;

/**
 * booleanConfigHandlings for {@link net.gwerder.java.messagevortex.MailVortex}.
 *
 * @author martin@gwerder.net (Martin GWERDER)
 */
@RunWith(JUnit4.class)
public class ConfigTest {

    static {
        MessageVortexLogger.setGlobalLogLevel(Level.ALL);
    }

    @Test
    public void booleanConfigHandling() {
        try{
            MessageVortexConfig.getDefault().getBooleanValue("booleanConfigHandling");
            fail("should raise NPE but nothing happened");
        } catch(NullPointerException npe) {
            // all OK this is expected
        } catch(Exception e) {
            fail("should raise NPE but a different exception is raised ("+e+")");
        }

        try{
            MessageVortexConfig.getDefault().setBooleanValue("booleanConfigHandling",true);
            fail("should raise NPE but nothing happened");
        } catch(NullPointerException npe) {
            // all OK this is expected
        } catch(Exception e) {
            fail("should raise NPE but a different exception is raised ("+e+")");
        }

        try{
            MessageVortexConfig.getDefault().getStringValue("stringConfigHandling");
            fail("should raise NPE but nothing happened");
        } catch(NullPointerException npe) {
            // all OK this is expected
        } catch(Exception e) {
            fail("should raise NPE but a different exception is raised ("+e+")");
        }

        try{
            MessageVortexConfig.getDefault().setStringValue("StringConfigHandling","test");
            fail("should raise NPE but nothing happened");
        } catch(NullPointerException npe) {
            // all OK this is expected
        } catch(Exception e) {
            fail("should raise NPE but a different exception is raised ("+e+")");
        }

        try{
            // String
            assertTrue("Should return true on first creation", MessageVortexConfig.getDefault().createStringConfigValue("stringConfigHandling","def"));
            assertFalse("Should return false on recreation",MessageVortexConfig.getDefault().createStringConfigValue("stringConfigHandling","otherdef"));
            assertTrue("Should return true as default value","def".equals(MessageVortexConfig.getDefault().getStringValue("stringConfigHandling")));
            assertTrue("Should return true as last value","def".equals(MessageVortexConfig.getDefault().setStringValue("stringConfigHandling","otherval")));
            assertTrue("Should return false as last value","otherval".equals(MessageVortexConfig.getDefault().setStringValue("stringConfigHandling","thirdval")));
            assertTrue("Should return false as last value","thirdval".equals(MessageVortexConfig.getDefault().getStringValue("stringConfigHandling")));
            assertTrue("Should return false as last value","thirdval".equals(MessageVortexConfig.getDefault().setStringValue("stringConfigHandling","fourthval")));

            //Boolean
            try{
                MessageVortexConfig.getDefault().createBooleanConfigValue("booleanConfigHandling","",true);
            } catch (Exception e) {
                fail("Should return true on first creation");
            }
            try{
                MessageVortexConfig.getDefault().createBooleanConfigValue("booleanConfigHandling","",false);
                fail("Should not  be successful on recreation");
            } catch(Exception e) {
                // this exception is intended
            }
            assertTrue("Should return true as default value",MessageVortexConfig.getDefault().getBooleanValue("booleanConfigHandling"));
            assertTrue("Should return true as last value",MessageVortexConfig.getDefault().setBooleanValue("booleanConfigHandling",false));
            assertFalse("Should return false as last value",MessageVortexConfig.getDefault().setBooleanValue("booleanConfigHandling",false));
            assertFalse("Should return false as last value",MessageVortexConfig.getDefault().getBooleanValue("booleanConfigHandling"));
            assertFalse("Should return false as last value",MessageVortexConfig.getDefault().setBooleanValue("booleanConfigHandling",true));
            assertTrue("Should return true as last value",MessageVortexConfig.getDefault().setBooleanValue("booleanConfigHandling",true));
            assertTrue("Should return true as last value",MessageVortexConfig.getDefault().getBooleanValue("booleanConfigHandling"));
        } catch(Exception e) {
            fail("should not raise an exception but did ("+e+")");
        }

        try{
            MessageVortexConfig.getDefault().setStringValue("booleanConfigHandling","test");
            fail("should raise CCE but nothing happened");
        } catch(ClassCastException npe) {
            // all OK this is expected
        } catch(Exception e) {
            fail("should raise CCE but a different exception is raised ("+e+")");
        }

        try{
            MessageVortexConfig.getDefault().getStringValue("booleanConfigHandling");
            fail("should raise CCE but nothing happened");
        } catch(ClassCastException npe) {
            // all OK this is expected
        } catch(Exception e) {
            fail("should raise CCE but a different exception is raised ("+e+")");
        }

        try{
            MessageVortexConfig.getDefault().setBooleanValue("stringConfigHandling",true);
            fail("should raise CCE but nothing happened");
        } catch(ClassCastException npe) {
            // all OK this is expected
        } catch(Exception e) {
            fail("should raise CCE but a different exception is raised ("+e+")");
        }

        try{
            MessageVortexConfig.getDefault().getBooleanValue("stringConfigHandling");
            fail("should raise CCE but nothing happened");
        } catch(ClassCastException npe) {
            // all OK this is expected
        } catch(Exception e) {
            fail("should raise CCE but a different exception is raised ("+e+")");
        }

        try{
            MessageVortexConfig.getDefault().copy();
        } catch(Exception e) {
            fail("should not raise an exception ("+e+")");
        }

    }

}
