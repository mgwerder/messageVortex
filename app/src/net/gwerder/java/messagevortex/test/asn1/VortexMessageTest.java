package net.gwerder.java.messagevortex.test.asn1;

import net.gwerder.java.messagevortex.MessageVortexLogger;
import net.gwerder.java.messagevortex.asn1.*;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.util.Arrays;
import java.util.logging.Level;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * Tests for the VortexMessage class.
 *
 * Created by martin.gwerder on 30.05.2016.
 */
@RunWith(JUnit4.class)
public class VortexMessageTest {

    private static final java.util.logging.Logger LOGGER;

    static {
        LOGGER = MessageVortexLogger.getLogger((new Throwable()).getStackTrace()[0].getClassName());
        MessageVortexLogger.setGlobalLogLevel(Level.ALL);
    }

    /***
     * Reencodes 100 Messages and checks wether their byte and Value Notation Dumps are equivalent
     */
    @Test
    public void fuzzingMessage() {
        try {
            for (int i = 0; i < 100; i++) {
                LOGGER.log( Level.INFO, "Testing VortexMessage reencoding " + (i + 1) + " of " + 100 );
                VortexMessage s = new VortexMessage(new Prefix(),new InnerMessage(  ));
                String s1=s.dumpValueNotation( "" );
                byte[] b1 = s.toBytes();
                assertTrue( "Byte representation may not be null", b1 != null );
                byte[] b2 = (new VortexMessage( b1,null )).toBytes();
                assertTrue( "Byte arrays should be equal when reencoding", Arrays.equals( b1, b2 ) );
                String s2=s.dumpValueNotation( "" );
                assertTrue( "Value Notations should be equal when reencoding", s1.equals( s2 ) );
            }
        } catch (Exception e) {
            LOGGER.log( Level.WARNING,"Unexpected exception",e);
            fail( "fuzzer encountered exception in VortexMessage ("+e.toString()+")" );
        }
    }

    @Test
    public void fuzzingIntToByteConverter() {
        LOGGER.log( Level.INFO, "Testing basic byte reencoding length 4");
        testIntByteConverter( 0,4 );
        testIntByteConverter( 1,4 );
        testIntByteConverter( 4294967295l,4 );
        for (int i = 0; i < 100; i++) {
            int rand = (int) (Math.random() * Integer.MAX_VALUE);
            LOGGER.log( Level.INFO, "  Testing byte reencoding " + (i + 1) + " of " + 200 + " [" + rand + "]" );
            testIntByteConverter( rand,4 );
        }
        LOGGER.log( Level.INFO, "Testing basic byte reencoding length 2");
        testIntByteConverter( 0,2 );
        testIntByteConverter( 1,2 );
        testIntByteConverter( 65525l,2 );
        for (int i = 101; i < 200; i++) {
            int rand = (int) (Math.random() * 65535);
            LOGGER.log( Level.INFO, "  Testing byte reencoding " + (i + 1) + " of " + 200 + " [" + rand + "]" );
            testIntByteConverter( rand,2 );
        }
    }

    private void testIntByteConverter(long i, int len) {
        byte[] b= VortexMessage.getLongAsBytes( i,len );
        assertTrue("wrong number of bytes returned (expected="+len+"; received="+b.length+")",b.length==len);
        assertTrue( "Error reencoding "+i+ " (old="+i+";"+ Block.toHex(b)+";new="+ VortexMessage.getBytesAsInteger( b )+"]", VortexMessage.getBytesAsInteger( b ) == i );
    }


}
