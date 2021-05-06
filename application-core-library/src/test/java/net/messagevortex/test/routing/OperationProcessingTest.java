package net.messagevortex.test.routing;

import net.messagevortex.ExtendedSecureRandom;
import net.messagevortex.MessageVortexLogger;
import net.messagevortex.asn1.*;
import net.messagevortex.router.operation.Operation;
import net.messagevortex.router.operation.*;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class OperationProcessingTest {

  private static final java.util.logging.Logger LOGGER;

  static {
    LOGGER = MessageVortexLogger.getLogger((new Throwable()).getStackTrace()[0].getClassName());
    MessageVortexLogger.setGlobalLogLevel(Level.ALL);
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
      fail("Exception while testing redundancy operation");
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
    assertTrue("add operation not added", p.addOperation(iop));
    assertTrue("payload not added", p.setPayload(new PayloadChunk(1, inBuffer, null)) == null);

    // straight operation
    Operation oop = new RemoveRedundancy(new RemoveRedundancyOperation(1000, dataStripes, redundancy, Arrays.asList(keys), 2000, gfSize));
    assertTrue("remove operation not added", p.addOperation(oop));
    byte[] b = p.getPayload(2000).getPayload();
    assertTrue("error testing straight redundancy calculation", b != null && Arrays.equals(inBuffer, b));

    // redundancy operation
    LOGGER.log(Level.INFO, "  Recovery Test");
    Operation oop2 = new RemoveRedundancy(new RemoveRedundancyOperation(3000, dataStripes, redundancy, Arrays.asList(keys), 4000, gfSize));
    assertTrue("add operation for rebuild test not added", p.addOperation(oop2));
    // set random passthrus
    Map<Integer, Operation> l = new HashMap<>();
    while (l.size() < iop.getOutputId().length) {
      int i = esr.nextInt(dataStripes + redundancy);
      if (!l.containsKey(i)) {
        Operation o = new IdMapOperation(1000 + i, 3000 + i, 1);
        if (p.addOperation(o)) {
          l.put(i, o);
        } else {
          fail("add operation failed unexpectedly");
        }
      }
    }
    assertTrue("error in determining canRun", oop2.canRun());
    assertTrue("error testing redundancy calculation with random stripes", Arrays.equals(inBuffer, p.getPayload(4000).getPayload()));
    assertTrue("remove operation for rebuild test not added", p.removeOperation(oop2));
    for (Operation o : l.values()) {
      assertTrue("error removing passthru operation", p.removeOperation(o));
    }

    assertTrue("unable to successfully remove add operation", p.removeOperation(iop));
    assertTrue("unable to successfully remove remove redundancy operation", p.removeOperation(oop));
    assertTrue("unable remove payload data", p.setPayload(new PayloadChunk(1, null, null)) != null);

  }

}
