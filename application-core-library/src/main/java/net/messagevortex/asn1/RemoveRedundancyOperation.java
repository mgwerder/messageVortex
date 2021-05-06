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

import org.bouncycastle.asn1.ASN1Encodable;

import java.io.IOException;
import java.io.Serializable;
import java.util.List;

/**
 * Represents a remove redundancy operation.
 */
public class RemoveRedundancyOperation extends AbstractRedundancyOperation implements Serializable {

  public static final long serialVersionUID = 100000000023L;

  RemoveRedundancyOperation() {
  }

  @Override
  public Operation getNewInstance(ASN1Encodable object) throws IOException {
    return new RemoveRedundancyOperation(object);
  }

  public RemoveRedundancyOperation(int inputId, int dataStripes, int redundancy,
                                   List<SymmetricKey> keys, int outputId, int gfSize) {
    super(inputId, dataStripes, redundancy, keys, outputId, gfSize);
  }

  public RemoveRedundancyOperation(ASN1Encodable to) throws IOException {
    super(to);
  }

}
