package net.messagevortex.test.asn1;

import net.messagevortex.MessageVortexLogger;
import net.messagevortex.asn1.AlgorithmParameter;
import net.messagevortex.asn1.AsymmetricAlgorithmSpec;
import net.messagevortex.asn1.CipherSpec;
import net.messagevortex.asn1.encryption.Algorithm;
import net.messagevortex.asn1.encryption.CipherUsage;
import net.messagevortex.asn1.encryption.DumpType;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.logging.Level;

/***
 * Test class for CipherSpec
 *
 * @author martin@gwerder.net (Martin GWERDER)
 */
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
            Assertions.assertTrue(s2.equals(s), "Reencoded CipherSpec is not equal");
        }catch( Exception e) {
            e.printStackTrace();
            Assertions.fail( "catched unexpected exception" );
        }
    }

}
