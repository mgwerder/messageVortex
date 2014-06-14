package net.gwerder.java.mailvortex.test;

import org.junit.runner.RunWith;
import junit.framework.TestSuite;
import org.junit.runners.Suite;
import junit.framework.JUnit4TestAdapter;

@RunWith(Suite.class)
@Suite.SuiteClasses({
  MailVortexTest.class,
  ImapCommandTest.class,
  ImapCommandLoginTest.class
})

public class MainSuite {

  public static junit.framework.Test suite() {    
        final TestSuite s = new TestSuite();
		s.addTest(new JUnit4TestAdapter(MailVortexTest.class));
        s.addTest(new JUnit4TestAdapter(ImapCommandTest.class));
        s.addTest(new JUnit4TestAdapter(ImapCommandLoginTest.class));
        return s;
  }
  // the class remains empty,
  // used only as a holder for the above annotations
}