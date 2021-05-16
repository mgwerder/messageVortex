package net.messagevortex.test.core;

import net.messagevortex.Config;
import net.messagevortex.MessageVortex;
import net.messagevortex.MessageVortexConfig;
import net.messagevortex.test.GlobalJunitExtension;
import net.messagevortex.transport.dummy.DummyTransportTrx;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.parallel.ResourceAccessMode;
import org.junit.jupiter.api.parallel.ResourceLock;
import org.junit.jupiter.api.parallel.Resources;

import javax.crypto.Cipher;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.logging.Level;
import java.util.logging.Logger;


/**
 * Tests for {@link MessageVortex}.
 *
 * @author martin@gwerder.net (Martin GWERDER)
 */
@DisplayName("Running MessageVortex from scratch")
@ExtendWith(GlobalJunitExtension.class)
public class MessageVortexTest {

  private static final Logger LOGGER;

  static {
    LOGGER = Logger.getLogger((new Throwable()).getStackTrace()[0].getClassName());
  }

  public static void init() {
      try {
        Config cfg=MessageVortexConfig.getDefault();
        cfg.setNumericValue(null,"smtp_incoming_port",588,-1);
        int i = cfg.getNumericValue(null,"smtp_incoming_port");
        LOGGER.log( Level.INFO, "Did read value "+i+"(should be 588)" );
        Assertions.assertTrue(i==588, "value is unexpected ("+i+")");
      } catch( IOException ioe ) {
        LOGGER.log( Level.SEVERE, "Unable to parse config file", ioe );
      } catch( Exception ioe ) {
        LOGGER.log( Level.SEVERE, "Unable to parse config file (generic)", ioe );
      }
  }

  @Test
  @DisplayName("Getting help on the commandline")
  @ResourceLock(value = Resources.SYSTEM_PROPERTIES, mode = ResourceAccessMode.READ_WRITE)
  public void getHelp() {
    init();
    int e = MessageVortex.mainReturn(new String[]{"--help"});
    Assertions.assertTrue(e == 103, "Errorcode for --help is not 103 but " + e);
  }

  @Test
  @DisplayName("getting the version information on commandline")
  @ResourceLock(value = Resources.SYSTEM_PROPERTIES, mode = ResourceAccessMode.READ_WRITE)
  public void getVersion() {
    init();
    Integer e = MessageVortex.mainReturn(new String[]{"--version"});
    Assertions.assertTrue(e != null && e == 103, "Errorcode for --version is not 103 but " + e);
  }

  @Test
  @DisplayName("Running MessageVortex with a zero timeout (shutdown immediately")
  @ResourceLock(value = Resources.SYSTEM_PROPERTIES, mode = ResourceAccessMode.READ_WRITE)
  public void runRegularlyAndShutdown() {
    init();
    try {
      DummyTransportTrx.clearDummyEndpoints();
      DummyTransportTrx.setLocalMode(true);
      Assertions.assertEquals(0, MessageVortex.mainReturn(new String[] {"--timeoutAndDie=0"}), "Errorcode is not 0");
    } catch (Exception e) {
      e.printStackTrace();
      Assertions.fail("got unexpected exception " + e + "\n" + e.getStackTrace()[0].toString() );
    }
  }

  @Test
  @DisplayName("Look for remaining processes when reusing JVM")
  @ResourceLock(value = Resources.SYSTEM_PROPERTIES, mode = ResourceAccessMode.READ_WRITE)
  public void runRegularlyAndShutdownTwice() {
    init();
    runRegularlyAndShutdown();
    runRegularlyAndShutdown();
  }

  @Test
  @DisplayName("Running MessageVortex with a timeout")
  @ResourceLock(value = Resources.SYSTEM_PROPERTIES, mode = ResourceAccessMode.READ_WRITE)
  public void runRegularlyWithTimeout() {
    init();
    try {
      DummyTransportTrx.clearDummyEndpoints();
      DummyTransportTrx.setLocalMode(true);
      long start = System.currentTimeMillis();
      int ret = MessageVortex.mainReturn(new String[] {"--timeoutAndDie=3"});
      long duration = System.currentTimeMillis()-start;
      Assertions.assertAll("checking final result",
              ()->Assertions.assertEquals(ret, 0,"Errorcode is not 0"),
              ()->Assertions.assertTrue(duration >=3000, "Duration was below 3s (duration:"+duration+")")

      );
    } catch (Exception e) {
      e.printStackTrace();
      Assertions.fail("got unexpected exception " + e + "\n" + e.getStackTrace()[0].toString() );
    }
  }

  @Test
  @DisplayName("test current JRE for suitability from CLI")
  @ResourceLock(value = Resources.SYSTEM_PROPERTIES, mode = ResourceAccessMode.READ_WRITE)
  public void testJREReadiness() {
    init();
    try {
      int i = Cipher.getMaxAllowedKeyLength("AES");
      LOGGER.log(Level.INFO, "Max keylength for AES is " + i);
      Assertions.assertTrue(i > 128, "Looks like JRE is not having a unlimited JCE installed (AES max allowed key length is = " + i + ")");
    } catch (NoSuchAlgorithmException nsa) {
      Assertions.fail("should not throw exception in test (" + nsa.getMessage() + ")");
    }
  }
}
