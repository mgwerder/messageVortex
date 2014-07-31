package net.gwerder.java.mailvortex.test;

import org.junit.runner.RunWith;
import junit.framework.TestSuite;
import org.junit.runners.Suite;
import junit.framework.JUnit4TestAdapter;
 
@RunWith(Suite.class)
@Suite.SuiteClasses({
  MailVortexTest.class,
  VersionTest.class,
  ImapLineTest.class,
  ImapClientTest.class,
  ImapCommandTest.class,
  ImapCommandLoginTest.class,
  ImapCommandNoopTest.class,
  ImapAuthenticationDummyProxyTest.class
})

public class MainSuite {

  public static junit.framework.Test suite() {    
        final TestSuite s = new TestSuite();
        s.addTest(new JUnit4TestAdapter(MailVortexTest.class));
        s.addTest(new JUnit4TestAdapter(VersionTest.class));
        s.addTest(new JUnit4TestAdapter(ImapLineTest.class));
        s.addTest(new JUnit4TestAdapter(ImapClientTest.class));
        s.addTest(new JUnit4TestAdapter(ImapCommandTest.class));
        s.addTest(new JUnit4TestAdapter(ImapCommandLoginTest.class));
        s.addTest(new JUnit4TestAdapter(ImapCommandNoopTest.class));
        s.addTest(new JUnit4TestAdapter(ImapAuthenticationDummyProxyTest.class));
        
        return s;
  }
  // the class remains empty,
  // used only as a holder for the above annotations
}