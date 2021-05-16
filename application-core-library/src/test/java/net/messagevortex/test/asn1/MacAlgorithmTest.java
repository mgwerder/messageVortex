package net.messagevortex.test.asn1;

import net.messagevortex.MessageVortexLogger;
import net.messagevortex.asn1.MacAlgorithm;
import net.messagevortex.asn1.encryption.Algorithm;
import net.messagevortex.asn1.encryption.AlgorithmType;
import net.messagevortex.asn1.encryption.DumpType;
import net.messagevortex.test.GlobalJunitExtension;
import org.bouncycastle.asn1.ASN1Encodable;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;


/**
 * General Test class for all unspecific Block tests.
 *
 * Created by martin.gwerder on 30.05.2016.
 */
@ExtendWith(GlobalJunitExtension.class)
public class MacAlgorithmTest {

    private static final java.util.logging.Logger LOGGER;
    static {
        LOGGER = MessageVortexLogger.getLogger((new Throwable()).getStackTrace()[0].getClassName());
    }

    @Test
    public void basicMacAlgorithmExceptionTest() {
        // null on algorithm
        try {
            new MacAlgorithm((Algorithm)null);
        } catch(NullPointerException ioe) {
            // this is expected benhaviour
        } catch(Exception e) {
            Assertions.fail("got unexpected exception");
        }
        // null on ASN1§Encodeable
        try {
            new MacAlgorithm((ASN1Encodable) null);
        } catch(NullPointerException ioe) {
            // this is expected benhaviour
        } catch(Exception e) {
            Assertions.fail("got unexpected exception ("+e+")");
        }
        // bad algorithm
        try {
            new MacAlgorithm(Algorithm.AES128);
        } catch(IOException ioe) {
            // this is expected benhaviour
        } catch(Exception e) {
            Assertions.fail("got unexpected exception");
        }
        MacAlgorithm ma=null;
        try {
            ma=new MacAlgorithm(Algorithm.SHA512);
        } catch(Exception e) {
            Assertions.fail("got unexpected exception");
        }
        Assertions.assertTrue(ma.getAlgorithm().equals(Algorithm.SHA512), "error verifying AlgTypeSetting");
        try {
            Assertions.assertTrue(ma.setAlgorithm(Algorithm.SHA384).equals(Algorithm.SHA512), "error verifying AlgTypeSetting (2)");
        } catch(Exception e) {
            Assertions.fail("got unexpected exception");
        }
        try {
            Assertions.assertTrue(ma.setAlgorithm(Algorithm.SHA512).equals(Algorithm.SHA384), "error verifying AlgTypeSetting (3)");
        } catch(Exception e) {
            Assertions.fail("got unexpected exception");
        }
        try {
            if(ma!=null) ma.setAlgorithm(Algorithm.RSA);
        } catch(IOException ioe) {
            // this is expected benhaviour
        } catch(Exception e) {
            Assertions.fail("got unexpected exception");
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
                Assertions.fail("unexpected exception");
            }
        }
    }



}
