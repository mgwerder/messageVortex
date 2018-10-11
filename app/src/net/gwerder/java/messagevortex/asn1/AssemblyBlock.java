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

import net.gwerder.java.messagevortex.asn1.encryption.DumpType;
import org.bouncycastle.asn1.*;

import java.io.IOException;
import java.io.Serializable;

/**
 * Contains all classes extending assembly blocks (Payload operations).
 */
public class AssemblyBlock extends AbstractBlock implements Serializable {

  public static final long serialVersionUID = 100000000002L;

  int routingBlockIndex = -1;
  int[] payloadBlockIndex = new int[0];

  public AssemblyBlock(ASN1Encodable object) throws IOException {
    parse(object);
  }

  @Override
  protected void parse(ASN1Encodable to) throws IOException {
    ASN1Sequence s1 = ASN1Sequence.getInstance(to);
    int i = 0;
    routingBlockIndex = ASN1Integer.getInstance(s1.getObjectAt(i++)).getValue().intValue();
    ASN1Sequence s2 = ASN1Sequence.getInstance(s1.getObjectAt(i++));
    int[] l = new int[s1.size()];
    int j = 0;
    for (ASN1Encodable e : s2) {
      l[j++] = ASN1Integer.getInstance(e).getValue().intValue();
    }
    payloadBlockIndex = l;
  }

  @Override
  public String dumpValueNotation(String prefix, DumpType dumptype) throws IOException {
    StringBuilder sb = new StringBuilder();
    sb.append('{').append(CRLF);
    sb.append(prefix).append("  routingBlockIndex ").append(routingBlockIndex).append(CRLF);
    sb.append(prefix).append("  payloadBlockIndex { ");
    int j = 0;
    for (int i : payloadBlockIndex) {
      if (j > 0) {
        sb.append(", ");
      }
      sb.append(i);
      j++;
    }
    sb.append(" }");
    return sb.toString();
  }

  @Override
  public ASN1Object toAsn1Object(DumpType dumpType) throws IOException {
    ASN1EncodableVector v = new ASN1EncodableVector();
    v.add(new ASN1Integer(routingBlockIndex));
    ASN1EncodableVector v2 = new ASN1EncodableVector();
    for (int i : payloadBlockIndex) {
      v2.add(new ASN1Integer(i));
    }
    v.add(new DERSequence(v2));
    return new DERSequence(v);
  }

}
