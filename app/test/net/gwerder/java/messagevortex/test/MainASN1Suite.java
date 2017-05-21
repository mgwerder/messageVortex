package net.gwerder.java.messagevortex.test;


import junit.framework.JUnit4TestAdapter;
import junit.framework.TestSuite;
import net.gwerder.java.messagevortex.test.asn1.*;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;

@RunWith(Suite.class)
@Suite.SuiteClasses({
        FuzzerTest.class,
        VortexMessageTest.class,
        DumpTypeTest.class,
        AbstractBlockTest.class,
        MacAlgorithmTest.class,
        SecurityLevelTest.class,
        SymmetricKeyTest.class,
        AsymmetricKeyTest.class,
        AsymmetricKeyReencodingTest.class,
        IdentityStoreTest.class,
        IdentityBlockTest.class
})

/***
 * Test all classes related to parsing/handling ASN.1 data.
 */
public class MainASN1Suite {

    public static junit.framework.Test suite() {
        final TestSuite s = new TestSuite();
        s.addTest(new JUnit4TestAdapter(FuzzerTest.class));
        s.addTest(new JUnit4TestAdapter(VortexMessageTest.class));
        s.addTest(new JUnit4TestAdapter(DumpTypeTest.class));
        s.addTest(new JUnit4TestAdapter(AbstractBlockTest.class));
        s.addTest(new JUnit4TestAdapter(MacAlgorithmTest.class));
        s.addTest(new JUnit4TestAdapter( SecurityLevelTest.class ) );
        s.addTest(new JUnit4TestAdapter( SymmetricKeyTest.class ) );
        s.addTest(new JUnit4TestAdapter(AsymmetricKeyTest.class));
        s.addTest(new JUnit4TestAdapter(AsymmetricKeyReencodingTest.class));
        s.addTest(new JUnit4TestAdapter(IdentityStoreTest.class));
        s.addTest(new JUnit4TestAdapter(IdentityBlockTest.class));
        return s;
    }

}