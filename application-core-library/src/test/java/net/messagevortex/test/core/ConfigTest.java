package net.messagevortex.test.core;

import net.messagevortex.Config;
import net.messagevortex.MessageVortex;
import net.messagevortex.MessageVortexConfig;
import net.messagevortex.test.GlobalJunitExtension;
import net.messagevortex.transport.dummy.DummyTransportTrx;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.parallel.ResourceAccessMode;
import org.junit.jupiter.api.parallel.ResourceLock;
import org.junit.jupiter.api.parallel.Resources;

import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;


/**
 * booleanConfigHandlings for {@link MessageVortex}.
 *
 * @author martin@gwerder.net (Martin GWERDER)
 */
@ExtendWith(GlobalJunitExtension.class)
public class ConfigTest {

  private static final Logger LOGGER;

  static {
    LOGGER = Logger.getLogger((new Throwable()).getStackTrace()[0].getClassName());
  }

  @Test
  @ResourceLock(value = Resources.SYSTEM_PROPERTIES, mode = ResourceAccessMode.READ_WRITE)
  public void basicConfigHandling() {

    try {
      // FIXME test if copy is complete
      MessageVortexConfig.getDefault().copy();
    } catch (Exception e) {
      Assertions.fail("should not raise an exception (" + e + ")");
    }

  }

  @Test
  @ResourceLock(value = Resources.SYSTEM_PROPERTIES, mode = ResourceAccessMode.READ_WRITE)
  public void stringConfigHandling() {
    try {
      MessageVortexConfig.getDefault().getStringValue(null, "stringConfigHandling");
      Assertions.fail("should raise NPE but nothing happened");
    } catch (NullPointerException npe) {
      // all OK this is expected
    } catch (Exception e) {
      Assertions.fail("should raise NPE but a different exception is raised (" + e + ")");
    }

    try {
      MessageVortexConfig.getDefault().setStringValue(null, "StringConfigHandling", "test", -1);
      Assertions.fail("should raise NPE but nothing happened");
    } catch (NullPointerException npe) {
      // all OK this is expected
    } catch (Exception e) {
      Assertions.fail("should raise NPE but a different exception is raised (" + e + ")");
    }
    try {
      // String
      Assertions.assertTrue(MessageVortexConfig.getDefault().createStringConfigValue("stringConfigHandling", "def", "def"), "Should return true on first creation");
      Assertions.assertFalse(MessageVortexConfig.getDefault().createStringConfigValue("stringConfigHandling", "otherdef", "otherdef"), "Should return false on recreation");
      Assertions.assertTrue("def".equals(MessageVortexConfig.getDefault().getStringValue(null, "stringConfigHandling")), "Should return true as default value");
      Assertions.assertTrue("def".equals(MessageVortexConfig.getDefault().setStringValue(null, "stringConfigHandling", "otherval", -1)), "Should return true as last value");
      Assertions.assertTrue("otherval".equals(MessageVortexConfig.getDefault().setStringValue(null, "stringConfigHandling", "thirdval", -1)), "Should return false as last value");
      Assertions.assertTrue("thirdval".equals(MessageVortexConfig.getDefault().getStringValue(null, "stringConfigHandling")), "Should return false as last value");
      Assertions.assertTrue("thirdval".equals(MessageVortexConfig.getDefault().setStringValue(null, "stringConfigHandling", "fourthval", -1)), "Should return false as last value");
    } catch (Exception e) {
      e.printStackTrace();
      Assertions.fail("should not raise an exception but did (" + e + ")");
    }

    try {
      MessageVortexConfig.getDefault().setBooleanValue(null, "stringConfigHandling", true, -1);
      Assertions.fail("should raise CCE but nothing happened");
    } catch (ClassCastException npe) {
      // all OK this is expected
    } catch (Exception e) {
      Assertions.fail("should raise CCE but a different exception is raised (" + e + ")");
    }

    try {
      MessageVortexConfig.getDefault().getBooleanValue(null, "stringConfigHandling");
      Assertions.fail("should raise CCE but nothing happened");
    } catch (ClassCastException npe) {
      // all OK this is expected
    } catch (Exception e) {
      Assertions.fail("should raise CCE but a different exception is raised (" + e + ")");
    }
    try {
      Assertions.assertTrue(MessageVortexConfig.getDefault().removeConfigValue("stringConfigHandling"), "should return true when removing an existing value");
      Assertions.assertTrue(!MessageVortexConfig.getDefault().removeConfigValue("stringConfigHandling"), "should return false when removing a non-existing value");
    } catch (Exception e) {
      Assertions.fail("should raise an exception but did (" + e + ")");
    }
  }


  @Test
  @ResourceLock(value = Resources.SYSTEM_PROPERTIES, mode = ResourceAccessMode.READ_WRITE)
  public void fileHandling() {
    try {
      // clear all unwanted rubish
      MessageVortexConfig.getDefault().clear();
      DummyTransportTrx.clearDummyEndpoints();

      // read config file
      MessageVortexConfig.getDefault().load("messageVortex.cfg");

      // write config file
      MessageVortexConfig.getDefault().store("messageVortex.cfgWritten");
    } catch (Exception e) {
      e.printStackTrace();
      Assertions.fail("should work but an exception is raised (" + e + ")");
    }
  }

  @Test
  @ResourceLock(value = Resources.SYSTEM_PROPERTIES, mode = ResourceAccessMode.READ_WRITE)
  public void numericConfigHandling() {
    try {
      MessageVortexConfig.getDefault().getNumericValue(null, "numericConfigHandling");
      Assertions.fail("should raise NPE but nothing happened");
    } catch (NullPointerException npe) {
      // all OK this is expected
    } catch (Exception e) {
      e.printStackTrace();
      Assertions.fail("should raise NPE but a different exception is raised (" + e + ")");
    }

    try {
      MessageVortexConfig.getDefault().setNumericValue(null, "numericConfigHandling", 5, -1);
      Assertions.fail("should raise NPE but nothing happened");
    } catch (NullPointerException npe) {
      // all OK this is expected
    } catch (Exception e) {
      Assertions.fail("should raise NPE but a different exception is raised (" + e + ")");
    }
    try {
      // numeric
      try {
        MessageVortexConfig.getDefault().createNumericConfigValue("numericConfigHandling", "def", 5);
      } catch (Exception e) {
        e.printStackTrace();
        Assertions.fail("got unexpected exception while creating numeric config");
      }
      try {
        MessageVortexConfig.getDefault().createNumericConfigValue("numericConfigHandling", "def", 5);
        Assertions.fail("command did unexpectedly succeed");
      } catch (IllegalArgumentException e) {
        // this is expected
      } catch (Exception e) {
        Assertions.fail("got unexpected exception while creating numeric config");
      }

      Assertions.assertTrue(MessageVortexConfig.getDefault().getNumericValue(null, "numericConfigHandling") == 5, "Should return true as default value");
      Assertions.assertTrue(MessageVortexConfig.getDefault().setNumericValue(null, "numericConfigHandling", 10, -1) == 5, "Should return true as last value");
      Assertions.assertTrue(MessageVortexConfig.getDefault().setNumericValue(null, "numericConfigHandling", 15, -1) == 10, "Should return false as last value");
      Assertions.assertTrue(MessageVortexConfig.getDefault().getNumericValue(null, "numericConfigHandling") == 15, "Should return false as last value");
      Assertions.assertTrue(MessageVortexConfig.getDefault().setNumericValue(null, "numericConfigHandling", 20, -1) == 15, "Should return false as last value");

    } catch (Exception e) {
      e.printStackTrace();
      Assertions.fail("should not raise an exception but did (" + e + ")");
    }

    try {
      MessageVortexConfig.getDefault().setBooleanValue(null, "numericConfigHandling", true, -1);
      Assertions.fail("should raise CCE but nothing happened");
    } catch (ClassCastException npe) {
      // all OK this is expected
    } catch (Exception e) {
      Assertions.fail("should raise CCE but a different exception is raised (" + e + ")");
    }

    try {
      MessageVortexConfig.getDefault().getBooleanValue(null, "numericConfigHandling");
      Assertions.fail("should raise CCE but nothing happened");
    } catch (ClassCastException npe) {
      // all OK this is expected
    } catch (Exception e) {
      Assertions.fail("should raise CCE but a different exception is raised (" + e + ")");
    }

    try {
      Assertions.assertTrue(MessageVortexConfig.getDefault().removeConfigValue("numericConfigHandling"), "should return true when removing an existing value");
      Assertions.assertTrue(!MessageVortexConfig.getDefault().removeConfigValue("numericConfigHandling"), "should return false when removing a non-existing value");
    } catch (Exception e) {
      Assertions.fail("should raise an exception but did (" + e + ")");
    }
  }

  @Test
  @ResourceLock(value = Resources.SYSTEM_PROPERTIES, mode = ResourceAccessMode.READ_WRITE)
  public void booleanConfigHandling() {
    try {
      MessageVortexConfig.getDefault().getBooleanValue(null, "booleanConfigHandling");
      Assertions.fail("should raise NPE but nothing happened");
    } catch (NullPointerException npe) {
      // all OK this is expected
    } catch (Exception e) {
      e.printStackTrace();
      Assertions.fail("should raise NPE but a different exception is raised (" + e + ")");
    }

    try {
      MessageVortexConfig.getDefault().setBooleanValue(null, "booleanConfigHandling", true, -1);
      Assertions.fail("should raise NPE but nothing happened");
    } catch (NullPointerException npe) {
      // all OK this is expected
    } catch (Exception e) {
      Assertions.fail("should raise NPE but a different exception is raised (" + e + ")");
    }

    try {
      //Boolean
      try {
        MessageVortexConfig.getDefault().createBooleanConfigValue("booleanConfigHandling", "", true);
      } catch (Exception e) {
        Assertions.fail("Should return true on first creation");
      }
      try {
        MessageVortexConfig.getDefault().createBooleanConfigValue("booleanConfigHandling", "", false);
        Assertions.fail("Should not  be successful on recreation");
      } catch (Exception e) {
        // this exception is intended
      }
      Assertions.assertTrue(MessageVortexConfig.getDefault().getBooleanValue(null, "booleanConfigHandling"), "Should return true as default value");
      Assertions.assertTrue(MessageVortexConfig.getDefault().setBooleanValue(null, "booleanConfigHandling", false, -1), "Should return true as last value");
      Assertions.assertFalse(MessageVortexConfig.getDefault().setBooleanValue(null, "booleanConfigHandling", false, -1), "Should return false as last value");
      Assertions.assertFalse(MessageVortexConfig.getDefault().getBooleanValue(null, "booleanConfigHandling"), "Should return false as last value");
      Assertions.assertFalse(MessageVortexConfig.getDefault().setBooleanValue(null, "booleanConfigHandling", true, -1), "Should return false as last value");
      Assertions.assertTrue(MessageVortexConfig.getDefault().setBooleanValue(null, "booleanConfigHandling", true, -1), "Should return true as last value");
      Assertions.assertTrue(MessageVortexConfig.getDefault().getBooleanValue(null, "booleanConfigHandling"), "Should return true as last value");

      // check predefined ressources of vortexConfig
      Assertions.assertTrue(MessageVortexConfig.getDefault().getStringValue(null, "smtp_outgoing_user") == null, "Should return null as default value");
    } catch (Exception e) {
      e.printStackTrace();
      Assertions.fail("should not raise an exception but did (" + e + ")");
    }

    try {
      MessageVortexConfig.getDefault().setStringValue(null, "booleanConfigHandling", "test", -1);
      Assertions.fail("should raise CCE but nothing happened");
    } catch (ClassCastException npe) {
      // all OK this is expected
    } catch (Exception e) {
      Assertions.fail("should raise CCE but a different exception is raised (" + e + ")");
    }

    try {
      MessageVortexConfig.getDefault().getStringValue(null, "booleanConfigHandling");
      Assertions.fail("should raise CCE but nothing happened");
    } catch (ClassCastException npe) {
      // all OK this is expected
    } catch (Exception e) {
      Assertions.fail("should raise CCE but a different exception is raised (" + e + ")");
    }

    try {
      Assertions.assertTrue(MessageVortexConfig.getDefault().removeConfigValue("booleanConfigHandling"), "should return true when removing an existing value");
      Assertions.assertTrue(!MessageVortexConfig.getDefault().removeConfigValue("booleanConfigHandling"), "should return false when removing a non-existing value");
    } catch (Exception e) {
      Assertions.fail("should raise an exception but did (" + e + ")");
    }

  }

  @Test
  @ResourceLock(value = Resources.SYSTEM_PROPERTIES, mode = ResourceAccessMode.READ_WRITE)
  public void writingParsingConfigFile() {
    try {
      LOGGER.log(Level.INFO, "Getting std config");
      Config cfg1 = MessageVortexConfig.getDefault();
      LOGGER.log(Level.INFO, "storing std config to string");
      String cfg1s = cfg1.store();
      String filename = System.getProperty("java.io.tmpdir") + "/tmpfile.cfg";
      LOGGER.log(Level.INFO, "storing std config to file to " + filename);
      cfg1.store(filename);
      Assertions.assertTrue(new File(filename).exists(), "File not created");
      LOGGER.log(Level.INFO, "Getting new Config object");
      Config cfg2 = new Config(cfg1.getResouceFilename());
      LOGGER.log(Level.INFO, "Rereading file");
      cfg2.load(filename);
      LOGGER.log(Level.INFO, "Storing new config to String too");
      String cfg2s = cfg2.store();
      final String delim = "=======================================================";
      System.out.println(delim + "cfgs1\r\n" + cfg1s + "\r\n" + delim + "cfgs2\r\n" + cfg2s + "\r\n" + delim);
      Assertions.assertTrue(cfg1s.equals(cfg2s), "Config store not equal after write/reload cycle");
      new File(filename).delete();
    } catch (IOException ioe) {
      ioe.printStackTrace();
      System.out.flush();
      System.err.flush();
      Assertions.fail("got unexpected exception");
    }

  }

}
