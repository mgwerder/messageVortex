package net.messagevortex.asn1;

import org.bouncycastle.asn1.ASN1Encodable;
import org.bouncycastle.asn1.ASN1TaggedObject;

import java.io.IOException;

public class OperationFactory {

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
    OperationType opType = OperationType.getById(tag);
    if (opType == null || OperationType.getById(tag).getFactory() == null) {
      throw new IOException("unknown tag for choice detected");
    }
    return opType.getFactory()
        .getNewInstance(ASN1TaggedObject.getInstance(object).getBaseObject());
  }

}
