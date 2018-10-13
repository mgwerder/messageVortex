package net.messagevortex.test.asn1;

import net.messagevortex.MessageVortexLogger;
import net.messagevortex.asn1.encryption.SecurityLevel;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.util.logging.Level;

import static org.junit.Assert.assertTrue;

/**
 * Created by martin.gwerder on 06.06.2016.
 */
@RunWith(JUnit4.class)
public class SecurityLevelTest {

    private static final java.util.logging.Logger LOGGER;
    static {
        LOGGER = MessageVortexLogger.getLogger( (new Throwable()).getStackTrace()[0].getClassName() );
        MessageVortexLogger.setGlobalLogLevel( Level.ALL );
    }

    @Test
    /***
     * Testing null beheour of toHex()
     */
    public void incrementTest() {
        assertTrue( "incrementing LOW (got " + SecurityLevel.LOW.next() + ")", SecurityLevel.LOW.next() == SecurityLevel.MEDIUM );
        assertTrue( "incrementing MEDIUM", SecurityLevel.MEDIUM.next() == SecurityLevel.HIGH );
        assertTrue( "incrementing HIGH", SecurityLevel.HIGH.next() == SecurityLevel.QUANTUM );
        assertTrue( "incrementing QUANTUM", SecurityLevel.QUANTUM.next() == null );
    }
}

