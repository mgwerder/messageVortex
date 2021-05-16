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

import net.messagevortex.asn1.encryption.CipherUsage;
import net.messagevortex.asn1.encryption.DumpType;
import org.bouncycastle.asn1.ASN1Encodable;
import org.bouncycastle.asn1.ASN1EncodableVector;
import org.bouncycastle.asn1.ASN1Enumerated;
import org.bouncycastle.asn1.ASN1Object;
import org.bouncycastle.asn1.ASN1Sequence;
import org.bouncycastle.asn1.ASN1TaggedObject;
import org.bouncycastle.asn1.DERSequence;
import org.bouncycastle.asn1.DERTaggedObject;

import java.io.IOException;
import java.io.Serializable;


/**
 * <p>Represents a the Blending specification of the cipher specification including usage.</p>
 */
public class CipherSpec extends AbstractBlock implements Serializable {

  public static final long serialVersionUID = 100000000006L;

  private static final int SYMMETRIC = 16001;
  private static final int ASYMMETRIC = 16002;
  private static final int MAC = 16003;
  private static final int USAGE = 16004;

  /* The endpoint address to be used */
  private AsymmetricAlgorithmSpec asymmetricSpec = null;
  private SymmetricAlgorithmSpec symmetricSpec = null;
  private MacAlgorithmSpec macSpec = null;
  private CipherUsage cipherUsage = CipherUsage.ENCRYPT;

  /***
   * <p>Create object from ASN.1 code.</p>
   *
   * @param to the ASN.1 code
   * @throws IOException if parsing of ASN.1 code fails
   */
  public CipherSpec(ASN1Encodable to) throws IOException {
    parse(to);
  }

  public CipherSpec(CipherUsage cipherUsage) {
    this.cipherUsage = cipherUsage;
  }

  protected final void parse(ASN1Encodable to) throws IOException {
    ASN1Sequence s1 = ASN1Sequence.getInstance(to);
    int i = 0;

    // parse optional fields sequence
    ASN1TaggedObject to1 = ASN1TaggedObject.getInstance(s1.getObjectAt(i++));
    if (to1.getTagNo() == ASYMMETRIC) {
      asymmetricSpec = new AsymmetricAlgorithmSpec(to1.getObject());
      to1 = ASN1TaggedObject.getInstance(s1.getObjectAt(i++));
    }
    if (to1.getTagNo() == SYMMETRIC) {
      symmetricSpec = new SymmetricAlgorithmSpec(to1.getObject());
      to1 = ASN1TaggedObject.getInstance(s1.getObjectAt(i++));
    }
    if (to1.getTagNo() == MAC) {
      macSpec = new MacAlgorithmSpec(to1.getObject());
      to1 = ASN1TaggedObject.getInstance(s1.getObjectAt(i++));
    }
    if (to1.getTagNo() != USAGE) {
      throw new IOException("expected USAGE (" + USAGE + ") but got " + to1.getTagNo()
              + " when parsing CipherSpec");
    }
    cipherUsage = CipherUsage.getById(ASN1Enumerated.getInstance(to1.getObject()).getValue()
            .intValue());

  }

  public AsymmetricAlgorithmSpec getAsymmetricSpec() {
    return asymmetricSpec;
  }

  /***
   * <p>Sets the specification for an asymmetric key.</p>
   *
   * @param spec the specification to be set
   * @return the previously set specification
   */
  public AsymmetricAlgorithmSpec setAsymmetricSpec(AsymmetricAlgorithmSpec spec) {
    AsymmetricAlgorithmSpec ret = this.asymmetricSpec;
    this.asymmetricSpec = spec;
    return ret;
  }

  public SymmetricAlgorithmSpec getSymmetricSpec() {
    return symmetricSpec;
  }

  /***
   * <p>Sets the specification for an symmetric key.</p>
   *
   * @param spec the specification to be set
   * @return the previously set specification
   */
  public SymmetricAlgorithmSpec setSymmetricSpec(SymmetricAlgorithmSpec spec) {
    SymmetricAlgorithmSpec ret = this.symmetricSpec;
    this.symmetricSpec = spec;
    return ret;
  }

  public MacAlgorithmSpec getMacSpec() {
    return macSpec;
  }

  /***
   * <p>Sets the specification for an mac algorithm.</p>
   *
   * @param spec the specification to be set
   * @return the previously set specification
   */
  public MacAlgorithmSpec setMacSpec(MacAlgorithmSpec spec) {
    MacAlgorithmSpec ret = this.macSpec;
    this.macSpec = spec;
    return ret;
  }

  public CipherUsage getCipherUsage() {
    return cipherUsage;
  }

  /***
   * <p>Sets the usage type for the cypher specified in this set.</p>
   *
   * @param usage the usage to be set
   * @return the previously set usage
   */
  public CipherUsage setCipherUsage(CipherUsage usage) {
    CipherUsage ret = cipherUsage;
    this.cipherUsage = usage;
    return ret;
  }

  @Override
  public String dumpValueNotation(String prefix, DumpType dumpType) throws IOException {
    StringBuilder sb = new StringBuilder();
    sb.append('{').append(CRLF);
    if (asymmetricSpec != null) {
      sb.append(prefix).append("  ").append("asymmetric ")
              .append(asymmetricSpec.dumpValueNotation(prefix + "  ", dumpType))
              .append(',').append(CRLF);
    }
    if (symmetricSpec != null) {
      sb.append(prefix).append("  ").append("symmetric ")
              .append(symmetricSpec.dumpValueNotation(prefix + "  ", dumpType))
              .append(',').append(CRLF);
    }
    if (macSpec != null) {
      sb.append(prefix).append("  ").append("mac ")
              .append(macSpec.dumpValueNotation(prefix + "  ", dumpType))
              .append(',').append(CRLF);
    }
    sb.append(prefix).append("  ").append("cipherUsage ")
            .append(cipherUsage.getUsageString()).append(CRLF);
    sb.append(prefix).append('}');
    return sb.toString();
  }

  @Override
  public ASN1Object toAsn1Object(DumpType dumpType) throws IOException {
    if (cipherUsage == null) {
      throw new IOException("CipherSpec is empty .. unable to create CipherSpec");
    }
    ASN1EncodableVector v = new ASN1EncodableVector();
    if (asymmetricSpec != null) {
      v.add(new DERTaggedObject(ASYMMETRIC, asymmetricSpec.toAsn1Object(dumpType)));
    }
    if (symmetricSpec != null) {
      v.add(new DERTaggedObject(SYMMETRIC, symmetricSpec.toAsn1Object(dumpType)));
    }
    if (macSpec != null) {
      v.add(new DERTaggedObject(MAC, macSpec.toAsn1Object(dumpType)));
    }
    v.add(new DERTaggedObject(USAGE, new ASN1Enumerated(cipherUsage.getId())));
    return new DERSequence(v);
  }

  @Override
  public boolean equals(Object t) {
    if (t == null) {
      return false;
    }
    if (t.getClass() != this.getClass()) {
      return false;
    }
    CipherSpec o = (CipherSpec) t;
    try {
      return dumpValueNotation("", DumpType.ALL).equals(o.dumpValueNotation("", DumpType.ALL));
    } catch (IOException ioe) {
      return false;
    }
  }

  @Override
  public int hashCode() {
    // this methode is required for code sanity
    try {
      return dumpValueNotation("", DumpType.ALL).hashCode();
    } catch (IOException ioe) {
      return "FAILED".hashCode();
    }
  }
}
