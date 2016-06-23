package net.gwerder.java.mailvortex.test.core;
 
import net.gwerder.java.mailvortex.Version;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
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
        System.out.println("Version is "+ Version.getVersion());
        assertTrue("Version String does not match regexp",net.gwerder.java.mailvortex.Version.getVersion().matches("[0-9]+\\.[0-9]+\\.[0-9]+"));
    }    

    @Test
    public void testBuild() {
        System.out.println("Build is "+ Version.getBuild());
        assertTrue("Build String ("+Version.getBuild()+") does not match regexp.",Version.getBuild().matches("[0-9]+\\.[0-9]+\\.[0-9]+ \\([0-9]+\\)"));
    }    

}