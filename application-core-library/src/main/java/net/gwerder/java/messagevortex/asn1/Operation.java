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
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.bouncycastle.asn1.ASN1Encodable;
import org.bouncycastle.asn1.ASN1TaggedObject;

/**
 * Represents a the Blending specification of the routing block.
 */
public abstract class Operation extends AbstractBlock implements Serializable {

  public static final long serialVersionUID = 100000000012L;

  public static final int SPLIT_PAYLOAD = 150;
  public static final int MERGE_PAYLOAD = 160;
  public static final int XOR_SPLIT_PAYLOAD = 250;
  public static final int XOR_MERGE_PAYLOAD = 260;
  public static final int ENCRYPT_PAYLOAD = 300;
  public static final int DECRYPT_PAYLOAD = 310;
  public static final int ADD_REDUNDANCY = 400;
  public static final int REMOVE_REDUNDANCY = 410;

  private static final Map<Integer, Operation> operations = new ConcurrentHashMap<>();
  private static boolean initInProgress = false;

  /* constructor */
  Operation() {
    init();
  }

  public static Operation getInstance(ASN1Encodable object) throws IOException {
    if (operations.isEmpty()) {
      throw new IOException("init() not called");
    }
    int tag = ASN1TaggedObject.getInstance(object).getTagNo();
    if (operations.get(tag) == null) {
      throw new IOException("unknown tag for choice detected");
    }
    return operations.get(tag).getNewInstance(ASN1TaggedObject.getInstance(object).getObject());
  }

  public static void init() {
    synchronized (operations) {
      if (operations.isEmpty() && !initInProgress) {
        initInProgress = true;
        operations.put(SPLIT_PAYLOAD, new SplitPayloadOperation());
        operations.put(MERGE_PAYLOAD, new MergePayloadOperation());
        operations.put(XOR_SPLIT_PAYLOAD, new XorSplitPayloadOperation());
        operations.put(XOR_MERGE_PAYLOAD, new XorMergePayloadOperation());
        operations.put(ENCRYPT_PAYLOAD, new EncryptPayloadOperation());
        operations.put(DECRYPT_PAYLOAD, new DecryptPayloadOperation());
        operations.put(ADD_REDUNDANCY, new AddRedundancyOperation());
        operations.put(REMOVE_REDUNDANCY, new RemoveRedundancyOperation());
        initInProgress = false;
      }
    }
  }

  public static Operation parseInstance(ASN1TaggedObject object) throws IOException {
    if (operations.isEmpty()) {
      throw new IOException("init() not called");
    }
    if (operations.get(object.getTagNo()) == null) {
      throw new IOException("got unknown tag number for operation (" + object.getTagNo());
    }
    return operations.get(object.getTagNo()).getInstance(object.getObject());
  }

  public abstract Operation getNewInstance(ASN1Encodable object) throws IOException;

}
