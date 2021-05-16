package net.messagevortex.test.accounting;

import net.messagevortex.MessageVortexLogger;
import net.messagevortex.accounting.DummyAccountant;
import net.messagevortex.accounting.HeaderVerifier;
import net.messagevortex.test.GlobalJunitExtension;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

/**
 * General Test class for all unspecific Block tests.
 * <p>
 * Created by martin.gwerder on 30.05.2016.
 */
@ExtendWith(GlobalJunitExtension.class)
public class DummyAccountantTest {

    private static final java.util.logging.Logger LOGGER;

    static {
        LOGGER = MessageVortexLogger.getLogger((new Throwable()).getStackTrace()[0].getClassName());
    }

    @Test
    public void basicDummyAccountantTest() {
        HeaderVerifier a = new DummyAccountant("");

        Assertions.assertTrue(a.verifyHeaderForProcessing(null) == Integer.MAX_VALUE, "wrong reply for header processing from dummy acountant");
    }


}
