package net.messagevortex.test.routing;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.logging.Level;
import net.messagevortex.MessageVortexLogger;
import net.messagevortex.asn1.AsymmetricKey;
import net.messagevortex.asn1.IdentityStore;
import net.messagevortex.asn1.IdentityStoreBlock;
import net.messagevortex.asn1.encryption.DumpType;
import net.messagevortex.router.Edge;
import net.messagevortex.router.GraphSet;
import net.messagevortex.router.MessageFactory;
import org.bouncycastle.asn1.ASN1Encoding;
import org.bouncycastle.asn1.ASN1OutputStream;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/**
 * Created by martin.gwerder on 13.06.2016.
 */
@RunWith(JUnit4.class)
public class MessageFactoryTest {
  
  private static final java.util.logging.Logger LOGGER;
  
  static {
    LOGGER = MessageVortexLogger.getLogger((new Throwable()).getStackTrace()[0].getClassName());
    MessageVortexLogger.setGlobalLogLevel(Level.ALL);
    
    // Danger: any other value than 1 messes up demo store creation
    AsymmetricKey.setDequeueProbability(1);
  }
  
  @Test
  public void simpleMessageFactoryTest() throws IOException {
    LOGGER.log(Level.INFO, "getting example store from " + System.getProperty("java.io.tmpdir"));
    IdentityStore is = null;
    try {
      is = new IdentityStore(new File("CachedIdentityStoreExample.der"));
    } catch (Exception ioe) {
      is = IdentityStore.getNewIdentityStoreDemo(false);
      String fn = System.getProperty("java.io.tmpdir") + "/IdentityStoreExample1.der"
      File fd = new File(fn).getParentFile();
      if (fd != null) {
        try {
          fd.mkdirs();
        } catch (SecurityException se) {
          LOGGER.log(Level.INFO, "unable to create parent directory " + fn);
        }
      }
      ASN1OutputStream f = ASN1OutputStream.create(new FileOutputStream(fn), ASN1Encoding.DER);
      f.writeObject(is.toAsn1Object(DumpType.ALL_UNENCRYPTED));
      f.close();
    }
    int maxTests = 10;
    for (int i = 1; i <= maxTests; i++) {
      LOGGER.log(Level.INFO, "cycle " + i + " of " + maxTests);
      LOGGER.log(Level.INFO, "  building message (" + i + " of " + maxTests + ")");
      MessageFactory smf = MessageFactory.buildMessage("Subject: This is the message subject\n\nhello", 0, 1, is.getAnonSet(8).toArray(new IdentityStoreBlock[0]), is);
      smf.build();
      GraphSet gs = smf.getGraph();
      for (Edge gt : gs) {
        assertTrue("unreached endpoint", gs.targetReached(gt.getFrom()) && gs.targetReached(gt.getTo()));
      }
      LOGGER.log(Level.INFO, "  getting routes (" + i + " of " + maxTests + ")");
      GraphSet[] g = gs.getRoutes();
      if (g == null || g.length == 0) {
        System.out.println(gs.dump());
        fail("Routes not found (" + (g != null ? g.length : -1) + ")");
      }
      LOGGER.log(Level.INFO, "  testing full GraphSet (" + i + " of " + maxTests + ")");
      for (GraphSet gt : g) {
        for (Edge gt2 : gt) {
          assertTrue("unreached endpoint", gt.targetReached(gt2.getFrom()) && gt.targetReached(gt2.getTo()));
        }
      }
    }
  }
  
}
