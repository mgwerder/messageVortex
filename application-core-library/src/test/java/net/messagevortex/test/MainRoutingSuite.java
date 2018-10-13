package net.messagevortex.test;

/**
 * Test all classes related to routing.
 */

import junit.framework.JUnit4TestAdapter;
import junit.framework.TestSuite;
import net.messagevortex.asn1.AsymmetricKey;
import net.messagevortex.test.routing.EdgeTest;
import net.messagevortex.test.routing.FullMessageTest;
import net.messagevortex.test.routing.InternalPayloadTest;
import net.messagevortex.test.routing.MathModeTest;
import net.messagevortex.test.routing.MatrixTest;
import net.messagevortex.test.routing.MessageFactoryTest;
import net.messagevortex.test.routing.OperationProcessingTest;
import org.junit.Before;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;

@RunWith(Suite.class)
@Suite.SuiteClasses({
        InternalPayloadTest.class,
        MessageFactoryTest.class,
        MatrixTest.class,
        EdgeTest.class,
        OperationProcessingTest.class,
        MathModeTest.class,
        FullMessageTest.class,
})

public class MainRoutingSuite {

    @Before
    public void init() {
        AsymmetricKey.setCacheFileName("AsymmetricKey.cache");
    }

    public static junit.framework.Test suite() {
        final TestSuite s = new TestSuite();
        s.addTest(new JUnit4TestAdapter(InternalPayloadTest.class));
        s.addTest(new JUnit4TestAdapter(MessageFactoryTest.class));
        s.addTest(new JUnit4TestAdapter(MatrixTest.class));
        s.addTest(new JUnit4TestAdapter(EdgeTest.class));
        s.addTest(new JUnit4TestAdapter(MathModeTest.class));
        s.addTest(new JUnit4TestAdapter(OperationProcessingTest.class));
        s.addTest(new JUnit4TestAdapter(FullMessageTest.class));
        return s;
    }

}