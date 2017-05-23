package net.gwerder.java.messagevortex.test.asn1;

import net.gwerder.java.messagevortex.MessageVortexLogger;
import net.gwerder.java.messagevortex.asn1.SymmetricKey;
import net.gwerder.java.messagevortex.asn1.encryption.*;
import org.junit.Test;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.Vector;
import java.util.logging.Level;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * Tests basic functions of symmetric encryption.
 *
 * Created by martin.gwerder on 03.06.2016.
 */
public class SymmetricKeyTest {

    private static final java.util.logging.Logger LOGGER;
    private static final Random sr = new Random();

    static {
        LOGGER = MessageVortexLogger.getLogger((new Throwable()).getStackTrace()[0].getClassName());
        MessageVortexLogger.setGlobalLogLevel( Level.ALL);
    }

    private int ksDisc=16384;

    @Test
    public void symmetricKeySizeTest() {
        assertTrue( "getKeySize for AES256 is bad", Algorithm.AES128.getKeySize() == 128 );
        assertTrue( "getKeySize for AES256 is bad", Algorithm.AES256.getKeySize() == 256 );
        assertTrue( "getKeySize for CAMELLIA256 is bad", Algorithm.CAMELLIA192.getKeySize() == 192 );
        assertTrue( "getKeySize for CAMELLIA256 is bad", Algorithm.CAMELLIA256.getKeySize() == 256 );
    }

    @Test
    public void fuzzingSymmetricEncryption() throws Exception {
        class TestThread extends Thread {
            private int size;
            private Algorithm alg;
            private Exception ex=null;

            private TestThread(int size, Algorithm alg) {
                this.size = size;
                this.alg = alg;
            }

            public void run() {
                LOGGER.log(Level.INFO, "Testing " + alg + "/" + size + "");
                LOGGER.log(Level.INFO, "  creating key");
                SymmetricKey s = null;
                try {
                    s = new SymmetricKey(alg, Padding.getDefault(alg.getAlgorithmType()) , Mode.getDefault(alg.getAlgorithmType()));
                } catch (Exception ioe) {
                    setException(ioe);
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
                    setException(ioe);
                    LOGGER.log(Level.WARNING, "unexpected exception", ioe);
                    fail("IOException while reencrypting");
                }
                assertTrue("error in encrypt/decrypt cycle with " + alg + " (same object)", Arrays.equals(b1, b3));
                LOGGER.log(Level.INFO, "  doing an encrypt/decrypt cycle with a reencoded key");
                try {
                    b3 = (new SymmetricKey(s.toBytes(DumpType.ALL_UNENCRYPTED))).decrypt(b2);
                } catch (Exception ioe) {
                    LOGGER.log(Level.WARNING, "unexpected exception", ioe);
                    setException(ioe);
                    fail("Constructor threw IOException");
                }
                assertTrue("error in encrypt/decrypt cycle with " + alg + " (same reserialized object)", Arrays.equals(b1, b3));
            }

            public Exception getException() { return ex; }

            public void setException(Exception e) {
                ex = e;
            }
        }

        List<Thread> t = new Vector<>();
        for(Algorithm alg: Algorithm.getAlgorithms( AlgorithmType.SYMMETRIC )) {
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
            for (Thread t1 : t) {
                t1.join();
                Exception e=((TestThread)(t1)).getException();
                if(e!=null) {
                    throw e;
                }
            }
        } catch (InterruptedException ie) {
            fail( "Got exception while waitinmg for end of tests" );
        }
    }

    @Test
    public void transferKeyTest() {
        for(Algorithm alg: Algorithm.getAlgorithms( AlgorithmType.SYMMETRIC )) {
            int size = alg.getKeySize();
            try {
                LOGGER.log( Level.INFO, "starting tests with " + alg.toString() + " and keysize " + size );
                for (int i = 0; i < ksDisc / size; i++) {
                    LOGGER.log( Level.FINE, "starting test " + (i + 1) + " of " + ksDisc / size );
                    System.out.print(".");
                    SymmetricKey k1 = new SymmetricKey(alg, Padding.getDefault(AlgorithmType.SYMMETRIC),Mode.getDefault( AlgorithmType.SYMMETRIC ));
                    SymmetricKey k2 = new SymmetricKey(alg, Padding.getDefault(AlgorithmType.SYMMETRIC),Mode.getDefault( AlgorithmType.SYMMETRIC ));
                    k2.setKey( k1.getKey() );
                    k2.setIV( k1.getIV() );
                    assertTrue( "error in key transfer cycle with "+alg+" ",k1.equals( k2 ));
                    assertTrue( "reencode error in key transfer cycle with "+alg+" ",Arrays.equals(k1.toBytes(DumpType.ALL_UNENCRYPTED),k2.toBytes(DumpType.ALL_UNENCRYPTED)));
                }
                System.out.println("");
            } catch(Exception e) {
                LOGGER.log( Level.WARNING,"Unexpected exception",e);
                fail("fuzzer encountered exception in Symmetric en/decryption test with algorithm "+alg.toString());
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
                        size = a.getKeySize( sl );
                        maximumPayload = p.getMaxSize( size );
                        if (maximumPayload < 0 && sl == SecurityLevel.QUANTUM) {
                            LOGGER.log( Level.INFO, "  skipping test for " + a + "/" + p + " due to insufficient key length" );
                            maximumPayload = -100000;
                        } else if (maximumPayload < 0) {
                            sl = sl.next();
                        }
                    }
                    if (maximumPayload == -100000) {
                        LOGGER.log( Level.INFO, "  What is going on here??" );
                        break;
                    }
                    LOGGER.log( Level.INFO, "  testing " + a + "/" + p + " with level "+sl );
                    for (int i = 0; i < 100; i++) {
                        SymmetricKey ak = new SymmetricKey( a, p, Mode.getDefault( AlgorithmType.SYMMETRIC ) );
                        assertTrue( "negative maximum payload for " + a.toString() + "/" + size + "/" + p.toString(), maximumPayload > 1 );
                        byte[] b = new byte[maximumPayload];
                        sr.nextBytes( b );
                        byte[] b2 = ak.decrypt( ak.encrypt( b ) );
                        assertTrue( "byte arrays mus be equal after redecryption", Arrays.equals( b, b2 ) );
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
                    File f = new File("testfile_SymmetricKey_" +p.toString()+"_"+ a.getAlgorithmFamily() + ".der");
                    OutputStream o = new FileOutputStream(f);
                    o.write(ak.toBytes(DumpType.ALL_UNENCRYPTED));
                    o.close();
                } catch (Exception e) {
                    fail("unexpected exception");
                }
            }
        }
    }


}
