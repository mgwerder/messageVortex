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
import org.bouncycastle.asn1.ASN1TaggedObject;

/**
 * Represents a the Blending specification of the router block.
 */
public abstract class Operation extends AbstractBlock implements Serializable {

  public static final long serialVersionUID = 100000000012L;

  enum Operations {
    SPLIT_PAYLOAD(150, new SplitPayloadOperation()),
    MERGE_PAYLOAD(160, new MergePayloadOperation()),
    ENCRYPT_PAYLOAD(300, new EncryptPayloadOperation()),
    DECRYPT_PAYLOAD(310, new DecryptPayloadOperation()),
    ADD_REDUNDANCY(400, new AddRedundancyOperation()),
    REMOVE_REDUNDANCY(410, new RemoveRedundancyOperation()),
    MAP(1001, new MapBlockOperation());

    int id;
    Operation operation;

    Operations(int id, Operation operation) {
      this.id = id;
      this.operation = operation;
    }

    int getId() {
      return id;
    }

    Operation getFactory() {
      return operation;
    }

    /***
     * <p>Look up an algorithm by id.</p>
     *
     * @param id     the idto be looked up
     * @return the algorithm or null if not known
     */
    public static Operations getById(int id) {
      for (Operations e : values()) {
        if (e.id == id) {
          return e;
        }
      }
      return null;
    }
  }

  private int tagNumber = -1;

  /* constructor */
  Operation() {
  }

  /***
   * <p>Gets the respective Operation by tag number.</p>
   *
   * @param object the object to be parsed
   * @return the operation object
   *
   * @throws IOException if no operations have been registered or an unknown tag number is detected
   */
  public static Operation getInstance(ASN1Encodable object) throws IOException {
    int tag = ASN1TaggedObject.getInstance(object).getTagNo();
    if (Operations.getById(tag) == null || Operations.getById(tag).getFactory() == null) {
      throw new IOException("unknown tag for choice detected");
    }
    return Operations.getById(tag).getFactory()
        .getNewInstance(ASN1TaggedObject.getInstance(object).getObject());
  }

  /***
   * <p>Gets an instance of the object.</p>
   *
   * @param asn1Encodable the object to be parsed
   * @return the parsed operation object
   *
   * @throws IOException if parsing fails
   */
  public abstract Operation getNewInstance(ASN1Encodable asn1Encodable) throws IOException;

  /***
   * <p>sets the ag number to be set when encoding the operation.</p>
   *
   * @param newTagNumber the new tag number to be set
   */
  protected void setTagNumber(int newTagNumber) {
    tagNumber = newTagNumber;
  }

  protected int getTagNumber() {
    return tagNumber;
  }

}
