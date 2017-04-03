package net.gwerder.java.messagevortex.test.core;
 
import net.gwerder.java.messagevortex.Version;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import static org.junit.Assert.*;

/**
 * Tests for {@link net.gwerder.java.messagevortex.MailVortex}.
 *
 * @author martin@gwerder.net (Martin GWERDER)
 */
@RunWith(JUnit4.class)
public class VersionTest {

    @Test
    public void testVersion() {
        System.out.println("Version is "+ Version.getVersion());
        assertTrue("Version String does not match regexp",net.gwerder.java.messagevortex.Version.getVersion().matches("[0-9]+\\.[0-9]+\\.[0-9]+"));
    }    

    @Test
    public void testBuild() {
        System.out.println("Build is "+ Version.getBuild());
        assertTrue("Build String ("+Version.getBuild()+") does not match regexp.",Version.getBuild().matches("[0-9]+\\.[0-9]+\\.[0-9]+ \\([0-9]+\\)"));
    }    

}