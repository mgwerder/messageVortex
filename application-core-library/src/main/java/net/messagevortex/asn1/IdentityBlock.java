package net.messagevortex.asn1;

import net.messagevortex.asn1.encryption.Algorithm;
import net.messagevortex.asn1.encryption.AlgorithmType;
import net.messagevortex.asn1.encryption.DumpType;
import net.messagevortex.asn1.encryption.SecurityLevel;
import org.bouncycastle.asn1.*;

import java.io.IOException;
import java.io.Serializable;
import java.util.Arrays;

/***
 * <p>Represents an identity block of a vortexMessage.</p>
 */
public class IdentityBlock extends AbstractBlock implements Serializable, Dumpable {

  public enum IdentityStatus {
    NEW(0),
    PUZZLE_REQUESTED(1),
    PUZZLE_RECEIVED(2),
    PUZZLE_SENT(3),
    ESTABLISHED(10),
    EXPIRED(100);

    private final int statusNumber;

    IdentityStatus(int num) {
      this.statusNumber = num;
    }

    public int getStatusNumber() {
      return statusNumber;
    }
  }

  public static final long serialVersionUID = 100000000008L;

  private static int nextID = 0;
  private static final Object nextIdSemaphore = new Object();

  private static final int ENCRYPTED_HEADER_KEY = 1000;
  private static final int ENCRYPTED_BLOCK = 1001;
  private static final int PLAIN_BLOCK = 1002;
  private static final int ENCRYPTED_HEADER = 1101;

  private static final HeaderRequest[] NULLREQUESTS = new HeaderRequest[0];


  private SymmetricKey headerKey;
  private byte[] encryptedHeaderKey = null;
  private AsymmetricKey identityKey = null;
  private long serial;
  private int maxReplays;
  private UsagePeriod valid = null;
  private int forwardSecret = -1;
  private MacAlgorithm hash = new MacAlgorithm(Algorithm.getDefault(AlgorithmType.HASHING));
  private HeaderRequest[] requests;
  private long identifier = -1;
  private byte[] padding = null;
  private byte[] encryptedIdentityBlock = null;
  private final IdentityStatus status = IdentityStatus.NEW;

  private final int id;

  private AsymmetricKey ownIdentity = null;

  /***
   * <p>Creates a new IdentityBlock with a medium security default key.</p>
   *
   * <p>This is a convenience wrapper for @see IdentityBlock(AsymmetricKey)</p>
   *
   * @throws IOException if generation of the key fails
   */
  public IdentityBlock() throws IOException {
    this(new AsymmetricKey(
            Algorithm.getDefault(AlgorithmType.ASYMMETRIC).getParameters(SecurityLevel.MEDIUM))
    );
  }

  /***
   * <p>Generates a new IdentityBlock for the given key.</p>
   *
   * <p>The new identity block is characterized by 1 replay (not replayable),
   * a random serial, a usage perod of 1 hour, and no requests.</p>
   *
   * @param key the key to be used
   * @throws IOException if generation of the block fails
   */
  public IdentityBlock(AsymmetricKey key) throws IOException {
    this.identityKey = key;
    this.serial = (long) (Math.random() * 4294967295L);
    this.maxReplays = 1;
    this.valid = new UsagePeriod(3600);
    this.requests = IdentityBlock.NULLREQUESTS;
    synchronized (nextIdSemaphore) {
      id = nextID++;
    }
  }

  /***
   * <p>Parses the given identity block using the specified key.</p>
   *
   * @param b a byte array reflecting the encrypted IdentityBlock
   * @param ownIdentity the identity to be used to decrypt the block
   * @throws IOException if parsing fails for any reason
   */
  public IdentityBlock(byte[] b, AsymmetricKey ownIdentity) throws IOException {
    this.ownIdentity = ownIdentity;
    ASN1Encodable s = ASN1Sequence.getInstance(b);
    synchronized (nextIdSemaphore) {
      id = nextID++;
    }
    parse(s);
  }


  /***
   * <p>Parses the given unecrypted identity block.</p>
   *
   * @param b a byte array reflecting the IdentityBlock
   * @throws IOException if parsing fails for any reason
   */
  public IdentityBlock(byte[] b) throws IOException {
    ASN1Encodable s = ASN1Sequence.getInstance(b);
    synchronized (nextIdSemaphore) {
      id = nextID++;
    }
    parse(s);
  }

  /***
   * <p>Create object from ASN.1 code.</p>
   *
   * @param to the ASN.1 code
   * @throws IOException if parsing of ASN.1 code fails
   */
  public IdentityBlock(ASN1Encodable to) throws IOException {
    this(to, null);
  }

  /***
   * <p>Create object from encrypted ASN.1 code.</p>
   *
   * @param to the ASN.1 code
   * @param ownIdentity the identity to be used to decrypt the block
   * @throws IOException if parsing of ASN.1 code fails
   */
  public IdentityBlock(ASN1Encodable to, AsymmetricKey ownIdentity) throws IOException {
    super();
    this.ownIdentity = ownIdentity;
    parse(to);
    synchronized (nextIdSemaphore) {
      id = nextID++;
    }
  }

  public void setRequests(HeaderRequest[] hr) {
    this.requests = Arrays.copyOf(hr,hr.length);
  }

  @Override
  protected final void parse(ASN1Encodable o) throws IOException {
    ASN1Sequence s = ASN1Sequence.getInstance(o);
    ASN1Sequence s1;
    int j = 0;
    ASN1TaggedObject to = ASN1TaggedObject.getInstance(s.getObjectAt(j++));
    if (to.getTagNo() == ENCRYPTED_HEADER_KEY) {
      if (ownIdentity == null) {
        encryptedHeaderKey = toDer(to.getObject());
        to = DERTaggedObject.getInstance(s.getObjectAt(j++));
      } else {
        headerKey = new SymmetricKey(ownIdentity.decrypt(toDer(to.getObject())));
        to = DERTaggedObject.getInstance(s.getObjectAt(j++));
      }
    }
    byte[] signVerifyObject = toDer(to.getObject());
    if ((headerKey != null && to.getTagNo() == ENCRYPTED_HEADER) || to.getTagNo() == PLAIN_BLOCK) {
      if (headerKey != null && to.getTagNo() == ENCRYPTED_BLOCK) {
        s1 = ASN1Sequence.getInstance(headerKey.decrypt(toDer(to.getObject())));
      } else {
        s1 = ASN1Sequence.getInstance(to.getObject());
      }
      int i = 0;
      ASN1Encodable s3 = s1.getObjectAt(i++);
      identityKey = new AsymmetricKey(toDer(s3.toASN1Primitive()));
      serial = ASN1Integer.getInstance(s1.getObjectAt(i++)).getValue().longValue();
      maxReplays = ASN1Integer.getInstance(s1.getObjectAt(i++)).getValue().intValue();
      valid = new UsagePeriod(s1.getObjectAt(i++));
      forwardSecret = ASN1Integer.getInstance(s1.getObjectAt(i++)).getValue().intValue();
      hash = new MacAlgorithm(s1.getObjectAt(i++));
      ASN1Sequence s2 = ASN1Sequence.getInstance(s1.getObjectAt(i++));
      requests = new HeaderRequest[s2.size()];
      for (int y = 0; y < s2.size(); y++) {
        requests[y] = HeaderRequestFactory.getInstance(s2.getObjectAt(y));
      }
      while (s1.size() > i) {
        to = ASN1TaggedObject.getInstance(s1.getObjectAt(i++));
        if (to.getTagNo() == 1) {
          identifier = ASN1Integer.getInstance(to.getObject()).getValue().longValue();
        } else if (to.getTagNo() == 2) {
          padding = ASN1OctetString.getInstance(s1.getObjectAt(i)).getOctets();
        }
      }
    } else {
      encryptedIdentityBlock = toDer(to.getObject());
    }

    byte[] signature = ASN1OctetString.getInstance(s.getObjectAt(j++)).getOctets();
    if (!identityKey.verify(signVerifyObject, signature, hash.getAlgorithm())) {
      throw new IOException("Exception while verifying signature of identity block");
    }
  }

  /***
   * <p>Gets the maximum number of replays for this block.</p>
   *
   * @return the currently set maximum number of replays
   */
  public int getReplay() {
    return maxReplays;
  }

  /***
   * <p>Sets the maximum number of replays for this block.</p>
   *
   * @param maxReplay the maximum nuber of replays to be set
   * @return the previously set maximum
   */
  public int setReplay(int maxReplay) {
    int old = getReplay();
    this.maxReplays = maxReplay;
    return old;
  }

  /***
   * <p>Gets the currently set validity period of the block.</p>
   *
   * @return the previously set validity period
   */
  public UsagePeriod getUsagePeriod() {
    return new UsagePeriod(valid);
  }

  /***
   * <p>Sets the maximum usage period of the block.</p>
   *
   * @param valid the new usage period to be set
   * @return the previously set usage period
   */
  public UsagePeriod setUsagePeriod(UsagePeriod valid) {
    UsagePeriod old = getUsagePeriod();
    this.valid = new UsagePeriod(valid);
    return old;
  }

  /***
   * <p>Gets the identity representation (asymmetric key) of the block.</p>
   *
   * @return the previously set identity
   */
  public AsymmetricKey getOwnIdentity() {
    return ownIdentity;
  }

  /***
   * <p>Sets the identity representation (asymmetric key) of the block.</p>
   *
   * @param oid the identity key
   * @return the previously set identity
   */
  public AsymmetricKey setOwnIdentity(AsymmetricKey oid) {
    AsymmetricKey old = ownIdentity;
    ownIdentity = oid;
    return old;
  }

  /***
   * <p>Gets the identity representation (asymmetric key) of the block.</p>
   *
   * @return the previously set identity
   */
  public AsymmetricKey getIdentityKey() {
    if (identityKey == null) {
      return null;
    }
    return new AsymmetricKey(identityKey);
  }

  /***
   * <p>Sets the identity representation (asymmetric key) of the block.</p>
   *
   * @param oid the identity key
   * @return the previously set identity
   */
  public AsymmetricKey setIdentityKey(AsymmetricKey oid) {
    AsymmetricKey old = identityKey;
    this.identityKey = oid;
    return old;
  }

  private void sanitizeHeaderKey() throws IOException {
    if (headerKey == null && encryptedHeaderKey == null) {
      headerKey = new SymmetricKey();
    }
  }

  /***
   * <p>Dumps the identity block as ASN.1 der encoded object.</p>
   *
   * @return the block as der encodable object
   * @throws IOException   if the block is not encodable
   */
  public ASN1Object toAsn1Object(DumpType dumpType) throws IOException {
    return toAsn1Object(dumpType, null);
  }

  /***
   * <p>Dumps the identity block as ASN.1 der encoded object.</p>
   *
   * @param dumpType the type of dump to be used
   * @param targetIdentity the identity to be used to secure the Identity block (target identity)
   * @return the block as der encodable object
   *
   * @throws IOException   if the block is not encodable
   */
  public ASN1Object toAsn1Object(DumpType dumpType, AsymmetricKey targetIdentity)
          throws IOException {
    sanitizeHeaderKey();
    if (headerKey == null && encryptedHeaderKey == null) {
      throw new NullPointerException("headerKey may not be null");
    }
    ASN1EncodableVector v1 = new ASN1EncodableVector();
    boolean encryptIdentity = false;
    if (headerKey != null && targetIdentity != null) {
      v1.add(new DERTaggedObject(
              true,
              ENCRYPTED_HEADER_KEY,
              new DEROctetString(targetIdentity.encrypt(headerKey.toBytes(dumpType))))
      );
      encryptIdentity = true;
    } else if (encryptedHeaderKey != null) {
      v1.add(new DERTaggedObject(
              true,
              ENCRYPTED_HEADER_KEY,
              new DEROctetString(encryptedHeaderKey))
      );
      encryptIdentity = true;
    }
    ASN1Encodable ae;
    if (encryptedIdentityBlock != null) {
      ae = new DEROctetString(encryptedIdentityBlock);
    } else {
      ASN1EncodableVector v = new ASN1EncodableVector();
      ASN1Object o = identityKey.toAsn1Object(DumpType.ALL);
      if (o == null) {
        throw new IOException("identityKey did return null object");
      }
      v.add(o);
      v.add(new ASN1Integer(serial));
      v.add(new ASN1Integer(maxReplays));
      o = valid.toAsn1Object(dumpType);
      if (o == null) {
        throw new IOException("validity did return null object");
      }
      v.add(o);
      v.add(new ASN1Integer(forwardSecret));
      v.add(hash.toAsn1Object(dumpType));
      ASN1EncodableVector s = new ASN1EncodableVector();
      for (HeaderRequest r : requests) {
        s.add(r.toAsn1Object(dumpType));
      }
      v.add(new DERSequence(s));
      if (identifier > -1) {
        v.add(new DERTaggedObject(true, 1, new ASN1Integer(identifier)));
      }
      if (padding != null) {
        v.add(new DERTaggedObject(true, 2, new DEROctetString(padding)));
      }
      ae = new DERSequence(v);
    }
    ASN1Object o;
    if (encryptIdentity) {
      if (headerKey == null) {
        throw new IOException("header key is empty but block should be encrypted");
      }
      // store identity encrypted
      o = new DEROctetString(headerKey.encrypt(toDer(ae.toASN1Primitive())));
      v1.add(new DERTaggedObject(true, ENCRYPTED_BLOCK, o));
    } else {
      // store identity plain
      o = ae.toASN1Primitive();
      v1.add(new DERTaggedObject(true, PLAIN_BLOCK, o));
    }
    v1.add(new DEROctetString(identityKey.sign(toDer(o))));
    return new DERSequence(v1);
  }

  public String dumpValueNotation(String prefix) throws IOException {
    return dumpValueNotation(prefix, DumpType.PUBLIC_ONLY);
  }

  /***
   * <p>Dumps the current block state in ASN.1 value notation.</p>
   *
   * @param prefix the prefix to be prepended to each line (whitespaces for indentation)
   * @param dumpType     the type of dump to be used
   * @return a String representing the ASN.1 value notation of the Block
   *
   * @throws IOException if the block is not encodable
   */
  public String dumpValueNotation(String prefix, DumpType dumpType) throws IOException {
    StringBuilder sb = new StringBuilder();
    sb.append("{" + CRLF);
    if (encryptedHeaderKey != null) {
      sb.append(prefix).append("  headerKey ").append(toHex(encryptedHeaderKey));
    }
    if (encryptedIdentityBlock != null) {
      sb.append(prefix).append("  blocks encrypted ").append(toHex(encryptedIdentityBlock));
    } else {
      sb.append(prefix).append("  blocks plain {").append(CRLF);
      sb.append(prefix).append("    identityKey ")
              .append(identityKey.dumpValueNotation(prefix + "  ", DumpType.PRIVATE_COMMENTED))
              .append(',').append(CRLF);
      sb.append(prefix).append("    serial ").append(serial).append(',').append(CRLF);
      sb.append(prefix).append("    maxReplays ").append(maxReplays).append(',').append(CRLF);
      sb.append(prefix).append("    valid ")
              .append(valid.dumpValueNotation(prefix + "  ", dumpType)).append(',').append(CRLF);
      sb.append(prefix).append("    forwardSecret ").append(forwardSecret).append(CRLF);
      sb.append(prefix).append("    decryptionKey ''B,").append(CRLF);
      sb.append(prefix).append("    requests {").append(CRLF);
      for (HeaderRequest r : requests) {
        sb.append(valid.dumpValueNotation(prefix + "  ", dumpType)).append(CRLF);
        sb.append(r.dumpValueNotation(prefix + "  ", dumpType)).append(CRLF);
      }
      sb.append(prefix).append("    }");
      if (padding != null) {
        sb.append(',').append(CRLF);
        sb.append(prefix).append("    padding ").append(toHex(padding)).append(CRLF);
      } else {
        sb.append(CRLF);
      }
      sb.append(prefix).append("  },");
    }
    sb.append(prefix).append('}');
    return sb.toString();
  }

  /***
   * <p>Get the serial of the identity block.</p>
   *
   * @return the currently set serial number
   */
  public long getSerial() {
    return serial;
  }

  /***
   * <p>Set the serial of the identity block.</p>
   *
   * @param serial the serial to be set
   * @return the previously set serial number
   */
  public long setSerial(long serial) {
    long ret = getSerial();
    this.serial = serial;
    return ret;
  }


  @Override
  public boolean equals(Object t) {
    if (t == null) {
      return false;
    }
    if (t.getClass() != this.getClass()) {
      return false;
    }
    IdentityBlock o = (IdentityBlock) t;
    try {
      return dumpValueNotation("", DumpType.ALL_UNENCRYPTED).equals(o.dumpValueNotation("",
              DumpType.ALL_UNENCRYPTED));
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

  public String toString() {
    return "Identity" + id;
  }

}
