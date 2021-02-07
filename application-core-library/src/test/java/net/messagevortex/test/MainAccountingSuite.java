package net.messagevortex.test;

/*
 * Test all classes related to parsing/handling ASN.1 data.
 *
 */

import junit.framework.JUnit4TestAdapter;
import junit.framework.TestSuite;
import net.messagevortex.asn1.AsymmetricKey;
import net.messagevortex.test.accounting.DummyAccountantTest;
import org.junit.Before;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;

@RunWith(Suite.class)
@Suite.SuiteClasses({
        DummyAccountantTest.class
})

public class MainAccountingSuite {

    @Before
    public void init() {
        AsymmetricKey.setCacheFileName("AsymmetricKey.cache");
    }

    public static junit.framework.Test suite() {
        final TestSuite s = new TestSuite();
        s.addTest(new JUnit4TestAdapter(DummyAccountantTest.class));
        return s;
    }

}
