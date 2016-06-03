package net.gwerder.java.mailvortex.test.asn1;

import net.gwerder.java.mailvortex.MailvortexLogger;
import net.gwerder.java.mailvortex.asn1.Identity;
import net.gwerder.java.mailvortex.asn1.Message;
import net.gwerder.java.mailvortex.asn1.Payload;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.util.Arrays;
import java.util.logging.Level;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * Tests for the Message class.
 *
 * Created by martin.gwerder on 30.05.2016.
 */
@RunWith(JUnit4.class)
public class MessageTest {

    private static final java.util.logging.Logger LOGGER;

    static {
        LOGGER = MailvortexLogger.getLogger((new Throwable()).getStackTrace()[0].getClassName());
        MailvortexLogger.setGlobalLogLevel(Level.ALL);
    }

    @Test
    /***
     * Reencodes 100 Messages and checks wether their byte and Value Notation Dumps are equivalent
     */
    public void fuzzingMessage() {
        try {
            for (int i = 0; i < 100; i++) {
                LOGGER.log( Level.INFO, "Testing Message reencoding " + (i + 1) + " of " + 100 );
                Message s = new Message(new Identity(),new Payload());
                String s1=s.dumpValueNotation( "" );
                byte[] b1 = s.toBytes();
                assertTrue( "Byte representation may not be null", b1 != null );
                byte[] b2 = (new Message( b1 )).toBytes();
                assertTrue( "Byte arrays should be equal when reencoding", Arrays.equals( b1, b2 ) );
                String s2=s.dumpValueNotation( "" );
                assertTrue( "Value Notations should be equal when reencoding", s1.equals( s2 ) );
            }
        } catch (Exception e) {
            LOGGER.log( Level.WARNING,"Unexpected exception",e);
            fail( "fuzzer encountered exception in Message ("+e.toString()+")" );
        }
    }


}
