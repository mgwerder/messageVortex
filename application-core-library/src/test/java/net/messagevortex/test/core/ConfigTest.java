package net.messagevortex.test.core;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.logging.Level;
import net.messagevortex.MessageVortex;
import net.messagevortex.MessageVortexConfig;
import net.messagevortex.MessageVortexLogger;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/**
 * booleanConfigHandlings for {@link MessageVortex}.
 *
 * @author martin@gwerder.net (Martin GWERDER)
 */
@RunWith(JUnit4.class)
public class ConfigTest {
  
  static {
    MessageVortexLogger.setGlobalLogLevel(Level.ALL);
  }
  
  @Test
  public void basicConfigHandling() {
    
    try {
      // FIXME test if copy is complete
      MessageVortexConfig.getDefault().copy();
    } catch (Exception e) {
      fail("should not raise an exception (" + e + ")");
    }
    
  }
  
  @Test
  public void stringConfigHandling() {
    try {
      MessageVortexConfig.getDefault().getStringValue("stringConfigHandling");
      fail("should raise NPE but nothing happened");
    } catch (NullPointerException npe) {
      // all OK this is expected
    } catch (Exception e) {
      fail("should raise NPE but a different exception is raised (" + e + ")");
    }
    
    try {
      MessageVortexConfig.getDefault().setStringValue("StringConfigHandling", "test");
      fail("should raise NPE but nothing happened");
    } catch (NullPointerException npe) {
      // all OK this is expected
    } catch (Exception e) {
      fail("should raise NPE but a different exception is raised (" + e + ")");
    }
    try {
      // String
      assertTrue("Should return true on first creation", MessageVortexConfig.getDefault().createStringConfigValue("stringConfigHandling", "def", "def"));
      assertFalse("Should return false on recreation", MessageVortexConfig.getDefault().createStringConfigValue("stringConfigHandling", "otherdef", "otherdef"));
      assertTrue("Should return true as default value", "def".equals(MessageVortexConfig.getDefault().getStringValue("stringConfigHandling")));
      assertTrue("Should return true as last value", "def".equals(MessageVortexConfig.getDefault().setStringValue("stringConfigHandling", "otherval")));
      assertTrue("Should return false as last value", "otherval".equals(MessageVortexConfig.getDefault().setStringValue("stringConfigHandling", "thirdval")));
      assertTrue("Should return false as last value", "thirdval".equals(MessageVortexConfig.getDefault().getStringValue("stringConfigHandling")));
      assertTrue("Should return false as last value", "thirdval".equals(MessageVortexConfig.getDefault().setStringValue("stringConfigHandling", "fourthval")));
      
    } catch (Exception e) {
      e.printStackTrace();
      fail("should not raise an exception but did (" + e + ")");
    }
    
    try {
      MessageVortexConfig.getDefault().setBooleanValue("stringConfigHandling", true);
      fail("should raise CCE but nothing happened");
    } catch (ClassCastException npe) {
      // all OK this is expected
    } catch (Exception e) {
      fail("should raise CCE but a different exception is raised (" + e + ")");
    }
    
    try {
      MessageVortexConfig.getDefault().getBooleanValue("stringConfigHandling");
      fail("should raise CCE but nothing happened");
    } catch (ClassCastException npe) {
      // all OK this is expected
    } catch (Exception e) {
      fail("should raise CCE but a different exception is raised (" + e + ")");
    }
    
  }
  
  
  @Test
  public void numericConfigHandling() {
    try {
      MessageVortexConfig.getDefault().getNumericValue("numericConfigHandling");
      fail("should raise NPE but nothing happened");
    } catch (NullPointerException npe) {
      // all OK this is expected
    } catch (Exception e) {
      e.printStackTrace();
      fail("should raise NPE but a different exception is raised (" + e + ")");
    }
    
    try {
      MessageVortexConfig.getDefault().setNumericValue("numericConfigHandling", 5);
      fail("should raise NPE but nothing happened");
    } catch (NullPointerException npe) {
      // all OK this is expected
    } catch (Exception e) {
      fail("should raise NPE but a different exception is raised (" + e + ")");
    }
    try {
      // numeric
      try {
        MessageVortexConfig.getDefault().createNumericConfigValue("numericConfigHandling", "def", 5);
      } catch (Exception e) {
        e.printStackTrace();
        fail("got unexpected exception while creating numeric config");
      }
      try {
        MessageVortexConfig.getDefault().createNumericConfigValue("numericConfigHandling", "def", 5);
        fail("command did unexpectedly succeed");
      } catch (IllegalArgumentException e) {
        // this is expected
      } catch (Exception e) {
        fail("got unexpected exception while creating numeric config");
      }
      
      assertTrue("Should return true as default value", MessageVortexConfig.getDefault().getNumericValue("numericConfigHandling") == 5);
      assertTrue("Should return true as last value", MessageVortexConfig.getDefault().setNumericValue("numericConfigHandling", 10) == 5);
      assertTrue("Should return false as last value", MessageVortexConfig.getDefault().setNumericValue("numericConfigHandling", 15) == 10);
      assertTrue("Should return false as last value", MessageVortexConfig.getDefault().getNumericValue("numericConfigHandling") == 15);
      assertTrue("Should return false as last value", MessageVortexConfig.getDefault().setNumericValue("numericConfigHandling", 20) == 15);
      
    } catch (Exception e) {
      e.printStackTrace();
      fail("should not raise an exception but did (" + e + ")");
    }
    
    try {
      MessageVortexConfig.getDefault().setBooleanValue("numericConfigHandling", true);
      fail("should raise CCE but nothing happened");
    } catch (ClassCastException npe) {
      // all OK this is expected
    } catch (Exception e) {
      fail("should raise CCE but a different exception is raised (" + e + ")");
    }
    
    try {
      MessageVortexConfig.getDefault().getBooleanValue("numericConfigHandling");
      fail("should raise CCE but nothing happened");
    } catch (ClassCastException npe) {
      // all OK this is expected
    } catch (Exception e) {
      fail("should raise CCE but a different exception is raised (" + e + ")");
    }
    
  }
  
  @Test
  public void booleanConfigHandling() {
    try {
      MessageVortexConfig.getDefault().getBooleanValue("booleanConfigHandling");
      fail("should raise NPE but nothing happened");
    } catch (NullPointerException npe) {
      // all OK this is expected
    } catch (Exception e) {
      e.printStackTrace();
      fail("should raise NPE but a different exception is raised (" + e + ")");
    }
    
    try {
      MessageVortexConfig.getDefault().setBooleanValue("booleanConfigHandling", true);
      fail("should raise NPE but nothing happened");
    } catch (NullPointerException npe) {
      // all OK this is expected
    } catch (Exception e) {
      fail("should raise NPE but a different exception is raised (" + e + ")");
    }
    
    try {
      //Boolean
      try {
        MessageVortexConfig.getDefault().createBooleanConfigValue("booleanConfigHandling", "", true);
      } catch (Exception e) {
        fail("Should return true on first creation");
      }
      try {
        MessageVortexConfig.getDefault().createBooleanConfigValue("booleanConfigHandling", "", false);
        fail("Should not  be successful on recreation");
      } catch (Exception e) {
        // this exception is intended
      }
      assertTrue("Should return true as default value", MessageVortexConfig.getDefault().getBooleanValue("booleanConfigHandling"));
      assertTrue("Should return true as last value", MessageVortexConfig.getDefault().setBooleanValue("booleanConfigHandling", false));
      assertFalse("Should return false as last value", MessageVortexConfig.getDefault().setBooleanValue("booleanConfigHandling", false));
      assertFalse("Should return false as last value", MessageVortexConfig.getDefault().getBooleanValue("booleanConfigHandling"));
      assertFalse("Should return false as last value", MessageVortexConfig.getDefault().setBooleanValue("booleanConfigHandling", true));
      assertTrue("Should return true as last value", MessageVortexConfig.getDefault().setBooleanValue("booleanConfigHandling", true));
      assertTrue("Should return true as last value", MessageVortexConfig.getDefault().getBooleanValue("booleanConfigHandling"));
      
      // check predefined ressources of vortexConfig
      assertTrue("Should return null as default value", MessageVortexConfig.getDefault().getStringValue("smtp_incomming_username") == null);
    } catch (Exception e) {
      e.printStackTrace();
      fail("should not raise an exception but did (" + e + ")");
    }
    
    try {
      MessageVortexConfig.getDefault().setStringValue("booleanConfigHandling", "test");
      fail("should raise CCE but nothing happened");
    } catch (ClassCastException npe) {
      // all OK this is expected
    } catch (Exception e) {
      fail("should raise CCE but a different exception is raised (" + e + ")");
    }
    
    try {
      MessageVortexConfig.getDefault().getStringValue("booleanConfigHandling");
      fail("should raise CCE but nothing happened");
    } catch (ClassCastException npe) {
      // all OK this is expected
    } catch (Exception e) {
      fail("should raise CCE but a different exception is raised (" + e + ")");
    }
    
  }
  
}
