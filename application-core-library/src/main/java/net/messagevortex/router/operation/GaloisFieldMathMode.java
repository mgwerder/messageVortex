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

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Offers galoise Math required for redundancy matrices.
 */
public class GaloisFieldMathMode implements MathMode {

  private final int gfFieldSize;
  private final int omega;
  private final int[] gfLog;
  private final int[] gfInverseLog;

  static final int[] PRIM_POLYNOM = new int[]{3, 7, 11, 19, 37, 67, 137, 285, 529, 1033, 2053,
                                              4179, 8219, 17475, 32771, 69643};
  static final Map<Integer, GaloisFieldMathMode> cachedMathMode = new ConcurrentHashMap<>();

  private GaloisFieldMathMode(int omega) {
    if (omega < 2 || omega > 16) {
      throw new ArithmeticException("illegal GF size " + omega + " (PRIM_POLYNOM unknown)");
    }
    this.omega = omega;
    gfFieldSize = (int) Math.pow(2, omega);
    gfLog = new int[gfFieldSize];
    gfInverseLog = new int[gfFieldSize];
    int b = 1;
    for (int log = 0; log < gfFieldSize - 1; log++) {
      gfLog[b % gfFieldSize] = log;
      gfInverseLog[log % gfFieldSize] = b;
      b = BitShifter.lshift(b, 1, (byte) 33);
      if ((b & gfFieldSize) != 0) {
        b = b ^ PRIM_POLYNOM[omega - 1];
      }
    }
    // initialize undefined values with 0
    gfLog[0] = -1;
    gfInverseLog[gfFieldSize - 1] = -1;
  }

  /***
   * <p>Gets a singleton math mode for the specified omega.</p>
   *
   * @param omega  the number of bits to be used
   * @return the math mode (singleton)
   */
  public static GaloisFieldMathMode getGaloisFieldMathMode(int omega) {
    GaloisFieldMathMode ret = cachedMathMode.get(omega);
    if (ret == null) {
      ret = new GaloisFieldMathMode(omega);
      cachedMathMode.put(omega, ret);
    }
    return ret;
  }

  @Override
  public int mul(int c1, int c2) {
    if (c1 == 0 || c2 == 0) {
      return 0;
    }
    int sumLog = gfLog[c1] + gfLog[c2];
    if (sumLog >= gfFieldSize - 1) {
      sumLog -= gfFieldSize - 1;
    }
    return gfInverseLog[sumLog];
  }

  @Override
  public int div(int c1, int divisor) {
    if (c1 == 0) {
      return 0;
    }
    if (divisor == 0) {
      throw new ArithmeticException("Divisionby 0");
    }
    int diffLog = gfLog[c1] - gfLog[divisor];
    while (diffLog < 0) {
      diffLog += gfFieldSize - 1;
    }
    return gfInverseLog[diffLog];
  }

  @Override
  public int add(int c1, int c2) {
    return c1 ^ c2;
  }

  @Override
  public int sub(int c1, int c2) {
    return add(c1, c2);
  }

  public int[] getGfLog() {
    return gfLog;
  }

  public int[] getGfIlog() {
    return gfInverseLog;
  }

  @Override
  public String toString() {
    return "GF(2^" + omega + ")";
  }
}
