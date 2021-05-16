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
import org.bouncycastle.asn1.ASN1Integer;
import org.bouncycastle.asn1.ASN1Object;
import org.bouncycastle.asn1.ASN1Sequence;
import org.bouncycastle.asn1.DERSequence;

import java.io.Serializable;

/**
 * <p>ASN1 parser for increasing message quota.</p>
 */
public class HeaderRequestIncreaseTransferQuota extends HeaderRequest implements Serializable {

  public static final long serialVersionUID = 100000000026L;

  private long quota = -1;

  public HeaderRequestIncreaseTransferQuota() {
    super();
  }

  /***
   * <p>Creates a request block from the ASN.1 structure.</p>
   *
   * @param ae            the structure to be parsed
   */
  public HeaderRequestIncreaseTransferQuota(ASN1Encodable ae) {
    this();
    if (ae != null) {
      parse(ae);
    }
  }

  protected final void parse(ASN1Encodable ae) {
    ASN1Sequence s1 = ASN1Sequence.getInstance(ae);
    quota = ASN1Integer.getInstance(s1.getObjectAt(0)).getValue().intValue();
  }

  protected HeaderRequest getRequest(ASN1Encodable ae) {
    return new HeaderRequestIncreaseTransferQuota(ae);
  }

  public long getQuota() {
    return quota;
  }

  /***
   * <p>Sets the quota of the request.</p>
   *
   * @param newQuota the new quota to be set
   * @return         the previously set quota
   */
  public long setQuota(long newQuota) {
    long old = quota;
    quota = newQuota;
    return old;
  }

  public int getId() {
    return 0;
  }

  @Override
  public String dumpValueNotation(String prefix, DumpType dumpType) {
    StringBuilder sb = new StringBuilder();
    sb.append("{" + CRLF);
    if (quota > -1) {
      sb.append(prefix).append("  quota ").append(quota).append(CRLF);
    }
    sb.append(prefix).append('}');
    return sb.toString();
  }

  @Override
  ASN1Object intToAsn1Object(DumpType dumpType) {
    ASN1EncodableVector s1 = new ASN1EncodableVector();
    s1.add(new ASN1Integer(quota));
    return new DERSequence(s1);
  }
}
