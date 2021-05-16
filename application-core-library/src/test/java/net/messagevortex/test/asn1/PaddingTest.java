package net.messagevortex.test.asn1;

import net.messagevortex.MessageVortexLogger;
import net.messagevortex.asn1.encryption.Prng;
import net.messagevortex.router.operation.AddRedundancy;
import net.messagevortex.test.GlobalJunitExtension;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.io.IOException;
import java.security.SecureRandom;
import java.util.logging.Level;

@ExtendWith(GlobalJunitExtension.class)
public class PaddingTest {

  private static final java.util.logging.Logger LOGGER;

  static {
    LOGGER = MessageVortexLogger.getLogger((new Throwable()).getStackTrace()[0].getClassName());
  }

  private class TestCase {
    private byte[] in;
    private byte[] out;
    int blockSize;
    int numberOfOutBlocks;
    int c1;
    int c2;
    Prng prng;

    public TestCase(byte[] in, byte[] out, int blockSize, int numberOfOutBlocks, int c1, int c2, Prng prng) {
      this.in = in;
      this.out = out;
      this.blockSize = blockSize;
      this.numberOfOutBlocks = numberOfOutBlocks;
      this.c1 = c1;
      this.c2 = c2;
      this.prng = prng;
    }

    public void runTest() throws IOException {
      LOGGER.log(Level.INFO, "");
      byte[] padded = AddRedundancy.pad(blockSize, numberOfOutBlocks, in, prng, c1, c2);
      prng.reset();
      byte[] unpadded = AddRedundancy.unpad(blockSize, numberOfOutBlocks, padded, prng);
      Assertions.assertTrue(in.length == unpadded.length, "padding test failed: unpadded array is not of same size");
      for (int a = 0; a < unpadded.length; a++) {
        Assertions.assertTrue(in[a] == unpadded[a], "Error comparing array at pos " + a);
      }
    }
  }

  @Test
  public void simplePrngTest() {
    byte[] b = new byte[100];
    for (int i = 0; i < 1000; i++) {
      Prng p = new AddRedundancy.SimplePrng();
      for (int j = 0; j < b.length; j++) {
        b[j] = p.nextByte();
      }
      p.reset();
      for (int j = 0; j < b.length; j++) {
        byte c = p.nextByte();
        Assertions.assertTrue(b[j] == c, "Error comparing byte " + j + " (expected; " + b[j] + "; is: " + c);
      }
    }
  }

  @Test
  public void basicPaddingTest() {
    try {
      // pad just one byte with empty c1 and c2
      TestCase tc = new TestCase(new byte[1], null, 256, 30, 0, 0, new AddRedundancy.SimplePrng());
      tc.runTest();
      // pad just zero bytes with empty c1 and c2
      tc = new TestCase(new byte[0], null, 256, 30, 0, 0, new AddRedundancy.SimplePrng());
      tc.runTest();
      // pad just zero bytes with c1=1
      tc = new TestCase(new byte[1], null, 256, 30, 1, 0, new AddRedundancy.SimplePrng());
      tc.runTest();
      // pad just zero bytes with c2=1
      tc = new TestCase(new byte[1], null, 256, 30, 0, 1, new AddRedundancy.SimplePrng());
      tc.runTest();
      // stepping test
      for (int i = 0; i < 40; i++) {
        LOGGER.log(Level.INFO, "Starting basic padding test " + i);
        byte[] b = new byte[(i) * 1048];
        tc = new TestCase(b, null, 256, 30, 1765498380, 20, new AddRedundancy.SimplePrng());
        tc.runTest();
      }
    } catch (IOException ioe) {
      ioe.printStackTrace();
      System.out.flush();
      System.err.flush();
      Assertions.fail("test failed due to exception when unpadding");
    }
  }

  @Test
  public void fuzzingPaddingTest() {
    for (int i = 0; i < 40; i++) {
      LOGGER.log(Level.INFO, "Starting fuzzing padding test " + i);
      SecureRandom sr = new SecureRandom();
      try {
        byte[] b = new byte[sr.nextInt(16192)];
        int c1 = sr.nextInt();
        int c2 = sr.nextInt(1024 * 1024);
        LOGGER.log(Level.INFO, "testing with size " + b.length + " and parameters c1=" + c1 + " and c2=" + c2);
        TestCase tc = new TestCase(b, null, 256, 30, c1, c2, new AddRedundancy.SimplePrng());
        tc.runTest();
      } catch (IOException ioe) {
        ioe.printStackTrace();
        System.out.flush();
        System.err.flush();
        Assertions.fail("test failed due to exception when unpadding");
      }
    }
  }
}
