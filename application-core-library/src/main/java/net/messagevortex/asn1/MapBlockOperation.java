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
 * Contains all classes extending assembly blocks (Payload operations).
 */
public class MapBlockOperation extends Operation implements Serializable {

  public static final long serialVersionUID = 100000000002L;

  int originalId = -1;
  int newId = -1;

  MapBlockOperation() {
  }

  /***
   * <p>Create object from ASN.1 code.</p>
   *
   * @param object the ASN.1 code
   */
  public MapBlockOperation(ASN1Encodable object) {
    parse(object);
  }

  @Override
  protected final void parse(ASN1Encodable to)  {
    ASN1Sequence s1 = ASN1Sequence.getInstance(to);
    int i = 0;
    originalId = ASN1Integer.getInstance(s1.getObjectAt(i++)).getValue().intValue();
    newId = ASN1Integer.getInstance(s1.getObjectAt(i++)).getValue().intValue();
  }

  @Override
  public String dumpValueNotation(String prefix, DumpType dumptype) {
    StringBuilder sb = new StringBuilder();
    sb.append('{').append(CRLF);
    sb.append(prefix).append("  originalId ").append(originalId).append(',').append(CRLF);
    sb.append(prefix).append("  newId ").append(newId).append(CRLF);
    sb.append(prefix).append('}').append(CRLF);
    return sb.toString();
  }

  @Override
  public ASN1Object toAsn1Object(DumpType dumpType) {
    ASN1EncodableVector v = new ASN1EncodableVector();
    v.add(new ASN1Integer(originalId));
    v.add(new ASN1Integer(newId));
    return new DERSequence(v);
  }

  @Override
  public Operation getNewInstance(ASN1Encodable object)  {
    return new MapBlockOperation(object);
  }
}
