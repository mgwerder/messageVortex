package net.messagevortex.router.operation;

import net.messagevortex.MessageVortexLogger;
import net.messagevortex.asn1.AbstractRedundancyOperation;
import net.messagevortex.asn1.AddRedundancyOperation;
import net.messagevortex.asn1.PayloadChunk;
import net.messagevortex.asn1.SymmetricKey;
import net.messagevortex.asn1.VortexMessage;
import net.messagevortex.asn1.encryption.Prng;

import java.io.IOException;
import java.io.Serializable;
import java.math.BigInteger;
import java.util.Random;
import java.util.Vector;
import java.util.concurrent.ThreadLocalRandom;
import java.util.logging.Level;

/**
 * <p>This is the core of the redundancy add operation.</p>
 *
 * <p>It builds redundant data blocksfrom the existing data blocks.</p>
 */
public class AddRedundancy extends AbstractOperation implements Serializable {

  private static final long MAX_SIZE = 2L << 31; // calculate 2^32

  /***
   * <p>Wrapper for the java random number generator (not normative).</p>
   */
  public static class SimplePrng implements Prng {

    private final Random sr = new Random();
    private final long seed;

    public SimplePrng() {
      seed = sr.nextLong();
      sr.setSeed(seed);
    }

    public SimplePrng(long seed) {
      this.seed = seed;
    }


    /***
     * <p>Get the next random byte of the Prng.</p>
     *
     * @return the next random byte
     */
    public synchronized byte nextByte() {
      byte[] a = new byte[1];
      sr.nextBytes(a);
      return a[0];
    }

    /***
     * <p>Resets the Prng to the initially seeded state.</p>
     */
    public void reset() {
      sr.setSeed(seed);
    }
  }

  private static final Prng localPrng = new SimplePrng();

  public static final long serialVersionUID = 100000000018L;

  private static final java.util.logging.Logger LOGGER;

  static {
    LOGGER = MessageVortexLogger.getLogger((new Throwable()).getStackTrace()[0].getClassName());
    //MessageVortexLogger.setGlobalLogLevel(Level.ALL);
  }

  AbstractRedundancyOperation operation;

  public AddRedundancy(AddRedundancyOperation op) {
    this.operation = op;
  }

  @Override
  public boolean canRun() {
    return payload.getPayload(operation.getInputId()) != null;
  }

  @Override
  public int[] getOutputId() {
    int[] ret = new int[operation.getDataStripes() + operation.getRedundancy()];
    int id = operation.getOutputId();
    for (int i = 0; i < ret.length; i++) {
      ret[i] = id + i;
    }
    return ret;
  }

  @Override
  public int[] getInputId() {
    int[] ret = new int[1];
    for (int i = 0; i < ret.length; i++) {
      ret[i] = operation.getInputId() + i;
    }
    return ret;
  }

  @Override
  public int[] execute(int[] id) {
    if (!canRun() || id == null) {
      return new int[0];
    }
    LOGGER.log(Level.INFO, "executing add redundancy operation (" + this + ")");
    byte[] in = payload.getPayload(operation.getInputId()).getPayload();

    Matrix out = executeInt(in);

    // set output
    LOGGER.log(Level.INFO, "  setting output chunks");
    int tot = 0;
    assert out.getY() == (operation.getRedundancy() + operation.getDataStripes());
    try {
      for (int i = 0; i < out.getY(); i++) {
        int plid = operation.getOutputId() + i;
        byte[] b = operation.getKeys()[i].encrypt(out.getRowAsByteArray(i));
        payload.setCalculatedPayload(plid, new PayloadChunk(plid, b, getUsagePeriod()));
        tot += b.length;
      }
    } catch (IOException ioe) {
      for (int i : getOutputId()) {
        payload.setCalculatedPayload(i, null);
      }
      LOGGER.log(Level.INFO, "  failed");
      return new int[0];
    }
    LOGGER.log(Level.INFO, "  done (chunk size: " + out.getRowAsByteArray(0).length + "; total:"
        + tot + ")");

    return getOutputId();
  }

  /***
   * <p>Execute the add redundancy operation on the provided data.</p>
   * @param in           data to add redundancy
   * @param redundancy   the number of redundancy blocks
   * @param dataStripes  the number of data stripes
   * @param gf           the size of the GF
   * @return the data with added redundancy
   */
  public static byte[] execute(byte[] in, int redundancy, int dataStripes, int gf) {
    AddRedundancy ar = new AddRedundancy(
        new AddRedundancyOperation(-1, redundancy, dataStripes,
            new Vector<SymmetricKey>(), -1, gf)
    );

    LOGGER.log(Level.INFO, "executing add redundancy operation (" + ar + ")");
    Matrix out = ar.executeInt(in);

    // set output
    LOGGER.log(Level.INFO, "  setting output chunks");
    int tot = 0;
    byte[] totArray = new byte[0];
    for (int i = 0; i < out.getY(); i++) {
      byte[] b = out.getRowAsByteArray(i);
      byte[] t = new byte[tot + b.length];
      System.arraycopy(totArray, 0, t, 0, totArray.length);
      System.arraycopy(b, 0, t, totArray.length, b.length);
      totArray = t;
      tot += b.length;
    }
    LOGGER.log(Level.INFO, "  done (chunk size: " + out.getRowAsByteArray(0).length + "; total:"
        + tot + ")");
    return totArray;
  }

  private Matrix executeInt(byte[] in) {
    // do the padding
    int paddingSize = 4;
    int size = in.length + paddingSize;
    int keySize = operation.getKeys().length != 0 ? operation.getKeys()[1].getKeySize() / 8 : 32;
    if (size % (keySize * operation.getDataStripes()) > 0) {
      size = keySize * operation.getDataStripes() * (size
          / (keySize * operation.getDataStripes()) + 1);
    }
    byte[] in2 = new byte[size];
    byte[] pad = VortexMessage.getLongAsBytes(in.length, paddingSize);
    String msg = "  calculated padded size (original: " + in.length + "; blocks: "
        + operation.getDataStripes() + "; block size: " + keySize + "; padded size: "
        + size + ")";
    LOGGER.log(Level.INFO, msg);

    // copy length prefix
    System.arraycopy(pad, 0, in2, 0, paddingSize);

    // copy data
    System.arraycopy(in, 0, in2, paddingSize, in.length);

    // repeat length prefix
    for (int i = 0; i < in2.length - in.length - paddingSize; i++) {
      in2[i + paddingSize + in.length] = pad[i % pad.length];
    }

    // do the redundancy calc
    MathMode mm = GaloisFieldMathMode.getGaloisFieldMathMode(operation.getGfSize());
    LOGGER.log(Level.INFO, "  preparing data matrixContent");
    Matrix data = new Matrix(in2.length / operation.getDataStripes(), operation.getDataStripes(),
        mm, in2);
    LOGGER.log(Level.INFO, "  data matrixContent is " + data.getX() + "x" + data.getY());
    LOGGER.log(Level.INFO, "  preparing redundancy matrixContent");
    RedundancyMatrix r = new RedundancyMatrix(operation.getDataStripes(),
        operation.getDataStripes() + operation.getRedundancy(), mm);
    LOGGER.log(Level.INFO, "  redundancy matrixContent is " + r.getX() + "x" + r.getY());
    LOGGER.log(Level.INFO, "  calculating");
    return r.mul(data);
  }

  private static long gcd(long n1, long n2) {
    long gcd = 1;

    for (int i = 1; i <= n1 && i <= n2; ++i) {
      // Checks if i is factor of both integers
      if (n1 % i == 0 && n2 % i == 0) {
        gcd = i;
      }
    }

    return gcd;
  }

  private static long lcm(long n1, long n2) {
    long gcd = gcd(n1, n2);
    return (n1 * n2) / gcd;
  }

  /***
   * <p>padds a given payload block.</p>
   *
   * @param blocksize the size of the blocks of the used encryption in the addRedundancy operation
   * @param numberOfOutBlocks the number of resulting blocks in the addRedundancy operation
   * @param data the data to be padded (payload block
   * @param prng the Prng to be used for padding
   * @param c1 the padding parameter c1 as specified in the padding spec
   * @param c2 the padding parameter c2 as specified in the padding spec
   * @return the padded data array
   */
  public static byte[] pad(int blocksize, int numberOfOutBlocks, byte[] data,
                           Prng prng, int c1, int c2) {
    LOGGER.log(Level.FINEST, "starting padding of " + data.length + " bytes with blocksize "
        + blocksize + " and output block count with " + numberOfOutBlocks);

    // catch some bad values
    if (c1 < 0) {
      c1 = 0;
    }
    if (c2 < 0) {
      c2 = 0;
    }

    // calculate sizes
    long outRowSize = lcm(blocksize, numberOfOutBlocks);
    long containerSize = ((long) (Math.ceil(((double) data.length + c2) / outRowSize))
        * outRowSize);
    LOGGER.log(Level.FINEST, "container size of padded array is " + (containerSize + 4)
        + " bytes (c1: " + c1 + "; c2: " + c2 + ")");

    // calculate padding value
    long modOp = (long) (Math.floor((0.0 + MAX_SIZE - 1 - data.length) / containerSize))
        * containerSize;
    long pval = (containerSize == 0 ? ThreadLocalRandom.current().nextLong(MAX_SIZE)
        : new BigInteger("" + data.length).add(new BigInteger("" + c1).multiply(
        new BigInteger("" + containerSize))).mod(new BigInteger("" + modOp)).longValue());
    LOGGER.log(Level.FINEST, "Padding value is " + pval + "");
    assert modOp < MAX_SIZE : "modulo value too big (" + modOp + ">" + MAX_SIZE + ")";
    assert pval < MAX_SIZE : "Padding value too big (" + pval + ">" + MAX_SIZE + ")";

    // create new container
    byte[] out = new byte[(int) containerSize + 4];

    // insert size descriptor
    out[0] = (byte) ((pval & 255) - 128);
    out[1] = (byte) (((pval >>> 8) & 255) - 128);
    out[2] = (byte) (((pval >>> 16) & 255) - 128);
    out[3] = (byte) (((pval >>> 24) & 255) - 128);
    LOGGER.log(Level.FINEST, "Encoded padding value is " + out[0] + ";" + out[1] + ";" + out[2]
        + ";" + out[3] + "");

    // insert data (inefficient yet working)
    for (int a = 4; a < data.length + 4; a++) {
      out[a] = data[a - 4];
    }

    // insert padding
    if (prng == null) {
      prng = localPrng;
    }
    for (int a = 4 + data.length; a < out.length; a++) {
      byte val = prng.nextByte();
      out[a] = val;
      if (a < 8 + data.length) {
        LOGGER.log(Level.FINEST, "  Padding start value is " + val + " at " + a);
      }
    }

    LOGGER.log(Level.FINEST, "Padding is done up to size " + out.length);

    return out;
  }

  /***
   * <p>Removes padding from a byte array.</p>
   * @param blocksize          encryption block size
   * @param numberOfOutBlocks  number of out streams in the RS function
   * @param in                 the padded array
   * @param prng               the random number generator for the padding data
   * @return the unpadded data stream
   *
   * @throws IOException       if unpadding fails for any reason
   */
  public static byte[] unpad(int blocksize, int numberOfOutBlocks, byte[] in, Prng prng)
      throws IOException {
    LOGGER.log(Level.FINEST, "starting unpadding of " + in.length + " bytes");

    // extract size descriptor
    LOGGER.log(Level.FINEST, "Encoded padding value is " + in[0] + ";" + in[1] + ";" + in[2] + ";"
        + in[3] + "");
    long size = ((long) (in[0]) + 128) + ((long) (in[1]) + 128) * 256 + ((long) (in[2]) + 128) * 256
        * 256 + ((long) (in[3]) + 128) * 256 * 256 * 256;
    LOGGER.log(Level.FINEST, "Padding value is " + size);
    if (in.length > 4) {
      size = size % (in.length - 4);
    } else {
      size = 0;
    }
    LOGGER.log(Level.FINEST, "size is " + size + " bytes");

    // creating output
    byte[] out = new byte[(int) size];
    for (int a = 4; a < out.length + 4; a++) {
      out[a - 4] = in[a];
    }

    // check if padding is correct
    if (prng != null) {
      for (int a = out.length + 4; a < in.length; a++) {
        byte val = prng.nextByte();
        if (a < 8 + out.length) {
          LOGGER.log(Level.FINEST, "  Padding start value is " + val + " at " + a);
        }
        if (in[a] != val) {
          throw new IOException("error verifying padding at position " + a + " in container");
        }
      }
    }

    return out;
  }

  public String toString() {
    return getInputId()[0] + "->addRedundancy(" + getOutputId().length + ")->" + getOutputId()[0];
  }

}
