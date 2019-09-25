package net.messagevortex.test.routing;

import static junit.framework.TestCase.assertTrue;
import static org.jsoup.helper.Validate.fail;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import net.messagevortex.MessageVortexLogger;
import net.messagevortex.asn1.IdentityStore;
import net.messagevortex.asn1.IdentityStoreBlock;
import net.messagevortex.asn1.encryption.DumpType;
import net.messagevortex.router.JGraph;
import net.messagevortex.router.SimpleMessageFactory;
import org.bouncycastle.asn1.DEROutputStream;
import org.junit.Test;

public class JGraphTest {

  private static final Logger LOGGER;

  static {
    LOGGER = Logger.getLogger((new Throwable()).getStackTrace()[0].getClassName());
    MessageVortexLogger.setGlobalLogLevel(Level.ALL);
  }

  @Test
  public void storeFileTest() {
    IdentityStore is = null;
    LOGGER.log(Level.INFO,"loading identity store");
    try {
      is = new IdentityStore(new File(System.getProperty("java.io.tmpdir") + "/IdentityStoreExample1.der"));
    } catch (IOException ioe) {
      try {
        LOGGER.log(Level.INFO,"creating missing identity store");
        is = IdentityStore.getNewIdentityStoreDemo(false);
        DEROutputStream f = new DEROutputStream(
                new FileOutputStream(
                        System.getProperty("java.io.tmpdir") + "/IdentityStoreExample1.der"
                )
        );
        f.writeObject(is.toAsn1Object(DumpType.ALL_UNENCRYPTED));
        f.close();
      } catch(IOException ioe2) {
        ioe2.printStackTrace();
        fail("got unexpected exception");
      }
    }
    try {
      LOGGER.log(Level.INFO,"creating  graph");
      SimpleMessageFactory smf = new SimpleMessageFactory("", 0, 1,
              is.getAnonSet(7).toArray(new IdentityStoreBlock[0]), is);
      smf.build();
      System.out.println();
      LOGGER.log(Level.INFO,"printing graph... got " + smf.getGraph().getRoutes().length + " routes");
      new File("graphTest.jpg").delete();
      assertTrue("checking for deleted image",!new File("graphTest.jpg").exists());
      final JGraph jg = new JGraph(smf.getGraph());
      Thread t = new Thread() {
        public void run() {
          try {
            LOGGER.log(Level.INFO,"saving image");
            jg.saveScreenShot("graphTest.jpg");
          } catch(IOException ioe) {
            ioe.printStackTrace();
            fail("got unexpected exception");
          }
        }
      };
      try {
        t.start();
        t.join();
      } catch(InterruptedException ie) {
        fail("got interrupted exception");
      }
      assertTrue("checking for written image",new File("graphTest.jpg").exists());
    } catch(IOException ioe) {
      ioe.printStackTrace();
      fail("got unexpected exception");
    }
  }

}
