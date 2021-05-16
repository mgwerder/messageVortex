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
import net.messagevortex.asn1.encryption.DumpType;
import org.bouncycastle.asn1.ASN1Encodable;
import org.bouncycastle.asn1.ASN1EncodableVector;
import org.bouncycastle.asn1.ASN1Object;
import org.bouncycastle.asn1.ASN1Primitive;
import org.bouncycastle.asn1.ASN1Sequence;
import org.bouncycastle.asn1.DERSequence;

import java.io.IOException;
import java.io.Serializable;
import java.util.Arrays;
import java.util.logging.Level;

/**
 * <p>ASN1 parser class for header reply.</p>
 */
public class PrefixBlock extends AbstractBlock implements Serializable {

  public static final long serialVersionUID = 100000000014L;

  byte[] encrypted = null;
  AsymmetricKey decryptionKey = null;

  /* The key used for decryption of the rest of the VortexMessage. */
  SymmetricKey key = null;

  private static final java.util.logging.Logger LOGGER;

  static {
    LOGGER = MessageVortexLogger.getLogger((new Throwable()).getStackTrace()[0].getClassName());
  }

  /**
   * <p>Creates a prefix with a random symmetric key.</p>
   *
   * @throws IOException if key generation fails
   */
  public PrefixBlock() throws IOException {
    this(null);
  }

  /***
   * <p>Creates a prefix with the given key.</p>
   *
   * @param sk symmetric key to embed in the prefix block
   *
   * @throws IOException if key generation fails
   */
  public PrefixBlock(SymmetricKey sk) throws IOException {
    if (sk == null) {
      key = new SymmetricKey();
    } else {
      key = sk;
    }
  }

  /***
   * <p>Creates a prefix by parsing to in plan (unencrypted).</p>
   *
   * @param to The primitive to be parsed
   * @param ak the asymmetric key required to decrypt the block
   *
   * @throws IOException if parsing fails
   */
  public PrefixBlock(ASN1Primitive to, AsymmetricKey ak) throws IOException {
    this(toDer(to), ak);
  }

  /***
   * <p>Creates a prefix from the provided byte array by decyphering it with the provided key.</p>
   *
   * @param to the ASN1 OCTET STRING containing the encrypted prefix
   * @param ak the host key
   *
   * @throws IOException  if parsing of the prefix block fails
   */
  public PrefixBlock(byte[] to, AsymmetricKey ak) throws IOException {
    if (ak != null) {
      setDecryptionKey(ak);
    }
    AsymmetricKey decrypt = getDecryptionKey();
    if (decrypt != null && decrypt.hasPrivateKey()) {
      parse(ASN1Sequence.getInstance(decrypt.decrypt(to)));
    } else {
      try {
        parse(ASN1Sequence.getInstance(to));
      } catch (IOException | RuntimeException ioe) {
        LOGGER.log(Level.WARNING, "Parsing of prefix block failed", ioe);
        setDecryptionKey(null);
        key = null;
        encrypted = Arrays.copyOf(to, to.length);
      }
    }
  }

  @Override
  protected final void parse(ASN1Encodable to) throws IOException {
    encrypted = null;
    LOGGER.log(Level.FINER, "Executing parse()");
    ASN1Sequence s1 = ASN1Sequence.getInstance(to);

    // getting key
    key = new SymmetricKey(toDer(s1.getObjectAt(0).toASN1Primitive()), null);
  }

  /***
   * <p>Sets the decryption key for the prefix block.</p>
   *
   * <p>If the prefixblock is already encrypted a decryption is attempted.</p>
   *
   * @param dk   the decryption key to be used when decrypting the block
   * @return the previous decryption key
   * @throws IOException if decryption tails
   */
  public final AsymmetricKey setDecryptionKey(AsymmetricKey dk) throws IOException {
    AsymmetricKey old = getDecryptionKey();
    decryptionKey = dk;
    if (isEncrypted()) {
      parse(dk.decrypt(encrypted));
    }
    return old;
  }

  public final AsymmetricKey getDecryptionKey() {
    return decryptionKey;
  }

  /***
   * <p>Sets the symmetric key contained in the block.</p>
   *
   * @param dk the decryption key for all subsequent blocks
   * @return the key set before the change
   */
  public SymmetricKey setKey(SymmetricKey dk) {
    if (dk == null) {
      throw new NullPointerException("symmetric key may not be null");
    }
    SymmetricKey old = getKey();
    key = dk;
    encrypted = null;
    return old;
  }

  public SymmetricKey getKey() {
    return key;
  }

  @Override
  public ASN1Object toAsn1Object(DumpType dumpType) throws IOException {
    if (getKey() == null && isEncrypted()) {
      throw new IOException("only encrypted form may be dumped without providing a valid "
          + "decryption key");
    }
    ASN1EncodableVector v = new ASN1EncodableVector();
    ASN1Encodable o = getKey().toAsn1Object(dumpType);
    if (o == null) {
      throw new IOException("returned symmetric object may not be null");
    }
    v.add(o);

    LOGGER.log(Level.FINER, "done toAsn1Object() of PrefixBlock");
    return new DERSequence(v);
  }

  @Override
  public String dumpValueNotation(String prefix, DumpType dumpType) throws IOException {
    StringBuilder sb = new StringBuilder();
    if (DumpType.ALL == dumpType || DumpType.PUBLIC_ONLY == dumpType) {
      // dump standard block as octet string
      sb.append("encrypted ").append(toHex(toEncBytes()));
    } else {
      // dump as unecrypted structure
      sb.append("plain  {").append(CRLF);
      sb.append(prefix).append("  key ").append(key.dumpValueNotation(prefix + "  ", dumpType))
          .append(CRLF);
      sb.append(prefix).append('}');
    }
    return sb.toString();
  }

  @Override
  public boolean equals(Object o) {
    if (o == null) {
      return false;
    }

    // must be equivalent in type
    if (o.getClass() != this.getClass()) {
      return false;
    }

    // do casting
    PrefixBlock p = (PrefixBlock) (o);

    // look for not equal keys
    if ((p.getKey() == null && getKey() != null) || (p.getKey() != null && getKey() == null)) {
      return false;
    }
    return !(p.getKey() != null && !p.getKey().equals(getKey()));
  }

  @Override
  public int hashCode() {
    return getKey().hashCode();
  }

  /***
   * <p>get the encryption status of the prefix block.</p>
   *
   * @return true if the block is encrypted
   */
  public boolean isEncrypted() {
    return encrypted != null;
  }

  /***
   * <p>Get the ASN.1 encoded prefix block in encrypted form.</p>
   *
   * @return the encrypted ASN.1 rncoded block
   * @throws IOException if encoding fails
   */
  public byte[] toEncBytes() throws IOException {
    if (decryptionKey != null && encrypted == null && decryptionKey.hasPrivateKey()) {
      int maxSize = decryptionKey.getPadding().getMaxSize(decryptionKey.getBlockSize());
      byte[] b = toBytes(DumpType.PUBLIC_ONLY);
      if (maxSize < b.length) {
        throw new IOException("unable to encrypt current prefix block (prefixSize: " + b.length
            + "; maxSize: " + maxSize + ")");
      }
      return decryptionKey.encrypt(b);
    } else {
      if (encrypted == null) {
        return null;
      } else {
        return encrypted.clone();
      }
    }
  }
}
