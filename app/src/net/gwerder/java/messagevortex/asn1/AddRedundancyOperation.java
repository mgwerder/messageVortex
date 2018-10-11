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
import java.util.List;

/**
 * Represents a addRedundancy operation on a routing block.
 */
public class AddRedundancyOperation extends AbstractRedundancyOperation implements Serializable {

  public static final long serialVersionUID = 100000000032L;

  AddRedundancyOperation() {
  }

  public AddRedundancyOperation(int inputId, int dataStripes, int redundancy, List<SymmetricKey> keys, int outputId, int gfSize) {
    super(inputId, dataStripes, redundancy, keys, outputId, gfSize);
  }

  public AddRedundancyOperation(ASN1Encodable to) throws IOException {
    super(to);
  }

  public static AddRedundancyOperation getInstance(Object obj) throws IOException {
    if (obj == null || obj instanceof AddRedundancyOperation) {
      return (AddRedundancyOperation) obj;
    } else if (obj instanceof ASN1TaggedObject) {
      ASN1TaggedObject to = ASN1TaggedObject.getInstance(obj);
      return new AddRedundancyOperation(to.getObject());
    }

    throw new IllegalArgumentException("unknown object in getInstance");
  }

  @Override
  public ASN1Object toAsn1Object(DumpType dumpType) throws IOException {
    return new DERTaggedObject(true, ADD_REDUNDANCY, super.toAsn1Object(dumpType));
  }

  public ASN1Primitive toASN1Primitive() throws IOException {
    return toAsn1Object(DumpType.PUBLIC_ONLY).toASN1Primitive();
  }

  public Operation getNewInstance(ASN1Encodable object) throws IOException {
    return new AddRedundancyOperation(object);
  }
}
