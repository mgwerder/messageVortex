package net.gwerder.java.messagevortex.test.core;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import javax.crypto.Cipher;

import java.security.NoSuchAlgorithmException;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * Tests for {@link net.gwerder.java.messagevortex.MailVortex}.
 *
 * @author martin@gwerder.net (Martin GWERDER)
 */
@RunWith(JUnit4.class)
public class MessageVortexTest {

    @Test
    public void getHelp() {
        assertTrue("Errorcode for --help is not 0",net.gwerder.java.messagevortex.MailVortex.main(new String[] {"--help"})==0);
    }    

    @Test
    public void getRunRegularlyAndShutdown() {
        assertTrue("Errorcode is not 0",net.gwerder.java.messagevortex.MailVortex.main(new String[0])==0);
    }    
    @Test
    public void getRunRegularlyAndShutdownNull() {
        assertTrue("Errorcode is not 0",net.gwerder.java.messagevortex.MailVortex.main(null)==0);
    }

    @Test
    public void testJREReadiness() {
        try {
            int i=Cipher.getMaxAllowedKeyLength("AES");
            assertTrue("Looks like JRE is not having a unlimited JCE installed (AES max allowed key length is = "+i+")",i > 128);
        } catch (NoSuchAlgorithmException nsa) {
            fail("should not throw exception in test ("+nsa.getMessage()+")");
        }
    }
}