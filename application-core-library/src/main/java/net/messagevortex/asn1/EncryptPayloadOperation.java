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

import java.io.IOException;
import java.io.Serializable;
import org.bouncycastle.asn1.ASN1Encodable;

public class EncryptPayloadOperation extends AbstractCryptPayloadOperation implements Serializable {

  public static final long serialVersionUID = 100000000029L;

  /***
   * <p>This is an empty constructor for template instanciation.</p>
   */
  EncryptPayloadOperation() {
    // empty constructor for template instanciation
  }

  /***
   * <p>Create a functional encryption operation.</p>
   *
   * @param sourceBlock the block in the workspace to be encrypted
   * @param targetBlock the resulting block in the workspace
   * @param key the key to be applied (null for generating a random key
   *
   * @throws IOException if key generation fails
   */
  public EncryptPayloadOperation(int sourceBlock,int targetBlock, SymmetricKey key)
          throws IOException {
    if (key == null) {
      key = new SymmetricKey();
    }
    setTagNumber(OperationType.ENCRYPT_PAYLOAD.getId());
    this.originalId = sourceBlock;
    this.newId = targetBlock;
    this.key = key;
  }

  /***
   * <p>Create object from ASN.1 code.</p>
   *
   * @param object the ASN.1 code
   * @throws IOException if parsing of ASN.1 code fails
   */
  public EncryptPayloadOperation(ASN1Encodable object) throws IOException {
    super(object);
  }

  @Override
  public Operation getNewInstance(ASN1Encodable object) throws IOException {
    return new EncryptPayloadOperation(object);
  }
}
