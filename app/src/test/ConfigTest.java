package net.gwerder.java.mailvortex.test;

import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.junit.Ignore;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import static org.junit.Assert.*;

import java.util.logging.Level;
import net.gwerder.java.mailvortex.*;
import net.gwerder.java.mailvortex.imap.*;

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
        boolean v;

        try{
            Config.getBooleanValue("booleanConfigHandling");
            fail("should raise NPE but nothing happened");
        } catch(NullPointerException npe) {
            // all OK this is expected
        } catch(Exception e) {
            fail("should raise NPE but a different exception is raised ("+e+")");
        }

        try{
            Config.setBooleanValue("booleanConfigHandling",true);
            fail("should raise NPE but nothing happened");
        } catch(NullPointerException npe) {
            // all OK this is expected
        } catch(Exception e) {
            fail("should raise NPE but a different exception is raised ("+e+")");
        }
        
        try{
            assertTrue("Should return true on first creation",Config.createBooleanConfigValue("booleanConfigHandling",true));
            assertFalse("Should return false on recreation",Config.createBooleanConfigValue("booleanConfigHandling",false));
            assertTrue("Should return true as default value",Config.getBooleanValue("booleanConfigHandling"));
            assertTrue("Should return true as last value",Config.setBooleanValue("booleanConfigHandling",false));
            assertFalse("Should return false as last value",Config.setBooleanValue("booleanConfigHandling",false));
            assertFalse("Should return false as last value",Config.getBooleanValue("booleanConfigHandling"));
            assertFalse("Should return false as last value",Config.setBooleanValue("booleanConfigHandling",true));
            assertTrue("Should return true as last value",Config.setBooleanValue("booleanConfigHandling",true));
            assertTrue("Should return true as last value",Config.getBooleanValue("booleanConfigHandling"));
        } catch(Exception e) {
            fail("should raise NPE but a different exception is raised ("+e+")");
        }
    }

}