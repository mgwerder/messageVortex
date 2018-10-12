package net.gwerder.java.messagevortex.asn1;

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

import java.io.IOException;
import java.io.Serializable;
import net.gwerder.java.messagevortex.asn1.encryption.DumpType;
import org.bouncycastle.asn1.ASN1Encodable;
import org.bouncycastle.asn1.ASN1EncodableVector;
import org.bouncycastle.asn1.ASN1Integer;
import org.bouncycastle.asn1.ASN1Object;
import org.bouncycastle.asn1.ASN1Sequence;
import org.bouncycastle.asn1.DERSequence;

/**
 * <p>Represents a merge payload operation.</p>
 *
 * <p>This operation joins two payloads to one new payload with size first.length+second.length.</p>
 */
public class MergePayloadOperation extends Operation implements Serializable {

  public static final long serialVersionUID = 100000000024L;

  int originalFirstId = -1;
  int originalSecondId = -1;
  int newId = -1;

  MergePayloadOperation() {
  }

  /***
   * <p>Create object from ASN.1 code.</p>
   *
   * @param object the ASN.1 code
   * @throws IOException if parsing of ASN.1 code fails
   */
  public MergePayloadOperation(ASN1Encodable object) throws IOException {
    parse(object);
  }

  @Override
  protected void parse(ASN1Encodable to) throws IOException {
    ASN1Sequence s1 = ASN1Sequence.getInstance(to);
    int i = 0;
    originalFirstId = ASN1Integer.getInstance(s1.getObjectAt(i++)).getValue().intValue();
    originalSecondId = ASN1Integer.getInstance(s1.getObjectAt(i++)).getValue().intValue();
    newId = ASN1Integer.getInstance(s1.getObjectAt(i++)).getValue().intValue();
  }

  @Override
  public String dumpValueNotation(String prefix, DumpType dumptype) throws IOException {
    StringBuilder sb = new StringBuilder();
    sb.append('{').append(CRLF);
    sb.append(prefix).append("  originalFirstId ").append(originalFirstId).append(',').append(CRLF);
    sb.append(prefix).append("  originalSecondId ").append(originalSecondId).append(',')
            .append(CRLF);
    sb.append(prefix).append("  newId ").append(newId).append(CRLF);
    sb.append(prefix).append('}');
    return sb.toString();
  }

  @Override
  public ASN1Object toAsn1Object(DumpType dumpType) throws IOException {
    ASN1EncodableVector s1 = new ASN1EncodableVector();
    s1.add(new ASN1Integer(originalFirstId));
    s1.add(new ASN1Integer(originalSecondId));
    s1.add(new ASN1Integer(newId));
    return new DERSequence(s1);
  }

  @Override
  public Operation getNewInstance(ASN1Encodable object) throws IOException {
    return new MergePayloadOperation(object);
  }
}
