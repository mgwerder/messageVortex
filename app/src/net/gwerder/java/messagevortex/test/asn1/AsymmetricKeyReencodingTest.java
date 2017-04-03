package net.gwerder.java.messagevortex.test.asn1;

import net.gwerder.java.messagevortex.ExtendedSecureRandom;
import net.gwerder.java.messagevortex.MailvortexLogger;
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

    private static final java.util.logging.Logger LOGGER;
    private static final int ksDisc = 8192; //16384
    private static ExtendedSecureRandom sr = new ExtendedSecureRandom();

    static {
        LOGGER = MailvortexLogger.getLogger((new Throwable()).getStackTrace()[0].getClassName());
        MailvortexLogger.setGlobalLogLevel(Level.ALL);
    }

    private String testname;
    private Algorithm alg;
    private Padding   pad;
    private Mode      mode;
    private int       size;
    private int repeat;
    private Map<String,Object> params;

    public AsymmetricKeyReencodingTest(String testname, Algorithm alg, Padding pad, Mode mode, int size, int repeat, Map<String,Object> params) {
        this.testname = testname;
        this.alg=alg;
        this.pad=pad;
        this.mode=mode;
        this.size=size;
        this.repeat = repeat;
        this.params = params;
    }

    @Parameters(name = "{0}")
    public static Collection<Object[]> generateData() {
        List<Object[]> ret = new Vector<>();
        for(Algorithm alg: Algorithm.getAlgorithms( AlgorithmType.ASYMMETRIC )) {
            for (Map.Entry<SecurityLevel,Map<String,Object>> params : alg.getParameters().entrySet() ) {
                int ks=(Integer)(params.getValue().get("keySize_0"));
                int j = Math.min( (int) Math.pow( 2, ksDisc / ks ), 100 );
                ret.add( new Object[]{"" + alg.getAlgorithm() + "/" + ks, alg, Padding.getDefault( AlgorithmType.ASYMMETRIC ),Mode.getDefault( AlgorithmType.ASYMMETRIC ), ks, j,params.getValue() } );
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
                AsymmetricKey s = new AsymmetricKey( alg, size, params);
                byte[] b1 = s.toBytes();
                s.dumpValueNotation( "" );
                assertTrue( "Byte representation may not be null", b1 != null );
                AsymmetricKey s2 = new AsymmetricKey( b1 );
                byte[] b2 = (s2).toBytes();
                //System.out.println("dumping object tuple \n"+s.dumpValueNotation( "" )+"\n"+s2.dumpValueNotation( "" ));
                assertTrue( "Byte arrays should be equal when reencoding ("+s+"/"+s2+")", Arrays.equals( b1, b2 ) );
            }
        } catch (Exception e) {
            LOGGER.log( Level.WARNING,"Unexpected exception",e);
            fail( "fuzzer encountered exception in Asymmetric key with algorithm " + alg.toString() +"/"+ size+" ("+e.toString()+")" );
        }
    }

    @Test
    public void fuzzingAsymmetricEncryption() {
        AsymmetricKey s;
        String currentObject = null;
        try {
            LOGGER.log( Level.INFO, "Running encryption test with " + alg + "/" + Mode.getDefault(alg.getAlgorithmType()) + "/" + pad + " (" + size + ")" );
            for (int i = 0; i < repeat; i++) {
                s = new AsymmetricKey( alg, size ,params);
                currentObject = s.dumpValueNotation( "", DumpType.ALL );
                byte[] b1 = new byte[sr.nextInt( Math.min( s.getPadding().getMaxSize( size ), 1024 ) )];
                sr.nextBytes( b1 );
                byte[] b2 = s.decrypt( s.encrypt( b1 ) );
                assertTrue( "error in encrypt/decrypt cycle with " + alg + " (same object)", Arrays.equals( b1, b2 ) );
                b2 = s.decrypt( s.encrypt( b1 ) );
                assertTrue( "error in encrypt/decrypt cycle with " + alg + " (same object; with keys specified for signature)", Arrays.equals( b1, b2 ) );
                byte[] sig = s.sign( b1 );
                assertTrue( "error in encrypt/decrypt cycle with " + alg + " (same object; with keys specified for signature)", s.verify( b1, sig ) );
            }
            LOGGER.log( Level.INFO, "done with " + alg + "/unspecified/" + pad + " (" + size + ")" );
        } catch(Exception e) {
            LOGGER.log(Level.WARNING,"Unexpected exception",e);
            fail("fuzzer encountered exception in Symmetric en/decryption test with algorithm " + alg.toString() + "\n" + currentObject);
        } finally {
            System.err.flush();
            System.out.flush();
        }
    }

}
