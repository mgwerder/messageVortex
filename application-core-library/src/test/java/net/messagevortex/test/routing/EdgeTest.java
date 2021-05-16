package net.messagevortex.test.routing;

import net.messagevortex.MessageVortexLogger;
import net.messagevortex.asn1.IdentityStoreBlock;
import net.messagevortex.router.Edge;
import net.messagevortex.test.GlobalJunitExtension;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.io.IOException;
import java.util.logging.Level;

/**
 * Created by martin.gwerder on 19.04.2017.
 */
@ExtendWith(GlobalJunitExtension.class)
public class EdgeTest {

  private static final java.util.logging.Logger LOGGER;

  static {
    LOGGER = MessageVortexLogger.getLogger((new Throwable()).getStackTrace()[0].getClassName());
  }

  @Test
  public void edgeEqualityTest() {
    LOGGER.log(Level.INFO, "Testing edge equality");
    // Test  rshift
    LOGGER.log(Level.INFO, "  Creating Edges");

    IdentityStoreBlock[] isb = null;
    try {
      isb = new IdentityStoreBlock[]{IdentityStoreBlock.getIdentityStoreBlockDemo(IdentityStoreBlock.IdentityType.NODE_IDENTITY, null, false), IdentityStoreBlock.getIdentityStoreBlockDemo(IdentityStoreBlock.IdentityType.NODE_IDENTITY, null, false), IdentityStoreBlock.getIdentityStoreBlockDemo(IdentityStoreBlock.IdentityType.NODE_IDENTITY, null, false)};
    } catch (IOException ioe) {
      Assertions.fail("exception getting demo blocks from identity store");
    }
    try {
      new Edge(isb[0], null, 1, 2);
      Assertions.fail("Null oarameter is not expected to succeed");
    } catch (NullPointerException e) {
      // this is expected
    } catch (Exception e) {
      Assertions.fail("exception is not expected (" + e + ")");
    }

    try {
      new Edge(null, isb[0], 1, 2);
      Assertions.fail("Null oarameter is not expected to succeed");
    } catch (NullPointerException e) {
      // this is expected
    } catch (Exception e) {
      Assertions.fail("exception is not expected (" + e + ")");
    }

    try {
      new Edge(isb[0], isb[0], 1, 2);
      Assertions.fail("Equality test should fail");
    } catch (IllegalArgumentException e) {
      // this is expected
    } catch (Exception e) {
      Assertions.fail("exception is not expected (" + e + ")");
    }

    Assertions.assertFalse(isb[0] == null, "IdentityStorBlock precondition (0)");
    Assertions.assertFalse(isb[1] == null, "IdentityStorBlock precondition (1)");
    Assertions.assertFalse(isb[2] == null, "IdentityStorBlock precondition (2)");
    Edge[] e = new Edge[]{new Edge(isb[0], isb[1], 1, 2), new Edge(isb[0], isb[1], 1, 2), new Edge(isb[0], isb[1], 1, 3), new Edge(isb[0], isb[1], 2, 2), new Edge(isb[0], isb[2], 1, 2), new Edge(isb[2], isb[1], 1, 2), new Edge(isb[1], isb[0], 1, 2)};
    Assertions.assertFalse(e[0].equals(null), "equal to null failed");
    Assertions.assertTrue(e[0].equals(e[0]), "equal to self");
    Assertions.assertTrue(e[0].equals(e[1]), "equal to aequivalet object");
    Assertions.assertFalse(e[0].equals(e[2]), "equal to different object");
    Assertions.assertFalse(e[0].equals(e[3]), "equal to different object");
    Assertions.assertFalse(e[0].equals(e[4]), "equal to different object");
    Assertions.assertFalse(e[0].equals(e[5]), "equal to different object");
    Assertions.assertFalse(e[0].equals(new Object()), "equal to different object");
  }

}
