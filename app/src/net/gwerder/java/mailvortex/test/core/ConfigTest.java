package net.gwerder.java.mailvortex.test.core;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import static org.junit.Assert.*;

import java.util.logging.Level;
import net.gwerder.java.mailvortex.*;

/**
 * booleanConfigHandlings for {@link net.gwerder.java.mailvortex.MailVortex}.
 *
 * @author martin@gwerder.net (Martin GWERDER)
 */
@RunWith(JUnit4.class)
public class ConfigTest {

    static {
        MailvortexLogger.setGlobalLogLevel(Level.ALL);
    }    

    @Test
    public void booleanConfigHandling() {
        try{
            Config.getDefault().getBooleanValue("booleanConfigHandling");
            fail("should raise NPE but nothing happened");
        } catch(NullPointerException npe) {
            // all OK this is expected
        } catch(Exception e) {
            fail("should raise NPE but a different exception is raised ("+e+")");
        }

        try{
            Config.getDefault().setBooleanValue("booleanConfigHandling",true);
            fail("should raise NPE but nothing happened");
        } catch(NullPointerException npe) {
            // all OK this is expected
        } catch(Exception e) {
            fail("should raise NPE but a different exception is raised ("+e+")");
        }
        
        try{
            Config.getDefault().getStringValue("stringConfigHandling");
            fail("should raise NPE but nothing happened");
        } catch(NullPointerException npe) {
            // all OK this is expected
        } catch(Exception e) {
            fail("should raise NPE but a different exception is raised ("+e+")");
        }

        try{
            Config.getDefault().setStringValue("StringConfigHandling","test");
            fail("should raise NPE but nothing happened");
        } catch(NullPointerException npe) {
            // all OK this is expected
        } catch(Exception e) {
            fail("should raise NPE but a different exception is raised ("+e+")");
        }
        
        try{
            // String
            assertTrue("Should return true on first creation",Config.getDefault().createStringConfigValue("stringConfigHandling","def"));
            assertFalse("Should return false on recreation",Config.getDefault().createStringConfigValue("stringConfigHandling","otherdef"));
            assertTrue("Should return true as default value","def".equals(Config.getDefault().getStringValue("stringConfigHandling")));
            assertTrue("Should return true as last value","def".equals(Config.getDefault().setStringValue("stringConfigHandling","otherval")));
            assertTrue("Should return false as last value","otherval".equals(Config.getDefault().setStringValue("stringConfigHandling","thirdval")));
            assertTrue("Should return false as last value","thirdval".equals(Config.getDefault().getStringValue("stringConfigHandling")));
            assertTrue("Should return false as last value","thirdval".equals(Config.getDefault().setStringValue("stringConfigHandling","fourthval")));
            
            //Boolean
            assertTrue("Should return true on first creation",Config.getDefault().createBooleanConfigValue("booleanConfigHandling",true));
            assertFalse("Should return false on recreation",Config.getDefault().createBooleanConfigValue("booleanConfigHandling",false));
            assertTrue("Should return true as default value",Config.getDefault().getBooleanValue("booleanConfigHandling"));
            assertTrue("Should return true as last value",Config.getDefault().setBooleanValue("booleanConfigHandling",false));
            assertFalse("Should return false as last value",Config.getDefault().setBooleanValue("booleanConfigHandling",false));
            assertFalse("Should return false as last value",Config.getDefault().getBooleanValue("booleanConfigHandling"));
            assertFalse("Should return false as last value",Config.getDefault().setBooleanValue("booleanConfigHandling",true));
            assertTrue("Should return true as last value",Config.getDefault().setBooleanValue("booleanConfigHandling",true));
            assertTrue("Should return true as last value",Config.getDefault().getBooleanValue("booleanConfigHandling"));
        } catch(Exception e) {
            fail("should not raise an exception but did ("+e+")");
        }

        try{
            Config.getDefault().setStringValue("booleanConfigHandling","test");
            fail("should raise CCE but nothing happened");
        } catch(ClassCastException npe) {
            // all OK this is expected
        } catch(Exception e) {
            fail("should raise CCE but a different exception is raised ("+e+")");
        }
        
        try{
            Config.getDefault().getStringValue("booleanConfigHandling");
            fail("should raise CCE but nothing happened");
        } catch(ClassCastException npe) {
            // all OK this is expected
        } catch(Exception e) {
            fail("should raise CCE but a different exception is raised ("+e+")");
        }
        
        try{
            Config.getDefault().setBooleanValue("stringConfigHandling",true);
            fail("should raise CCE but nothing happened");
        } catch(ClassCastException npe) {
            // all OK this is expected
        } catch(Exception e) {
            fail("should raise CCE but a different exception is raised ("+e+")");
        }
        
        try{
            Config.getDefault().getBooleanValue("stringConfigHandling");
            fail("should raise CCE but nothing happened");
        } catch(ClassCastException npe) {
            // all OK this is expected
        } catch(Exception e) {
            fail("should raise CCE but a different exception is raised ("+e+")");
        }

        try{
            Config.getDefault().copy();
        } catch(Exception e) {
            fail("should not raise an exception ("+e+")");
        }
        
    }

}