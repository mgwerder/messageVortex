package net.messagevortex.test.core;

import net.messagevortex.Config;
import net.messagevortex.MessageVortex;
import net.messagevortex.MessageVortexConfig;
import net.messagevortex.MessageVortexLogger;
import net.messagevortex.transport.dummy.DummyTransportTrx;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.ResourceAccessMode;
import org.junit.jupiter.api.parallel.ResourceLock;
import org.junit.jupiter.api.parallel.Resources;

import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import static org.junit.Assert.*;


/**
 * booleanConfigHandlings for {@link MessageVortex}.
 *
 * @author martin@gwerder.net (Martin GWERDER)
 */
public class ConfigTest {

  private static final Logger LOGGER;

  static {
    LOGGER = Logger.getLogger((new Throwable()).getStackTrace()[0].getClassName());
    MessageVortexLogger.setGlobalLogLevel(Level.ALL);
  }

  @Test
  @ResourceLock(value = Resources.SYSTEM_PROPERTIES, mode = ResourceAccessMode.READ_WRITE)
  public void basicConfigHandling() {

    try {
      // FIXME test if copy is complete
      MessageVortexConfig.getDefault().copy();
    } catch (Exception e) {
      fail("should not raise an exception (" + e + ")");
    }

  }

  @Test
  @ResourceLock(value = Resources.SYSTEM_PROPERTIES, mode = ResourceAccessMode.READ_WRITE)
  public void stringConfigHandling() {
    try {
      MessageVortexConfig.getDefault().getStringValue(null, "stringConfigHandling");
      fail("should raise NPE but nothing happened");
    } catch (NullPointerException npe) {
      // all OK this is expected
    } catch (Exception e) {
      fail("should raise NPE but a different exception is raised (" + e + ")");
    }

    try {
      MessageVortexConfig.getDefault().setStringValue(null, "StringConfigHandling", "test", -1);
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
      assertTrue("Should return true as default value", "def".equals(MessageVortexConfig.getDefault().getStringValue(null, "stringConfigHandling")));
      assertTrue("Should return true as last value", "def".equals(MessageVortexConfig.getDefault().setStringValue(null, "stringConfigHandling", "otherval", -1)));
      assertTrue("Should return false as last value", "otherval".equals(MessageVortexConfig.getDefault().setStringValue(null, "stringConfigHandling", "thirdval", -1)));
      assertTrue("Should return false as last value", "thirdval".equals(MessageVortexConfig.getDefault().getStringValue(null, "stringConfigHandling")));
      assertTrue("Should return false as last value", "thirdval".equals(MessageVortexConfig.getDefault().setStringValue(null, "stringConfigHandling", "fourthval", -1)));
    } catch (Exception e) {
      e.printStackTrace();
      fail("should not raise an exception but did (" + e + ")");
    }

    try {
      MessageVortexConfig.getDefault().setBooleanValue(null, "stringConfigHandling", true, -1);
      fail("should raise CCE but nothing happened");
    } catch (ClassCastException npe) {
      // all OK this is expected
    } catch (Exception e) {
      fail("should raise CCE but a different exception is raised (" + e + ")");
    }

    try {
      MessageVortexConfig.getDefault().getBooleanValue(null, "stringConfigHandling");
      fail("should raise CCE but nothing happened");
    } catch (ClassCastException npe) {
      // all OK this is expected
    } catch (Exception e) {
      fail("should raise CCE but a different exception is raised (" + e + ")");
    }
    try {
      assertTrue("should return true when removing an existing value", MessageVortexConfig.getDefault().removeConfigValue("stringConfigHandling"));
      assertTrue("should return false when removing a non-existing value", !MessageVortexConfig.getDefault().removeConfigValue("stringConfigHandling"));
    } catch (Exception e) {
      fail("should raise an exception but did (" + e + ")");
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
      fail("should work but an exception is raised (" + e + ")");
    }
  }

  @Test
  @ResourceLock(value = Resources.SYSTEM_PROPERTIES, mode = ResourceAccessMode.READ_WRITE)
  public void numericConfigHandling() {
    try {
      MessageVortexConfig.getDefault().getNumericValue(null, "numericConfigHandling");
      fail("should raise NPE but nothing happened");
    } catch (NullPointerException npe) {
      // all OK this is expected
    } catch (Exception e) {
      e.printStackTrace();
      fail("should raise NPE but a different exception is raised (" + e + ")");
    }

    try {
      MessageVortexConfig.getDefault().setNumericValue(null, "numericConfigHandling", 5, -1);
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

      assertTrue("Should return true as default value", MessageVortexConfig.getDefault().getNumericValue(null, "numericConfigHandling") == 5);
      assertTrue("Should return true as last value", MessageVortexConfig.getDefault().setNumericValue(null, "numericConfigHandling", 10, -1) == 5);
      assertTrue("Should return false as last value", MessageVortexConfig.getDefault().setNumericValue(null, "numericConfigHandling", 15, -1) == 10);
      assertTrue("Should return false as last value", MessageVortexConfig.getDefault().getNumericValue(null, "numericConfigHandling") == 15);
      assertTrue("Should return false as last value", MessageVortexConfig.getDefault().setNumericValue(null, "numericConfigHandling", 20, -1) == 15);

    } catch (Exception e) {
      e.printStackTrace();
      fail("should not raise an exception but did (" + e + ")");
    }

    try {
      MessageVortexConfig.getDefault().setBooleanValue(null, "numericConfigHandling", true, -1);
      fail("should raise CCE but nothing happened");
    } catch (ClassCastException npe) {
      // all OK this is expected
    } catch (Exception e) {
      fail("should raise CCE but a different exception is raised (" + e + ")");
    }

    try {
      MessageVortexConfig.getDefault().getBooleanValue(null, "numericConfigHandling");
      fail("should raise CCE but nothing happened");
    } catch (ClassCastException npe) {
      // all OK this is expected
    } catch (Exception e) {
      fail("should raise CCE but a different exception is raised (" + e + ")");
    }

    try {
      assertTrue("should return true when removing an existing value", MessageVortexConfig.getDefault().removeConfigValue("numericConfigHandling"));
      assertTrue("should return false when removing a non-existing value", !MessageVortexConfig.getDefault().removeConfigValue("numericConfigHandling"));
    } catch (Exception e) {
      fail("should raise an exception but did (" + e + ")");
    }
  }

  @Test
  @ResourceLock(value = Resources.SYSTEM_PROPERTIES, mode = ResourceAccessMode.READ_WRITE)
  public void booleanConfigHandling() {
    try {
      MessageVortexConfig.getDefault().getBooleanValue(null, "booleanConfigHandling");
      fail("should raise NPE but nothing happened");
    } catch (NullPointerException npe) {
      // all OK this is expected
    } catch (Exception e) {
      e.printStackTrace();
      fail("should raise NPE but a different exception is raised (" + e + ")");
    }

    try {
      MessageVortexConfig.getDefault().setBooleanValue(null, "booleanConfigHandling", true, -1);
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
      assertTrue("Should return true as default value", MessageVortexConfig.getDefault().getBooleanValue(null, "booleanConfigHandling"));
      assertTrue("Should return true as last value", MessageVortexConfig.getDefault().setBooleanValue(null, "booleanConfigHandling", false, -1));
      assertFalse("Should return false as last value", MessageVortexConfig.getDefault().setBooleanValue(null, "booleanConfigHandling", false, -1));
      assertFalse("Should return false as last value", MessageVortexConfig.getDefault().getBooleanValue(null, "booleanConfigHandling"));
      assertFalse("Should return false as last value", MessageVortexConfig.getDefault().setBooleanValue(null, "booleanConfigHandling", true, -1));
      assertTrue("Should return true as last value", MessageVortexConfig.getDefault().setBooleanValue(null, "booleanConfigHandling", true, -1));
      assertTrue("Should return true as last value", MessageVortexConfig.getDefault().getBooleanValue(null, "booleanConfigHandling"));

      // check predefined ressources of vortexConfig
      assertTrue("Should return null as default value", MessageVortexConfig.getDefault().getStringValue(null, "smtp_outgoing_user") == null);
    } catch (Exception e) {
      e.printStackTrace();
      fail("should not raise an exception but did (" + e + ")");
    }

    try {
      MessageVortexConfig.getDefault().setStringValue(null, "booleanConfigHandling", "test", -1);
      fail("should raise CCE but nothing happened");
    } catch (ClassCastException npe) {
      // all OK this is expected
    } catch (Exception e) {
      fail("should raise CCE but a different exception is raised (" + e + ")");
    }

    try {
      MessageVortexConfig.getDefault().getStringValue(null, "booleanConfigHandling");
      fail("should raise CCE but nothing happened");
    } catch (ClassCastException npe) {
      // all OK this is expected
    } catch (Exception e) {
      fail("should raise CCE but a different exception is raised (" + e + ")");
    }

    try {
      assertTrue("should return true when removing an existing value", MessageVortexConfig.getDefault().removeConfigValue("booleanConfigHandling"));
      assertTrue("should return false when removing a non-existing value", !MessageVortexConfig.getDefault().removeConfigValue("booleanConfigHandling"));
    } catch (Exception e) {
      fail("should raise an exception but did (" + e + ")");
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
      assertTrue("File not created", new File(filename).exists());
      LOGGER.log(Level.INFO, "Getting new Config object");
      Config cfg2 = new Config(cfg1.getResouceFilename());
      LOGGER.log(Level.INFO, "Rereading file");
      cfg2.load(filename);
      LOGGER.log(Level.INFO, "Storing new config to String too");
      String cfg2s = cfg2.store();
      final String delim = "=======================================================";
      System.out.println(delim + "cfgs1\r\n" + cfg1s + "\r\n" + delim + "cfgs2\r\n" + cfg2s + "\r\n" + delim);
      assertTrue("Config store not equal after write/reload cycle", cfg1s.equals(cfg2s));
      new File(filename).delete();
    } catch (IOException ioe) {
      ioe.printStackTrace();
      System.out.flush();
      System.err.flush();
      fail("got unexpected exception");
    }

  }

}
