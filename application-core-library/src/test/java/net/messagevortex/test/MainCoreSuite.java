package net.messagevortex.test;

import junit.framework.JUnit4TestAdapter;
import junit.framework.TestSuite;
import net.messagevortex.asn1.AsymmetricKey;
import net.messagevortex.test.core.ConfigTest;
import net.messagevortex.test.core.MessageVortexTest;
import net.messagevortex.test.core.RandomTest;
import net.messagevortex.test.core.VersionTest;
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
        s.addTest(new JUnit4TestAdapter(RandomTest.class));
        s.addTest(new JUnit4TestAdapter(VersionTest.class));
        s.addTest(new JUnit4TestAdapter(ConfigTest.class));

        return s;
  }
  // the class remains empty,
  // used only as a holder for the above annotations
}
