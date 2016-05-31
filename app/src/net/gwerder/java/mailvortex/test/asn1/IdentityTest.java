package net.gwerder.java.mailvortex.test.asn1;

import net.gwerder.java.mailvortex.MailvortexLogger;
import net.gwerder.java.mailvortex.asn1.Identity;
import net.gwerder.java.mailvortex.asn1.Message;
import net.gwerder.java.mailvortex.asn1.AsymmetricKey;
import net.gwerder.java.mailvortex.asn1.Key;
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
        LOGGER = MailvortexLogger.getLogger((new Throwable()).getStackTrace()[0].getClassName());
        MailvortexLogger.setGlobalLogLevel(Level.ALL);
    }

    @Test
    /***
     * Reencodes 100 Identities and checks wether their byte and Value Notation Dumps are equivalent
     */
    public void testingIdentityDump1() {
        try {
            for (int i = 0; i < 10; i++) {
                Identity s = new Identity();
                assertTrue( "Identity may not be null", s != null );
                String s1=s.dumpValueNotation( "" );
                byte[] b1 = s.toBytes();
                assertTrue( "Byte representation may not be null", b1 != null );
                byte[] b2 = (new Identity( b1,null )).toBytes();
                assertTrue( "Byte arrays should be equal when reencoding", Arrays.equals( b1, b2 ) );
                String s2=(new Identity(b2,null)).dumpValueNotation( "" );
                assertTrue( "Value Notations should be equal when reencoding", s1.equals( s2 ) );
                // redoing it encrypted
                Identity i3 = new Identity();
                AsymmetricKey ak=new AsymmetricKey(Key.Algorithm.SECP521R1,0);
                assertTrue( "Identity may not be null", i3 != null );
                String s3=i3.dumpValueNotation( "" );
                byte[] b3 = i3.toBytes();
                assertTrue( "Byte representation may not be null", b3 != null );
                Identity i4=(new Identity( ak.encrypt(b3,true),ak ));
                byte[] b4 = i4.toBytes();
                assertTrue( "Byte arrays should be equal when reencoding", Arrays.equals( b3, b4 ) );
                String s4= i4.dumpValueNotation( "" );
                assertTrue( "Value Notations should be equal when reencoding", s3.equals( s4 ) );
            }
        } catch (Exception e) {
            LOGGER.log( Level.WARNING,"Unexpected exception",e);
            fail( "fuzzer encountered exception in Identity ("+e.toString()+")" );
        }
    }

}
