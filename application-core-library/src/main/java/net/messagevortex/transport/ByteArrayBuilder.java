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

import java.util.Arrays;

public class ByteArrayBuilder {

  byte[] values = new byte[0];

  /**
   * Builds a byte array.
   *
   * @param bytes  Array to be appended
   * @param length Number of bytes to be attached
   * @return A reference for daisy chaining operations
   */
  public ByteArrayBuilder append(byte[] bytes, int length) {
    int offset = values.length;
    values = Arrays.copyOf(values, offset + length);
    System.arraycopy(bytes, 0, values, offset, length);
    return this;
  }

  /**
   * Get the current byte array.
   *
   * @return The requested byte array
   */
  public byte[] toBytes() {
    return values.clone();
  }

}
