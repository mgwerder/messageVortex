package net.gwerder.java.messagevortex.test.accounting;

import net.gwerder.java.messagevortex.MessageVortexLogger;
import net.gwerder.java.messagevortex.accounting.DummyAccountant;
import net.gwerder.java.messagevortex.accounting.HeaderVerifier;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.util.logging.Level;

import static org.junit.Assert.assertTrue;

/**
 * General Test class for all unspecific Block tests.
 *
 * Created by martin.gwerder on 30.05.2016.
 */
@RunWith(JUnit4.class)
public class DummyAccountantTest {

    private static final java.util.logging.Logger LOGGER;
    static {
        LOGGER = MessageVortexLogger.getLogger((new Throwable()).getStackTrace()[0].getClassName());
        MessageVortexLogger.setGlobalLogLevel(Level.ALL);
    }

    @Test
    public void basicDummyAccountantTest() {
        HeaderVerifier a=new DummyAccountant();

        assertTrue("wrong reply for header processing from dummy acountant",a.verifyHeaderForProcessing(null)==Integer.MAX_VALUE);
    }



}
