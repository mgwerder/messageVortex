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

import net.messagevortex.ExtendedSecureRandom;
import net.messagevortex.MessageVortexLogger;
import net.messagevortex.asn1.encryption.Algorithm;
import net.messagevortex.asn1.encryption.DumpType;
import net.messagevortex.asn1.encryption.SecurityLevel;
import org.bouncycastle.asn1.*;
import org.bouncycastle.util.encoders.Base64;

import java.io.IOException;
import java.io.Serializable;
import java.util.logging.Level;

/**
 * This class represents one block of an identity store for storage.
 */
public class IdentityStoreBlock extends AbstractBlock implements Serializable {

  public static final long serialVersionUID = 10000000024L;

  public enum IdentityType {
    OWNED_IDENTITY,
    NODE_IDENTITY,
    RECIPIENT_IDENTITY
  }

  public static final String UNENCODABLE = "<UNENCODABLE>";

  private static final java.util.logging.Logger LOGGER;

  static {
    LOGGER = MessageVortexLogger.getLogger((new Throwable()).getStackTrace()[0].getClassName());
    //MessageVortexLogger.setGlobalLogLevel(Level.ALL);
  }

  private UsagePeriod valid = null;
  private int messageQuota = 0;
  private int transferQuota = 0;
  private AsymmetricKey identityKey = null;
  private String nodeAddress = null;
  private AsymmetricKey nodeKey = null;
  private IdentityType idType = null;

  public IdentityStoreBlock() {
    super();
  }

  /***
   * <p>Create object from ASN.1 code.</p>
   *
   * @param ae the ASN.1 code
   * @throws IOException if parsing of ASN.1 code fails
   */
  public IdentityStoreBlock(ASN1Encodable ae) throws IOException {
    parse(ae);
  }

  /***
   * <p>Create an identity store block from an url.</p>
   *
   * @param url th url to be parsed
   *
   * @throws IOException if parsing of the url fails
   */
  public IdentityStoreBlock(String url) throws IOException {
    if (url != null && url.toLowerCase().startsWith("vortexsmtp://")) {
      LOGGER.log(Level.INFO, "Creating identity from URL " + url);
      String[] s = url.substring(13).split("\\.\\.");
      String[] s2 = s[2].split("@");
      identityKey = new AsymmetricKey(Base64.decode(s[1]));
      nodeAddress = "smtp:" + s[0] + "@" + s2[0];
      valid = new UsagePeriod();
    } else {
      throw new IOException("unable to parse " + url);
    }
  }

  @Override
  public int hashCode() {
    try {
      return dumpValueNotation("", DumpType.ALL_UNENCRYPTED).hashCode();
    } catch (IOException e) {
      return "".hashCode();
    }
  }

  /***
   * <p>Get a new, random IdentityStoreBlock of the specified type.</p>
   *
   * @param it identity type to be generated
   * @param id the id of the generated block
   * @param complete provide only the specified subset or a complete entry with private keys
   * @return the requessted IdentityStoreBlock
   * @throws IOException if generation fails
   */
  public static IdentityStoreBlock getIdentityStoreBlockDemo(IdentityType it, String id,
                                                             boolean complete)
          throws IOException {
    IdentityStoreBlock ret = new IdentityStoreBlock();
    ret.setValid(new UsagePeriod(3600 * 24 * 30));
    ret.setTransferQuota(ExtendedSecureRandom.nextInt(1024 * 1024 * 1024));
    ret.setMessageQuota(ExtendedSecureRandom.nextInt(1024 * 1024));
    ret.idType = it;
    byte[] b;
    if (id == null) {
      b = new byte[ExtendedSecureRandom.nextInt(20) + 3];
      ExtendedSecureRandom.nextBytes(b);
      id = toHex(b) + "@localhost";
    }
    switch (it) {
      case OWNED_IDENTITY:
        // my own identity to decrypt everything
        try {
          ret.setIdentityKey(new AsymmetricKey(Algorithm.RSA.getParameters(SecurityLevel.LOW)));
          ret.setNodeAddress("smtp:" + id);
          ret.setNodeKey(null);
        } catch (Exception e) {
          throw new IOException("Exception while generating owned identity", e);
        }
        break;
      case NODE_IDENTITY:
        // My identities I have on remote nodes
        try {
          ret.setIdentityKey(null);
          ret.setNodeAddress("smtp:" + id);
          AsymmetricKey ak = new AsymmetricKey();
          if (!complete) {
            ak.setPrivateKey(null);
          }
          ret.setNodeKey(ak);
        } catch (Exception e) {
          throw new IOException("Exception while generating node identity", e);
        }
        break;
      case RECIPIENT_IDENTITY:
        // Identities for receiving mails
        try {
          AsymmetricKey ak = new AsymmetricKey(Algorithm.EC.getParameters(SecurityLevel.LOW));
          if (!complete) {
            ak.setPrivateKey(null);
          }
          ret.setIdentityKey(ak);
          ret.setNodeAddress("smtp:" + id);
          ak = new AsymmetricKey();
          if (!complete) {
            ak.setPrivateKey(null);
          }
          ret.setNodeKey(ak);
        } catch (Exception e) {
          throw new IOException("Exception while generating recipient identity", e);
        }
        break;
      default:
        // Unknown type just ignore it
        return null;
    }
    return ret;
  }

  /***
   * <p>Set a new identity key.</p>
   *
   * @param k the new identity key
   * @return the previously set identity key
   */
  public AsymmetricKey setIdentityKey(AsymmetricKey k) {
    AsymmetricKey old = identityKey;
    identityKey = k;
    return old;
  }

  /***
   * <p>get the currently set identity key.</p>
   *
   * @return the identity key currently set
   */
  public AsymmetricKey getIdentityKey() {
    return identityKey;
  }

  /***
   * <p>The usage period of the identity block.</p>
   *
   * @param np the validity period to be set
   * @return the previously set validity period
   */
  public UsagePeriod setValid(UsagePeriod np) {
    UsagePeriod old = valid;
    valid = np;
    return old;
  }

  /***
   * <p>Gets the currently set validity period.</p>
   *
   * @return the currently set validity period
   */
  public UsagePeriod getValid() {
    return valid;
  }

  /***
   * <p>Setting the limit for number of incoming messages.</p>
   *
   * @param nq the new quota
   * @return the previously set quota
   */
  public int setMessageQuota(int nq) {
    int old = messageQuota;
    messageQuota = nq;
    return old;
  }

  /***
   * <p>Getting the limit for number of incoming messages.</p>
   *
   * @return the currently set limit
   */
  public int getMessageQuota() {
    return messageQuota;
  }


  /***
   * <p>Setting the limmit for the outgoing transfer quotas in bytes.</p>
   *
   * @param tq the new transfer quotas in bytes
   * @return the previously set quota
   */
  public int setTransferQuota(int tq) {
    int old = transferQuota;
    transferQuota = tq;
    return old;
  }

  /***
   * <p>Getting the transfer quota in bytes for outgoing messages.</p>
   *
   * @return the currently set transfer quota
   */
  public int getTransferQuota() {
    return transferQuota;
  }

  /***
   * <p>Setting the node address.</p>
   *
   * @param na the new node address
   * @return the previously set node address
   */
  public String setNodeAddress(String na) {
    String old = nodeAddress;
    nodeAddress = na;
    return old;
  }

  /***
   * <p>Getting the currently set node address.</p>
   *
   * @return the currently set node address
   */
  public String getNodeAddress() {
    return nodeAddress;
  }

  /***
   * <p>Setting the current node key.</p>
   *
   * @param k the new node key
   * @return the previously set node key
   */
  public AsymmetricKey setNodeKey(AsymmetricKey k) {
    AsymmetricKey old = nodeKey;
    nodeKey = k;
    return old;
  }

  /***
   * <p>Getting the currently set node key.</p>
   *
   * @return the currently set node key
   */
  public AsymmetricKey getNodeKey() {
    return nodeKey;
  }

  protected final void parse(ASN1Encodable p) throws IOException {
    LOGGER.log(Level.FINER, "Executing parse()");
    ASN1Sequence s1 = ASN1Sequence.getInstance(p);
    int i = 0;
    valid = new UsagePeriod(s1.getObjectAt(i++));
    messageQuota = ASN1Integer.getInstance(s1.getObjectAt(i++)).getValue().intValue();
    transferQuota = ASN1Integer.getInstance(s1.getObjectAt(i++)).getValue().intValue();
    LOGGER.log(Level.FINER, "Finished parse()");
    for (; i < s1.size(); i++) {
      ASN1TaggedObject to = ASN1TaggedObject.getInstance(s1.getObjectAt(i));
      switch (to.getTagNo()) {
        case 1001:
          identityKey = new AsymmetricKey(toDer(to.getBaseObject()));
          break;
        case 1002:
          nodeAddress = ((ASN1String) (to.getBaseObject())).getString();
          break;
        case 1003:
          nodeKey = new AsymmetricKey(toDer(to.getBaseObject()));
          break;
        default:
          throw new IOException("unknown tag encountered");
      }
    }
  }

  @Override
  public ASN1Object toAsn1Object(DumpType dumpType) throws IOException {
    // Prepare encoding
    LOGGER.log(Level.FINER, "Executing toAsn1Object()");

    ASN1EncodableVector v = new ASN1EncodableVector();

    v.add(valid.toAsn1Object(dumpType));
    v.add(new ASN1Integer(messageQuota));
    v.add(new ASN1Integer(transferQuota));

    if (identityKey != null) {
      v.add(new DERTaggedObject(true, 1001, identityKey.toAsn1Object(dumpType)));
    }
    if (nodeAddress != null) {
      v.add(new DERTaggedObject(true, 1002, new DERUTF8String(nodeAddress)));
    }
    if (nodeKey != null) {
      v.add(new DERTaggedObject(true, 1003, nodeKey.toAsn1Object(dumpType)));
    }

    ASN1Sequence seq = new DERSequence(v);
    LOGGER.log(Level.FINER, "done toAsn1Object()");
    return seq;
  }

  @Override
  public String dumpValueNotation(String prefix, DumpType dumpType) throws IOException {
    StringBuilder sb = new StringBuilder();
    sb.append('{').append(CRLF);
    String url = getUrl();
    sb.append(prefix).append("  -- ").append(url).append(CRLF);
    sb.append(prefix).append("  -- size: ").append(url.length()).append(CRLF);
    sb.append(prefix).append("  -- encoded size: ").append(
            toAsn1Object(DumpType.ALL_UNENCRYPTED).getEncoded().length
    ).append(CRLF);
    sb.append(prefix).append("  valid ").append(valid.dumpValueNotation(prefix + "  ", dumpType))
            .append(',').append(CRLF);
    sb.append(prefix).append("  messageQuota ").append(messageQuota).append(',').append(CRLF);
    sb.append(prefix).append("  transferQuota ").append(transferQuota);
    if (identityKey != null) {
      sb.append(',').append(CRLF);
      sb.append(prefix).append("  identity ")
              .append(identityKey.dumpValueNotation(prefix + "  ", dumpType));
    }
    if (nodeAddress != null) {
      sb.append(',').append(CRLF);
      sb.append(prefix).append("  nodeAddress \"").append(nodeAddress).append('"');
    }
    if (nodeKey != null) {
      sb.append(',').append(CRLF);
      sb.append(prefix).append("  nodeKey ")
              .append(nodeKey.dumpValueNotation(prefix + "  ", dumpType));
    }
    sb.append(CRLF);
    sb.append(prefix).append('}');
    return sb.toString();
  }

  /***
   * <p>Gets an URL representation of the identity.</p>
   *
   * @return the url or IdentityStoreBlock.UNENCODABLE on fail
   * @throws IOException on failure
   */
  public String getUrl() throws IOException {
    if (nodeAddress.startsWith("smtp:")) {
      String[] addr = nodeAddress.substring(5).split("@");
      if (identityKey == null) {
        LOGGER.log(Level.WARNING, "unable to encode identity key of " + nodeAddress
                + " (key is null)");
        return UNENCODABLE;
      }
      ASN1Object e = identityKey.toAsn1Object(DumpType.PUBLIC_ONLY);
      String keySpec = toBase64(e.getEncoded());
      return "vortexsmtp://" + addr[0] + ".." + keySpec + ".." + addr[1] + "@localhost";
    } else {
      return UNENCODABLE;
    }
  }

  /***
   * <p>Getting the type of identity this key reflects.</p>
   *
   * <p>If the type has not been set this methode assumes for a public/private
   * keypair a node key.</p>
   *
   * @return the type of identity
   */
  public IdentityType getType() {
    if (idType != null) {
      return idType;
    }
    if (nodeKey == null && identityKey.privateKey != null) {
      return IdentityType.OWNED_IDENTITY;
    }
    return identityKey == null ? IdentityType.NODE_IDENTITY : IdentityType.RECIPIENT_IDENTITY;
  }

  @Override
  public boolean equals(Object t) {
    if (t == null) {
      return false;
    }
    if (t.getClass() != this.getClass()) {
      return false;
    }
    IdentityStoreBlock isb = (IdentityStoreBlock) t;
    if (!valid.equals(isb.valid)) {
      return false;
    }
    if (messageQuota != isb.messageQuota) {
      return false;
    }
    if (transferQuota != isb.transferQuota) {
      return false;
    }
    if ((identityKey == null && isb.identityKey != null) || (identityKey != null
            && !identityKey.equals(isb.identityKey))) {
      return false;
    }
    if ((nodeAddress != null && !nodeAddress.equals(isb.nodeAddress)) || (nodeAddress == null
            && isb.nodeAddress != null)) {
      return false;
    }
    return (nodeKey == null && isb.nodeKey == null)
            || (nodeKey != null && nodeKey.equals(isb.nodeKey));
  }

}
