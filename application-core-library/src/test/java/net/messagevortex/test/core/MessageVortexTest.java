package net.messagevortex.test.core;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.crypto.Cipher;
import net.messagevortex.Config;
import net.messagevortex.MessageVortex;
import net.messagevortex.MessageVortexConfig;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

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
        cfg.setNumericValue("smtp_incomming_port",588);
        int i = cfg.getNumericValue("smtp_incomming_port");
        LOGGER.log( Level.SEVERE, "Did read value "+i+"(should be 588)" );
        assertTrue( "value is unexpected ("+i+")",i==588);
      } catch( IOException ioe ) {
        LOGGER.log( Level.SEVERE, "Unable to parse config file", ioe );
      } catch( Exception ioe ) {
        LOGGER.log( Level.SEVERE, "Unable to parse config file (generic)", ioe );
      }
  }
  
  @Test
  public void getHelp() {
    assertTrue("Errorcode for --help is not 100", MessageVortex.main(new String[]{"--help"}) == 100);
  }
  
  @Test
  public void getRunRegularlyAndShutdown() {
    try {
      assertTrue("Errorcode is not 0", MessageVortex.main(new String[0]) == 0);
    } catch (Exception e) {
      e.printStackTrace();
      fail("got unexpected exception");
    }
  }
  
  @Test
  public void getRunRegularlyAndShutdownNull() {
    try {
      assertTrue("Errorcode is not 0", MessageVortex.main(null) == 0);
    } catch (Exception e) {
      e.printStackTrace();
      fail("got unexpected exception");
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
