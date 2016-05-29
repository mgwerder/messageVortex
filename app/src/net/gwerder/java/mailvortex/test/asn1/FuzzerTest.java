package net.gwerder.java.mailvortex.test.asn1;

import net.gwerder.java.mailvortex.MailvortexLogger;
import net.gwerder.java.mailvortex.asn1.*;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.security.SecureRandom;
import java.util.Arrays;
import java.util.Random;
import java.util.logging.Level;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * Fuzzer Tests for ASN1 Parser Classes {@link net.gwerder.java.mailvortex.asn1.Block}.
 *
 * @author martin@gwerder.net (Martin GWERDER)
 */
@RunWith(JUnit4.class)
public class FuzzerTest {

    public static final int ASYMMETRIC_FUZZER_CYCLES=10;
    public static final int SYMMETRIC_FUZZER_CYCLES =1000;

    public static final int BLOCK_FUZZER_CYCLES =1000;

    private static final java.util.logging.Logger LOGGER;

    static {
        LOGGER = MailvortexLogger.getLogger((new Throwable()).getStackTrace()[0].getClassName());
        MailvortexLogger.setGlobalLogLevel(Level.ALL);
    }

    @Test
    public void fuzzingMessage() {
        try {
            for (int i = 0; i < BLOCK_FUZZER_CYCLES; i++) {
                Message s = new Message(new Identity(),new Payload());
                assertTrue( "Message may not be null", s != null );
                byte[] b1 = s.toBytes();
                assertTrue( "Byte representation may not be null", b1 != null );
                byte[] b2 = (new Message( b1 )).toBytes();
                assertTrue( "Byte arrays should be equal when reencoding", Arrays.equals( b1, b2 ) );
            }
        } catch (Exception e) {
            LOGGER.log(Level.WARNING,"Unexpected exception",e);
            fail( "fuzzer encountered exception in Message ("+e.toString()+")" );
        }
    }

    @Test
    public void fuzzingPayload() {
        try {
            for (int i = 0; i < BLOCK_FUZZER_CYCLES; i++) {
                Payload s = new Payload();
                assertTrue( "Payload may not be null", s != null );
                byte[] b1 = s.toBytes();
                assertTrue( "Byte representation may not be null", b1 != null );
                byte[] b2 = (new Payload( b1 )).toBytes();
                assertTrue( "Byte arrays should be equal when reencoding", Arrays.equals( b1, b2 ) );
            }
        } catch (Exception e) {
            LOGGER.log(Level.WARNING,"Unexpected exception",e);
            fail( "fuzzer encountered exception in Payload ("+e.toString()+")" );
        }
    }

    @Test
    public void fuzzingPayloadChunk() {
        try {
            SecureRandom r=new SecureRandom(  );
            for (int i = 0; i < BLOCK_FUZZER_CYCLES; i++) {
                PayloadChunk s = new PayloadChunk();
                byte[] plb=new byte[r.nextInt(1024*1024)];
                r.nextBytes( plb );
                s.setPayload(plb);
                assertTrue( "PayloadChunk may not be null", s != null );
                byte[] b1 = s.toBytes();
                assertTrue( "Byte representation may not be null", b1 != null );
                byte[] b2 = (new PayloadChunk( b1 )).toBytes();
                assertTrue( "Byte arrays should be equal when reencoding", Arrays.equals( b1, b2 ) );
            }
        } catch (Exception e) {
            LOGGER.log(Level.WARNING,"Unexpected exception",e);
            fail( "fuzzer encountered exception in PayloadChunk ("+e.toString()+")" );
        }
    }

    @Test
    public void fuzzingIdentity() {
        try {
            for (int i = 0; i < BLOCK_FUZZER_CYCLES; i++) {
                Identity s = new Identity();
                assertTrue( "Identity may not be null", s != null );
                byte[] b1 = s.toBytes();
                assertTrue( "Byte representation may not be null", b1 != null );
                Identity s2=new Identity( b1,null );
                byte[] b2 = (s2).toBytes();
                System.out.println("dumping object tuple \n"+s.dumpValueNotation( "" )+"\n"+s2.dumpValueNotation( "" ));
                assertTrue( "Byte arrays should be equal when reencoding", Arrays.equals( b1, b2 ) );
            }
        } catch (Exception e) {
            LOGGER.log(Level.WARNING,"Unexpected exception",e);
            fail( "fuzzer encountered exception in Identity ("+e.toString()+")" );
        }
    }

    @Test
    public void fuzzingUsagePeriod() {
        Random r = new Random();
        try {
            for (int i = 0; i < BLOCK_FUZZER_CYCLES; i++) {
                UsagePeriod s = new UsagePeriod( r.nextInt(3600*24*356) );
                if(r.nextInt(10)>8) {
                    s.setNotBefore(null);
                }
                if(r.nextInt(10)>8) {
                    s.setNotAfter(null);
                }
                assertTrue( "UsagePeriod may not be null", s != null );
                byte[] b1 = s.toBytes();
                assertTrue( "Byte representation may not be null", b1 != null );
                UsagePeriod s2=new UsagePeriod( b1 );
                byte[] b2 = (s2).toBytes();
                System.out.println("dumping object tuple \n"+s.dumpValueNotation( "" )+"\n"+s2.dumpValueNotation( "" ));
                assertTrue( "Byte arrays should be equal when reencoding ("+s.getNotBefore()+"/"+s.getNotAfter()+") ["+b1.length+"/"+b2.length+"]", Arrays.equals( b1, b2 ) );
            }
        } catch (Exception e) {
            LOGGER.log(Level.WARNING,"Unexpected exception",e);
            fail( "fuzzer encountered exception in UsagePeriod ("+e.toString()+")" );
        }
    }

    @Test
    public void fuzzingSymmetricEncryption() {
        SecureRandom sr=new SecureRandom(  );
        for(Key.Algorithm alg: Key.Algorithm.getAlgorithms( Block.AlgorithmType.SYMMETRIC )) {
            try {
                System.out.println("Testing "+alg+" ("+SYMMETRIC_FUZZER_CYCLES+")");
                for (int i = 0; i < SYMMETRIC_FUZZER_CYCLES; i++) {
                    SymmetricKey s = new SymmetricKey( alg );
                    byte[] b1=new byte[sr.nextInt(64*1024)];
                    sr.nextBytes( b1 );
                    byte[] b2=s.decrypt( s.encrypt(b1) );
                    assertTrue( "error in encrypt/decrypt cycle with "+alg+" (same object)",Arrays.equals( b1,b2));
                    b2=(new SymmetricKey(s.toBytes())).decrypt( s.encrypt(b1) );
                    assertTrue( "error in encrypt/decrypt cycle with "+alg+" (same reserialized object)",Arrays.equals( b1,b2));
                }
            } catch(Exception e) {
                LOGGER.log(Level.WARNING,"Unexpected exception",e);
                fail("fuzzer encountered exception in Symmetric en/decryp test with algorithm "+alg.toString());
            }
        }
    }

    @Test
    public void fuzzingAsymmetricEncryption() {
        SecureRandom sr=new SecureRandom(  );
        for(Key.Algorithm alg: Key.Algorithm.getAlgorithms( Block.AlgorithmType.ASYMMETRIC )) {
            for(int size:new int[] {1024,2048}) {
                try {
                    System.out.print("Testing "+alg+"/"+size+" ("+ASYMMETRIC_FUZZER_CYCLES+")");
                    for (int i = 0; i < ASYMMETRIC_FUZZER_CYCLES; i++) {
                        System.out.print(".");
                        AsymmetricKey s = new AsymmetricKey(alg,size);
                        byte[] b1=new byte[sr.nextInt(size/8-11)];
                        sr.nextBytes( b1 );
                        byte[] b2=s.decrypt( s.encrypt(b1) );
                        assertTrue( "error in encrypt/decrypt cycle with "+alg+" (same object)",Arrays.equals( b1,b2));
                        b2=(new AsymmetricKey(s.toBytes())).decrypt( s.encrypt(b1) );
                        assertTrue( "error in encrypt/decrypt cycle with "+alg+" (same reserialized object)",Arrays.equals( b1,b2));
                    }
                    System.out.println("");
                } catch(Exception e) {
                    LOGGER.log(Level.WARNING,"Unexpected exception",e);
                    fail("fuzzer encountered exception in Symmetric en/decryption test with algorithm "+alg.toString());
                }
            }
        }
    }

    @Test
    public void fuzzingSymmetricKey() {
        for(Key.Algorithm alg: Key.Algorithm.getAlgorithms( Block.AlgorithmType.SYMMETRIC )) {
            try {
                System.out.println("Testing "+alg+" ("+SYMMETRIC_FUZZER_CYCLES+")");
                for (int i = 0; i < SYMMETRIC_FUZZER_CYCLES; i++) {
                    SymmetricKey s = new SymmetricKey( alg );
                    assertTrue( "Symmetric may not be null",s!=null);
                    byte[] b1=s.toBytes();
                    assertTrue( "Byte representation may not be null",b1!=null);
                    byte[] b2=(new SymmetricKey(b1,null,false)).toBytes();
                    assertTrue( "Byte arrays should be equal when reencoding",Arrays.equals(b1,b2));
                }
            } catch(Exception e) {
                LOGGER.log(Level.WARNING,"Unexpected exception",e);
                fail("fuzzer encountered exception in Symmetric key with algorithm "+alg.toString());
            }
        }
    }

    @Test
    public void fuzzingAsymmetricKey() {
        for(Key.Algorithm alg: Key.Algorithm.getAlgorithms( Block.AlgorithmType.ASYMMETRIC )) {
            for (int ks : new int[]{512, 1024, 2048, 4096 }) {
//                if(alg!=Key.Algorithm.DSA || ks<2048) {
                    System.out.println("Testing "+alg+"/"+ks+" ("+ASYMMETRIC_FUZZER_CYCLES+")");
                    try {
                        for (int i = 0; i < ASYMMETRIC_FUZZER_CYCLES; i++) {
                            AsymmetricKey s = new AsymmetricKey( alg, ks );
                            assertTrue( "Asymmetric may not be null", s != null );
                            byte[] b1 = s.toBytes();
                            s.dumpValueNotation( "" );
                            assertTrue( "Byte representation may not be null", b1 != null );
                            AsymmetricKey s2=new AsymmetricKey( b1 );
                            byte[] b2 = (s2).toBytes();
                            System.out.println("dumping object tuple \n"+s.dumpValueNotation( "" )+"\n"+s2.dumpValueNotation( "" ));
                            assertTrue( "Byte arrays should be equal when reencoding", Arrays.equals( b1, b2 ) );
                        }
                    } catch (Exception e) {
                        LOGGER.log(Level.WARNING,"Unexpected exception",e);
                        fail( "fuzzer encountered exception in Asymmetric key with algorithm " + alg.toString() +"/"+ ks+" ("+e.toString()+")" );
                    }
                }
 //           }
        }
    }

}