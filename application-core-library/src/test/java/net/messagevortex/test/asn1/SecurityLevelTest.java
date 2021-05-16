package net.messagevortex.test.asn1;

import net.messagevortex.MessageVortexLogger;
import net.messagevortex.asn1.encryption.SecurityLevel;
import net.messagevortex.test.GlobalJunitExtension;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

/*
 * Created by martin.gwerder on 06.06.2016.
 */
@ExtendWith(GlobalJunitExtension.class)
public class SecurityLevelTest {

    private static final java.util.logging.Logger LOGGER;
    static {
        LOGGER = MessageVortexLogger.getLogger( (new Throwable()).getStackTrace()[0].getClassName() );
    }

    @Test
    /*
     * Testing null behaviour of toHex()
     */
    public void incrementTest() {
        Assertions.assertTrue(SecurityLevel.LOW.next() == SecurityLevel.MEDIUM, "incrementing LOW (got " + SecurityLevel.LOW.next() + ")");
        Assertions.assertTrue(SecurityLevel.MEDIUM.next() == SecurityLevel.HIGH, "incrementing MEDIUM");
        Assertions.assertTrue(SecurityLevel.HIGH.next() == SecurityLevel.QUANTUM, "incrementing HIGH");
        Assertions.assertTrue(SecurityLevel.QUANTUM.next() == null, "incrementing QUANTUM");
    }
}

