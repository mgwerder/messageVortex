package net.gwerder.java.mailvortex.test;
 
import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.junit.Ignore;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.junit.Assert;
import java.util.concurrent.TimeoutException;
import static org.junit.Assert.*;

/**
 * Tests for {@link net.gwerder.java.mailvortex.MailVortex}.
 *
 * @author martin@gwerder.net (Martin GWERDER)
 */
@RunWith(JUnit4.class)
public class MailVortexTest {

    @Test
    public void getHelp() {
        assertTrue("Errorcode for --help is not 0",net.gwerder.java.mailvortex.MailVortex.main(new String[] {"--help"})==0);
    }    

    @Test
    public void getRunRegularlyAndShutdown() {
        assertTrue("Errorcode is not 0",net.gwerder.java.mailvortex.MailVortex.main(new String[0])==0);
    }    
    @Test
    public void getRunRegularlyAndShutdownNull() {
        assertTrue("Errorcode is not 0",net.gwerder.java.mailvortex.MailVortex.main(null)==0);
    }    
}