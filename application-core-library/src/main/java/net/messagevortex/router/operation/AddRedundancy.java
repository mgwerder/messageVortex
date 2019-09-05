package net.messagevortex.router.operation;

// ************************************************************************************
// * Copyright (c) 2018 Martin Gwerder (martin@gwerder.net)
// *
// * Permission is hereby granted, free of charge, to any person obtaining a copy
// * of this software and associated documentation files (the "Software"), to deal
// * in the Software without restriction, including without limitation the rights
// * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
// * copies of the Software, and to permit persons to whom the Software is
// * furnished to do so, subject to the following conditions:
// *
// * The above copyright notice and this permission notice shall be included in all
// * copies or substantial portions of the Software.
// *
// * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
// * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
// * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
// * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
// * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
// * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
// * SOFTWARE.
// ************************************************************************************

import java.io.IOException;
import java.io.Serializable;
import java.math.BigInteger;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Random;
import java.util.Vector;
import java.util.logging.Level;

import net.messagevortex.MessageVortexLogger;
import net.messagevortex.asn1.AbstractRedundancyOperation;
import net.messagevortex.asn1.AddRedundancyOperation;
import net.messagevortex.asn1.PayloadChunk;
import net.messagevortex.asn1.SymmetricKey;
import net.messagevortex.asn1.VortexMessage;
import net.messagevortex.asn1.encryption.Prng;

/**
 * <p>This is the core of the redundancy add operation.</p>
 *
 * <p>It builds redundant data blocksfrom the existing data blocks.</p>
 */
public class AddRedundancy extends AbstractOperation implements Serializable {

  private static final long MAX_SIZE = (long) Math.pow(2, 32);

  /***
   * <p>Wrapper for the java random number generator (not normative).</p>
   */
  public static class SimplePrng implements Prng {

    private Random sr = new Random();
    private long seed = sr.nextLong();

    public SimplePrng() {
      sr.setSeed(seed);
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

  private static Prng localPrng = new SimplePrng();

  public static final long serialVersionUID = 100000000018L;

  private static final java.util.logging.Logger LOGGER;

  static {
    LOGGER = MessageVortexLogger.getLogger((new Throwable()).getStackTrace()[0].getClassName());
    MessageVortexLogger.setGlobalLogLevel(Level.ALL);
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
    if (!canRun()) {
      return new int[0];
    }
    LOGGER.log(Level.INFO, "executing add redundancy operation (" + toString() + ")");
    byte[] in = payload.getPayload(operation.getInputId()).getPayload();

    Matrix out = executeInt(in);

    // set output
    LOGGER.log(Level.INFO, "  setting output chunks");
    int tot = 0;
    assert out.getY() == (operation.getRedundancy() + operation.getDataStripes());
    try {
      for (int i = 0; i < out.getY(); i++) {
        int plid = operation.getOutputId() + i;
        byte[] b = operation.getkeys()[i].encrypt(out.getRowAsByteArray(i));
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

  public static byte[] execute(byte[] in, int redundancy, int dataStripes, int gf) {
    AddRedundancy ar = new AddRedundancy(new AddRedundancyOperation(-1, redundancy, dataStripes, new Vector<SymmetricKey>(), -1, gf));

    LOGGER.log(Level.INFO, "executing add redundancy operation (" + ar.toString() + ")");
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
    int keySize = operation.getkeys().length!=0?operation.getkeys()[1].getKeySize() / 8:32;
    if (size % (keySize * operation.getDataStripes()) > 0) {
      size = keySize * operation.getDataStripes() * (size
              / (keySize * operation.getDataStripes()) + 1);
    }
    byte[] in2 = new byte[size];
    byte[] pad = VortexMessage.getLongAsBytes(in.length, paddingSize);
    LOGGER.log(Level.INFO, "  calculated padded size (original: " + in.length + "; blocks: "
            + operation.getDataStripes() + "; block size: " + keySize + "; padded size: "
            + size + ")");

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

  /***
   * <p>padds a given payload block.</p>
   * @param blocksize the size of the blocks of the used encryption in the addRedundancy operation
   * @param numberOfOutBlocks the number of resulting blocks in the addRedundancy operation
   * @param data the data to be padded (payload block
   * @param prng the Prng to be used for padding
   * @param c1 the padding parameter c1 as specified in the padding spec
   * @param c2 the padding parameter c2 as specified in the padding spec
   * @return
   */
  public static byte[] pad(int blocksize, int numberOfOutBlocks, byte[] data,
                           Prng prng, int c1, int c2) {
    LOGGER.log(Level.FINEST, "starting padding of " + data.length + " bytes");

    // catch some bad values
    if (c1 < 0) {
      c1 = 0;
    }
    if (c2 < 0) {
      c2 = 0;
    }

    // calculate sizes
    int outRowSize = blocksize * numberOfOutBlocks;
    long containerSize = (long) (Math.ceil(((double) data.length + 4 + c2) / outRowSize))
            * outRowSize;
    LOGGER.log(Level.FINEST, "container size of padded array is " + containerSize + " bytes (c1: "
            + c1 + "; c2: " + c2 + ")");

    // calculate padding value
    long pval = (new BigInteger("" + data.length).add(new BigInteger("" + c1).multiply(
            new BigInteger("" + containerSize)).mod(new BigInteger(""
            + (((MAX_SIZE - data.length) / containerSize) * containerSize))
    ))).longValue();
    LOGGER.log(Level.FINEST, "Padding value is " + pval + "");

    // create new container
    byte[] out = new byte[(int) containerSize];

    // insert size descriptor
    out[0] = (byte) ((pval & 255) - 128);
    out[1] = (byte) (((pval >>> 8) & 255) - 128);
    out[2] = (byte) (((pval >>> 16) & 255) - 128);
    out[3] = (byte) (((pval >>> 24) & 255) - 128);

    // insert data (inefficient yet working)
    for (int a = 4; a < data.length + 4; a++) {
      out[a] = data[a - 4];
    }

    // insert padding
    if (prng == null) {
      prng = localPrng;
    }
    for (int a = 4 + data.length; a < out.length; a++) {
      out[a] = prng.nextByte();
    }

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
    long size = ((long) (in[0]) + 128) + ((long) (in[1]) + 128) * 256 + ((long) (in[2]) + 128) * 256
            * 256 + ((long) (in[3]) + 128) * 256 * 256 * 256;
    LOGGER.log(Level.FINEST, "Padding value is " + size);
    size = size % in.length;
    LOGGER.log(Level.FINEST, "size is " + size + " bytes");

    // creating output
    byte[] out = new byte[(int) size];
    for (int a = 4; a < out.length + 4; a++) {
      out[a - 4] = in[a];
    }

    // check if padding is correct
    if (prng != null) {
      for (int a = out.length + 4; a < in.length; a++) {
        if (in[a] != prng.nextByte()) {
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
