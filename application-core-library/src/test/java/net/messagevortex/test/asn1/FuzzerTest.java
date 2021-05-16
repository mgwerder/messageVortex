package net.messagevortex.test.asn1;

import net.messagevortex.ExtendedSecureRandom;
import net.messagevortex.MessageVortexLogger;
import net.messagevortex.asn1.*;
import net.messagevortex.asn1.encryption.Algorithm;
import net.messagevortex.asn1.encryption.AlgorithmType;
import net.messagevortex.asn1.encryption.DumpType;
import net.messagevortex.asn1.encryption.SecurityLevel;
import net.messagevortex.test.GlobalJunitExtension;
import org.bouncycastle.asn1.ASN1InputStream;
import org.bouncycastle.asn1.ASN1Sequence;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.util.Arrays;
import java.util.Date;
import java.util.Random;
import java.util.logging.Level;


/**
 * Fuzzer Tests for ASN1 Parser Classes {@link AbstractBlock}.
 *
 * @author martin@gwerder.net (Martin GWERDER)
 */
@ExtendWith(GlobalJunitExtension.class)
public class FuzzerTest {

    public static final int BLOCK_FUZZER_CYCLES = 30;
    private static final java.util.logging.Logger LOGGER;

    private static final ExtendedSecureRandom esr=new ExtendedSecureRandom();

    static {
        LOGGER = MessageVortexLogger.getLogger((new Throwable()).getStackTrace()[0].getClassName());
    }

    private final int ksDisc = 8192;

    @Test
    public void fuzzingMessage() throws Exception {
        try {
            AsymmetricKey ownIdentity = new AsymmetricKey(Algorithm.RSA.getParameters(SecurityLevel.LOW));
            for (int i = 0; i < BLOCK_FUZZER_CYCLES; i++) {
                LOGGER.log( Level.INFO, "Starting fuzzer cycle " + (i + 1) + " of " + BLOCK_FUZZER_CYCLES );
                IdentityBlock id = new IdentityBlock();
                id.setOwnIdentity( ownIdentity );
                PrefixBlock p=new PrefixBlock();
                p.setKey( new SymmetricKey() );
                IdentityBlock identity=new IdentityBlock();
                RoutingCombo routing=new RoutingCombo();
                VortexMessage s = new VortexMessage( p,new InnerMessageBlock( new PrefixBlock(),identity,routing ) );
                Assertions.assertTrue(s != null, "VortexMessage may not be null");
                byte[] b1 = s.toBytes(DumpType.ALL_UNENCRYPTED);
                Assertions.assertTrue(b1 != null, "Byte representation may not be null");
                VortexMessage s2=new VortexMessage( b1,null );
                Assertions.assertTrue(s.getPrefix().equals(s2.getPrefix()), "Contained PrefixBlock is not considered equivalent (original key="+s.getPrefix().getKey()+";new key="+s2.getPrefix().getKey()+")");
                byte[] b2 = (s2).toBytes(DumpType.ALL_UNENCRYPTED);
                Assertions.assertTrue(Arrays.equals( b1, b2 ), "Byte arrays should be equal when reencoding");
            }
        } catch (Exception e) {
            LOGGER.log(Level.WARNING,"Unexpected exception",e);

            throw e;
       }
    }

    @Test
    public void fuzzingPayloadChunk() {
        try {
            for (int i = 0; i < BLOCK_FUZZER_CYCLES; i++) {
                PayloadChunk s = new PayloadChunk();
                byte[] plb=new byte[esr.nextInt(1024*1024)];
                esr.nextBytes( plb );
                s.setPayload(plb);
                s.setId(1000);
                Assertions.assertTrue(s != null, "PayloadChunk may not be null");
                byte[] b1 = s.toBytes(DumpType.ALL_UNENCRYPTED);
                Assertions.assertTrue(b1 != null, "Byte representation may not be null");
                byte[] b2 = (new PayloadChunk( new ASN1InputStream(b1).readObject(),null )).toBytes(DumpType.ALL_UNENCRYPTED);
                Assertions.assertTrue(Arrays.equals( b1, b2 ), "Byte arrays should be equal when reencoding");
            }
        } catch (Exception e) {
            LOGGER.log(Level.WARNING,"Unexpected exception",e);
            Assertions.fail("fuzzer encountered exception in PayloadChunk ("+ e +")");
        }
    }

    @Test
    public void fuzzingIdentity() {
        String lastTuple = "";
        try {
            for (int i = 0; i < BLOCK_FUZZER_CYCLES; i++) {
                IdentityBlock s = new IdentityBlock();
                Assertions.assertTrue(s != null, "IdentityBlock may not be null");
                byte[] b1 = s.toBytes(DumpType.ALL_UNENCRYPTED);
                Assertions.assertTrue(b1 != null, "Byte representation may not be null");
                IdentityBlock s2 = new IdentityBlock( b1 );
                byte[] b2 = (s2).toBytes(DumpType.ALL_UNENCRYPTED);
                lastTuple = s.dumpValueNotation( "" ) + "\n" + s2.dumpValueNotation( "" );
                Assertions.assertTrue(Arrays.equals( b1, b2 ), "Byte arrays should be equal when reencoding");
            }
        } catch (Exception e) {
            LOGGER.log( Level.WARNING, "Unexpected exception (" + lastTuple + ")", e );
            Assertions.fail("fuzzer encountered exception in IdentityBlock ("+ e +")");
        }
    }

    @Test
    public void fuzzingUsagePeriod() {
        Random r = new Random();
        String lastTuple = "";
        try {
            for (int i = 0; i < BLOCK_FUZZER_CYCLES; i++) {
                UsagePeriod s = new UsagePeriod( r.nextInt(3600*24*356) );
                if(r.nextInt(10)>8) {
                    s.setNotBefore(new Date());
                }
                if(r.nextInt(10)>8) {
                    s.setNotAfter(new Date());
                }
                Assertions.assertTrue(s != null, "UsagePeriod may not be null");
                byte[] b1 = s.toBytes(DumpType.ALL_UNENCRYPTED);
                Assertions.assertTrue(b1 != null, "Byte representation may not be null");
                UsagePeriod s2=new UsagePeriod( b1 );
                byte[] b2 = (s2).toBytes(DumpType.ALL_UNENCRYPTED);
                lastTuple = s.dumpValueNotation( "",DumpType.ALL_UNENCRYPTED ) + "\n" + s2.dumpValueNotation( "",DumpType.ALL_UNENCRYPTED );
                Assertions.assertTrue(Arrays.equals( b1, b2 ), "Byte arrays should be equal when reencoding ("+s.getNotBefore()+"/"+s.getNotAfter()+") ["+b1.length+"/"+b2.length+"]");
            }
        } catch (Exception e) {
            LOGGER.log( Level.WARNING, "Unexpected exception (" + lastTuple + ")", e );
            Assertions.fail("fuzzer encountered exception in UsagePeriod ("+ e +")");
        }
    }

    @Test
    public void fuzzingSymmetricEncryption() {
        for(Algorithm alg: Algorithm.getAlgorithms( AlgorithmType.SYMMETRIC )) {
            try {
                LOGGER.log( Level.INFO, "Testing " + alg + " (" + ksDisc / 16 + ")" );
                for (int i = 0; i < ksDisc / 16; i++) {
                    SymmetricKey s = new SymmetricKey( alg );
                    byte[] b1=new byte[esr.nextInt(64*1024)];
                    esr.nextBytes( b1 );
                    byte[] b2=s.decrypt( s.encrypt(b1) );
                    Assertions.assertTrue(Arrays.equals( b1,b2), "error in encrypt/decrypt cycle with "+alg+" (same object)");
                    b2=(new SymmetricKey(s.toBytes(DumpType.ALL_UNENCRYPTED))).decrypt( s.encrypt(b1) );
                    Assertions.assertTrue(Arrays.equals( b1,b2), "error in encrypt/decrypt cycle with "+alg+" (same reserialized object)");
                }
            } catch(Exception e) {
                LOGGER.log(Level.WARNING,"Unexpected exception",e);
                Assertions.fail("fuzzer encountered exception in Symmetric en/decryp test with algorithm "+alg.toString());
            }
        }
    }

    @Test
    public void fuzzingSymmetricKey() throws Exception {
        for(Algorithm alg: Algorithm.getAlgorithms( AlgorithmType.SYMMETRIC )) {
            try {
                int size=alg.getKeySize()/8;
                LOGGER.log(Level.INFO,"testing "+alg+" ("+ksDisc/size+")");
                for (int i = 0; i < ksDisc/size; i++) {
                    SymmetricKey s = new SymmetricKey( alg );
                    Assertions.assertTrue(s!=null, "Symmetric may not be null");
                    byte[] b1=s.toBytes(DumpType.ALL_UNENCRYPTED);
                    Assertions.assertTrue(b1!=null, "Byte representation may not be null");
                    byte[] b2 = (new SymmetricKey( b1, null )).toBytes(DumpType.ALL_UNENCRYPTED);
                    Assertions.assertTrue(Arrays.equals(b1,b2), "Byte arrays should be equal when reencoding");
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
                LOGGER.log(Level.INFO,"creating store");
                IdentityStore s = IdentityStore.getIdentityStoreDemo();
                Assertions.assertTrue(s!=null, "IdentityStore may not be null");
                LOGGER.log(Level.INFO,"encoding demo binary");
                byte[] b1=s.toBytes(DumpType.ALL_UNENCRYPTED);
                Assertions.assertTrue(b1!=null, "Byte representation may not be null");
                LOGGER.log(Level.INFO,"reencoding demo binary");
                byte[] b2=(new IdentityStore(b1)).toBytes(DumpType.ALL_UNENCRYPTED);
                if(!Arrays.equals(b1,b2)) {
                    System.out.println((new IdentityStore( b1 ) ).dumpValueNotation( "",DumpType.ALL_UNENCRYPTED ));
                    System.out.println((new IdentityStore( b2 ) ).dumpValueNotation( "",DumpType.ALL_UNENCRYPTED ));
                    System.out.flush();
                    Assertions.fail("Byte arrays should be equal when reencoding");
                }
            }
        } catch(Exception e) {
            LOGGER.log(Level.WARNING,"Unexpected exception",e);
            throw e;
        }
    }

    @Test
    public void fuzzingRoutingBlock() throws Exception {
        AsymmetricKeyPreCalculator.setCacheFileName("");
        try{
            for (int i = 0; i < ksDisc/8192; i++) {
                LOGGER.log(Level.INFO,"creating router block");
                RoutingCombo routing=new RoutingCombo();
                Assertions.assertTrue(routing!=null, "Routing Block may not be null");
                LOGGER.log(Level.INFO,"encoding binary");
                byte[] b1=routing.toBytes(DumpType.ALL_UNENCRYPTED);
                Assertions.assertTrue(b1!=null, "Byte representation may not be null");
                LOGGER.log(Level.INFO,"reencoding from binary");
                RoutingCombo routing2=new RoutingCombo(ASN1Sequence.getInstance(b1));
                byte[] b2=(routing2).toBytes(DumpType.ALL_UNENCRYPTED);
                Assertions.assertTrue(Arrays.equals(b1,b2), "Byte arrays should be equal when reencoding");
                Assertions.assertTrue(routing.dumpValueNotation("",DumpType.ALL_UNENCRYPTED).equals(routing2.dumpValueNotation("",DumpType.ALL_UNENCRYPTED)), "ASN1 value dumps should be equal when reencoding (ALL_UNENCRYPTED)");
                Assertions.assertTrue(routing.dumpValueNotation("",DumpType.PUBLIC_ONLY).equals(routing2.dumpValueNotation("",DumpType.PUBLIC_ONLY)), "ASN1 value dumps should be equal when reencoding (PUBLIC_ONLY)");
                Assertions.assertTrue(routing.equals(routing2), "equal() should be true when reencoding");
            }
        } catch(Exception e) {
            LOGGER.log(Level.WARNING,"Unexpected exception",e);
            throw e;
        }
    }
}
