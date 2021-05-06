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

import org.bouncycastle.asn1.ASN1Enumerated;

import java.util.ArrayList;

/**
 * Enumeration to list available encryption modes.
 */
public enum Mode {
  ECB(10000, "ECB", false, new String[] {
      "ECIES", "RSA", "CAMELLIA128", "CAMELLIA192", "CAMELLIA256", "Twofish128",
      "Twofish192", "Twofish256"}, new Padding[] {Padding.PKCS7}),
  CBC(10001, "CBC", true, new String[] {
      "aes128", "aes192", "aes256", "CAMELLIA128", "CAMELLIA192", "CAMELLIA256",
      "Twofish128", "Twofish192", "Twofish256"}, new Padding[] {Padding.PKCS7}),
  EAX(10002, "EAX", true, new String[] {
      "CAMELLIA128", "CAMELLIA192", "CAMELLIA256", "Twofish128", "Twofish192",
      "Twofish256"}, new Padding[] {Padding.PKCS7}),
  CTR(10003, "CTR", true, new String[] {
      "aes128", "aes192", "aes256", "CAMELLIA128", "CAMELLIA192", "CAMELLIA256",
      "Twofish128", "Twofish192", "Twofish256"}, new Padding[] {Padding.PKCS7}),
  CCM(10004, "CCM", true, new String[] {
      "aes128", "aes192", "aes256", "CAMELLIA128", "CAMELLIA192", "CAMELLIA256",
      "Twofish128", "Twofish192", "Twofish256"}, new Padding[] {Padding.PKCS7}),
  GCM(10005, "GCM", true, new String[] {
      "aes128", "aes192", "AES256", "CAMELLIA128", "CAMELLIA192", "CAMELLIA256",
      "Twofish128", "Twofish192", "Twofish256"}, new Padding[] {Padding.PKCS7}),
  OCB(10006, "OCB", true, new String[] {
      "aes128", "aes192", "AES256", "CAMELLIA128", "CAMELLIA192", "CAMELLIA256",
      "Twofish128", "Twofish192", "Twofish256"}, new Padding[] {Padding.PKCS7}),
  OFB(10007, "OFB", true, new String[] {
      "CAMELLIA128", "CAMELLIA192", "CAMELLIA256", "Twofish128", "Twofish192",
      "Twofish256"}, new Padding[] {Padding.PKCS7}),
  NONE(10100, "NONE", false, new String[] {"ECIES", "RSA"}, new Padding[] {Padding.PKCS7});

  final int id;
  final String txt;
  final boolean requiresInitVector;
  final String[] alg;
  final Padding[] pad;
  final ASN1Enumerated asn;

  Mode(int id, String txt, boolean iv, String[] alg, Padding[] pad) {
    this.id = id;
    this.txt = txt;
    this.requiresInitVector = iv;
    this.alg = alg;
    this.pad = pad;
    this.asn = new ASN1Enumerated(id);
  }

  public boolean getRequiresInitVector() {
    return this.requiresInitVector;
  }

  /**
   * Get enumeration element by its ASN.1 ID.
   *
   * @param id the ID of the element to be obtained
   * @return the element or null if the ID is unknown
   */
  public static Mode getById(int id) {
    for (Mode e : values()) {
      if (e.id == id) {
        return e;
      }
    }
    return null;
  }


  /**
   * <p>Get enumeration element by its name.</p>
   *
   * @param name the name of the element to be obtained
   * @return the element or null if the name is unknown
   */
  public static Mode getByString(String name) {
    for (Mode e : values()) {
      if (e.txt.equals(name)) {
        return e;
      }
    }
    return null;
  }

  /***
   * <p>Gets the currently set default value for the given type.</p>
   *
   * @param type the type for which the default value is required
   * @return the default value requested
   */
  public static Mode getDefault(AlgorithmType type) {
    switch (type) {
      case ASYMMETRIC:
        return ECB;
      case SYMMETRIC:
        return CBC;
      default:
        throw new IllegalArgumentException("Type " + type + " is not suitable for mode.");
    }
  }

  /***
   * <p>Gets the ASN.1 numerical ID.</p>
   *
   * @return the numerical ID
   */
  public int getId() {
    return id;
  }

  /***
   * <p>Gets the mode identifier as required by the encryption provider.</p>
   *
   * <p>This value is returned regardless of the support of the provider classes.</p>
   *
   * @return the mode identifier
   */
  public String toString() {
    return txt;
  }

  /***
   * <p>Gets all known paddings regardless of their support.</p>
   *
   * @return an array of all paddings
   */
  public Padding[] getPaddings() {
    return pad.clone();
  }

  /***
   * <p>Gets all cipher modes suitable for the specified algorithm.</p>
   *
   * @param alg the algorithm to be supported
   * @return an array of modes supported
   */
  public static Mode[] getModes(Algorithm alg) {
    ArrayList<Mode> l = new ArrayList<>();
    for (Mode m : values()) {
      for (String a : m.alg) {
        if (alg == Algorithm.getByString(a)) {
          l.add(m);
        }
      }
    }
    return l.toArray(new Mode[0]);
  }

  /**
   * <p>Gets the corresponding ASN1 enumeration.</p>
   *
   * @return the corresponding ASN1 enumeration
   */
  public ASN1Enumerated toAsn1() {
    return asn;
  }

}

