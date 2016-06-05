package net.gwerder.java.mailvortex.test.asn1;

import net.gwerder.java.mailvortex.MailvortexLogger;
import net.gwerder.java.mailvortex.asn1.*;
import net.gwerder.java.mailvortex.asn1.encryption.Algorithm;
import net.gwerder.java.mailvortex.asn1.encryption.AlgorithmType;
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

    public static final int BLOCK_FUZZER_CYCLES = 30;
    private static final java.util.logging.Logger LOGGER;

    static {
        LOGGER = MailvortexLogger.getLogger((new Throwable()).getStackTrace()[0].getClassName());
        MailvortexLogger.setGlobalLogLevel(Level.ALL);
    }

    private int ksDisc = 16384;

    @Test
    public void fuzzingMessage() throws Exception {
        try {
            AsymmetricKey ownIdentity = new AsymmetricKey();
            for (int i = 0; i < BLOCK_FUZZER_CYCLES; i++) {
                LOGGER.log( Level.INFO, "Starting fuzzer cycle " + (i + 1) + " of " + BLOCK_FUZZER_CYCLES );
                Identity id = new Identity();
                id.setOwnIdentity( ownIdentity );
                Message s = new Message( id, new Payload() );
                assertTrue( "Message may not be null", s != null );
                byte[] b1 = s.toBytes();
                assertTrue( "Byte representation may not be null", b1 != null );
                byte[] b2 = (new Message( b1 )).toBytes();
                assertTrue( "Byte arrays should be equal when reencoding", Arrays.equals( b1, b2 ) );
            }
        } catch (Exception e) {
            LOGGER.log(Level.WARNING,"Unexpected exception",e);

            throw e;
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
                Identity s2 = new Identity( b1 );
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
        for(Algorithm alg: Algorithm.getAlgorithms( AlgorithmType.SYMMETRIC )) {
            try {
                LOGGER.log( Level.INFO, "Testing " + alg + " (" + ksDisc / 16 + ")" );
                for (int i = 0; i < ksDisc / 16; i++) {
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
    public void fuzzingSymmetricKey() throws Exception {
        for(Algorithm alg: Algorithm.getAlgorithms( AlgorithmType.SYMMETRIC )) {
            try {
                System.out.println("Testing "+alg+" ("+ksDisc+")");
                for (int i = 0; i < ksDisc; i++) {
                    SymmetricKey s = new SymmetricKey( alg );
                    assertTrue( "Symmetric may not be null",s!=null);
                    byte[] b1=s.toBytes();
                    assertTrue( "Byte representation may not be null",b1!=null);
                    byte[] b2 = (new SymmetricKey( b1, null )).toBytes();
                    assertTrue( "Byte arrays should be equal when reencoding",Arrays.equals(b1,b2));
                }
            } catch(Exception e) {
                LOGGER.log(Level.WARNING,"Unexpected exception",e);
                throw e;
            }
        }
    }

    @Test
    public void fuzzingIdentityStore() throws Exception {
        try{
            LOGGER.log(Level.INFO,"testing with "+ksDisc/8192+" stores");
            for (int i = 0; i < ksDisc/8192; i++) {
                IdentityStore.resetDemo();
                LOGGER.log(Level.FINE,"creating store");
                IdentityStore s = IdentityStore.getIdentityStoreDemo();
                assertTrue( "IdentityStore may not be null",s!=null);
                LOGGER.log(Level.FINE,"encoding demo binary");
                byte[] b1=s.toBytes();
                assertTrue( "Byte representation may not be null",b1!=null);
                LOGGER.log(Level.FINE,"reencoding demo binary");
                byte[] b2=(new IdentityStore(b1)).toBytes();
                if(!Arrays.equals(b1,b2)) {
                    System.out.println((new IdentityStore( b1 ) ).dumpValueNotation( "" ));
                    System.out.println((new IdentityStore( b2 ) ).dumpValueNotation( "" ));
                    System.out.flush();
                    fail( "Byte arrays should be equal when reencoding" );
                }
            }
        } catch(Exception e) {
            LOGGER.log(Level.WARNING,"Unexpected exception",e);
            throw e;
        }
    }

}