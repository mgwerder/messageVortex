package net.gwerder.java.messagevortex.routing.operation;
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

/**
 * <p>Shifts bits circularily.</p>
 */
public class BitShifter {

  /***
   * <p>shifts bits circularily right.</p>
   *
   * @param value   the value to be shifted
   * @param shift   the number of positions to be shifted
   * @param length  the length of the circular buffer
   * @return the new value
   */
  public static int rshift(int value, int shift, byte length) {
    return lshift(value, -shift, length);
  }

  /***
   * <p>shifts bits circularily left.</p>
   *
   * @param value   the value to be shifted
   * @param shift   the number of positions to be shifted
   * @param length  the length of the circular buffer
   * @return the new value
   */
  public static int lshift(int value, int shift, byte length) {
    long ret = value;
    if (shift == 0) {
      return value;
    }
    int lshift = shift % length;
    if (lshift < 0) {
      lshift += length;
    }

    // do shift
    ret = ret << lshift;

    // move overflow to lower end
    long bitmask = ((long) Math.pow(2, lshift) - 1) << length;
    long lowbits = (ret & bitmask) >> length;
    ret = ret | lowbits;

    // truncate result (inefficient but works)
    ret = ret & ((int) Math.pow(2, length) - 1);
    return (int) ret;
  }

}
