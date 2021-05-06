package net.messagevortex.asn1;

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

import net.messagevortex.asn1.encryption.Algorithm;
import net.messagevortex.asn1.encryption.AlgorithmType;
import net.messagevortex.asn1.encryption.DumpType;
import org.bouncycastle.asn1.ASN1Encodable;
import org.bouncycastle.asn1.ASN1Integer;
import org.bouncycastle.asn1.ASN1Object;

import java.io.IOException;
import java.io.Serializable;

/**
 * <p>Represents a Mac Algorithm as ASN.1 structure.</p>
 */
public class MacAlgorithm extends AbstractBlock implements Serializable {

  public static final long serialVersionUID = 100000000010L;

  Algorithm alg = null;

  public MacAlgorithm() {
    alg = Algorithm.getDefault(AlgorithmType.HASHING);
  }

  /***
   * <p>constructor to creates a mac algorith from an ASN.1 encoded object.</p>
   *
   * @param to            the object description in ASN.1 notation
   * @throws IOException  if an error occures during parsing
   * @throws NullPointerException if object is null
   */
  public MacAlgorithm(ASN1Encodable to) throws IOException {
    if (to == null) {
      throw new NullPointerException("object may not be null");
    }
    parse(to);
  }

  /***
   * <p>constructor to creates a mac algorith from an ASN.1 encoded object.</p>
   *
   * @param a             the object description in ASN.1 notation
   * @throws IOException  if an error occures during parsing
   * @throws NullPointerException if object is null
   */
  public MacAlgorithm(Algorithm a) throws IOException {
    if (a == null) {
      throw new NullPointerException("object may not be null");
    }
    if (a.getAlgorithmType() != AlgorithmType.HASHING) {
      throw new IOException("Algorithm must be of type hashing");
    }
    alg = a;
  }

  protected final void parse(ASN1Encodable to) throws IOException {
    Algorithm a = Algorithm.getById(ASN1Integer.getInstance(to).getValue().intValue());
    if (a == null || a.getAlgorithmType() != AlgorithmType.HASHING) {
      throw new IOException("Only hashing algorithms may be parsed");
    }
    alg = a;
  }

  @Override
  public ASN1Object toAsn1Object(DumpType dumpType) {
    return new ASN1Integer(alg.getId());
  }

  @Override
  public String dumpValueNotation(String prefix, DumpType dumpType) {
    return "" + alg.getId();
  }

  /***
   * <p>Sets the algorithm.</p>
   * @param alg the algorithm to be used
   * @return the previously set algorithm
   * @throws IOException if algorithm is not of the correct type
   */
  public Algorithm setAlgorithm(Algorithm alg) throws IOException {
    if (alg.getAlgorithmType() != AlgorithmType.HASHING) {
      throw new IOException("Algorithm must be of type hashing");
    }
    Algorithm old = this.alg;
    this.alg = alg;
    return old;
  }

  public Algorithm getAlgorithm() {
    return alg;
  }

}
