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
 * ASN1 parser for identity request.
 */
public class HeaderRequestIdentity extends HeaderRequest implements Serializable {

  public static final long serialVersionUID = 100000000027L;

  protected UsagePeriod period = null;

  public HeaderRequestIdentity() {
    super();
  }

  /***
   * <p>Creates a class from the given ASN.1 object.</p>
   *
   * @param ae            the ASN.1 object to be translated
   * @throws IOException  if parsing of the ASN.1 fails
   */
  public HeaderRequestIdentity(ASN1Encodable ae) throws IOException {
    this();
    if (ae != null) {
      parse(ae);
    }
  }

  protected final void parse(ASN1Encodable ae) throws IOException {
    ASN1Sequence s1 = ASN1Sequence.getInstance(ae);
    period = new UsagePeriod(s1.getObjectAt(0));
  }

  protected HeaderRequest getRequest(ASN1Encodable ae) throws IOException {
    return new HeaderRequestIdentity(ae);
  }

  public int getId() {
    return 0;
  }

  public UsagePeriod getUsagePeriod() {
    return period;
  }

  /***
   * <p>Sets the usage period of the identity to the new value.</p>
   * @param newPeriod the new usage period
   * @return          the previously set usage period
   */
  public UsagePeriod setUsagePeriod(UsagePeriod newPeriod) {
    UsagePeriod old = period;
    this.period = newPeriod;
    return old;
  }

  @Override
  public String dumpValueNotation(String prefix, DumpType dumpType) {
    StringBuilder sb = new StringBuilder();
    sb.append('{').append(CRLF);
    if (period != null) {
      sb.append(prefix).append("  period ")
              .append(period.dumpValueNotation(prefix + "  ", dumpType)).append(CRLF);
    }
    sb.append(prefix).append('}');
    return sb.toString();
  }

  @Override
  public ASN1Object intToAsn1Object(DumpType dumpType) {
    ASN1EncodableVector s1 = new ASN1EncodableVector();
    s1.add(period.toAsn1Object(dumpType));
    return new DERSequence(s1);
  }
}
