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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * <p>Enumeration listing all available padding types for encryption.</p>
 */
public enum Padding implements Serializable {
  NONE(1000, "NoPadding", new AlgorithmType[] {AlgorithmType.SYMMETRIC}, new SizeCalc() {
    public int maxSize(int s) {
      return s / 8;
    }
  }),
  PKCS1(1001, "PKCS1Padding", new AlgorithmType[] {AlgorithmType.ASYMMETRIC}, new SizeCalc() {
    public int maxSize(int s) {
      return (s / 8) - 11;
    }
  }),
  OAEP_SHA256_MGF1(1100, "OAEPWithSHA256AndMGF1Padding",
      new AlgorithmType[] {AlgorithmType.ASYMMETRIC}, new SizeCalc() {
        public int maxSize(int s) {
          return (s / 8) - 2 - (256 / 4);
        }
      }
  ),
  OAEP_SHA384_MGF1(1101, "OAEPWithSHA384AndMGF1Padding",
      new AlgorithmType[] {AlgorithmType.ASYMMETRIC}, new SizeCalc() {
        public int maxSize(int s) {
          return s / 8 - 2 - 384 / 4;
        }
      }
  ),
  OAEP_SHA512_MGF1(1102, "OAEPWithSHA512AndMGF1Padding",
      new AlgorithmType[] {AlgorithmType.ASYMMETRIC}, new SizeCalc() {
        public int maxSize(int s) {
          return s / 8 - 2 - 512 / 4;
        }
      }
  ),
  PKCS7(1007, "PKCS7Padding",
      new AlgorithmType[] {AlgorithmType.SYMMETRIC}, new SizeCalc() {
        public int maxSize(int s) {
          return s / 8 - 1;
        }
      }
  );

  public static final long serialVersionUID = 100000000038L;

  private static final Map<AlgorithmType, Padding> DEFAULT_PADDING = new HashMap<>();

  private final int id;
  private final String txt;
  private final Set<AlgorithmType> at;
  private final SizeCalc sizeCalculator;
  final ASN1Enumerated asn;

  Padding(int id, String txt, AlgorithmType[] at, SizeCalc sizeCalculator) {
    this.id = id;
    this.txt = txt;
    this.at = new HashSet<>();
    this.at.addAll(Arrays.asList(at));
    this.sizeCalculator = sizeCalculator;
    this.asn = new ASN1Enumerated(id);
  }

  /***
   * <p>Get applicable padding sets for a given Algorithm type.</p>
   *
   * @param at        the type of algorithm
   * @return an array of supported paddings
   */
  public static Padding[] getAlgorithms(AlgorithmType at) {
    List<Padding> v = new ArrayList<>();
    for (Padding val : values()) {
      if (val.at.contains(at)) {
        v.add(val);
      }
    }
    return v.toArray(new Padding[v.size()]);
  }


  /***
   * <p>Get a padding by its ASN.1 ID.</p>
   *
   * @param id    the ASN.1 numericcal ID
   * @return the padding or null if ID is unknown
   */
  public static Padding getById(int id) {
    for (Padding e : values()) {
      if (e.id == id) {
        return e;
      }
    }
    return null;
  }

  /***
   * <p>Get a padding by its name.</p>
   *
   * @param name  the name used by the cryptographic provider
   * @return the padding or null if name is unknown
   */
  public static Padding getByString(String name) {
    for (Padding e : values()) {
      if (e.txt.equals(name)) {
        return e;
      }
    }
    return null;
  }

  /***
   * <p>Get the default padding for a given AlgorithmType.</p>
   *
   * @param at    the algorithm type
   * @return the default padding for the given algorithm type
   */
  public static Padding getDefault(AlgorithmType at) {
    // init hashmap if necesary
    synchronized (DEFAULT_PADDING) {
      if (DEFAULT_PADDING.isEmpty()) {
        synchronized (DEFAULT_PADDING) {
          DEFAULT_PADDING.clear();
          DEFAULT_PADDING.put(AlgorithmType.ASYMMETRIC, Padding.PKCS1);
          DEFAULT_PADDING.put(AlgorithmType.SYMMETRIC, Padding.PKCS7);
        }
      }
    }

    // return padding
    Padding p = DEFAULT_PADDING.get(at);
    if (p == null) {
      throw new NullPointerException("no default padding for " + at);
    }
    return p;
  }

  /***
   * <p>Get the numeric ASN.1 id of the padding.</p>
   *
   * @return the id of the padding
   */
  public int getId() {
    return id;
  }

  /***
   * <p>Get the textual representation of the padding for the cryptographic provider.</p>
   *
   * @return the name used within the cryptographic provider
   */
  public String toString() {
    return txt;
  }

  /***
   * <p>Gets the maximum payload size.</p>
   *
   * <p>The payload size is calculated by &lt;block size&gt;-&lt;padding overhead&gt;.</p>
   *
   * @param blockSize   the block size of the cryptographic algorithm (usually equals the key size)
   * @return the number of bytes a single block may hold including the padding information.
   */
  public int getMaxSize(int blockSize) {
    return sizeCalculator.maxSize(blockSize);
  }

  /***
   * <p>Get the corresponding ASN1 enumeration.</p>
   *
   * @return the ASN1 enumeration representing this padding
   */
  public ASN1Enumerated toAsn1() {
    return asn;
  }

}
