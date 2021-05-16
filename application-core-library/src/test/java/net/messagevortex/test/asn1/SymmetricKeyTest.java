package net.messagevortex.test.asn1;

import net.messagevortex.MessageVortexLogger;
import net.messagevortex.asn1.SymmetricKey;
import net.messagevortex.asn1.encryption.Algorithm;
import net.messagevortex.asn1.encryption.AlgorithmType;
import net.messagevortex.asn1.encryption.DumpType;
import net.messagevortex.asn1.encryption.Mode;
import net.messagevortex.asn1.encryption.Padding;
import net.messagevortex.asn1.encryption.SecurityLevel;
import net.messagevortex.test.GlobalJunitExtension;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.Random;
import java.util.logging.Level;


/**
 * Tests basic functions of symmetric encryption.
 *
 * Created by martin.gwerder on 03.06.2016.
 */
@ExtendWith(GlobalJunitExtension.class)
public class SymmetricKeyTest {

    private static final java.util.logging.Logger LOGGER;
    private static final Random sr = new Random();

    static {
        LOGGER = MessageVortexLogger.getLogger((new Throwable()).getStackTrace()[0].getClassName());
    }

    private int ksDisc=16384;

    @Test
    public void symmetricKeySizeTest() {
        Assertions.assertTrue(Algorithm.AES128.getKeySize() == 128, "getKeySize for AES256 is bad");
        Assertions.assertTrue(Algorithm.AES256.getKeySize() == 256, "getKeySize for AES256 is bad");
        Assertions.assertTrue(Algorithm.CAMELLIA192.getKeySize() == 192, "getKeySize for CAMELLIA256 is bad");
        Assertions.assertTrue(Algorithm.CAMELLIA256.getKeySize() == 256, "getKeySize for CAMELLIA256 is bad");
    }

    private void symmetricEncryptionTestRun(Algorithm alg,int size) {
        LOGGER.log(Level.INFO, "Testing " + alg + "/" + size + "");
        LOGGER.log(Level.INFO, "  creating key");
        SymmetricKey s = null;
        try {
            s = new SymmetricKey(alg, Padding.getDefault(alg.getAlgorithmType()) , Mode.getDefault(alg.getAlgorithmType()));
        } catch (Exception ioe) {
            LOGGER.log(Level.WARNING, "unexpected exception", ioe);
            Assertions.fail("Constructor threw IOException");
        }
        byte[] b1 = new byte[0];
        while (b1.length < 10) {
            b1 = new byte[sr.nextInt(Math.min(s.getPadding().getMaxSize(size), 1024))];
        }
        sr.nextBytes(b1);
        LOGGER.log(Level.INFO, "  doing an encrypt/decrypt cycle with " + b1.length + " bytes");
        byte[] b2 = null;
        byte[] b3 = null;
        try {
            b2 = s.encrypt(b1);
            b3 = s.decrypt(b2);
        } catch (IOException ioe) {
            LOGGER.log(Level.WARNING, "unexpected exception", ioe);
            Assertions.fail("IOException while reencrypting");
        }
        Assertions.assertTrue(Arrays.equals(b1, b3), "error in encrypt/decrypt cycle with " + alg + " (same object)");
        LOGGER.log(Level.INFO, "  doing an encrypt/decrypt cycle with a reencoded key");
        try {
            b3 = (new SymmetricKey(s.toBytes(DumpType.ALL_UNENCRYPTED))).decrypt(b2);
        } catch (Exception ioe) {
            LOGGER.log(Level.WARNING, "unexpected exception", ioe);
            Assertions.fail("Constructor threw IOException");
        }
        Assertions.assertTrue(Arrays.equals(b1, b3), "error in encrypt/decrypt cycle with " + alg + " (same reserialized object)");
    }

    @Test
    public void fuzzingSymmetricEncryption() {
        for(Algorithm alg: Algorithm.getAlgorithms( AlgorithmType.SYMMETRIC )) {
            for (int size : new int[]{alg.getKeySize( SecurityLevel.LOW ), alg.getKeySize( SecurityLevel.MEDIUM ), alg.getKeySize( SecurityLevel.HIGH ), alg.getKeySize( SecurityLevel.QUANTUM )}) {
                try {
                    int j = (int) Math.min(Math.pow(2, ksDisc / 8 / size), 100);
                    LOGGER.log(Level.INFO, "Testing " + alg + "/" + size + " (" + j + " passes)");
                    for (int i = 0; i < j; i++) {
                        symmetricEncryptionTestRun(alg, size);
                    }
                } catch (Exception e) {
                    LOGGER.log(Level.WARNING, "Unexpected exception", e);
                    Assertions.fail("fuzzer encountered exception in Symmetric en/decryption test with algorithm " + alg);
                }
            }
        }
    }

    @Test
    public void transferKeyTest() {
        for(Algorithm alg: Algorithm.getAlgorithms( AlgorithmType.SYMMETRIC )) {
            int size = alg.getKeySize();
            try {
                LOGGER.log( Level.INFO, "starting tests with " + alg + " and keysize " + size );
                for (int i = 0; i < ksDisc / size; i++) {
                    LOGGER.log( Level.FINE, "starting test " + (i + 1) + " of " + ksDisc / size );
                    System.out.print(".");
                    SymmetricKey k1 = new SymmetricKey(alg, Padding.getDefault(AlgorithmType.SYMMETRIC),Mode.getDefault( AlgorithmType.SYMMETRIC ));
                    SymmetricKey k2 = new SymmetricKey(alg, Padding.getDefault(AlgorithmType.SYMMETRIC),Mode.getDefault( AlgorithmType.SYMMETRIC ));
                    k2.setKey( k1.getKey() );
                    k2.setIv( k1.getIv() );
                    Assertions.assertTrue(k1.equals( k2 ), "error in key transfer cycle with "+alg+" ");
                    Assertions.assertTrue(Arrays.equals(k1.toBytes(DumpType.ALL_UNENCRYPTED),k2.toBytes(DumpType.ALL_UNENCRYPTED)), "reencode error in key transfer cycle with "+alg+" ");
                }
                System.out.println("");
            } catch(Exception e) {
                LOGGER.log( Level.WARNING,"Unexpected exception",e);
                Assertions.fail("fuzzer encountered exception in Symmetric en/decryption test with algorithm "+ alg);
            }
        }
    }

    @Test
    public void fuzzingSymmetricKeyPadding() throws IOException {
        for (Padding p : Padding.getAlgorithms( AlgorithmType.SYMMETRIC )) {
            try {
                for (Algorithm a : Algorithm.getAlgorithms( AlgorithmType.SYMMETRIC )) {
                    LOGGER.log( Level.INFO, "testing " + a + "/" + p );
                    int maximumPayload = -1;
                    int size = 0;
                    SecurityLevel sl = SecurityLevel.LOW;
                    while (maximumPayload < 0 && maximumPayload != -100000) {
                        size = a.getBlockSize( sl );
                        maximumPayload = p.getMaxSize( size );
                        if (maximumPayload < 0 && sl == SecurityLevel.QUANTUM) {
                            LOGGER.log( Level.INFO, "  skipping test for " + a + "/" + p + " due to insufficient key length" );
                            maximumPayload = -100000;
                        } else if (maximumPayload < 0) {
                            sl = sl.next();
                        }
                    }
                    if( "aes".equals(a.getAlgorithmFamily().toLowerCase() ) ) {
                        // AES has a fixed block size of 128 bits
                        maximumPayload=16;
                    }
                    if (maximumPayload == -100000) {
                        LOGGER.log( Level.INFO, "  What is going on here??" );
                        break;
                    }
                    LOGGER.log( Level.INFO, "  testing " + a + "/" + p + " with level "+sl+" (size: "+maximumPayload+")" );
                    for (int i = 0; i < 100; i++) {
                        SymmetricKey ak = new SymmetricKey( a, p, Mode.getDefault( AlgorithmType.SYMMETRIC ) );
                        Assertions.assertTrue(maximumPayload > 1, "negative maximum payload for " + a + "/" + size + "/" + p);
                        byte[] b = new byte[maximumPayload];
                        sr.nextBytes(b);
                        byte[] b2 = ak.decrypt(ak.encrypt(b));

                        Assertions.assertTrue(b.length==b2.length, "byte arrays must be of equal size after redecryption (b: "+b.length+"; b2: "+b2.length+")");
                        Assertions.assertTrue(Arrays.equals(b, b2), "byte arrays must be equal after redecryption");
                    }
                    LOGGER.log( Level.INFO, "  done " + a + "/" + p + " with level "+sl );

                }
            } catch (IOException ioe) {
                throw ioe;
            }
        }
    }

    @Test
    public void writeAsAsn1() {
        for (Padding p : Padding.getAlgorithms( AlgorithmType.SYMMETRIC )) {
            for (Algorithm a : Algorithm.getAlgorithms(AlgorithmType.SYMMETRIC)) {
                try {
                    SymmetricKey ak = new SymmetricKey(a, p, Mode.getDefault(AlgorithmType.SYMMETRIC));
                    File f = new File("testfile_SymmetricKey_" + p +"_"+ a.getAlgorithmFamily() + ".der");
                    OutputStream o = new FileOutputStream(f);
                    o.write(ak.toBytes(DumpType.ALL_UNENCRYPTED));
                    o.close();
                } catch (Exception e) {
                    e.printStackTrace();
                    Assertions.fail("unexpected exception");
                }
            }
        }
    }


}
