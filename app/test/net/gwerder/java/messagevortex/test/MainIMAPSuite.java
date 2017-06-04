package net.gwerder.java.messagevortex.test;

import junit.framework.JUnit4TestAdapter;
import junit.framework.TestSuite;
import net.gwerder.java.messagevortex.asn1.AsymmetricKey;
import net.gwerder.java.messagevortex.test.imap.*;
import org.junit.Before;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;

@RunWith(Suite.class)
@Suite.SuiteClasses({
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

    @Before
    public void init() {
            AsymmetricKey.setCacheFileName("AsymmetricKey.cache");
      }

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
