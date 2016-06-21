package net.gwerder.java.mailvortex.test.asn1;

import net.gwerder.java.mailvortex.MailvortexLogger;
import net.gwerder.java.mailvortex.asn1.AsymmetricKey;
import net.gwerder.java.mailvortex.asn1.encryption.Algorithm;
import net.gwerder.java.mailvortex.asn1.encryption.AlgorithmType;
import net.gwerder.java.mailvortex.asn1.encryption.Padding;
import net.gwerder.java.mailvortex.asn1.encryption.SecurityLevel;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.Vector;
import java.util.logging.Level;

import static junit.framework.TestCase.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * Tests for AsymmetricKey class.
 *
 * Created by martin.gwerder on 31.05.2016.
 */
@RunWith(JUnit4.class)
public class AsymmetricKeyTest {

    private static final java.util.logging.Logger LOGGER;
    private static final Random sr = new Random();

    static {
        LOGGER = MailvortexLogger.getLogger((new Throwable()).getStackTrace()[0].getClassName());
        MailvortexLogger.setGlobalLogLevel(Level.ALL);
    }

    private int ksDisc=16384;

    @Test
    public void fuzzingAsymmetricEncryption() {
        class TestThread extends Thread {
            private int size;
            private Algorithm alg;

            private TestThread(int size, Algorithm alg) {
                this.size = size;
                this.alg = alg;
            }

            public void run() {
                LOGGER.log(Level.INFO, "Testing " + alg + "/" + size + "");
                LOGGER.log(Level.INFO, "  creating key");
                AsymmetricKey s = null;
                try {
                    s = new AsymmetricKey(alg, Padding.getDefault(alg.getAlgorithmType()), size);
                } catch (IOException ioe) {
                    LOGGER.log(Level.WARNING, "unexpected exception", ioe);
                    fail("Constructor threw IOException");
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
                    fail("IOException while reencrypting");
                }
                assertTrue("error in encrypt/decrypt cycle with " + alg + " (same object)", Arrays.equals(b1, b3));
                LOGGER.log(Level.INFO, "  doing an encrypt/decrypt cycle with a reencoded key");
                try {
                    b3 = (new AsymmetricKey(s.toBytes())).decrypt(b2);
                } catch (IOException ioe) {
                    LOGGER.log(Level.WARNING, "unexpected exception", ioe);
                    fail("Constructor threw IOException");
                }
                assertTrue("error in encrypt/decrypt cycle with " + alg + " (same reserialized object)", Arrays.equals(b1, b3));
                for (Algorithm a : Algorithm.getAlgorithms(AlgorithmType.HASHING)) {
                    b1 = new byte[sr.nextInt(4096) + 2048];
                    sr.nextBytes(b1);
                    byte[] sig = null;
                    try {
                        sig = s.sign(b1, a);
                    } catch (IOException ioe) {
                        LOGGER.log(Level.WARNING, "unexpected exception", ioe);
                        fail("Constructor threw IOException");
                    }
                    try {
                        LOGGER.log(Level.INFO, "  signing with " + a + " (signature size:" + sig.length + "; message size:" + b1.length + ")");
                        assertTrue("error in signature verification " + a + "With" + alg + "", s.verify(b1, sig, a));
                    } catch (IOException ioe) {
                        LOGGER.log(Level.WARNING, "unexpected exception", ioe);
                        fail("Constructor threw IOException");
                    }
                    try {
                        int pos = -1;
                        int value = 0;
                        while (pos == -1 || b1[pos] == (byte) (value)) {
                            pos = sr.nextInt(b1.length);
                            value = sr.nextInt(256);
                        }
                        byte old = b1[pos];
                        b1[pos] = (byte) value;
                        assertFalse("Error while verifying a bad signature (returned good; old was " + old + "; new was " + value + "; pos was " + pos + ")", s.verify(b1, sig, a));
                    } catch (IOException ioe) {
                        LOGGER.log(Level.FINE, "verification of bad signature threw an exception (this is OK)");
                    }
                }
            }
        }

        List<Thread> t = new Vector<>();
        for(Algorithm alg: Algorithm.getAlgorithms( AlgorithmType.ASYMMETRIC )) {
            for (int size : new int[]{alg.getKeySize( SecurityLevel.LOW ), alg.getKeySize( SecurityLevel.MEDIUM ), alg.getKeySize( SecurityLevel.HIGH ), alg.getKeySize( SecurityLevel.QUANTUM )})
                try {
                    int j = (int) Math.min( Math.pow( 2, ksDisc / 8 / size ), 100 );
                    LOGGER.log( Level.INFO, "Testing " + alg + "/" + size + " (" + j + " passes)" );
                    for (int i = 0; i < j; i++) {
                        Thread t1 = new TestThread(size, alg);
                        t.add( t1 );
                    }
                } catch (Exception e) {
                    LOGGER.log( Level.WARNING, "Unexpected exception", e );
                    fail( "fuzzer encountered exception in Symmetric en/decryption test with algorithm " + alg.toString() );
                }
        }

        for (Thread t1 : t) t1.start();
        try {
            for (Thread t1 : t) t1.join();
        } catch (InterruptedException ie) {
            fail( "Got exception while waitinmg for end of tests" );
        }
        throw new NullPointerException( "WARNING this test does not fail even if it should due to the threads!" );
    }

    @Test
    public void transferKeyTest() {
        for(Algorithm alg: Algorithm.getAlgorithms( AlgorithmType.ASYMMETRIC )) {
            int size = alg.getKeySize();
            try {
                LOGGER.log( Level.INFO, "starting tests with " + alg.getAlgorithm() + " and keysize " + size );
                for (int i = 0; i < ksDisc / size; i++) {
                    LOGGER.log( Level.FINE, "starting test " + (i + 1) + " of " + ksDisc / size );
                    System.out.print(".");
                    AsymmetricKey k1 = new AsymmetricKey(alg, Padding.getDefault(AlgorithmType.ASYMMETRIC),size);
                    AsymmetricKey k2 = new AsymmetricKey(alg, Padding.getDefault(AlgorithmType.ASYMMETRIC),size);
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

    @Test
    public void asymmetricKeySizeTest() {
        assertTrue( "getKeySize for SECP384R1 is bad", Algorithm.SECP384R1.getKeySize() == 384 );
    }

    @Test
    public void fuzzingAsymmetricKeyPadding() {
        for (Padding p : Padding.getAlgorithms( AlgorithmType.ASYMMETRIC )) {
            try {
                for (Algorithm a : Algorithm.getAlgorithms( AlgorithmType.ASYMMETRIC )) {
                    LOGGER.log( Level.INFO, "testing " + a + "/" + p );
                    int maximumPayload = -1;
                    int size = 0;
                    SecurityLevel sl = SecurityLevel.LOW;
                    while (maximumPayload < 0 && maximumPayload != -100000) {
                        size = a.getKeySize( sl );
                        maximumPayload = p.getMaxSize( size );
                        if (maximumPayload < 0 && sl == SecurityLevel.QUANTUM) {
                            LOGGER.log( Level.INFO, "skipping test for " + a + "/" + p + " due to insufficient key length" );
                            maximumPayload = -100000;
                        } else if (maximumPayload < 0) {
                            sl = sl.next();
                        }
                    }
                    if (maximumPayload == -100000) break;
                    LOGGER.log( Level.INFO, "  minimum securityLevel is " + sl );
                    for (int i = 0; i < 100; i++) {
                        AsymmetricKey ak = new AsymmetricKey( a, p, size );
                        assertTrue( "negative maximum payload for " + a.getAlgorithm() + "/" + size + "/" + p.getPadding(), maximumPayload > 1 );
                        byte[] b = new byte[maximumPayload];
                        sr.nextBytes( b );
                        byte[] b2 = ak.decrypt( ak.encrypt( b ) );
                        assertTrue( "byte arrays mus be equal after redecryption", Arrays.equals( b, b2 ) );
                    }

                }
            } catch (IOException ioe) {
                fail( "got exception while fuzzing padding" );
            }
        }
    }
}
