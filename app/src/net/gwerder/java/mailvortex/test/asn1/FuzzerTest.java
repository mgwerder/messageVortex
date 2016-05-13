package net.gwerder.java.mailvortex.test.asn1;

import static net.gwerder.java.mailvortex.asn1.Key.Algorithm.AES128;
import static net.gwerder.java.mailvortex.asn1.Key.Algorithm.AES192;
import static net.gwerder.java.mailvortex.asn1.Key.Algorithm.AES256;
import static org.junit.Assert.assertEquals;

import net.gwerder.java.mailvortex.asn1.*;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import static org.junit.Assert.*;

import java.security.SecureRandom;
import java.util.Arrays;
import java.util.Random;
import java.util.logging.Level;
import net.gwerder.java.mailvortex.*;

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


    static {
        MailvortexLogger.setGlobalLogLevel(Level.ALL);
    }

    @Test
    public void FuzzingMessage() {
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
            e.printStackTrace();
            fail( "fuzzer encountered exception in Message ("+e.toString()+")" );
        }
    }

    @Test
    public void FuzzingPayload() {
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
            fail( "fuzzer encountered exception in Payload ("+e.toString()+")" );
            e.printStackTrace();
        }
    }

    @Test
    public void FuzzingPayloadChunk() {
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
            fail( "fuzzer encountered exception in PayloadChunk ("+e.toString()+")" );
            e.printStackTrace();
        }
    }

    @Test
    public void FuzzingIdentity() {
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
            fail( "fuzzer encountered exception in Identity ("+e.toString()+")" );
            e.printStackTrace();
        }
    }

    @Test
    public void FuzzingUsagePeriod() {
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
            fail( "fuzzer encountered exception in UsagePeriod ("+e.toString()+")" );
            e.printStackTrace();
        }
    }

    @Test
    public void FuzzingSymmetricKey() {
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
                fail("fuzzer encountered exception in Symmetric key with algorithm "+alg.toString());
                e.printStackTrace();
            }
        }
    }

    @Test
    public void FuzzingAsymmetricKey() {
        for(Key.Algorithm alg: Key.Algorithm.getAlgorithms( Block.AlgorithmType.ASYMMETRIC )) {
            for (int ks : new int[]{512, 1024, 2048, 4096 }) {
                if(alg!=Key.Algorithm.DSA || ks<2048) {
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
                        fail( "fuzzer encountered exception in Asymmetric key with algorithm " + alg.toString() +"/"+ ks+" ("+e.toString()+")" );
                        e.printStackTrace();
                    }
                }
            }
        }
    }

}