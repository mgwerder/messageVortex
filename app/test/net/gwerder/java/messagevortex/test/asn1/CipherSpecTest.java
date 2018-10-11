package net.gwerder.java.messagevortex.test.asn1;

import net.gwerder.java.messagevortex.MessageVortexLogger;
import net.gwerder.java.messagevortex.asn1.AlgorithmParameter;
import net.gwerder.java.messagevortex.asn1.AsymmetricAlgorithmSpec;
import net.gwerder.java.messagevortex.asn1.CipherSpec;
import net.gwerder.java.messagevortex.asn1.encryption.Algorithm;
import net.gwerder.java.messagevortex.asn1.encryption.CipherUsage;
import net.gwerder.java.messagevortex.asn1.encryption.DumpType;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.util.logging.Level;

import static junit.framework.TestCase.assertTrue;
import static junit.framework.TestCase.fail;

/***
 * Test class for CipherSpec
 *
 * @author martin@gwerder.net (Martin GWERDER)
 */
@RunWith(JUnit4.class)
public class CipherSpecTest {

    private static final java.util.logging.Logger LOGGER;
    static {
        LOGGER = MessageVortexLogger.getLogger((new Throwable()).getStackTrace()[0].getClassName());
        MessageVortexLogger.setGlobalLogLevel(Level.ALL);
    }

    @Test
    public void reencodingCipherSpec() {
        try {
            CipherSpec s = new CipherSpec(CipherUsage.ENCRYPT);
            AsymmetricAlgorithmSpec aas = new AsymmetricAlgorithmSpec(Algorithm.AES256, new AlgorithmParameter());
            s.setAsymmetricSpec(aas);
            CipherSpec s2 = new CipherSpec(s.toAsn1Object(DumpType.ALL));
            assertTrue( "Reencoded CipherSpec is not equal", s2.equals(s) );
        }catch( Exception e) {
            e.printStackTrace();
            fail( "catched unexpected exception" );
        }
    }

}
