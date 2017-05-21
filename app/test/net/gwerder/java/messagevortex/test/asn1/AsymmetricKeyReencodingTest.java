package net.gwerder.java.messagevortex.test.asn1;

import net.gwerder.java.messagevortex.ExtendedSecureRandom;
import net.gwerder.java.messagevortex.MessageVortexLogger;
import net.gwerder.java.messagevortex.asn1.AlgorithmParameter;
import net.gwerder.java.messagevortex.asn1.AsymmetricKey;
import net.gwerder.java.messagevortex.asn1.encryption.*;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import java.util.*;
import java.util.logging.Level;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * Tests reencoding of asymetric keys.
 *
 * Created by martin.gwerder on 31.05.2016.
 */
@RunWith(Parameterized.class)
public class AsymmetricKeyReencodingTest {

    static{
        // start key precalculator
        AsymmetricKey.setCacheFileName("AsymmetricKey.cache");
    }

    private static final java.util.logging.Logger LOGGER;
    private static final int ksDisc = 8192; //16384
    private static ExtendedSecureRandom sr = new ExtendedSecureRandom();

    static {
        LOGGER = MessageVortexLogger.getLogger((new Throwable()).getStackTrace()[0].getClassName());
        MessageVortexLogger.setGlobalLogLevel(Level.ALL);
    }

    private String testname;
    private AlgorithmParameter parameter;
    private int repeat;

    public AsymmetricKeyReencodingTest(String testname, AlgorithmParameter p, int repeat) {
        this.testname = testname;
        this.parameter = p;
        this.repeat=repeat;
    }

    @Parameters(name = "{0}")
    public static Collection<Object[]> generateData() {
        List<Object[]> ret = new Vector<>();
        for(Algorithm alg: Algorithm.getAlgorithms( AlgorithmType.ASYMMETRIC )) {
            for (Map.Entry<SecurityLevel,AlgorithmParameter> params : alg.getParameters().entrySet() ) {
                int ks=Integer.parseInt(params.getValue().get(Parameter.KEYSIZE));
                int j = Math.min( (int) Math.pow( 2, ksDisc / ks ), 100 );
                ret.add( new Object[]{"" + params.getValue().get(Parameter.ALGORITHM) + "/" + ks, params.getValue(),j } );
            }
        }
        LOGGER.log( Level.INFO,"Prepared for fuzzer "+ret.size()+" tests");
        return ret;
    }


    @Test
    public void reencodingAsymetricKey()
    {
        try {
            LOGGER.log( Level.INFO, "running reencoding test for " + testname );
            for (int i = 0; i < repeat; i++) {
                LOGGER.log( Level.INFO, "  running reencoding round "+(i+1)+"/"+repeat+" for " + testname );
                LOGGER.log( Level.INFO, "  generating key of size "+parameter.get(Parameter.KEYSIZE) );
                AsymmetricKey s = new AsymmetricKey( parameter );
                LOGGER.log( Level.INFO, "  encoding" );
                byte[] b1 = s.toBytes(DumpType.ALL_UNENCRYPTED);
                assertTrue( "Byte representation may not be null", b1 != null );
                LOGGER.log( Level.INFO, "  reencoding" );
                AsymmetricKey s2 = new AsymmetricKey( b1 );
                byte[] b2 = (s2).toBytes(DumpType.ALL_UNENCRYPTED);
                //System.out.println("dumping object tuple \n"+s.dumpValueNotation( "" )+"\n"+s2.dumpValueNotation( "" ));
                assertTrue( "Byte arrays should be equal when reencoding ("+s+"/"+s2+")", Arrays.equals( b1, b2 ) );
                assertTrue( "dumped ASN value strings should be equal after reencoding 1", s.dumpValueNotation("",DumpType.ALL).equals( s2.dumpValueNotation("",DumpType.ALL) ) );
                assertTrue( "dumped ASN value strings should be equal after reencoding 2", s.dumpValueNotation("",DumpType.PUBLIC_ONLY).equals( s2.dumpValueNotation("",DumpType.PUBLIC_ONLY) ) );
            }
        } catch (Exception e) {
            LOGGER.log( Level.WARNING,"Unexpected exception",e);
            fail( "fuzzer encountered exception in Asymmetric key with algorithm " + parameter.get(Parameter.ALGORITHM) +"/"+ parameter.get(Parameter.KEYSIZE)+" ("+e.toString()+")" );
        }
    }

    @Test
    public void fuzzingAsymmetricEncryption() {
        AsymmetricKey s1;
        AsymmetricKey s2;
        String currentObject = null;
        try {
            LOGGER.log( Level.INFO, "Running encryption test with " + parameter.get(Parameter.ALGORITHM) + "/" + parameter.get(Parameter.MODE) + "/" + parameter.get(Parameter.PADDING) + "" );
            for (int i = 0; i < repeat; i++) {
                LOGGER.log( Level.INFO, "  Running encryption test "+i+"/"+repeat );
                LOGGER.log( Level.INFO, "  generating key");
                s1 = new AsymmetricKey( parameter );
                s2 = new AsymmetricKey( s1.toBytes(DumpType.ALL) );
                LOGGER.log( Level.INFO, "  dumping object");
                currentObject = s1.dumpValueNotation( "", DumpType.ALL );
                byte[] b1 = new byte[sr.nextInt( Math.min( s1.getPadding().getMaxSize( Integer.parseInt(parameter.get(Parameter.KEYSIZE)) ), 1024 ) )];
                sr.nextBytes( b1 );
                LOGGER.log( Level.INFO, "  running encryption/decryption test with " + parameter.get(Parameter.ALGORITHM) + "/" + parameter.get(Parameter.MODE) + "/" + parameter.get(Parameter.PADDING) + "" );
                byte[] b2 = s1.decrypt( s1.encrypt( b1 ) );
                assertTrue( "error in encrypt/decrypt cycle with " + parameter.get(Parameter.ALGORITHM) + " (same object)", Arrays.equals( b1, b2 ) );
                b2 = s1.decrypt( s1.encrypt( b1 ) );
                assertTrue( "error in encrypt/decrypt cycle with " + parameter.get(Parameter.ALGORITHM) + " (same object; with keys specified for signature)", Arrays.equals( b1, b2 ) );
                assertTrue( "error in encrypt/decrypt cycle with reencoded " + parameter.get(Parameter.ALGORITHM) + " (same object; with keys specified for signature)", Arrays.equals( b1, s2.decrypt(s1.encrypt(b1)) ) );
                // PKCS1 padding is suposed to change on every encryption round)
                if(!parameter.get(Parameter.PADDING).equals("PKCS1Padding") && s1.getAlgorithm()!=Algorithm.EC) {
                    assertTrue("error in encrypt/decrypt cycle with comparing encrypted byte array " + parameter.toString() + " (s1 only)", Arrays.equals(s1.encrypt(b1), s1.encrypt(b1)));
                    assertTrue("error in encrypt/decrypt cycle with comparing encrypted byte array " + parameter.toString() + " (s2 only)", Arrays.equals(s2.encrypt(b1), s2.encrypt(b1)));
                    assertTrue("error in encrypt/decrypt cycle with comparing encrypted byte array " + parameter.toString() + " (s1 vs s2)", Arrays.equals(s1.encrypt(b1), s2.encrypt(b1)));
                }
                LOGGER.log( Level.INFO, "  Running signature test with " + parameter.get(Parameter.ALGORITHM) + "/" + parameter.get(Parameter.MODE) + "/" + parameter.get(Parameter.PADDING) + "" );
                byte[] sig = s1.sign( b1 );
                assertTrue( "error verifying signature with " + parameter.get(Parameter.ALGORITHM) + " (same object; with keys specified for signature)", s1.verify( b1, sig ) );
                assertTrue( "error verifying signature with reencoded " + parameter.get(Parameter.ALGORITHM) + " (same object; with keys specified for signature)", s2.verify( b1, sig ) );
            }
            LOGGER.log( Level.INFO, "done with " + parameter.get(Parameter.ALGORITHM)  + "/unspecified/" + parameter.get(Parameter.PADDING)  + " (" + parameter.get(Parameter.KEYSIZE)   + ")" );
        } catch(Exception e) {
            LOGGER.log(Level.WARNING,"Unexpected exception",e);
            fail("fuzzer encountered exception in Symmetric en/decryption test with algorithm " + parameter.get(Parameter.ALGORITHM)  + "\n" + currentObject);
        } finally {
            System.err.flush();
            System.out.flush();
        }
    }

}
