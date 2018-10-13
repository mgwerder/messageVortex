package net.gwerder.java.messagevortex.test.core;

import net.gwerder.java.messagevortex.Config;
import net.gwerder.java.messagevortex.MessageVortex;
import net.gwerder.java.messagevortex.MessageVortexConfig;
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
 * Tests for {@link net.gwerder.java.messagevortex.MessageVortex}.
 *
 * @author martin@gwerder.net (Martin GWERDER)
 */
@RunWith(JUnit4.class)
public class MessageVortexTest {

    private static final Logger LOGGER;
    static {
        LOGGER = Logger.getLogger((new Throwable()).getStackTrace()[0].getClassName());
    }

    static {
        try {
            Config cfg=MessageVortexConfig.createConfig();
            cfg.setNumericValue("smtp_incomming_port",588);
        } catch( IOException ioe ) {
            LOGGER.log( Level.SEVERE, "Unable to parse config file", ioe );
        }


    }

    @Test
    public void getHelp() {
        assertTrue("Errorcode for --help is not 100", MessageVortex.main(new String[] {"--help"}) == 100 );
    }

    @Test
    public void getRunRegularlyAndShutdown() {
        assertTrue("Errorcode is not 0", MessageVortex.main(new String[0]) == 0 );
    }
    @Test
    public void getRunRegularlyAndShutdownNull() {
        assertTrue("Errorcode is not 0", MessageVortex.main(null) == 0 );
    }

    @Test
    public void testJREReadiness() {
        try {
            int i=Cipher.getMaxAllowedKeyLength("AES");
            LOGGER.log( Level.INFO , "Max keylength for AES is "+ i ) ;
            assertTrue("Looks like JRE is not having a unlimited JCE installed (AES max allowed key length is = "+i+")",i > 128);
        } catch (NoSuchAlgorithmException nsa) {
            fail("should not throw exception in test ("+nsa.getMessage()+")");
        }
    }
}
