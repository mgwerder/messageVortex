package net.gwerder.java.mailvortex.test.imap;
 
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
public class VersionTest {

    @Test
    public void testVersion() {
        assertTrue("Version String does not match regexp",net.gwerder.java.mailvortex.Version.getVersion().matches("[0-9]+\\.[0-9]+\\.[0-9]+"));
    }    

    @Test
    public void testBuild() {
        assertTrue("Build String does not match regexp",net.gwerder.java.mailvortex.Version.getBuild().matches("[0-9]+\\.[0-9]+\\.[0-9]+ \\([0-9]+\\)"));
    }    

}