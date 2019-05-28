package net.messagevortex.test.asn1;

import net.messagevortex.MessageVortexLogger;
import net.messagevortex.asn1.encryption.PRNG;
import net.messagevortex.router.operation.AddRedundancy;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.io.IOException;
import java.security.SecureRandom;
import java.util.logging.Level;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

@RunWith(JUnit4.class)
public class PaddingTest {

  private static final java.util.logging.Logger LOGGER;
  static {
    LOGGER = MessageVortexLogger.getLogger((new Throwable()).getStackTrace()[0].getClassName());
    MessageVortexLogger.setGlobalLogLevel(Level.ALL);
  }

  private class TestCase {
    private byte[] in;
    private byte[] out;
    int blockSize;
    int numberOfOutBlocks;
    int c1;
    int c2;
    PRNG prng;

    public TestCase( byte[] in, byte[] out, int blockSize, int numberOfOutBlocks, int c1, int c2, PRNG prng) {
      this.in = in;
      this.out = out;
      this.blockSize = blockSize;
      this.numberOfOutBlocks = numberOfOutBlocks;
      this.c1=c1;
      this.c2 = c2;
      this.prng = prng;
    }

    public void runTest() throws IOException {
      byte[] padded = AddRedundancy.pad(blockSize, numberOfOutBlocks, in, prng,c1, c2);
      prng.reset();
      byte[] unpadded = AddRedundancy.unpad(blockSize,numberOfOutBlocks, padded, prng);
      assertTrue("padding test failed: unpadded array is not of same size", in.length == unpadded.length);
      for (int a = 0; a < unpadded.length; a++) {
        assertTrue( "Error comparing array at pos " + a, in[a] == unpadded[a]);
      }
    }
  }

  @Test
  public void simplePrngTest() {
    byte[] b = new byte[100];
    for (int i = 0; i < 1000; i++) {
      PRNG p = new AddRedundancy.SimplePrng();
      for ( int j = 0; j < b.length; j++) {
        b[j] = p.nextByte();
      }
      p.reset();
      for ( int j = 0; j < b.length; j++) {
        byte c = p.nextByte();
        assertTrue("Error comparing byte " + j + " (expected; " + b[j] + "; is: " + c, b[j] == c);
      }
    }
  }

  @Test
  public void basicPaddingTest() {
    for ( int i = 0; i<40; i++) {
      LOGGER.log(Level.INFO, "Starting basic padding test " + i);
      try {
        byte[] b = new byte[(i+1)*1048];
        TestCase tc = new TestCase(b,null, 256, 30, 1765498380, 20, new AddRedundancy.SimplePrng());
        tc.runTest();
      } catch( IOException ioe) {
        ioe.printStackTrace();
        System.out.flush();
        System.err.flush();
        fail("test failed due to exception when unpadding");
      }
    }
  }

  @Test
  public void fuzzingPaddingTest() {
    for ( int i = 0; i<40; i++) {
      LOGGER.log(Level.INFO, "Starting fuzzing padding test " + i);
      SecureRandom sr = new SecureRandom();
      try {
        byte[] b = new byte[sr.nextInt(16192)];
        int c1 = sr.nextInt();
        int c2 = sr.nextInt(1024*1024);
        LOGGER.log(Level.INFO, "testing with size " + b.length +" and parameters c1="+c1+" and c2="+c2);
        TestCase tc = new TestCase(b,null, 256, 30, c1, c2, new AddRedundancy.SimplePrng());
        tc.runTest();
      } catch( IOException ioe) {
        ioe.printStackTrace();
        System.out.flush();
        System.err.flush();
        fail("test failed due to exception when unpadding");
      }
    }
  }
}
