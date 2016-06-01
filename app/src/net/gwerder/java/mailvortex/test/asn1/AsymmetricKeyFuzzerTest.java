package net.gwerder.java.mailvortex.test.asn1;

import net.gwerder.java.mailvortex.MailvortexLogger;
import net.gwerder.java.mailvortex.asn1.AsymmetricKey;
import net.gwerder.java.mailvortex.asn1.encryption.Algorithm;
import net.gwerder.java.mailvortex.asn1.encryption.AlgorithmType;
import net.gwerder.java.mailvortex.asn1.encryption.Mode;
import net.gwerder.java.mailvortex.asn1.encryption.Padding;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.security.SecureRandom;
import java.util.Arrays;
import java.util.Collection;
import java.util.Vector;
import java.util.logging.Level;

import org.junit.runners.Parameterized.Parameters;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * Created by martin.gwerder on 31.05.2016.
 */
@RunWith(Parameterized.class)
public class AsymmetricKeyFuzzerTest {

    private static final java.util.logging.Logger LOGGER;

    static {
        LOGGER = MailvortexLogger.getLogger((new Throwable()).getStackTrace()[0].getClassName());
        MailvortexLogger.setGlobalLogLevel(Level.ALL);
    }

    private Algorithm alg;
    private Padding   pad;
    private int       size;

    private static int ksDisc=8192; //16384

    private static SecureRandom sr=new SecureRandom(  );

    public AsymmetricKeyFuzzerTest(Algorithm alg, Padding pad,int size) {
        this.alg=alg;
        this.pad=pad;
        this.size=size;
    }

    @Parameters
    public static Collection<Object[]> generateData() {
        Vector<Object[]> ret=new Vector<Object[]>();
        for(Algorithm alg: Algorithm.getAlgorithms( AlgorithmType.ASYMMETRIC )) {
            for (int ks : new int[]{1024 }) { // FIXME ",2048,4096"
                int j = (int) Math.pow( 2, (int) (ksDisc / ks) );
                for (int i = 0; i < j; i++) {
                    ret.add( new Object[]{alg,Padding.getDefault(AlgorithmType.ASYMMETRIC),ks } );
                }
            }
        }
        LOGGER.log( Level.INFO,"Prepared for fuzzer "+ret.size()+" tests");
        return ret;
    }


    @Test
    public void reencodingAsymetricKey()
    {
        try {
            AsymmetricKey s = new AsymmetricKey( alg, pad, size );
            assertTrue( "Asymmetric may not be null", s != null );
            byte[] b1 = s.toBytes();
            s.dumpValueNotation( "" );
            assertTrue( "Byte representation may not be null", b1 != null );
            AsymmetricKey s2=new AsymmetricKey( b1 );
            byte[] b2 = (s2).toBytes();
            //System.out.println("dumping object tuple \n"+s.dumpValueNotation( "" )+"\n"+s2.dumpValueNotation( "" ));
            assertTrue( "Byte arrays should be equal when reencoding", Arrays.equals( b1, b2 ) );
        } catch (Exception e) {
            LOGGER.log( Level.WARNING,"Unexpected exception",e);
            fail( "fuzzer encountered exception in Asymmetric key with algorithm " + alg.toString() +"/"+ size+" ("+e.toString()+")" );
        }
    }

    @Test
    public void fuzzingAsymmetricEncryption() {
        AsymmetricKey s=null;
        try {
            LOGGER.log(Level.INFO,"Running with "+alg+"/"+ Mode.getDefault()+"/"+pad+" ("+size+")");
            s = new AsymmetricKey(alg, pad,size);
            byte[] b1=new byte[sr.nextInt(Math.min(s.getPadding().getMaxSize( size ),1024))];
            sr.nextBytes( b1 );
            byte[] b2=s.decrypt( s.encrypt(b1) );
            assertTrue( "error in encrypt/decrypt cycle with "+alg+" (same object)",Arrays.equals( b1,b2));
            b2=s.decrypt( s.encrypt(b1,true),false );
            assertTrue( "error in encrypt/decrypt cycle with "+alg+" (same object; with keys specified for encryption)",Arrays.equals( b1,b2));
            b2=s.decrypt( s.encrypt(b1,false),true );
            assertTrue( "error in encrypt/decrypt cycle with "+alg+" (same object; with keys specified for signature)",Arrays.equals( b1,b2));
            b2=(new AsymmetricKey(s.toBytes())).decrypt( s.encrypt(b1) );
            assertTrue( "error in encrypt/decrypt cycle with "+alg+" (same reserialized object)",Arrays.equals( b1,b2));
            LOGGER.log(Level.INFO,"done with "+alg+"/unspecified/"+pad+" ("+size+")");
        } catch(Exception e) {
            LOGGER.log(Level.WARNING,"Unexpected exception",e);
            fail("fuzzer encountered exception in Symmetric en/decryption test with algorithm "+alg.toString()+"\n"+s.dumpValueNotation( "", AsymmetricKey.DumpType.ALL ));
        } finally {
            System.err.flush();
            System.out.flush();
        }
    }

}
