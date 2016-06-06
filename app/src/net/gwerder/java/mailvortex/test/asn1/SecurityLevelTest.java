package net.gwerder.java.mailvortex.test.asn1;

import net.gwerder.java.mailvortex.MailvortexLogger;
import net.gwerder.java.mailvortex.asn1.encryption.SecurityLevel;
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
        LOGGER = MailvortexLogger.getLogger( (new Throwable()).getStackTrace()[0].getClassName() );
        MailvortexLogger.setGlobalLogLevel( Level.ALL );
    }

    @Test
    /***
     * Testing null beheour of toHex()
     */
    public void incrementTest() {
        assertTrue( "incrementing LOW (got " + SecurityLevel.LOW.next() + ")", SecurityLevel.LOW.next() == SecurityLevel.MEDIUM );
        assertTrue( "incrementing MEDIUM", SecurityLevel.MEDIUM.next() == SecurityLevel.HIGH );
        assertTrue( "incrementing HIGH", SecurityLevel.HIGH.next() == SecurityLevel.QUANTUM );
        assertTrue( "incrementing QUANTUM", SecurityLevel.QUANTUM.next() == SecurityLevel.LOW );
    }
}

