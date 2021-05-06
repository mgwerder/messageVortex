package net.messagevortex.test.routing;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.logging.Level;
import java.util.logging.Logger;

import net.messagevortex.MessageVortexLogger;
import net.messagevortex.asn1.IdentityStore;
import net.messagevortex.asn1.IdentityStoreBlock;
import net.messagevortex.asn1.encryption.DumpType;
import net.messagevortex.router.JGraph;
import net.messagevortex.router.SimpleMessageFactory;
import org.bouncycastle.asn1.ASN1Encoding;
import org.bouncycastle.asn1.ASN1OutputStream;
import org.junit.Assert;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class JGraphTest {

    private static final Logger LOGGER;

    static {
        LOGGER = Logger.getLogger((new Throwable()).getStackTrace()[0].getClassName());
        MessageVortexLogger.setGlobalLogLevel(Level.ALL);
    }

    @Test
    public void storeFileTest() {
        IdentityStore is = null;
        LOGGER.log(Level.INFO, "loading identity store");
        try {
            is = new IdentityStore(new File(System.getProperty("java.io.tmpdir") + "/IdentityStoreExample1.der"));
        } catch (IOException ioe) {
            try {
                LOGGER.log(Level.INFO, "creating missing identity store");
                is = IdentityStore.getNewIdentityStoreDemo(false);
                OutputStream os = new FileOutputStream(
                        System.getProperty("java.io.tmpdir") + "/IdentityStoreExample1.der"
                );
                ASN1OutputStream f = ASN1OutputStream.create(os, ASN1Encoding.DER);
                f.writeObject(is.toAsn1Object(DumpType.ALL_UNENCRYPTED));
                f.close();
            } catch (IOException ioe2) {
                ioe2.printStackTrace();
                Assertions.fail("got unexpected exception");
            }
        }
        try {
            LOGGER.log(Level.INFO, "creating  graph");
            SimpleMessageFactory smf = new SimpleMessageFactory("", 0, 1,
                    is.getAnonSet(7).toArray(new IdentityStoreBlock[0]), is);
            smf.build();
            System.out.println();
            LOGGER.log(Level.INFO, "printing graph... got " + smf.getGraph().getRoutes().length + " routes");
            new File("graphTest.jpg").delete();
            Assert.assertTrue("checking for deleted image", !new File("graphTest.jpg").exists());
            final JGraph jg = new JGraph(smf.getGraph());
            Thread t = new Thread() {
                public void run() {
                    try {
                        LOGGER.log(Level.INFO, "saving image");
                        jg.saveScreenshot("graphTest.jpg", 1024, 768);
                    } catch (IOException ioe) {
                        ioe.printStackTrace();
                        Assertions.fail("got unexpected exception");
                    }
                }
            };
            try {
                t.start();
                t.join();
            } catch (InterruptedException ie) {
                Assertions.fail("got interrupted exception");
            }
            Assert.assertTrue("checking for written image", new File("graphTest.jpg").exists());
        } catch (IOException ioe) {
            ioe.printStackTrace();
            Assertions.fail("got unexpected exception");
        }
    }

}
