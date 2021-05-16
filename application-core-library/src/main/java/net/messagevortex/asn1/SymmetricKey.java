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
import net.messagevortex.asn1.encryption.Algorithm;
import net.messagevortex.asn1.encryption.AlgorithmType;
import net.messagevortex.asn1.encryption.DumpType;
import net.messagevortex.asn1.encryption.Mode;
import net.messagevortex.asn1.encryption.Padding;
import net.messagevortex.asn1.encryption.Parameter;
import org.bouncycastle.asn1.ASN1Encodable;
import org.bouncycastle.asn1.ASN1EncodableVector;
import org.bouncycastle.asn1.ASN1Object;
import org.bouncycastle.asn1.ASN1OctetString;
import org.bouncycastle.asn1.ASN1Primitive;
import org.bouncycastle.asn1.ASN1Sequence;
import org.bouncycastle.asn1.DEROctetString;
import org.bouncycastle.asn1.DERSequence;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.IOException;
import java.io.Serializable;
import java.nio.charset.StandardCharsets;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.util.Arrays;

/**
 * Represents a Symmetric Key in the ASN.1 structure
 */
public class SymmetricKey extends Key implements Serializable {

  public static final long serialVersionUID = 100000000040L;

  private static final ExtendedSecureRandom secureRandom = new ExtendedSecureRandom();

  protected byte[] key = null;

  public SymmetricKey() throws IOException {
    this(Algorithm.getDefault(AlgorithmType.SYMMETRIC));
  }

  public SymmetricKey(Algorithm sk) throws IOException {
    this(sk, Padding.getDefault(AlgorithmType.SYMMETRIC), Mode.getDefault(AlgorithmType.SYMMETRIC));
  }

  /***
   * <p>Creates a new symmetric key according to spec.</p>
   *
   * @param sk    the algrithm of the symmetric key
   * @param pad   the default padding of the symmetric key
   * @param mode  the default mode of the symmetric key
   * @throws IOException if the algorithm is not known to the subsystem
   */
  public SymmetricKey(Algorithm sk, Padding pad, Mode mode) throws IOException {
    if (pad == null) {
      throw new NullPointerException("padding may not be null");
    }
    if (mode == null) {
      throw new NullPointerException("mode may not be null");
    }
    parameters.put(Parameter.ALGORITHM, sk.toString());
    parameters.put(Parameter.PADDING, pad.toString());
    parameters.put(Parameter.MODE, mode.toString());
    if (sk.toString().toLowerCase().startsWith("aes")) {
      createAes(sk.getKeySize());
    } else if (sk.toString().toLowerCase().startsWith("camellia")) {
      createCamellia(sk.getKeySize());
    } else if (sk.toString().toLowerCase().startsWith("twofish")) {
      createTwofish(sk.getKeySize());
    } else {
      throw new IOException("Algorithm " + sk + " is not encodable by the system");
    }
  }

  public SymmetricKey(byte[] sk) throws IOException {
    this(sk, null);
  }

  /***
   * <p>creates a new symmetric key from the given PKCS#1 blob.</p>
   *
   * @param sk     a binary PKCS#1 representation of the key to be constructed
   * @param deckey the decryption key
   * @throws IOException if failing to decrypt the symmetric key
   */
  public SymmetricKey(byte[] sk, AsymmetricKey deckey) throws IOException {
    // decrypt and decode
    ASN1Primitive s;
    if (deckey != null) {
      byte[] b;
      try {
        b = deckey.decrypt(sk);
      } catch (Exception e) {
        throw new IOException("Error while decrypting object", e);
      }
      s = DERSequence.fromByteArray(b);
    } else {
      s = DERSequence.fromByteArray(sk);
    }
    parse(s);
  }

  /***
   * <p>Sets a initialisation vector to be used by the keys default padding.</p>
   *
   * @param b  the initialisation vector
   * @return the previously set initialisation vector
   */
  public byte[] setIv(byte[] b) {
    String s = parameters.get(Parameter.IV);
    byte[] old;
    if (s == null) {
      old = null;
    } else {
      old = s.getBytes(StandardCharsets.UTF_8);
    }
    if (b == null || b.length == 0) {
      parameters.put(Parameter.IV, toHex(ExtendedSecureRandom.generateSeed(16)));
    } else {
      parameters.put(Parameter.IV, toHex(b));
    }
    return old;
  }

  public byte[] getIv() {
    return fromHex(parameters.get(Parameter.IV));
  }

  public AlgorithmParameter getParameter() {
    return new AlgorithmParameter(parameters);
  }

  public Padding getPadding() {
    return Padding.getByString(parameters.get(Parameter.PADDING.toString()));
  }

  /***
   * <p>Gets the key size from the key generation parameters.</p>
   *
   * @return the key size in bits or -1 if there is no key size set
   */
  public int getKeySize() {
    return parameters.get(Parameter.KEYSIZE) != null
                          ? Integer.parseInt(parameters.get(Parameter.KEYSIZE))
                          : getAlgorithm().getKeySize();
  }

  public Mode getMode() {
    return Mode.getByString(parameters.get(Parameter.MODE.toString()));
  }

  public Algorithm getAlgorithm() {
    return Algorithm.getByString(parameters.get(Parameter.ALGORITHM));
  }

  private void createAes(int keysize) {
    Mode mode = getMode();
    byte[] keyBytes = new byte[keysize / 8];
    ExtendedSecureRandom.nextBytes(keyBytes);
    if (mode.getRequiresInitVector() && (getIv() == null || getIv().length != 16)) {
      setIv(null);
    }
    SecretKeySpec aeskey = new SecretKeySpec(keyBytes, "AES");
    key = aeskey.getEncoded();
  }

  private void createCamellia(int keysize) {
    Mode mode = getMode();
    byte[] keyBytes = new byte[keysize / 8];
    ExtendedSecureRandom.nextBytes(keyBytes);
    if (mode.getRequiresInitVector()) {
      setIv(null);
    }
    SecretKeySpec camelliakey = new SecretKeySpec(keyBytes, "Camellia");
    key = camelliakey.getEncoded();
  }

  private void createTwofish(int keysize) {
    Mode mode = getMode();
    byte[] keyBytes = new byte[keysize / 8];
    ExtendedSecureRandom.nextBytes(keyBytes);
    if (mode.getRequiresInitVector()) {
      setIv(null);
    }
    SecretKeySpec twofishkey = new SecretKeySpec(keyBytes, "Twofish");
    key = twofishkey.getEncoded();
  }

  private Cipher getCipher() throws NoSuchAlgorithmException, NoSuchPaddingException {
    setIv(getIv());
    try {
      return Cipher.getInstance(getAlgorithm().getAlgorithmFamily().toUpperCase() + "/"
                                + getMode() + "/" + getPadding(),
                                getAlgorithm().getProvider());
    } catch (NoSuchProviderException e) {
      throw new NoSuchAlgorithmException("unknown provider", e);
    }
  }

  @Override
  public byte[] encrypt(byte[] b) throws IOException {
    try {
      Cipher c = getCipher();
      SecretKeySpec ks = new SecretKeySpec(key, getAlgorithm().getAlgorithmFamily().toUpperCase());
      if (getMode().getRequiresInitVector()) {
        setIv(getIv());
        c.init(Cipher.ENCRYPT_MODE, ks, new IvParameterSpec(getIv()));
      } else {
        c.init(Cipher.ENCRYPT_MODE, ks);
      }
      return c.doFinal(b);
    } catch (NoSuchAlgorithmException | NoSuchPaddingException | IllegalBlockSizeException
            | BadPaddingException e) {
      throw new IOException("Exception while encrypting", e);
    } catch (InvalidKeyException e) {
      throw new IOException("Exception while init of cipher [possible limmited JCE installed] ("
                            + getParameter() + "/" + getIv().length + "/" + (key.length * 8) + ")",
                            e);
    } catch (InvalidAlgorithmParameterException e) {
      throw new IOException("Exception while encrypting (" + getAlgorithm().getAlgorithmFamily()
                            + "/" + getIv().length + ")", e);
    }
  }

  @Override
  public byte[] decrypt(byte[] b) throws IOException {
    try {
      Cipher c = getCipher();
      SecretKeySpec ks = new SecretKeySpec(key, getAlgorithm().getAlgorithmFamily().toUpperCase());
      if (getMode().getRequiresInitVector()) {
        c.init(Cipher.DECRYPT_MODE, ks, new IvParameterSpec(getIv()));
      } else {
        c.init(Cipher.DECRYPT_MODE, ks);
      }
      return c.doFinal(b);
    } catch (NoSuchAlgorithmException | NoSuchPaddingException | IllegalBlockSizeException
             | InvalidAlgorithmParameterException | BadPaddingException e) {
      throw new IOException("Exception while decrypting", e);
    } catch (InvalidKeyException e) {
      throw new IOException("Exception while init of cipher", e);
    }
  }

  protected final void parse(ASN1Encodable to) throws IOException {
    // preparing parsing
    int i = 0;
    ASN1Sequence s1 = ASN1Sequence.getInstance(to);

    // parsing Symetric Key Idetifier
    parseKeyParameter(ASN1Sequence.getInstance(s1.getObjectAt(i++)));

    // getting key
    key = ASN1OctetString.getInstance(s1.getObjectAt(i)).getOctets();
  }

  public byte[] getKey() {
    return key.clone();
  }

  /***
   * <p>Directly replaces the keys binary representation.</p>
   *
   * @param b the binary representation of the symmetric key
   * @return the previously set key
   */
  public byte[] setKey(byte[] b) {
    byte[] old = key;
    key = Arrays.copyOf(b,b.length);
    return old;
  }

  @Override
  public ASN1Object toAsn1Object(DumpType dumpType) throws IOException {
    ASN1EncodableVector ret = new ASN1EncodableVector();
    ret.add(encodeKeyParameter(dumpType));
    ret.add(new DEROctetString(key));
    return new DERSequence(ret);
  }

  @Override
  public boolean equals(Object t) {
    // make sure object is not null
    if (t == null) {
      return false;
    }

    //make sure object is of right type
    if (t.getClass() != this.getClass()) {
      return false;
    }

    // compare  keys
    SymmetricKey o = (SymmetricKey) t;
    return o.dumpValueNotation("", DumpType.ALL_UNENCRYPTED)
            .equals(dumpValueNotation("", DumpType.ALL_UNENCRYPTED));
  }

  @Override
  public int hashCode() {
    return dumpValueNotation("", DumpType.ALL_UNENCRYPTED).hashCode();
  }

  @Override
  public String dumpValueNotation(String prefix, DumpType dumpType) {
    StringBuilder sb = new StringBuilder();
    sb.append('{').append(CRLF);
    sb.append(dumpKeyTypeValueNotation(prefix + "  ", dumpType)).append(',').append(CRLF);
    sb.append(prefix).append("  key ").append(toHex(key)).append(CRLF);
    sb.append(prefix).append('}');
    return sb.toString();
  }

  /***
   * <p>Gets a textual representation of the objects parameters (without the keys).</p>
   *
   * @return the string
   */
  @Override
  public String toString() {
    return "([SymmetricKey]hash=" + (key != null ? Arrays.hashCode(key) : "null")
            + ";" + parameters + ")";
  }

}
