package net.messagevortex.test.core;

import net.messagevortex.MessageVortex;
import net.messagevortex.Version;
import net.messagevortex.test.GlobalJunitExtension;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

/**
 * Tests for {@link MessageVortex} Version output.
 *
 * @author martin@gwerder.net (Martin GWERDER)
 */
@ExtendWith(GlobalJunitExtension.class)
public class VersionTest {

    @Test
    public void testVersion() {
        System.out.println("Version is " + Version.getStringVersion());
        Assertions.assertTrue(Version.getStringVersion().matches("[0-9]+\\.[0-9]+\\.[0-9]+"), "Version (" + Version.getBuild() + ") String does not match regexp");
    }

    @Test
    public void testBuild() {
        System.out.println("Build is " + Version.getBuild());
        Assertions.assertTrue(Version.getBuild() != null && Version.getBuild().matches("[0-9]+\\.[0-9]+\\.[0-9]+ \\([0-9a-f]+\\)"), "Build String (" + Version.getBuild() + ") does not match regexp.");
    }

}
