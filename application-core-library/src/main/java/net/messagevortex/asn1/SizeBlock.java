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
import org.bouncycastle.asn1.ASN1TaggedObject;
import org.bouncycastle.asn1.DERSequence;
import org.bouncycastle.asn1.DERTaggedObject;

import java.io.IOException;
import java.io.Serializable;

/**
 * Created by Martin on 04.06.2017.
 */
public class SizeBlock extends AbstractBlock implements Serializable {

  public static final long serialVersionUID = 100000000015L;

  private enum SizeType {
    PERCENT(15001),
    ABSOLUTE(15101);

    int id;

    SizeType(int id) {
      this.id = id;
    }

    public int getId() {
      return this.id;
    }

    public static SizeType getById(int id) {
      for (SizeType e : values()) {
        if (e.id == id) {
          return e;
        }
      }
      return null;
    }
  }

  SizeType type;
  int from;
  int to;

  public SizeBlock(ASN1Encodable o) throws IOException {
    parse(o);
  }

  @Override
  protected final void parse(ASN1Encodable to) throws IOException {
    ASN1Sequence s1 = ASN1Sequence.getInstance(to);
    ASN1TaggedObject tag = ASN1TaggedObject.getInstance(s1.getObjectAt(0));
    type = SizeType.getById(tag.getTagNo());
    if (type == null) {
      throw new IOException("Unknown type in SizeType " + tag.getTagNo());
    }

    ASN1Sequence s2 = ASN1Sequence.getInstance(tag.getObject());
    int i2 = 0;

    this.from = ASN1Integer.getInstance(s2.getObjectAt(i2++)).getValue().intValue();
    this.to = ASN1Integer.getInstance(s2.getObjectAt(i2++)).getValue().intValue();
  }

  @Override
  public String dumpValueNotation(String prefix, DumpType dumptype) {
    StringBuilder sb = new StringBuilder();
    sb.append(type.name().toLowerCase()).append(" {").append(CRLF);
    String s1 = "fromPercent";
    String s2 = "toPercent";
    if (type == SizeType.ABSOLUTE) {
      s1 = "fromAbsolute";
      s2 = "toAbsolute";
    }
    sb.append(prefix).append("  ").append(s1).append(from).append(',').append(CRLF);
    sb.append(prefix).append("  ").append(s2).append(to).append(CRLF);
    sb.append(prefix).append('}');
    return sb.toString();
  }

  @Override
  public ASN1Object toAsn1Object(DumpType dumpType) {
    ASN1EncodableVector v = new ASN1EncodableVector();

    // encode target sequence
    ASN1EncodableVector v2 = new ASN1EncodableVector();
    v2.add(new ASN1Integer(from));
    v2.add(new ASN1Integer(to));
    v.add(new DERTaggedObject(type.getId(), new DERSequence(v2)));

    return new DERSequence(v);
  }
}
