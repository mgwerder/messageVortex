package net.messagevortex.asn1;

import org.bouncycastle.asn1.ASN1Encodable;

import java.io.IOException;
import java.io.Serializable;

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
