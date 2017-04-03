
package net.gwerder.java.messagevortex.test.asn1;

import net.gwerder.java.messagevortex.MailvortexLogger;
import net.gwerder.java.messagevortex.asn1.IdentityStore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.io.IOException;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Vector;
import java.util.logging.Level;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * Created by martin.gwerder on 30.05.2016.
 */
@RunWith(JUnit4.class)
public class IdentityStoreTest {

    private static final java.util.logging.Logger LOGGER;

    static {
        LOGGER = MailvortexLogger.getLogger( (new Throwable()).getStackTrace()[0].getClassName() );
        MailvortexLogger.setGlobalLogLevel( Level.ALL );
    }

    @Test
    /***
     * Reencodes 100 IdentityStore and checks whether their byte and Value Notation Dumps are equivalent
     */
    public void testingIdentityStoreDump1() {
        try {
            for (int i = 0; i < 10; i++) {
                LOGGER.log( Level.INFO, "Testing Identity Store dumping " + (i + 1) + " of " + 10 );
                IdentityStore s = new IdentityStore();
                assertTrue( "IdentityStore may not be null", s != null );
                String s1 = s.dumpValueNotation( "" );
                byte[] b1 = s.toBytes();
                assertTrue( "Byte representation may not be null", b1 != null );
                byte[] b2 = (new IdentityStore( b1 )).toBytes();
                assertTrue( "Byte arrays should be equal when reencoding", Arrays.equals( b1, b2 ) );
                String s2 = (new IdentityStore( b2 )).dumpValueNotation( "" );
                assertTrue( "Value Notations should be equal when reencoding", s1.equals( s2 ) );
            }
        } catch (Exception e) {
            LOGGER.log( Level.WARNING, "Unexpected exception", e );
            fail( "fuzzer encountered exception in IdentityStore (" + e.toString() + ")" );
        }
    }

    @Test
    /***
     * Reencodes 4 IdentityStore and checks whether their byte and Value Notation Dumps are equivalent
     */
    public void testingIdentityStoreDemo() throws InterruptedException {
        class ISThread extends Thread {
            public IdentityStore is = null;

            public void run() {
                try {
                    is = IdentityStore.getNewIdentityStoreDemo(false);
                } catch (IOException ioe) {
                    LOGGER.log( Level.WARNING, "got IOException while generating new demo", ioe );
                }
            }
        }

        Date start = new Date();
        final IdentityStore[] arr = new IdentityStore[10];
        List<ISThread> t = new Vector<>();

        // prepare stores
        for (IdentityStore a:arr) {
            t.add( new ISThread() );
        }
        for (ISThread is : t) {
            is.start();
        }
        for (ISThread is : t) {
            is.join();
        }

        for (int i = 0; i < arr.length; i++) {
            arr[i] = t.get( i ).is;
        }
        LOGGER.log( Level.INFO, "store preparation took " + (((new Date()).getTime() - start.getTime()) / 1000) + " s" );

        //testing
        try {
            for (int i = 0; i < arr.length; i++) {
                LOGGER.log( Level.INFO, "Testing IdentityStore reencoding " + (i + 1) + " of " + arr.length );
                start = new Date();
                IdentityStore s1 = arr[i];
                assertTrue( "IdentityStore may not be null", s1 != null );
                byte[] b1 = s1.toBytes();
                assertTrue( "Byte representation may not be null", b1 != null );
                IdentityStore s2 = new IdentityStore( b1 );
                byte[] b2 = s2.toBytes();
                assertTrue( "Byte arrays should be equal when reencoding", Arrays.equals( b1, b2 ) );
                assertTrue( "Value Notations should be equal when reencoding", (new IdentityStore( b2 )).dumpValueNotation( "" ).equals( (new IdentityStore( b1 )).dumpValueNotation( "" ) ) );
                s2 = arr[(i + 1) % arr.length];
                b2 = s2.toBytes();
                assertTrue( "Value Notations should NOT be equal when reencoding new demo", !(new IdentityStore( b2 )).dumpValueNotation( "" ).equals( (new IdentityStore( b1 )).dumpValueNotation( "" ) ) );
                LOGGER.log( Level.INFO, "Testing IdentityStore reencoding " + (i + 1) + " took " + (((new Date()).getTime() - start.getTime()) / 1000) + " s" );
            }
        } catch (Exception e) {
            LOGGER.log( Level.WARNING, "Unexpected exception", e );
            fail( "fuzzer encountered exception in IdentityStore (" + e.toString() + ")" );
        }
    }

}
