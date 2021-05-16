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
import net.messagevortex.asn1.encryption.DumpType;
import net.messagevortex.asn1.encryption.Parameter;
import org.bouncycastle.asn1.ASN1Encodable;
import org.bouncycastle.asn1.ASN1EncodableVector;
import org.bouncycastle.asn1.ASN1Enumerated;
import org.bouncycastle.asn1.ASN1Sequence;
import org.bouncycastle.asn1.DERSequence;
import org.bouncycastle.jce.provider.BouncyCastleProvider;

import java.io.IOException;
import java.io.Serializable;
import java.security.Security;

/**
 * <p>Abstract class for all encryption key types.</p>
 */
public abstract class Key extends AbstractBlock implements Serializable {

  public static final long serialVersionUID = 100000000009L;

  static {
    Security.addProvider(new BouncyCastleProvider());
  }


  AlgorithmParameter parameters = new AlgorithmParameter();

  void parseKeyParameter(ASN1Sequence s) throws IOException {
    Algorithm alg = Algorithm.getById(
            ASN1Enumerated.getInstance(s.getObjectAt(0)).getValue().intValue()
    );
    parameters = new AlgorithmParameter(s.getObjectAt(1));
    parameters.put(Parameter.ALGORITHM.getId(), alg.toString());
  }

  ASN1Encodable encodeKeyParameter(DumpType dumpType) throws IOException {
    ASN1EncodableVector v = new ASN1EncodableVector();
    v.add(new ASN1Enumerated(Algorithm.getByString(
            parameters.get(Parameter.ALGORITHM.getId())
    ).getId()));
    v.add(parameters.toAsn1Object(dumpType));
    return new DERSequence(v);
  }

  String dumpKeyTypeValueNotation(String prefix, DumpType dumpType) {
    StringBuilder sb = new StringBuilder();
    sb.append(prefix).append("keyType ")
            .append(Algorithm.getByString(parameters.get(Parameter.ALGORITHM.getId())))
            .append(',').append(CRLF);
    sb.append(prefix).append("parameter ").append(parameters.dumpValueNotation(prefix, dumpType));
    return sb.toString();
  }

  public abstract byte[] decrypt(byte[] encrypted) throws IOException;

  public abstract byte[] encrypt(byte[] decrypted) throws IOException;
}
