package net.messagevortex.asn1.encryption;

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
 * <p>Enumeration of all possible security levels.</p>
 *
 * <p>The security level classifies the algorithms strength in regards to the security.</p>
 */
public enum SecurityLevel {
  LOW,
  MEDIUM,
  HIGH,
  QUANTUM;

  private static SecurityLevel defaultLevel = MEDIUM;

  /***
   * <p>Retrieves the default security level to be used.</p>
   *
   * @return the default security level
   */
  public static SecurityLevel getDefault() {
    return defaultLevel;
  }

  /***
   * <p>Sets the default security level to be used.</p>
   *
   * @param newLevel   the new default security level for all operations to be set
   * @return the previous security level
   */
  public static SecurityLevel setDefault(SecurityLevel newLevel) {
    SecurityLevel ret = defaultLevel;
    defaultLevel = newLevel;
    return ret;
  }

  /**
   * <p>Retrieves the next higher security level.</p>
   *
   * @return the next higher security level
   */
  public SecurityLevel next() {
    if (this.ordinal() == values().length - 1) {
      return null;
    }
    return values()[(this.ordinal() + 1) % values().length];
  }

  @Override
  public String toString() {
    return this.name();
  }

}
