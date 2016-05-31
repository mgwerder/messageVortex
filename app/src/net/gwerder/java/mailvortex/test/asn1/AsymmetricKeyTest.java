package net.gwerder.java.mailvortex.test.asn1;

import net.gwerder.java.mailvortex.MailvortexLogger;
import net.gwerder.java.mailvortex.asn1.AsymmetricKey;
import net.gwerder.java.mailvortex.asn1.encryption.Algorithm;
import net.gwerder.java.mailvortex.asn1.encryption.AlgorithmType;
import net.gwerder.java.mailvortex.asn1.encryption.Padding;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.util.Arrays;
import java.util.logging.Level;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * Created by martin.gwerder on 31.05.2016.
 */
@RunWith(JUnit4.class)
public class AsymmetricKeyTest {

    private static final java.util.logging.Logger LOGGER;

    static {
        LOGGER = MailvortexLogger.getLogger((new Throwable()).getStackTrace()[0].getClassName());
        MailvortexLogger.setGlobalLogLevel(Level.ALL);
    }

    @Test
    public void transferKeyTest() {
        for(Algorithm alg: Algorithm.getAlgorithms( AlgorithmType.ASYMMETRIC )) {
            int size=2048;
            try {
                System.out.print("Testing "+alg+"/"+size+" ("+16384/size+")");
                for (int i = 0; i < 16384/size; i++) {
                    System.out.print(".");
                    AsymmetricKey k1 = new AsymmetricKey(alg, Padding.getDefault(),size);
                    AsymmetricKey k2 = new AsymmetricKey(alg, Padding.getDefault(),size);
                    k2.setPrivateKey( k1.getPrivateKey() );
                    k2.setPublicKey(  k1.getPublicKey()  );
                    assertTrue( "error in key transfer cycle with "+alg+" ",k1.equals( k2));
                    assertTrue( "reencode error in key transfer cycle with "+alg+" ",Arrays.equals(k1.toBytes(),k2.toBytes()));
                }
                System.out.println("");
            } catch(Exception e) {
                LOGGER.log( Level.WARNING,"Unexpected exception",e);
                fail("fuzzer encountered exception in Symmetric en/decryption test with algorithm "+alg.toString());
            }
        }
    }

}
