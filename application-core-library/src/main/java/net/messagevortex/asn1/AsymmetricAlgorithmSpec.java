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

import net.messagevortex.MessageVortexLogger;
import net.messagevortex.asn1.encryption.Algorithm;
import net.messagevortex.asn1.encryption.DumpType;
import org.bouncycastle.asn1.ASN1Encodable;
import org.bouncycastle.asn1.ASN1EncodableVector;
import org.bouncycastle.asn1.ASN1Enumerated;
import org.bouncycastle.asn1.ASN1Object;
import org.bouncycastle.asn1.ASN1Sequence;
import org.bouncycastle.asn1.DERSequence;

import java.io.IOException;
import java.io.Serializable;

/**
 * <p>Specification for AsymmetricAlgorithmSpec.</p>
 */
public class AsymmetricAlgorithmSpec extends AbstractBlock implements Serializable, Dumpable {

  public static final long serialVersionUID = 100000000003L;

  private static final java.util.logging.Logger LOGGER;

  static {
    LOGGER = MessageVortexLogger.getLogger((new Throwable()).getStackTrace()[0].getClassName());
  }

  Algorithm algorithm;
  AlgorithmParameter parameter;

  /***
   * <p>Copy constructor.</p>
   *
   * @param  to           object to clone
   * @throws IOException  when failing to copy source object
   */
  public AsymmetricAlgorithmSpec(AsymmetricAlgorithmSpec to) throws IOException {
    parse(to.toAsn1Object(DumpType.ALL));
  }

  public AsymmetricAlgorithmSpec(Algorithm alg, AlgorithmParameter params) {
    this.algorithm = alg;
    this.parameter = params;
  }

  /***
   * <p>Constructor to build from ASN1 object.</p>
   *
   * @param  to           Object to be parsed
   * @throws IOException when failing to parse ASN1 object
   */
  public AsymmetricAlgorithmSpec(ASN1Encodable to) throws IOException {
    parse(to);
  }

  @Override
  protected final void parse(ASN1Encodable to) throws IOException {
    int i = 0;
    ASN1Sequence s1 = ASN1Sequence.getInstance(to);

    // getting algorithm
    ASN1Enumerated en = ASN1Enumerated.getInstance(s1.getObjectAt(i++));
    algorithm = Algorithm.getById(en.getValue().intValue());

    // get optional parameters
    if (s1.size() > 1) {
      parameter = new AlgorithmParameter(s1.getObjectAt(i++));
    }
  }

  /***
   * <p>Gets the algorithm (@see Algorithm).</p>
   *
   * @return the current algorithm
   */
  public Algorithm getAlgorithm() {
    return algorithm;
  }

  /***
   * <p>Get the algorithm parameters (@see AlgorithmParameter).</p>
   *
   * @return the current algorithm parameters
   */
  public AlgorithmParameter getAlgorithmParameter() {
    return parameter;
  }

  @Override
  public String dumpValueNotation(String prefix, DumpType dumptype) {
    StringBuilder sb = new StringBuilder();
    sb.append('{').append(CRLF);
    sb.append(prefix).append("  ").append("algorithm ").append(algorithm.name().toLowerCase());
    if (parameter != null) {
      sb.append(',').append(CRLF);
      sb.append(prefix).append("  ").append("parameter ")
              .append(parameter.dumpValueNotation(prefix + "  ", dumptype)).append(CRLF);
    } else {
      sb.append(CRLF);
    }
    sb.append(prefix).append('}');
    return sb.toString();
  }

  @Override
  public ASN1Object toAsn1Object(DumpType dumpType) throws IOException {
    if (algorithm == null) {
      throw new IOException("Algorithm is empty .. unable to create AsymmetricAlgorithmSpec");
    }
    ASN1EncodableVector v = new ASN1EncodableVector();
    v.add(new ASN1Enumerated(algorithm.getId()));
    if (parameter != null) {
      v.add(parameter.toAsn1Object(dumpType));
    }
    return new DERSequence(v);
  }
}
