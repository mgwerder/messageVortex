package net.messagevortex.asn1;

import org.bouncycastle.asn1.ASN1Encodable;

import java.io.IOException;
import java.io.Serializable;

/**
 * Represents a the Blending specification of the router block.
 */
public abstract class Operation extends AbstractBlock implements Serializable {

  public static final long serialVersionUID = 100000000012L;

  private int tagNumber = -1;

  /* constructor */
  Operation() {
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
