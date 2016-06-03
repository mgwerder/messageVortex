package net.gwerder.java.mailvortex.test;

import net.gwerder.java.mailvortex.test.core.ConfigTest;
import net.gwerder.java.mailvortex.test.core.MailVortexTest;
import net.gwerder.java.mailvortex.test.core.VersionTest;
import net.gwerder.java.mailvortex.test.imap.*;
import org.junit.runner.RunWith;
import junit.framework.TestSuite;
import org.junit.runners.Suite;
import junit.framework.JUnit4TestAdapter;
 
@RunWith(ConcurrentSuite.class)
@Suite.SuiteClasses({
  MailVortexTest.class,
  VersionTest.class,
  ConfigTest.class,
  CustomKeyManagerTest.class,
  ImapLineExceptionTest.class,
  ImapLineTest.class,
  ImapSSLTest.class,
  ImapClientTest.class,
  ImapCommandTest.class,
  ImapCommandLoginTest.class,
  ImapCommandNoopTest.class,
  ImapCommandCapabilityTest.class,
  ImapCommandLoginTest.class,
  ImapCommandLogoutTest.class,
  ImapAuthenticationDummyProxyTest.class
})

public class MainIMAPSuite {

  public static junit.framework.Test suite() {    
        final TestSuite s = new TestSuite();
        s.addTest(new JUnit4TestAdapter(CustomKeyManagerTest.class));
        s.addTest(new JUnit4TestAdapter(ImapLineExceptionTest.class));
        s.addTest(new JUnit4TestAdapter(ImapLineTest.class));
        s.addTest(new JUnit4TestAdapter(ImapSSLTest.class));
        s.addTest(new JUnit4TestAdapter(ImapClientTest.class));
        s.addTest(new JUnit4TestAdapter(ImapCommandTest.class));
        s.addTest(new JUnit4TestAdapter(ImapCommandLoginTest.class));
        s.addTest(new JUnit4TestAdapter(ImapCommandNoopTest.class));
        s.addTest(new JUnit4TestAdapter(ImapCommandCapabilityTest.class));
        s.addTest(new JUnit4TestAdapter(ImapCommandLoginTest.class));
        s.addTest(new JUnit4TestAdapter(ImapCommandLogoutTest.class));
        s.addTest(new JUnit4TestAdapter(ImapAuthenticationDummyProxyTest.class));
        
        return s;
  }
  // the class remains empty,
  // used only as a holder for the above annotations
}