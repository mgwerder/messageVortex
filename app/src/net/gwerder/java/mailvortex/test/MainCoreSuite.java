package net.gwerder.java.mailvortex.test;

import junit.framework.JUnit4TestAdapter;
import junit.framework.TestSuite;
import net.gwerder.java.mailvortex.test.core.ConfigTest;
import net.gwerder.java.mailvortex.test.core.MailVortexTest;
import net.gwerder.java.mailvortex.test.core.VersionTest;
import net.gwerder.java.mailvortex.test.imap.*;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;

@RunWith(ConcurrentSuite.class)
@Suite.SuiteClasses({
  MailVortexTest.class,
  VersionTest.class,
  ConfigTest.class,
})

public class MainCoreSuite {

  public static junit.framework.Test suite() {    
        final TestSuite s = new TestSuite();
        s.addTest(new JUnit4TestAdapter(MailVortexTest.class));
        s.addTest(new JUnit4TestAdapter(VersionTest.class));
        s.addTest(new JUnit4TestAdapter(ConfigTest.class));

        return s;
  }
  // the class remains empty,
  // used only as a holder for the above annotations
}