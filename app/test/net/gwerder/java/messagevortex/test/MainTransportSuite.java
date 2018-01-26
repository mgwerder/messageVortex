package net.gwerder.java.messagevortex.test;

/**
 * Test all classes related to parsing/handling ASN.1 data.
 *
 * Created by martin.gwerder on 19.04.2016.
 */

import junit.framework.JUnit4TestAdapter;
import junit.framework.TestSuite;
import net.gwerder.java.messagevortex.asn1.AsymmetricKey;
import net.gwerder.java.messagevortex.test.transport.DummyTransportSenderTest;
import net.gwerder.java.messagevortex.test.transport.SMTPTransportSenderTest;
import org.junit.Before;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;

@RunWith(Suite.class)
@Suite.SuiteClasses({
        DummyTransportSenderTest.class,
        SMTPTransportSenderTest.class
})

public class MainTransportSuite {

    @Before
    public void init() {
        AsymmetricKey.setCacheFileName("AsymmetricKey.cache");
    }

    public static junit.framework.Test suite() {
        final TestSuite s = new TestSuite();
        s.addTest(new JUnit4TestAdapter(DummyTransportSenderTest.class));
        s.addTest(new JUnit4TestAdapter(SMTPTransportSenderTest.class));
        return s;
    }

}
