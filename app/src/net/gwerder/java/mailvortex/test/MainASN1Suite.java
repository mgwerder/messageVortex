package net.gwerder.java.mailvortex.test;

/**
 * Created by martin.gwerder on 19.04.2016.
 */

import junit.framework.JUnit4TestAdapter;
import junit.framework.TestSuite;
import net.gwerder.java.mailvortex.test.asn1.FuzzerTest;
import net.gwerder.java.mailvortex.test.asn1.MessageTest;
import net.gwerder.java.mailvortex.test.imap.*;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;

@RunWith(Suite.class)
@Suite.SuiteClasses({
        FuzzerTest.class,
        MessageTest.class
})

public class MainASN1Suite {

    public static junit.framework.Test suite() {
        final TestSuite s = new TestSuite();
        s.addTest(new JUnit4TestAdapter(FuzzerTest.class));
        s.addTest(new JUnit4TestAdapter(MessageTest.class));
        return s;
    }
    // the class remains empty,
    // used only as a holder for the above annotations
}
