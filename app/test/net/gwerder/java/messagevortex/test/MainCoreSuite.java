package net.gwerder.java.messagevortex.test;

import junit.framework.JUnit4TestAdapter;
import junit.framework.TestSuite;
import net.gwerder.java.messagevortex.asn1.AsymmetricKey;
import net.gwerder.java.messagevortex.test.core.ConfigTest;
import net.gwerder.java.messagevortex.test.core.MessageVortexTest;
import net.gwerder.java.messagevortex.test.core.VersionTest;
import org.junit.Before;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;

@RunWith(Suite.class)
@Suite.SuiteClasses({
  MessageVortexTest.class,
  VersionTest.class,
  ConfigTest.class,
})

public class MainCoreSuite {

    @Before
    public void init() {
        AsymmetricKey.setCacheFileName("AsymmetricKey.cache");
    }

    public static junit.framework.Test suite() {
        final TestSuite s = new TestSuite();
        s.addTest(new JUnit4TestAdapter(MessageVortexTest.class));
        s.addTest(new JUnit4TestAdapter(VersionTest.class));
        s.addTest(new JUnit4TestAdapter(ConfigTest.class));

        return s;
  }
  // the class remains empty,
  // used only as a holder for the above annotations
}
