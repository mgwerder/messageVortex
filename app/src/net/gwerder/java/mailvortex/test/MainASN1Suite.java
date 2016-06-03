package net.gwerder.java.mailvortex.test;

/**
 * Created by martin.gwerder on 19.04.2016.
 */

import junit.framework.JUnit4TestAdapter;
import junit.framework.TestSuite;
import net.gwerder.java.mailvortex.test.asn1.*;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;

@RunWith(ConcurrentSuite.class)
@Suite.SuiteClasses({
        FuzzerTest.class,
        MessageTest.class,
        BlockTest.class,
        AsymmetricKeyTest.class,
        AsymmetricKeyFuzzerTest.class,
        IdentityStoreTest.class,
        IdentityTest.class
})

public class MainASN1Suite {

    public static junit.framework.Test suite() {
        final TestSuite s = new TestSuite();
        s.addTest(new JUnit4TestAdapter(FuzzerTest.class));
        s.addTest(new JUnit4TestAdapter(MessageTest.class));
        s.addTest(new JUnit4TestAdapter(BlockTest.class));
        s.addTest(new JUnit4TestAdapter(AsymmetricKeyTest.class));
        s.addTest(new JUnit4TestAdapter(AsymmetricKeyFuzzerTest.class));
        s.addTest(new JUnit4TestAdapter(IdentityStoreTest.class));
        s.addTest(new JUnit4TestAdapter(IdentityTest.class));
        return s;
    }

}
