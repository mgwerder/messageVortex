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
 * <p>Enumeration for representing types of operation.</p>
 */
public enum CipherUsage {

  SIGN(200, "sign"),
  ENCRYPT(210, "encrypt");

  private final int id;
  private final String txt;

  CipherUsage(int id, String txt) {
    this.id = id;
    this.txt = txt;
  }

  public int getId() {
    return this.id;
  }

  public String getUsageString() {
    return this.txt;
  }

  /***
   * <p>Gets a usage by its name.</p>
   *
   * @param name the name to look up
   * @return the enum or null if not found
   */
  public static CipherUsage getByString(String name) {
    if (name == null) {
      return null;
    }
    for (CipherUsage e : values()) {
      if (e.txt.equals(name.toLowerCase())) {
        return e;
      }
    }
    return null;
  }

  /***
   * <p>Gets a usage by its Id.</p>
   *
   * @param id the id to look up
   * @return the enum or null if not found
   */
  public static CipherUsage getById(int id) {
    for (CipherUsage e : values()) {
      if (e.id == id) {
        return e;
      }
    }
    return null;
  }

}
