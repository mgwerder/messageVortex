package net.gwerder.java.messagevortex.test.asn1;

import net.gwerder.java.messagevortex.MessageVortexLogger;
import net.gwerder.java.messagevortex.asn1.MacAlgorithm;
import net.gwerder.java.messagevortex.asn1.encryption.Algorithm;
import net.gwerder.java.messagevortex.asn1.encryption.AlgorithmType;
import net.gwerder.java.messagevortex.asn1.encryption.DumpType;
import org.bouncycastle.asn1.ASN1Encodable;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.logging.Level;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * General Test class for all unspecific Block tests.
 *
 * Created by martin.gwerder on 30.05.2016.
 */
@RunWith(JUnit4.class)
public class MacAlgorithmTest {

    private static final java.util.logging.Logger LOGGER;
    static {
        LOGGER = MessageVortexLogger.getLogger((new Throwable()).getStackTrace()[0].getClassName());
        MessageVortexLogger.setGlobalLogLevel(Level.ALL);
    }

    @Test
    public void basicMacAlgorithmExceptionTest() {
        // null on algorithm
        try {
            new MacAlgorithm((Algorithm)null);
        } catch(NullPointerException ioe) {
            // this is expected benhaviour
        } catch(Exception e) {
            fail("got unexpected exception");
        }
        // null on ASN1§Encodeable
        try {
            new MacAlgorithm((ASN1Encodable) null);
        } catch(NullPointerException ioe) {
            // this is expected benhaviour
        } catch(Exception e) {
            fail("got unexpected exception ("+e+")");
        }
        // bad algorithm
        try {
            new MacAlgorithm(Algorithm.AES128);
        } catch(IOException ioe) {
            // this is expected benhaviour
        } catch(Exception e) {
            fail("got unexpected exception");
        }
        MacAlgorithm ma=null;
        try {
            ma=new MacAlgorithm(Algorithm.SHA512);
        } catch(Exception e) {
            fail("got unexpected exception");
        }
        assertTrue("Error AlgorithmType returns NULL instead of type",ma.getAlgorithm()!=null);
        assertTrue("error verifying AlgTypeSetting",ma.getAlgorithm().equals(Algorithm.SHA512));
        try {
            assertTrue("error verifying AlgTypeSetting (2)",ma.setAlgorithm(Algorithm.SHA384).equals(Algorithm.SHA512));
        } catch(Exception e) {
            fail("got unexpected exception");
        }
        try {
            assertTrue("error verifying AlgTypeSetting (3)",ma.setAlgorithm(Algorithm.SHA512).equals(Algorithm.SHA384));
        } catch(Exception e) {
            fail("got unexpected exception");
        }
        try {
            if(ma!=null) ma.setAlgorithm(Algorithm.RSA);
        } catch(IOException ioe) {
            // this is expected benhaviour
        } catch(Exception e) {
            fail("got unexpected exception");
        }
    }

    @Test
    public void writeAsAsn1() {
        for (Algorithm a : Algorithm.getAlgorithms(AlgorithmType.HASHING)) {
            try {
                MacAlgorithm ak = new MacAlgorithm(a);
                File f = new File("testfile_MacAlgorithm_" + a.getAlgorithmFamily() + ".der");
                OutputStream o = new FileOutputStream(f);
                o.write(ak.toBytes(DumpType.ALL_UNENCRYPTED));
                o.close();
            } catch (Exception e) {
                fail("unexpected exception");
            }
        }
    }



}
