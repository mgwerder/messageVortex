package net.messagevortex.test.routing;

import net.messagevortex.ExtendedSecureRandom;
import net.messagevortex.MessageVortexLogger;
import net.messagevortex.asn1.AddRedundancyOperation;
import net.messagevortex.asn1.IdentityBlock;
import net.messagevortex.asn1.PayloadChunk;
import net.messagevortex.asn1.RemoveRedundancyOperation;
import net.messagevortex.asn1.SymmetricKey;
import net.messagevortex.router.operation.AddRedundancy;
import net.messagevortex.router.operation.IdMapOperation;
import net.messagevortex.router.operation.InternalPayloadSpace;
import net.messagevortex.router.operation.InternalPayloadSpaceStore;
import net.messagevortex.router.operation.Operation;
import net.messagevortex.router.operation.RemoveRedundancy;
import net.messagevortex.test.GlobalJunitExtension;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;

@ExtendWith(GlobalJunitExtension.class)
public class OperationProcessingTest {

  private static final java.util.logging.Logger LOGGER;

  static {
    LOGGER = MessageVortexLogger.getLogger((new Throwable()).getStackTrace()[0].getClassName());
  }

  @Test
  public void redundancyOperationTest() {
    try {
      IdentityBlock identity = new IdentityBlock();
      InternalPayloadSpaceStore ps = new InternalPayloadSpaceStore();
      InternalPayloadSpace p = ps.getInternalPayload(identity);
      redundancyOperationTest(p, 3, 2, 8);
      int repeat = 5;
      for (int gfSize : new int[]{8, 16}) {
        for (int i = 0; i < repeat; i++) {
          // determine data to be processed
          int stripesRange = gfSize == 8 ? 220 : 500;
          int dataStripes = (int) (Math.random() * stripesRange) + 1;
          int redundancy = (int) (Math.random() * (stripesRange * 1.1 - dataStripes)) + 1;

          LOGGER.log(Level.INFO, "running fuzzer " + ((gfSize / 8 - 1) * repeat + i) + "/" + (repeat * 2) + "");
          redundancyOperationTest(p, dataStripes, redundancy, gfSize);

        }
      }
    } catch (IOException ioe) {
      ioe.printStackTrace();
      Assertions.fail("Exception while testing redundancy operation");
    }
  }

  private void redundancyOperationTest(InternalPayloadSpace p, int dataStripes, int redundancy, int gfSize) throws IOException {
    ExtendedSecureRandom esr = new ExtendedSecureRandom();
    // create symmetric keys for stripes
    SymmetricKey[] keys = new SymmetricKey[dataStripes + redundancy];
    for (int j = 0; j < keys.length; j++) keys[j] = new SymmetricKey();

    // create random data
    byte[] inBuffer = new byte[dataStripes * 10 + (int) (Math.random() * (dataStripes * 10))];
    esr.nextBytes(inBuffer);

    // do the test
    LOGGER.log(Level.INFO, "  fuzzing with dataStipes:" + dataStripes + "/redundancyStripes:" + redundancy + "/GF(" + gfSize + ")/dataSize:" + inBuffer.length + "");
    Operation iop = new AddRedundancy(new AddRedundancyOperation(1, dataStripes, redundancy, Arrays.asList(keys), 1000, gfSize));
    Assertions.assertTrue(p.addOperation(iop), "add operation not added");
    Assertions.assertTrue(p.setPayload(new PayloadChunk(1, inBuffer, null)) == null, "payload not added");

    // straight operation
    Operation oop = new RemoveRedundancy(new RemoveRedundancyOperation(1000, dataStripes, redundancy, Arrays.asList(keys), 2000, gfSize));
    Assertions.assertTrue(p.addOperation(oop), "remove operation not added");
    byte[] b = p.getPayload(2000).getPayload();
    Assertions.assertTrue(b != null && Arrays.equals(inBuffer, b), "error testing straight redundancy calculation");

    // redundancy operation
    LOGGER.log(Level.INFO, "  Recovery Test");
    Operation oop2 = new RemoveRedundancy(new RemoveRedundancyOperation(3000, dataStripes, redundancy, Arrays.asList(keys), 4000, gfSize));
    Assertions.assertTrue(p.addOperation(oop2), "add operation for rebuild test not added");
    // set random passthrus
    Map<Integer, Operation> l = new HashMap<>();
    while (l.size() < iop.getOutputId().length) {
      int i = esr.nextInt(dataStripes + redundancy);
      if (!l.containsKey(i)) {
        Operation o = new IdMapOperation(1000 + i, 3000 + i, 1);
        if (p.addOperation(o)) {
          l.put(i, o);
        } else {
          Assertions.fail("add operation failed unexpectedly");
        }
      }
    }
    Assertions.assertTrue(oop2.canRun(), "error in determining canRun");
    Assertions.assertTrue(Arrays.equals(inBuffer, p.getPayload(4000).getPayload()), "error testing redundancy calculation with random stripes");
    Assertions.assertTrue(p.removeOperation(oop2), "remove operation for rebuild test not added");
    for (Operation o : l.values()) {
      Assertions.assertTrue(p.removeOperation(o), "error removing passthru operation");
    }

    Assertions.assertTrue(p.removeOperation(iop), "unable to successfully remove add operation");
    Assertions.assertTrue(p.removeOperation(oop), "unable to successfully remove remove redundancy operation");
    Assertions.assertTrue(p.setPayload(new PayloadChunk(1, null, null)) != null, "unable remove payload data");

  }

}
