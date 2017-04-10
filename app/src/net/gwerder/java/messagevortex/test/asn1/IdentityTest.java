package net.gwerder.java.messagevortex.test.asn1;

import net.gwerder.java.messagevortex.MessageVortexLogger;
import net.gwerder.java.messagevortex.asn1.Identity;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.util.Arrays;
import java.util.logging.Level;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * Created by martin.gwerder on 30.05.2016.
 */
@RunWith(JUnit4.class)
public class IdentityTest {

    private static final java.util.logging.Logger LOGGER;

    static {
        LOGGER = MessageVortexLogger.getLogger((new Throwable()).getStackTrace()[0].getClassName());
        MessageVortexLogger.setGlobalLogLevel(Level.ALL);
    }

    @Test
    /***
     * Reencodes 100 Identities and checks wether their byte and Value Notation Dumps are equivalent
     */
    public void testingIdentityDump1() {
        try {
            for (int i = 0; i < 10; i++) {
                LOGGER.log( Level.INFO, "Testing Identity reencoding and dumping " + (i + 1) + " of " + 10 );
                Identity s = new Identity();
                assertTrue( "Identity may not be null", s != null );
                String s1=s.dumpValueNotation( "" );
                byte[] b1 = s.toBytes();
                assertTrue( "Byte representation may not be null", b1 != null );
                byte[] b2 = (new Identity( b1 )).toBytes();
                assertTrue( "Byte arrays should be equal when reencoding", Arrays.equals( b1, b2 ) );
                String s2 = (new Identity( b2 )).dumpValueNotation( "" );
                assertTrue( "Value Notations should be equal when reencoding", s1.equals( s2 ) );
            }
        } catch (Exception e) {
            LOGGER.log( Level.WARNING,"Unexpected exception",e);
            fail( "fuzzer encountered exception in Identity ("+e.toString()+")" );
        }
    }

}
