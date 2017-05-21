package net.gwerder.java.messagevortex.test.core;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import static org.junit.Assert.assertTrue;

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
}