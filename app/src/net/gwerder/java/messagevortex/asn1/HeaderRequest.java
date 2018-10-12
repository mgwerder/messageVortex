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
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

import net.gwerder.java.messagevortex.MessageVortexLogger;
import net.gwerder.java.messagevortex.asn1.encryption.DumpType;
import org.bouncycastle.asn1.ASN1Encodable;
import org.bouncycastle.asn1.ASN1Object;
import org.bouncycastle.asn1.ASN1TaggedObject;
import org.bouncycastle.asn1.DERTaggedObject;

/**
 * <p>ASN1 parser class for header request.</p>
 */
public abstract class HeaderRequest extends AbstractBlock implements Serializable, Dumpable {

  public static final long serialVersionUID = 100000000007L;

  private enum Type {
    IDENTITY(0, HeaderRequestIdentity.class),
    CAPABILITIES(1, HeaderRequestCapability.class),
    MESSAGE_QUOTA(2, HeaderRequestIncreaseMessageQuota.class),
    TRANSFER_QUOTA(3, HeaderRequestIncreaseTransferQuota.class),
    QUOTA_QUERY(4, HeaderRequestQueryQuota.class);

    int id;
    Class templateClass;

    Type(int id, Class templateClass) {
      this.id = id;
      this.templateClass = templateClass;
    }

    public int getId() {
      return this.id;
    }

    public Class getTemplateClass() {
      return this.templateClass;
    }

    public static Type getByClass(Class c) {
      for (Type e : values()) {
        if (e.getTemplateClass() == c) {
          return e;
        }
      }
      return null;
    }
  }

  private static final java.util.logging.Logger LOGGER;

  static {
    LOGGER = MessageVortexLogger.getLogger((new Throwable()).getStackTrace()[0].getClassName());
  }

  private static final List<HeaderRequest> req = new ArrayList<>();

  static {
    try {
      req.add(new HeaderRequestIdentity());
      req.add(new HeaderRequestQueryQuota());
    } catch (Exception e) {
      LOGGER.log(Level.SEVERE, "Exception when adding Requests in static constructor", e);
    }
  }

  protected HeaderRequest() {
  }

  /***
   * <p>Conversion helper for header request.</p>
   *
   * @param ae            asn.1 representation of the class
   * @return              the respective header object if parseable or null
   * @throws IOException  if parsing fails
   */
  public static HeaderRequest getInstance(ASN1Encodable ae) throws IOException {
    for (HeaderRequest hr : req) {
      if (Type.getByClass(hr.getClass()).getId() == ((ASN1TaggedObject) (ae)).getTagNo()) {
        return hr.getRequest(ae);
      }
    }
    return null;
  }

  public ASN1Object toAsn1Object(DumpType dt) throws IOException {
    Type tag = Type.getByClass(this.getClass());
    if (tag == null) {
      throw new IOException("Unknown Header Request type \"" + this.getClass().getCanonicalName()
              + "\"");
    }
    return new DERTaggedObject(tag.getId(), intToAsn1Object(dt));
  }

  abstract ASN1Object intToAsn1Object(DumpType dt) throws IOException;

  protected abstract HeaderRequest getRequest(ASN1Encodable ae) throws IOException;
}
