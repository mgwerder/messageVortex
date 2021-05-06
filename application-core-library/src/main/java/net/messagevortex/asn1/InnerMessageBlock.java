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

import net.messagevortex.MessageVortexLogger;
import net.messagevortex.asn1.encryption.Algorithm;
import net.messagevortex.asn1.encryption.DumpType;
import org.bouncycastle.asn1.*;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;

/***
 * <p>represents the inner encrypted part of a VortexMessage.</p>
 *
 * <p>This part is specified as InnerMessageBlock in the file asn.1/messageBlocks.asn1</p>
 */
public class InnerMessageBlock extends AbstractBlock implements Serializable {

  public static final long serialVersionUID = 100000000009L;

  public static final int PREFIX_PLAIN = 11011;
  public static final int PREFIX_ENCRYPTED = 11012;

  public static final int IDENTITY_PLAIN = 11021;
  public static final int IDENTITY_ENCRYPTED = 11022;

  public static final int ROUTING_PLAIN = 11031;
  public static final int ROUTING_ENCRYPTED = 11032;

  private byte[] padding = new byte[0];
  private PrefixBlock prefix;
  private IdentityBlock identity;
  private byte[] identitySignature = null;
  private RoutingCombo routing;
  private final Object payloadLock = new Object();
  private PayloadChunk[] payload = new PayloadChunk[0];

  private static final java.util.logging.Logger LOGGER;

  static {
    LOGGER = MessageVortexLogger.getLogger((new Throwable()).getStackTrace()[0].getClassName());
  }

  public InnerMessageBlock() throws IOException {
    this(new PrefixBlock(), new IdentityBlock(), new RoutingCombo());
  }

  public InnerMessageBlock(Algorithm sym, AsymmetricKey asym) throws IOException {
    this(new PrefixBlock(new SymmetricKey(sym)), new IdentityBlock(asym), new RoutingCombo());
  }

  /***
   * <p>Creates an inner message with the specified components.</p>
   *
   * @param prefix  the prefix block to be used
   * @param i       the header/identity block to be used
   * @param routing the router block to be used
   */
  public InnerMessageBlock(PrefixBlock prefix, IdentityBlock i, RoutingCombo routing) {
    this.prefix = prefix;
    identity = i;
    this.routing = routing;
  }

  public InnerMessageBlock(byte[] b, AsymmetricKey decryptionKey) throws IOException {
    parse(b, decryptionKey);
  }

  protected final void parse(byte[] p, AsymmetricKey decryptionKey) throws IOException {
    try (ASN1InputStream aIn = new ASN1InputStream(p)) {
      parse(aIn.readObject(), decryptionKey);
      if (identity == null) {
        throw new NullPointerException("IdentityBlock may not be null");
      }
    }
  }

  protected void parse(ASN1Encodable o) throws IOException {
    parse(o, null);
  }

  protected final void parse(ASN1Encodable o, AsymmetricKey decryptionKey) throws IOException {
    LOGGER.log(Level.FINER, "Executing parse()");
    int i = 0;
    ASN1Sequence s1 = ASN1Sequence.getInstance(o);

    // get padding
    padding = ASN1OctetString.getInstance(s1.getObjectAt(i++)).getOctets();

    // get prefix
    ASN1TaggedObject ato = ASN1TaggedObject.getInstance(s1.getObjectAt(i++));
    switch (ato.getTagNo()) {
      case PREFIX_PLAIN:
        prefix = new PrefixBlock(ato.getObject(), null);
        break;
      case PREFIX_ENCRYPTED:
        prefix = new PrefixBlock(ASN1OctetString.getInstance(ato.getObject()).getOctets(),
                decryptionKey);
        break;
      default:
        throw new IOException("got unexpected tag (expect: " + PREFIX_PLAIN + " or "
                + PREFIX_ENCRYPTED + "; got: " + ato.getTagNo() + ")");
    }

    // get identity
    ato = ASN1TaggedObject.getInstance(s1.getObjectAt(i++));
    byte[] identityEncoded;
    switch (ato.getTagNo()) {
      case IDENTITY_PLAIN:
        identityEncoded = toDer(ato.getObject());
        identity = new IdentityBlock(identityEncoded);
        break;
      case IDENTITY_ENCRYPTED:
        identityEncoded = ASN1OctetString.getInstance(ato.getObject()).getOctets();
        identity = new IdentityBlock(prefix.getKey().decrypt(identityEncoded));
        break;
      default:
        throw new IOException("got unexpected tag (expect: " + PREFIX_PLAIN + " or "
                + PREFIX_ENCRYPTED + "; got: " + ato.getTagNo() + ")");
    }

    // get signature
    identitySignature = ASN1OctetString.getInstance(s1.getObjectAt(i++)).getOctets();
    if (!identity.getIdentityKey().verify(identityEncoded, identitySignature)) {
      throw new IOException("failed verifying signature (signature length:"
              + identitySignature.length + "; signedBlock:" + toHex(identityEncoded) + ")");
    }

    // getting router block
    ASN1TaggedObject ae = ASN1TaggedObject.getInstance(s1.getObjectAt(i++));
    switch (ae.getTagNo()) {
      case ROUTING_PLAIN:
        routing = new RoutingCombo(ae.getObject());
        break;
      case ROUTING_ENCRYPTED:
        try {
          routing = new RoutingCombo(ASN1Sequence.getInstance(prefix.getKey()
                  .decrypt(ASN1OctetString.getInstance(ae.getObject()).getOctets())));
        } catch (IOException ioe) {
          throw new IOException("error while decrypting router block", ioe);
        }
        break;
      default:
        throw new IOException("got unexpected tag (expect: " + ROUTING_PLAIN + " or "
                + ROUTING_ENCRYPTED + "; got: " + ASN1TaggedObject.getInstance(ae).getTagNo()
                + ")");
    }

    // getting payload blocks
    ASN1Sequence seq = ASN1Sequence.getInstance(s1.getObjectAt(i++));
    List<PayloadChunk> p2 = new ArrayList<>(seq.size());
    long creationTime = new Date().getTime();
    long last = routing.getLastProcessTime();
    long first = routing.getFirstProcessTime();
    for (ASN1Encodable tr : seq) {
      p2.add(new PayloadChunk(tr, new UsagePeriod(new Date(creationTime + last),
              new Date(creationTime + first))));
    }
    payload = p2.toArray(new PayloadChunk[p2.size()]);
  }

  public ASN1Object toAsn1Object() throws IOException {
    return toAsn1Object(DumpType.PUBLIC_ONLY);
  }

  @Override
  public ASN1Object toAsn1Object(DumpType dumpType) throws IOException {
    // Prepare encoding
    LOGGER.log(Level.FINER, "Executing toAsn1Object()");

    ASN1EncodableVector v = new ASN1EncodableVector();
    v.add(new DEROctetString(padding));
    switch (dumpType) {
      case INTERNAL:
      case ALL_UNENCRYPTED:
        v.add(new DERTaggedObject(PREFIX_PLAIN, prefix.toAsn1Object(dumpType)));
        break;
      case ALL:
      case PUBLIC_ONLY:
      case PRIVATE_COMMENTED:
        try {
          v.add(new DERTaggedObject(PREFIX_ENCRYPTED, new DEROctetString(prefix.toEncBytes())));
        } catch (IOException | NullPointerException e) {
          throw new IOException("need a decryption key to encrypt prefix block ("
                  + prefix.getDecryptionKey() + ")", e);
        }
        break;
      default:
        throw new IllegalStateException("unable to handle dump type " + dumpType);
    }
    if (identity == null) {
      throw new IOException("identity may not be null when encoding");
    }
    LOGGER.log(Level.FINER, "adding identity");
    byte[] o = null;
    switch (dumpType) {
      case INTERNAL:
      case ALL_UNENCRYPTED:
        ASN1Object t = identity.toAsn1Object(dumpType);
        o = toDer(t);
        v.add(new DERTaggedObject(IDENTITY_PLAIN, t));
        break;
      case ALL:
      case PUBLIC_ONLY:
      case PRIVATE_COMMENTED:
        o = prefix.getKey().encrypt(identity.toBytes(dumpType));
        v.add(new DERTaggedObject(IDENTITY_ENCRYPTED, new DEROctetString(o)));
        break;
      default:
        throw new IllegalStateException("unable to handle dump type " + dumpType);
    }
    LOGGER.log(Level.FINER, "adding signature");
    if (identity.getIdentityKey() == null || !identity.getIdentityKey().hasPrivateKey()) {
      throw new IOException("identity needs private key to sign request ("
              + identity.getIdentityKey() + ")");
    }
    try {
      v.add(new DEROctetString(identity.getIdentityKey().sign(o)));
    } catch (IOException ioe) {
      throw new IOException("exception while signing identity", ioe);
    }

    // Writing encoded Routing Block
    if (routing == null) {
      throw new NullPointerException("router may not be null when encoding");
    }
    switch (dumpType) {
      case INTERNAL:
      case ALL_UNENCRYPTED:
        v.add(new DERTaggedObject(true, ROUTING_PLAIN, routing.toAsn1Object(dumpType)));
        break;
      case ALL:
      case PUBLIC_ONLY:
      case PRIVATE_COMMENTED:
        v.add(new DERTaggedObject(true, ROUTING_ENCRYPTED,
                new DEROctetString(prefix.getKey().encrypt(toDer(routing.toAsn1Object(dumpType)))))
        );
        break;
      default:
        throw new IOException("got unknown dump type " + dumpType.name());
    }

    // Writing encoded Payload Blocks
    ASN1EncodableVector v2 = new ASN1EncodableVector();
    if (payload != null && payload.length > 0) {
      for (PayloadChunk p : payload) {
        v2.add(p.toAsn1Object(dumpType));
      }
    }
    v.add(new DERSequence(v2));

    ASN1Sequence seq = new DERSequence(v);

    LOGGER.log(Level.FINER, "done toAsn1Object()");
    return seq;
  }

  public IdentityBlock getIdentity() {
    return identity;
  }

  public RoutingCombo getRouting() {
    return routing;
  }

  public PayloadChunk[] getPayload() {
    return payload.clone();
  }

  public PrefixBlock getPrefix() {
    return prefix;
  }

  @Override
  public String dumpValueNotation(String prefix, DumpType dt) throws IOException {
    StringBuilder sb = new StringBuilder();
    sb.append("  {").append(CRLF);
    sb.append(prefix).append("  padding ").append(toHex(padding)).append(CRLF);
    sb.append(prefix).append("  -- Dumping IdentityBlock").append(CRLF);
    sb.append(prefix).append("  identity ");
    switch (dt) {
      case ALL_UNENCRYPTED:
      case INTERNAL:
        // dumping plain identity
        sb.append("plain ")
                .append(identity.dumpValueNotation(prefix + "  ", DumpType.ALL_UNENCRYPTED))
                .append(',').append(CRLF);
        break;
      case ALL:
      case PRIVATE_COMMENTED:
      case PUBLIC_ONLY:
        // dumping encrypted identity
        sb.append("encrypted ").append(identity.dumpValueNotation(prefix + "  ", dt))
                .append(',').append(CRLF);
        break;
      default:
        throw new IOException("unable to handle dump type " + dt);
    }

    sb.append(prefix).append("  router ").append(routing.dumpValueNotation(prefix + "  ", dt))
            .append(',').append(CRLF);

    sb.append(prefix).append("  payload {");
    int i = 0;
    if (payload != null) {
      for (PayloadChunk p : payload) {
        if (i > 0) {
          sb.append(',');
        }
        sb.append(CRLF);
        p.dumpValueNotation(prefix + "  ", dt);
        i++;
      }
    }
    sb.append(CRLF);
    sb.append(prefix).append("  }").append(CRLF);

    sb.append(prefix).append('}');
    return sb.toString();
  }

  /***
   * <p>Sets the routing block.</p>
   *
   * @param newRouting the routing block to be set
   * @return the previously set routing block
   */
  public RoutingCombo setRouting(RoutingCombo newRouting) {
    RoutingCombo ret = routing;
    routing = newRouting;
    return ret;
  }

  /***
   * <p>Sets the payload block.</p>
   * @param chunkNumber the workspace number
   * @param payload the payload content
   * @return the previously set payload
   */
  public byte[] setPayload(int chunkNumber, byte[] payload) {
    // enlarge payload space
    enlargePayloadSpace(chunkNumber);
    byte[] ret = this.payload[chunkNumber] != null ? this.payload[chunkNumber].getPayload() : null;
    this.payload[chunkNumber] = new PayloadChunk(chunkNumber, payload, null);
    return ret;
  }

  private void enlargePayloadSpace(int i) {
    if (i < payload.length) {
      // no resizing required
      return;
    }
    synchronized (payloadLock) {
      PayloadChunk[] newSpace = new PayloadChunk[i + 1];
      int c = 0;
      for (PayloadChunk p : payload) {
        newSpace[c++] = p;
      }
      payload = newSpace;
    }
  }

  @Override
  public boolean equals(Object o) {
    if (o == null) {
      return false;
    }
    if (o.getClass() != this.getClass()) {
      return false;
    }
    InnerMessageBlock ib = (InnerMessageBlock) o;
    try {
      return Arrays.equals(toBytes(DumpType.ALL_UNENCRYPTED), ib.toBytes(DumpType.ALL_UNENCRYPTED));
    } catch (IOException ioe) {
      return false;
    }
  }

  @Override
  public int hashCode() {
    try {
      return prepareDump(dumpValueNotation("", DumpType.ALL_UNENCRYPTED)).hashCode();
    } catch (IOException ioe) {
      return -1;
    }
  }

}
