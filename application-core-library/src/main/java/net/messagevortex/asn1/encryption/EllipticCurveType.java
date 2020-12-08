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

import java.util.List;
import java.util.Vector;

/**
 * <p>Represents all supported EC named curves.</p>
 */
public enum EllipticCurveType {
  
  SECP384R1(2500, "secp384r1", 384, Algorithm.EC, SecurityLevel.MEDIUM),
  SECT409K1(2501, "sect409k1", 409, Algorithm.EC, SecurityLevel.HIGH),
  SECP521R1(2502, "secp521r1", 521, Algorithm.EC, SecurityLevel.QUANTUM);
  
  static EllipticCurveType def = SECP521R1;
  
  private int id;
  private EllipticCurveType courveType;
  private String txt;
  private Algorithm alg;
  private SecurityLevel secLevel;
  private int keySize;
  
  EllipticCurveType(int id, String txt, int keySize, Algorithm alg, SecurityLevel level) {
    this.id = id;
    this.txt = txt;
    this.alg = alg;
    this.secLevel = level;
    this.keySize = keySize;
  }
  
  /***
   * <p>Gets en elliptic curve by id.</p>
   *
   * @param id  the id to look up
   * @return the enum or null if not found
   */
  public static EllipticCurveType getById(int id) {
    for (EllipticCurveType e : values()) {
      if (e.id == id) {
        return e;
      }
    }
    return null;
  }
  
  /***
   * <p>Gets en elliptic curve by keySize.</p>
   *
   * @param ks  the keysize to look up
   * @return an array of suitable enums
   */
  public static EllipticCurveType[] getByKeySize(int ks) {
    List<EllipticCurveType> l = new Vector<>();
    for (EllipticCurveType e : values()) {
      if (e.getKeySize() == ks) {
        l.add(e);
      }
    }
    return l.toArray(new EllipticCurveType[l.size()]);
  }
  
  /***
   * <p>Gets en elliptic enum curve by name.</p>
   *
   * @param s  the name to look up
   * @return the enum or null if not found
   */
  public static EllipticCurveType getByString(String s) {
    for (EllipticCurveType e : values()) {
      if (e.toString().equals(s)) {
        return e;
      }
    }
    return null;
  }
  
  public int getId() {
    return id;
  }
  
  public String toString() {
    return txt;
  }
  
  public SecurityLevel getSecurityLevel() {
    return secLevel;
  }
  
  public Algorithm getAlgorithm() {
    return alg;
  }
  
  public int getKeySize() {
    return keySize;
  }
  
  public static EllipticCurveType getDefault() {
    return def;
  }
}

