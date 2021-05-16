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

import net.messagevortex.asn1.encryption.DumpType;
import org.bouncycastle.asn1.ASN1Encodable;
import org.bouncycastle.asn1.ASN1EncodableVector;
import org.bouncycastle.asn1.ASN1Object;
import org.bouncycastle.asn1.ASN1Sequence;
import org.bouncycastle.asn1.DERSequence;
import org.bouncycastle.asn1.DERUTF8String;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * <p>Represents a the Blending specification of the router block.</p>
 */
public class BlendingSpec extends AbstractBlock implements Serializable, Dumpable {

  public static final long serialVersionUID = 100000000005L;

  /* The endpoint address to be used */
  private String recipientAddress = null;
  private String media = "smtp:";
  private String blendingType = "attach";
  private BlendingParameter[] blendingParameter = new BlendingParameter[0];

  /***
   * <p>Create object from ASN.1 code.</p>
   *
   * @param to the ASN.1 code
   * @throws IOException if parsing of ASN.1 code fails
   */
  public BlendingSpec(ASN1Encodable to) throws IOException {
    parse(to);
  }

  public BlendingSpec(String blendingEndpointAddress) {
    this.recipientAddress = blendingEndpointAddress;
  }

  protected final void parse(ASN1Encodable to) throws IOException {
    ASN1Sequence s1 = ASN1Sequence.getInstance(to);
    int i = 0;

    // parse target sequence
    ASN1Sequence s2 = ASN1Sequence.getInstance(s1.getObjectAt(i++));
    int i2 = 0;
    // get media
    media = DERUTF8String.getInstance(s2.getObjectAt(i2++)).getString();
    // get recipient address
    recipientAddress = DERUTF8String.getInstance(s2.getObjectAt(i2++)).getString();

    //get blender type
    blendingType = DERUTF8String.getInstance(s1.getObjectAt(i++)).getString();

    // get Blending Parameter
    s2 = ASN1Sequence.getInstance(s1.getObjectAt(i++));
    List<BlendingParameter> al = new ArrayList<>(s2.size());
    for (ASN1Encodable e : s2) {
      al.add(new BlendingParameter(e));
    }
    blendingParameter = al.toArray(new BlendingParameter[al.size()]);
  }

  @Override
  public ASN1Object toAsn1Object(DumpType dumpType) throws IOException {
    ASN1EncodableVector v = new ASN1EncodableVector();

    // encode target sequence
    ASN1EncodableVector v2 = new ASN1EncodableVector();
    v2.add(new DERUTF8String(media));
    v2.add(new DERUTF8String(recipientAddress));
    v.add(new DERSequence(v2));

    // encode blender type
    v.add(new DERUTF8String(blendingType));

    // encode BlendingParameter
    v2 = new ASN1EncodableVector();
    if (blendingParameter != null && blendingParameter.length > 0) {
      for (BlendingParameter p : blendingParameter) {
        v2.add(p.toAsn1Object(dumpType));
      }
    }
    v.add(new DERSequence(v2));

    return new DERSequence(v);
  }

  @Override
  public String dumpValueNotation(String prefix, DumpType dumpType) throws IOException {
    StringBuilder sb = new StringBuilder();
    sb.append(" {").append(CRLF);
    sb.append(prefix).append("  target '").append(media).append(recipientAddress).append("',")
                     .append(CRLF);
    sb.append(prefix).append("  blendingType '").append(blendingType).append("',").append(CRLF);
    sb.append(prefix).append("  blendingParameter {");
    if (blendingParameter != null && blendingParameter.length > 0) {
      int i = 0;
      for (BlendingParameter p : blendingParameter) {
        if (i > 0) {
          sb.append(',');
        }
        sb.append(CRLF);
        sb.append(prefix).append(p.dumpValueNotation("", dumpType));
        i++;
      }
      sb.append(CRLF);
    }
    sb.append(prefix).append("  }").append(CRLF);
    sb.append(prefix).append('}');
    return sb.toString();
  }

  public String getRecipientAddress() {
    return recipientAddress;
  }

  /***
   * <p>sets the receiver address of the blender spec.</p>
   *
   * @param recipientAddress the encoded recipient address
   * @return the previously set recipient address
   */
  public String setRecipientAddress(String recipientAddress) {
    String old = this.recipientAddress;
    this.recipientAddress = recipientAddress;
    return old;
  }

  public String getMedia() {
    return media;
  }

  /***
   * <p>Sets the media type of the blender spec.</p>
   *
   * @param media the named media to set
   * @return the previously set media
   */
  public String setMedia(String media) {
    String old = this.media;
    this.media = media;
    return old;
  }

  public String getBlendingType() {
    return blendingType;
  }

  /***
   * <p>Sets the media type of the blender spec.</p>
   *
   * @param blendingType the blender type as string
   * @return the previously set blender type
   */
  public String setBlendingType(String blendingType) {
    String old = this.blendingType;
    this.blendingType = blendingType;
    return old;
  }

}
