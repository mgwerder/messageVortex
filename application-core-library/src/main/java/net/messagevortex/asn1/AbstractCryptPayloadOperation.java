package net.messagevortex.asn1;

import net.messagevortex.asn1.encryption.DumpType;
import org.bouncycastle.asn1.ASN1Encodable;
import org.bouncycastle.asn1.ASN1EncodableVector;
import org.bouncycastle.asn1.ASN1Integer;
import org.bouncycastle.asn1.ASN1Object;
import org.bouncycastle.asn1.ASN1Sequence;
import org.bouncycastle.asn1.DERSequence;
import org.bouncycastle.asn1.DERTaggedObject;

import java.io.IOException;
import java.io.Serializable;

public abstract class AbstractCryptPayloadOperation extends Operation
                implements Serializable, Dumpable {

  public static final long serialVersionUID = 100000000033L;

  int originalId = -1;
  SymmetricKey key = null;
  int newId = -1;

  AbstractCryptPayloadOperation() {}

  /***
   * <p>Create object from ASN.1 code.</p>
   *
   * @param object the ASN.1 code
   * @throws IOException if parsing of ASN.1 code fails
   */
  public AbstractCryptPayloadOperation(ASN1Encodable object) throws IOException {
    parse(object);
  }

  @Override
  protected final void parse(ASN1Encodable to) throws IOException {
    ASN1Sequence s1 = ASN1Sequence.getInstance(to);
    int i = 0;
    originalId = ASN1Integer.getInstance(s1.getObjectAt(i++)).getValue().intValue();
    key = new SymmetricKey(s1.getObjectAt(i++).toASN1Primitive().getEncoded());
    newId = ASN1Integer.getInstance(s1.getObjectAt(i)).getValue().intValue();
  }

  @Override
  public String dumpValueNotation(String prefix, DumpType dumptype) {
    StringBuilder sb = new StringBuilder();
    sb.append('[').append(getTagNumber()).append("] {").append(CRLF);
    sb.append(prefix).append("  originalId ").append(originalId).append(',').append(CRLF);
    sb.append(prefix).append("  key ").append(key.dumpValueNotation(prefix + "  ", dumptype))
                     .append(',').append(CRLF);
    sb.append(prefix).append("  newId ").append(newId).append(CRLF);
    sb.append(prefix).append('}');
    return sb.toString();
  }

  @Override
  public ASN1Object toAsn1Object(DumpType dumpType) throws IOException {
    ASN1EncodableVector v = new ASN1EncodableVector();
    v.add(new ASN1Integer(originalId));
    v.add(key.toAsn1Object(dumpType));
    v.add(new ASN1Integer(newId));
    return new DERTaggedObject(getTagNumber(), new DERSequence(v));
  }

  public abstract Operation getNewInstance(ASN1Encodable object) throws IOException;
}
