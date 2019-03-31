package net.messagevortex.test.core;

import net.messagevortex.Config;
import net.messagevortex.MessageVortex;
import net.messagevortex.MessageVortexConfig;
import net.messagevortex.transport.dummy.DummyTransportTrx;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import javax.crypto.Cipher;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.logging.Level;
import java.util.logging.Logger;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * Tests for {@link MessageVortex}.
 *
 * @author martin@gwerder.net (Martin GWERDER)
 */
@RunWith(JUnit4.class)
public class MessageVortexTest {

  private static final Logger LOGGER;

  static {
    LOGGER = Logger.getLogger((new Throwable()).getStackTrace()[0].getClassName());
  }

  @BeforeClass
  public static void init() {
      try {
        Config cfg=MessageVortexConfig.getDefault();
        cfg.setNumericValue(null,"smtp_incoming_port",588,-1);
        int i = cfg.getNumericValue(null,"smtp_incoming_port");
        LOGGER.log( Level.INFO, "Did read value "+i+"(should be 588)" );
        assertTrue( "value is unexpected ("+i+")",i==588);
      } catch( IOException ioe ) {
        LOGGER.log( Level.SEVERE, "Unable to parse config file", ioe );
      } catch( Exception ioe ) {
        LOGGER.log( Level.SEVERE, "Unable to parse config file (generic)", ioe );
      }
  }

  @Test
  public void getHelp() {
    int e = MessageVortex.mainReturn(new String[]{"--help"});
    assertTrue("Errorcode for --help is not 103 but " + e, e == 103);
  }

  @Test
  public void getVersion() {
    int e = MessageVortex.mainReturn(new String[]{"--version"});
    assertTrue("Errorcode for --version is not 103 but " + e, e == 103);
  }

  @Test
  public void runRegularlyAndShutdown() {
    try {
      DummyTransportTrx.clearDummyEndpoints();
      assertTrue("Errorcode is not 0", MessageVortex.mainReturn(new String[] {"--timeoutAndDie=0"}) == 0);
    } catch (Exception e) {
      e.printStackTrace();
      fail("got unexpected exception " + e + "\n" + e.getStackTrace()[0].toString() );
    }
  }

  @Test
  public void runRegularlyAndShutdownTwice() {
    runRegularlyAndShutdown();
    runRegularlyAndShutdown();
  }

  @Test
  public void runRegularlyWithTimeout() {
    try {
      long start = System.currentTimeMillis();
      int ret = MessageVortex.mainReturn(new String[] {"--timeoutAndDie=3"});
      long duration = System.currentTimeMillis()-start;
      assertTrue("Errorcode is not 0", ret == 0);
      assertTrue("Duration was below 3s (duration:"+duration+")", duration >=3000);
    } catch (Exception e) {
      e.printStackTrace();
      fail("got unexpected exception " + e + "\n" + e.getStackTrace()[0].toString() );
    }
  }

  @Test
  public void testJREReadiness() {
    try {
      int i = Cipher.getMaxAllowedKeyLength("AES");
      LOGGER.log(Level.INFO, "Max keylength for AES is " + i);
      assertTrue("Looks like JRE is not having a unlimited JCE installed (AES max allowed key length is = " + i + ")", i > 128);
    } catch (NoSuchAlgorithmException nsa) {
      fail("should not throw exception in test (" + nsa.getMessage() + ")");
    }
  }
}
