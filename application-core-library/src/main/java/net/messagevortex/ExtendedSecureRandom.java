package net.messagevortex;

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

import java.security.SecureRandom;

/**
 * <p>A Specialized random number generator for MessageVortex.</p>
 */
public class ExtendedSecureRandom {

  private static final SecureRandom sr = new SecureRandom();

  /***
   * <p>Returns an integer between 0 and bound-1.</p>
   *
   * @param bound the maximum value to be used
   * @return a random integer value between 0 and bound-1
   */
  public static final int nextInt(int bound) {
    return sr.nextInt(bound);
  }

  /***
   * <p>An array filled with random byte values.</p>
   *
   * @param array  the array to be filled
   */
  public static final void nextBytes(byte[] array) {
    sr.nextBytes(array);
  }

  /***
   * <p>Returns the given number of seed bytes, computed using the seed generation algorithm
   * that this class uses to seed itself.</p>
   *
   * @param i  the number of bytes to be generated
   * @return the seed bytes
   */
  public static final byte[] generateSeed(int i) {
    return sr.generateSeed(i);
  }

  /***
   * <p>Returns the next pseudorandom, uniformly distributed double value between 0.0 and 1.0 from
   * this random number generator's sequence.</p>
   *
   * <p>The general contract of nextDouble is that one double value, chosen (approximately)
   * uniformly from the range 0.0d (inclusive) to 1.0d (exclusive), is pseudorandomly
   * generated and returned.</p>
   *
   * <p>The method nextDouble is implemented by class Random as if by:</p>
   * <code>
   * public double nextDouble() {
   *   return (((long)next(26) << 27) + next(27)) / (double)(1L << 53);
   *   }
   * </code>
   * <p>The hedge "approximately" is used in the foregoing description only because the next
   * method is only approximately an unbiased source of independently chosen bits. If it were
   * a perfect source of randomly chosen bits, then the algorithm shown would choose double
   * values from the stated range with perfect uniformity.</p>
   *
   * @return the next pseudorandom, uniformly distributed double value between 0.0 and 1.0 from
   *         this random number generator's sequence
   * @FIXME Where is that from?
   */
  public static final double nextDouble() {
    return sr.nextDouble();
  }

  /***
   * <p>Returns an internal representation of the secure Random number generator.</p>
   *
   * @return the random number generator
   */
  public static final SecureRandom getSecureRandom() {
    return sr;
  }

  /***
   * <p>Returns a gaussian distributed value between 0 and 1 (maximum at 0.5).</p>
   *
   * @return a gaussian random value
   */
  public static final double nextGauss() {
    double result = -1;
    while (result < 0 || result > 1) {
      result = (sr.nextGaussian() + Math.E) / 2 / Math.E;
    }
    return result;
  }

  /***
   * <p>Returns a random time.</p>
   *
   * @return a gaussian random value
   */
  public static final double nextRandomTime(long start, long peak, long end) {
    double ret = -1;
    while (ret < start) {
      ret = sr.nextGaussian();
      if (ret < 0) {
        ret = ret * (peak - start) / Math.E + peak;
      } else {
        ret = ret * (end - peak) / Math.E + peak;
      }
    }
    return ret;
  }

}
