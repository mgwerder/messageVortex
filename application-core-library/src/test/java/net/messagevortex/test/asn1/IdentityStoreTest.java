
package net.messagevortex.test.asn1;

import net.messagevortex.MessageVortexLogger;
import net.messagevortex.asn1.IdentityStore;
import net.messagevortex.asn1.encryption.DumpType;
import net.messagevortex.test.GlobalJunitExtension;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.io.IOException;
import java.util.Arrays;
import java.util.Date;
import java.util.logging.Level;


/**
 * Created by martin.gwerder on 30.05.2016.
 */
@ExtendWith(GlobalJunitExtension.class)
public class IdentityStoreTest {

  private static final java.util.logging.Logger LOGGER;

  static {
    LOGGER = MessageVortexLogger.getLogger((new Throwable()).getStackTrace()[0].getClassName());
  }

  @Test
  public void testingIdentityStoreDump() {
    try {
      for (int i = 0; i < 10; i++) {
        LOGGER.log(Level.INFO, "Testing IdentityBlock Store dumping " + (i + 1) + " of " + 10);
        IdentityStore s = new IdentityStore();
        Assertions.assertTrue(s != null, "IdentityStore may not be null");
        String s1 = s.dumpValueNotation("", DumpType.ALL_UNENCRYPTED);
        byte[] b1 = s.toBytes(DumpType.ALL_UNENCRYPTED);
        Assertions.assertTrue(b1 != null, "Byte representation may not be null");
        byte[] b2 = (new IdentityStore(b1)).toBytes(DumpType.ALL_UNENCRYPTED);
        Assertions.assertTrue(Arrays.equals(b1, b2), "Byte arrays should be equal when reencoding");
        String s2 = (new IdentityStore(b2)).dumpValueNotation("", DumpType.ALL_UNENCRYPTED);
        Assertions.assertTrue(s1.equals(s2), "Value Notations should be equal when reencoding");
      }
    } catch (Exception e) {
      LOGGER.log(Level.WARNING, "Unexpected exception", e);
      Assertions.fail("fuzzer encountered exception in IdentityStore (" + e + ")");
    }
  }

  @Test
  public void testingIdentityStoreDemo() {
    Date start = new Date();
    final IdentityStore[] arr = new IdentityStore[10];
    for (int i = 0; i < arr.length; i++) {
      try {
        arr[i] = IdentityStore.getNewIdentityStoreDemo(false);
      } catch (IOException ioe) {
        LOGGER.log(Level.WARNING, "got IOException while generating new demo", ioe);
      }
    }
    LOGGER.log(Level.INFO, "store preparation took " + (((new Date()).getTime() - start.getTime()) / 1000) + " s");

    //testing
    try {
      for (int i = 0; i < arr.length; i++) {
        LOGGER.log(Level.INFO, "Testing IdentityStore reencoding " + (i + 1) + " of " + arr.length);
        start = new Date();
        IdentityStore s1 = arr[i];
        Assertions.assertTrue(s1 != null, "IdentityStore may not be null");
        byte[] b1 = s1.toBytes(DumpType.ALL_UNENCRYPTED);
        Assertions.assertTrue(b1 != null, "Byte representation may not be null");
        IdentityStore s2 = new IdentityStore(b1);
        byte[] b2 = s2.toBytes(DumpType.ALL_UNENCRYPTED);
        Assertions.assertTrue(Arrays.equals(b1, b2), "Byte arrays should be equal when reencoding");
        Assertions.assertTrue((new IdentityStore(b2)).dumpValueNotation("", DumpType.ALL_UNENCRYPTED).equals((new IdentityStore(b1)).dumpValueNotation("", DumpType.ALL_UNENCRYPTED)), "Value Notations should be equal when reencoding");
        s2 = arr[(i + 1) % arr.length];
        b2 = s2.toBytes(DumpType.ALL_UNENCRYPTED);
        Assertions.assertTrue(!(new IdentityStore(b2)).dumpValueNotation("", DumpType.ALL_UNENCRYPTED).equals((new IdentityStore(b1)).dumpValueNotation("", DumpType.ALL_UNENCRYPTED)), "Value Notations should NOT be equal when reencoding new demo");
        LOGGER.log(Level.INFO, "Testing IdentityStore reencoding " + (i + 1) + " took " + (((new Date()).getTime() - start.getTime()) / 1000) + " s");
        LOGGER.log(Level.INFO, "Encoded String is " + s2.dumpValueNotation("", DumpType.ALL_UNENCRYPTED));
      }
    } catch (Exception e) {
      LOGGER.log(Level.WARNING, "Unexpected exception", e);
      Assertions.fail("fuzzer encountered exception in IdentityStore (" + e + ")");
    }
  }

}
