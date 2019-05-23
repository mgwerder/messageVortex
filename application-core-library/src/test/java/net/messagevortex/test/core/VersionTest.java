package net.messagevortex.test.core;

import static org.junit.Assert.assertTrue;

import net.messagevortex.MessageVortex;
import net.messagevortex.Version;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/**
 * Tests for {@link MessageVortex}.
 *
 * @author martin@gwerder.net (Martin GWERDER)
 */
@RunWith(JUnit4.class)
public class VersionTest {

    @Test
    public void testVersion() {
        System.out.println("Version is "+ Version.getStringVersion());
        assertTrue("Version String does not match regexp",Version.getStringVersion().matches("[0-9]+\\.[0-9]+\\.[0-9]+"));
    }

    @Test
    public void testBuild() {
        System.out.println("Build is "+ Version.getBuild());
        assertTrue("Build String ("+Version.getBuild()+") does not match regexp.",Version.getBuild()!=null && Version.getBuild().matches("[0-9]+\\.[0-9]+\\.[0-9]+ \\([0-9a-f]+\\)"));
    }

}
