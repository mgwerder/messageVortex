package net.messagevortex.transport;

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

import java.util.Random;

public class RandomString {

  private static final char[] symbols;

  static {
    StringBuilder tmp = new StringBuilder();
    for (char ch = '0'; ch <= '9'; ++ch) {
      tmp.append(ch);
    }
    for (char ch = 'a'; ch <= 'z'; ++ch) {
      tmp.append(ch);
    }
    symbols = tmp.toString().toCharArray();
  }

  private static final Random random = new Random();

  private RandomString() {
    // dummy constructor to overrule the default constructor
  }

  /***
   * <p>Returns a random string with the symbol set [0-9a-z].</p>
   * @param length       the length in characters the string is requested
   * @return             the requested random string
   */
  public static String nextString(int length) {
    return nextString(length, new String(symbols));
  }

  /***
   * <p>Returns a random string.</p>
   * @param length       the length in characters the string is requested
   * @param symbolString the allowed symbols for the string
   * @return             the requested random string
   */
  public static String nextString(int length, String symbolString) {
    if (length < 1) {
      throw new IllegalArgumentException("length < 1: " + length);
    }
    char[] symbols = symbolString.toCharArray();
    char[] buf = new char[length];
    for (int i = 0; i < buf.length; ++i) {
      buf[i] = symbols[random.nextInt(symbols.length)];
    }
    return new String(buf);
  }
}

