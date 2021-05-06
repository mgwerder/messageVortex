package net.messagevortex.test;

import junit.framework.JUnit4TestAdapter;
import junit.framework.TestSuite;
import net.messagevortex.asn1.AsymmetricKey;
import net.messagevortex.test.imap.AuthenticationProxyTest;
import net.messagevortex.test.imap.CustomKeyManagerTest;
import net.messagevortex.test.imap.ImapClientTest;
import net.messagevortex.test.imap.ImapCommandCapabilityTest;
import net.messagevortex.test.imap.ImapCommandLoginTest;
import net.messagevortex.test.imap.ImapCommandLogoutTest;
import net.messagevortex.test.imap.ImapCommandNoopTest;
import net.messagevortex.test.imap.ImapCommandTest;
import net.messagevortex.test.imap.ImapLineExceptionTest;
import net.messagevortex.test.imap.ImapLineTest;
import net.messagevortex.test.imap.ImapSSLTest;
import net.messagevortex.test.imap.ImapURLParser;
import org.junit.jupiter.api.BeforeEach;
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
  AuthenticationProxyTest.class,
  ImapURLParser.class
})

public class MainIMAPSuite {

    @BeforeEach
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
        s.addTest(new JUnit4TestAdapter(AuthenticationProxyTest.class));
        s.addTest(new JUnit4TestAdapter(ImapURLParser.class));

        return s;
  }
  // the class remains empty,
  // used only as a holder for the above annotations
}
