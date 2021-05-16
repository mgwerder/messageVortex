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
public final class ExtendedSecureRandom {

    private static final java.util.logging.Logger LOGGER;

    static {
        LOGGER = MessageVortexLogger.getLogger((new Throwable()).getStackTrace()[0].getClassName());
        //MessageVortexLogger.setGlobalLogLevel(Level.ALL);
    }

    private static final SecureRandom sr = new SecureRandom();

    /***
     * <p>Returns an integer between 0 and bound-1.</p>
     *
     * @param bound the maximum value to be used
     * @return a random integer value between 0 and bound-1
     */
    public static int nextInt(int bound) {
        return sr.nextInt(bound);
    }

    /***
     * <p>Returns an integer between low and up-1.</p>
     *
     * @param low the minimum value to be returned
     * @param up the maximum value to be used
     * @return a random integer value between low and up-1
     *
     * @throws IllegalArgumentException if low &gt;= up
     */
    public static int nextInt(int low, int up) {
        if (low >= up) {
            throw new IllegalArgumentException("lower bound is not smaller than upper bound");
        }
        return low + sr.nextInt(up - low);
    }

    /***
     * <p>An array filled with random byte values.</p>
     *
     * @param array  the array to be filled
     */
    public static void nextBytes(byte[] array) {
        sr.nextBytes(array);
    }

    /***
     * <p>Returns the given number of seed bytes, computed using the seed generation algorithm
     * that this class uses to seed itself.</p>
     *
     * @param i  the number of bytes to be generated
     * @return the seed bytes
     */
    public static byte[] generateSeed(int i) {
        return sr.generateSeed(i);
    }

    /***
     * <p>Returns the next pseudorandom, uniformly distributed double value between 0.0 and 1.0 from
     * this random number generator's sequence.</p>
     *
     * @return the next pseudorandom value
     */
    public static double nextDouble() {
        return sr.nextDouble();
    }

    /***
     * <p>Returns an internal representation of the secure Random number generator.</p>
     *
     * @return the random number generator
     */
    public static SecureRandom getSecureRandom() {
        return sr;
    }

    /***
     * <p>Returns a gaussian distributed value between 0 and 1 (maximum at 0.5).</p>
     *
     * @return a gaussian random value
     */
    public static double nextGauss() {
        double result = -1;
        while (result < 0 || result > 1) {
            result = (sr.nextGaussian() + Math.E) / 2 / Math.E;
        }
        return result;
    }

    /***
     * <p>Returns a random time.</p>
     *
     * @param start the earliest allowed time
     * @param peak the peak time (50% chance)
     * @param end the latest time
     *
     * @return a gaussian random value
     */
    public static double nextRandomTime(long start, long peak, long end) {
        if (peak <= start || end <= peak) {
            throw new NullPointerException("random time must offer a valid window [start(" + start
                    + ")<peak(" + peak + ")<end(" + end + ")]");
        }
        double ret = -1;
        // LOGGER.log(Level.FINEST, "Getting random Time " + start + "/" + peak + "/" + end);
        while (ret < start || ret > end) {
            ret = sr.nextGaussian();
            double d = sr.nextDouble();
            if (d < (double) (peak - start) / (end - start)) {
                ret = peak - (Math.abs(ret) * (peak - start) / 5.0);
            } else {
                ret = peak + (Math.abs(ret) * (end - peak) / 5.0);
            }
        }
        // LOGGER.log(Level.FINEST, "Done getting random Time (" + (ret) + ")");
        return ret;
    }


}
