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
import java.util.HashMap;
import java.util.Map;
import org.bouncycastle.asn1.ASN1Encodable;
import org.bouncycastle.asn1.ASN1TaggedObject;

/**
 * Represents a the Blending specification of the router block.
 */
public abstract class Operation extends AbstractBlock implements Serializable {

  public static final long serialVersionUID = 100000000012L;

  public static final int SPLIT_PAYLOAD = 150;
  public static final int MERGE_PAYLOAD = 160;
  public static final int ENCRYPT_PAYLOAD = 300;
  public static final int DECRYPT_PAYLOAD = 310;
  public static final int ADD_REDUNDANCY = 400;
  public static final int REMOVE_REDUNDANCY = 410;

  private static final Map<Integer, Operation> operations = new HashMap<>();
  private static boolean initInProgress = false;

  /* constructor */
  Operation() {
    init();
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
    synchronized (operations) {
      if (operations.isEmpty()) {
        init();
      }
      int tag = ASN1TaggedObject.getInstance(object).getTagNo();
      if (operations.get(tag) == null) {
        throw new IOException("unknown tag for choice detected");
      }
      return operations.get(tag).getNewInstance(ASN1TaggedObject.getInstance(object).getObject());
    }
  }

  /***
   * <p>Registers all standard operations.</p>
   */
  private static void init() {
    synchronized (operations) {
      if (operations.isEmpty() && !initInProgress) {
        initInProgress = true;
        operations.put(SPLIT_PAYLOAD, new SplitPayloadOperation());
        operations.put(MERGE_PAYLOAD, new MergePayloadOperation());
        operations.put(ENCRYPT_PAYLOAD, new EncryptPayloadOperation());
        operations.put(DECRYPT_PAYLOAD, new DecryptPayloadOperation());
        operations.put(ADD_REDUNDANCY, new AddRedundancyOperation());
        operations.put(REMOVE_REDUNDANCY, new RemoveRedundancyOperation());
        initInProgress = false;
      }
    }
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

}
