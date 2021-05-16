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

import net.messagevortex.asn1.encryption.DumpType;
import org.bouncycastle.asn1.ASN1Encodable;
import org.bouncycastle.asn1.ASN1EncodableVector;
import org.bouncycastle.asn1.ASN1Object;
import org.bouncycastle.asn1.ASN1Sequence;
import org.bouncycastle.asn1.DERSequence;

import java.io.IOException;
import java.io.Serializable;

/**
 * ASN1 parser to request status of current quota.
 */
public class HeaderRequestQueryQuota extends HeaderRequest implements Serializable {

  public static final long serialVersionUID = 100000000025L;

  public HeaderRequestQueryQuota() {
    super();
  }

  /***
   * <p>Creates a request block from the ASN.1 structure.</p>
   *
   * @param ae            the structure to be parsed
   */
  public HeaderRequestQueryQuota(ASN1Encodable ae) {
    this();
    if (ae != null) {
      parse(ae);
    }
  }

  protected final void parse(ASN1Encodable ae) {
    //remove empty sequence
    ASN1Sequence.getInstance(ae);
  }

  protected HeaderRequest getRequest(ASN1Encodable ae) throws IOException {
    return new HeaderRequestQueryQuota(ae);
  }

  public int getId() {
    return 4;
  }

  @Override
  public String dumpValueNotation(String prefix, DumpType dumpType) {
    StringBuilder sb = new StringBuilder();
    sb.append("{}");
    return sb.toString();
  }

  @Override
  ASN1Object intToAsn1Object(DumpType dumpType) throws IOException {
    return new DERSequence(new ASN1EncodableVector());
  }
}
