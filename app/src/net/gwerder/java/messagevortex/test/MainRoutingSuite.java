package net.gwerder.java.messagevortex.test;

/**
 * Test all classes related to parsing/handling ASN.1 data.
 *
 * Created by martin.gwerder on 19.04.2016.
 */

import junit.framework.JUnit4TestAdapter;
import junit.framework.TestSuite;
import net.gwerder.java.messagevortex.test.routing.MessageFactoryTest;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;

@RunWith(Suite.class)
@Suite.SuiteClasses({
        MessageFactoryTest.class
})

public class MainRoutingSuite {

    public static junit.framework.Test suite() {
        final TestSuite s = new TestSuite();
        s.addTest(new JUnit4TestAdapter(MessageFactoryTest.class));
        return s;
    }

}