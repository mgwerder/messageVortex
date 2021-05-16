package net.messagevortex.asn1;


import net.messagevortex.ExtendedSecureRandom;
import net.messagevortex.MessageVortexLogger;
import net.messagevortex.asn1.encryption.Algorithm;
import net.messagevortex.asn1.encryption.AlgorithmType;
import net.messagevortex.asn1.encryption.DumpType;
import net.messagevortex.asn1.encryption.Mode;
import net.messagevortex.asn1.encryption.Padding;
import net.messagevortex.asn1.encryption.Parameter;
import net.messagevortex.asn1.encryption.SecurityLevel;
import org.bouncycastle.asn1.ASN1Encodable;
import org.bouncycastle.asn1.ASN1EncodableVector;
import org.bouncycastle.asn1.ASN1Object;
import org.bouncycastle.asn1.ASN1OctetString;
import org.bouncycastle.asn1.ASN1Sequence;
import org.bouncycastle.asn1.ASN1TaggedObject;
import org.bouncycastle.asn1.DEROctetString;
import org.bouncycastle.asn1.DERSequence;
import org.bouncycastle.asn1.DERTaggedObject;
import org.bouncycastle.jce.ECNamedCurveTable;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.jce.spec.ECParameterSpec;
import org.bouncycastle.pqc.jcajce.provider.BouncyCastlePQCProvider;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import java.io.IOException;
import java.io.Serializable;
import java.security.*;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Arrays;
import java.util.logging.Level;

/**
 * <p>Asymmetric Key Handling.</p>
 *
 * <p>This class parses and encodes Asymmetric keys from/to ASN.1.
 * It furthermore handles encoding and decoding of encrypted material.</p>
 */
public class AsymmetricKey extends Key implements Serializable, Dumpable {
  
  public static final long serialVersionUID = 100000000032L;
  
  private static final int PUBLIC_KEY_TAG = 2;
  private static final int PRIVATE_KEY_TAG = 3;
  
  static {
    Security.addProvider(new BouncyCastleProvider());
    
    Security.addProvider(new BouncyCastlePQCProvider());
  }
  
  private static final java.util.logging.Logger LOGGER;
  
  static {
    LOGGER = MessageVortexLogger.getLogger((new Throwable()).getStackTrace()[0].getClassName());
  }
  
  static {
    // start key precalculator
    try {
      AsymmetricKey.setCacheFileName("AsymmetricKey.cache");
    } catch (Exception e) {
      LOGGER.log(Level.WARNING, "Failed to set cache file name", e);
    }
  }
  
  protected byte[] publicKey = null;
  protected byte[] privateKey = null;

  /***
   * <p>Creates an asymmetric key based on the byte sequence.</p>
   *
   * @param b             the byte array containing the key.
   * @throws IOException  if an error occures during parsing
   */
  public AsymmetricKey(byte[] b) throws IOException {
    this(ASN1Sequence.getInstance(b));
    selftest();
  }
  
  /***
   * <p>Copy Constructor.</p>
   *
   * <p>This constuctor allows to create a copy of an AsymmetricKey</p>
   *
   * @param ak the key to copy
   */
  public AsymmetricKey(AsymmetricKey ak) {
    try {
      parse(ak.toAsn1Object(DumpType.ALL));
      selftest();
    } catch (IOException ioe) {
      throw new IllegalArgumentException("Error cloning key", ioe);
    }
  }
  
  /***
   * <p>Create object from ASN.1 code.</p>
   *
   * @param to the ASN.1 code
   * @throws IOException if parsing of ASN.1 code fails
   */
  private AsymmetricKey(ASN1Encodable to) throws IOException {
    parse(to);
    selftest();
  }
  
  /***
   * <p>Creates a new Asymmetric key based on the default values.</p>
   *
   * @throws IOException if an error happens during generation
   */
  public AsymmetricKey() throws IOException {
    this(Algorithm.getDefault(AlgorithmType.ASYMMETRIC).getParameters(SecurityLevel.MEDIUM));
    selftest();
  }
  
  /***
   * <p>creates a new asymmetric key based on the parameters given.</p>
   *
   * <p>If available a precalculated key will be offered.</p>
   *
   * @param params   the parameters to be used
   * @throws IOException if the key can not be generated with the given parameters
   */
  public AsymmetricKey(AlgorithmParameter params) throws IOException {
    this(params, true);
  }
  
  /***
   * <p>creates a new asymmetric key based on the parameters given.</p>
   *
   * <p>This call is mainly used by the cache manager to enforce new calculation of a key.</p>
   *
   * @param params             the parameters to be used
   * @param allowPrecalculated true if a precalculated key is allowed
   * @throws IOException if the key can not be generated with the given parameters
   */
  public AsymmetricKey(AlgorithmParameter params, boolean allowPrecalculated) throws IOException {
    if (params == null) {
      throw new NullPointerException("parameters may not be null");
    }
    this.parameters = new AlgorithmParameter(params.toAsn1Object(DumpType.INTERNAL));
    
    if (params.get(Parameter.ALGORITHM) == null) {
      throw new IOException("Algorithm null is not encodable by the system");
    }
    
    createKey(allowPrecalculated);
    
    selftest();
  }
  
  public static String setCacheFileName(String name) {
    return AsymmetricKeyPreCalculator.setCacheFileName(name);
  }
  
  public static String getCacheFileName() {
    return AsymmetricKeyPreCalculator.getCacheFileName();
  }
  
  private final void selftest() throws IOException {
    if (publicKey == null) {
      throw new IOException("selftest failed: Public key may not be null");
    }
    assert getAlgorithm() != Algorithm.EC || parameters.get(Parameter.CURVETYPE).indexOf(""
            + parameters.get(Parameter.BLOCKSIZE)) > 0 : "found mismatch in curve type vs "
            + "blocksize (" + parameters.get(Parameter.BLOCKSIZE) + "/"
            + parameters.get(Parameter.CURVETYPE) + ")";
    assert getAlgorithm() != Algorithm.RSA
            || parameters.get(Parameter.KEYSIZE).equals(parameters.get(Parameter.BLOCKSIZE))
            : "found mismatch in RSA keysize vs blocksize (ks:" + parameters.get(Parameter.KEYSIZE)
            + "/bs:" + parameters.get(Parameter.BLOCKSIZE) + ")";
  }
  
  private void createKey(boolean allowPrecomputed) throws IOException {
    // Selfcheck
    assert parameters != null;
    Algorithm alg = Algorithm.getByString(parameters.get(Parameter.ALGORITHM));
    assert alg != null;
    
    // check for precomputed key
    if (allowPrecomputed) {
      AsymmetricKey tk = AsymmetricKeyPreCalculator.getPrecomputedAsymmetricKey(parameters);
      if (tk != null) {
        // set precomputed values
        assert getKeySize() == tk.getKeySize();
        publicKey = tk.publicKey;
        privateKey = tk.privateKey;
        return;
      }
    }
    // create key pair
    if ("RSA".equals(alg.name())) {
      createRsaKey();
    } else if (alg == Algorithm.EC) {
      createEcKey();
    } else {
      throw new IOException("Encountered unsupported algorithm \"" + alg + "\"");
    }
  }
  
  private void createRsaKey() throws IOException {
    Algorithm alg = Algorithm.getByString(parameters.get(Parameter.ALGORITHM));
    int keySize = getKeySize();
    try {
      KeyPairGenerator keyGen = KeyPairGenerator.getInstance(alg.toString(), alg.getProvider());
      keyGen.initialize(keySize);
      KeyPair pair;
      pair = keyGen.genKeyPair();
      publicKey = pair.getPublic().getEncoded();
      privateKey = pair.getPrivate().getEncoded();
    } catch (IllegalStateException ise) {
      throw new IllegalStateException("unable to generate keys with " + alg + "/"
              + parameters.get(Parameter.MODE) + "/" + parameters.get(Parameter.PADDING)
              + " (size " + keySize + ")", ise);
    } catch (NoSuchAlgorithmException | NoSuchProviderException e) {
      throw new IOException("Exception while generating key pair", e);
    }
  }
  
  
  private void createEcKey() throws IOException {
    Algorithm alg = Algorithm.getByString(parameters.get(Parameter.ALGORITHM));
    try {
      if (parameters.get(Parameter.CURVETYPE) == null) {
        throw new IOException("curve type is not set");
      }
      ECParameterSpec ecpara = ECNamedCurveTable.getParameterSpec(parameters.get(
              Parameter.CURVETYPE)
      );
      KeyPairGenerator g = KeyPairGenerator.getInstance(alg.getAlgorithmFamily(), "BC");
      g.initialize(ecpara, ExtendedSecureRandom.getSecureRandom());
      KeyPair pair = g.generateKeyPair();
      publicKey = pair.getPublic().getEncoded();
      privateKey = pair.getPrivate().getEncoded();
    } catch (InvalidAlgorithmParameterException | NoSuchAlgorithmException
            | NoSuchProviderException e) {
      throw new IOException("Exception while initializing key generation", e);
    }
  }
  
  protected final void parse(ASN1Encodable to) throws IOException {
    ASN1Sequence s1 = ASN1Sequence.getInstance(to);
    // parsing asymmetric key identifier
    int i = 0;
    
    // parse parameters
    parseKeyParameter(ASN1Sequence.getInstance(s1.getObjectAt(i++)));
    
    //parse public key
    ASN1TaggedObject tagged = (ASN1TaggedObject) (s1.getObjectAt(i++));
    if (tagged.getTagNo() != PUBLIC_KEY_TAG) {
      throw new IOException("encountered wrong tag number when parsing public key (expected: "
              + PUBLIC_KEY_TAG + "; got:" + tagged.getTagNo() + ")");
    }
    publicKey = ASN1OctetString.getInstance(tagged.getObject()).getOctets();
    
    // parse private key
    if (s1.size() > i) {
      tagged = (ASN1TaggedObject) (s1.getObjectAt(i++));
      if (tagged.getTagNo() != PRIVATE_KEY_TAG) {
        throw new IOException("encountered wrong tag number when parsing private key (expected: "
                + PRIVATE_KEY_TAG + "; got:" + tagged.getTagNo() + ")");
      }
      privateKey = ASN1OctetString.getInstance(tagged.getObject()).getOctets();
    }
    
  }
  
  /***
   * <p>Checks if the object contains a private key.</p>
   *
   * @return true if the object contains a private key
   */
  public boolean hasPrivateKey() {
    return privateKey != null;
  }
  
  /***
   * <p>Generates the ASN1 notation of the object.</p>
   *
   * @param prefix the line prefix to be used (normally &quot;&quot;)
   * @return the string representation of the ASN1 dump
   */
  public String dumpValueNotation(String prefix) {
    return dumpValueNotation(prefix, DumpType.PUBLIC_ONLY);
  }
  
  /***
   * <p>Generates the ASN1 notation of the object.</p>
   *
   * @param prefix the line prefix to be used (normally &quot;&quot;)
   * @param dumpType     the dump type to be used (normally DumpType.PUBLIC_ONLY)
   * @return the string representation of the ASN1 dump
   */
  public String dumpValueNotation(String prefix, DumpType dumpType) {
    StringBuilder sb = new StringBuilder();
    sb.append('{').append(CRLF);
    sb.append(dumpKeyTypeValueNotation(prefix + "  ", dumpType)).append(',').append(CRLF);
    String s = toHex(publicKey);
    sb.append(prefix).append("  publicKey ").append(s);
    if (privateKey != null && privateKey.length != 0) {
      switch (dumpType) {
        case ALL:
        case ALL_UNENCRYPTED:
        case PRIVATE_COMMENTED:
          dumpPrivateKey(sb, dumpType, prefix + "  ");
          break;
        default:
          break;
      }
    } else {
      sb.append(CRLF);
    }
    sb.append(prefix).append('}');
    return sb.toString();
  }
  
  private void dumpPrivateKey(StringBuilder sb, DumpType dumpType, String prefix) {
    if (dumpType != DumpType.PRIVATE_COMMENTED) {
      sb.append(',');
    }
    sb.append(CRLF);
    String s = toHex(privateKey);
    sb.append(prefix);
    if (dumpType == DumpType.PRIVATE_COMMENTED) {
      sb.append("-- ");
    }
    sb.append("privateKey ").append(s).append(CRLF);
  }
  
  /***
   * <p>Dumps the key as ASN1 object.</p>
   *
   * @param dt the dump type to be used
   * @return the ASN1 object suitable for encoding
   * @throws IOException if not encodable
   */
  public ASN1Object toAsn1Object(DumpType dt) throws IOException {
    // at least a public key must be set
    if (publicKey == null) {
      throw new IOException("publicKey may not be null when dumping");
    }
    ASN1EncodableVector v = new ASN1EncodableVector();
    
    // add key parameters
    addToAsn1Parameter(v, dt);
    
    // add public key
    addToAsn1PublicKey(v, dt);
    
    // add private key
    addToAsn1PrivateKey(v, dt);
    
    return new DERSequence(v);
  }
  
  private void addToAsn1Parameter(ASN1EncodableVector v, DumpType dumpType) throws IOException {
    v.add(encodeKeyParameter(dumpType));
  }
  
  private void addToAsn1PublicKey(ASN1EncodableVector v, DumpType dt) {
    v.add(new DERTaggedObject(true, PUBLIC_KEY_TAG, new DEROctetString(publicKey)));
  }
  
  private void addToAsn1PrivateKey(ASN1EncodableVector v, DumpType dt) {
    if (privateKey != null && (dt == DumpType.ALL || dt == DumpType.ALL_UNENCRYPTED)) {
      v.add(new DERTaggedObject(true, PRIVATE_KEY_TAG, new DEROctetString(privateKey)));
    }
  }
  
  /***
   * <p>Encrypts a byte array using the key contained in this object.</p>
   *
   * @param b the plain text byte array to encrypt
   * @return the encrypted byte array including padding
   */
  @Override
  public byte[] encrypt(byte[] b) throws IOException {
    try {
      KeyPair key = getKeyPair();
      Cipher cipher = getCipher();
      java.security.Key k = key.getPublic();
      cipher.init(Cipher.ENCRYPT_MODE, k);
      return cipher.doFinal(b);
    } catch (InvalidKeySpecException e) {
      throw new IOException("Exception while getting key pair", e);
    } catch (NoSuchAlgorithmException | NoSuchPaddingException | NoSuchProviderException
            | IllegalBlockSizeException | BadPaddingException e) {
      throw new IOException("Exception while encrypting (len: " + b.length + ")", e);
    } catch (InvalidKeyException e) {
      throw new IOException("Exception while init of cipher ", e);
    }
  }
  
  /***
   * <p>Decrypts a byte array using the key contained in this object.</p>
   *
   * @param b the encrypted byte array
   * @return the plain text byte array
   */
  @Override
  public byte[] decrypt(byte[] b) throws IOException {
    try {
      KeyPair key = getKeyPair();
      Cipher cipher = getCipher();
      java.security.Key k = key.getPrivate();
      cipher.init(Cipher.DECRYPT_MODE, k);
      return cipher.doFinal(b);
    } catch (InvalidKeySpecException | NoSuchAlgorithmException | NoSuchPaddingException
            | NoSuchProviderException | InvalidKeyException | IllegalBlockSizeException
            | BadPaddingException e) {
      throw new IOException("Exception while decrypting (len: " + b.length + ")", e);
    }
    
  }
  
  /***
   * <p>Signs a byte array.</p>
   *
   * <p>This method uses the default hashing algorithm.</p>
   *
   * @param b the byte array to be signed
   * @return the signature
   * @throws IOException if unable to carry out signature
   */
  public byte[] sign(byte[] b) throws IOException {
    return sign(b, Algorithm.getDefault(AlgorithmType.HASHING));
  }
  
  /***
   * <p>Signs a byte array.</p>
   *
   * @param b the byte array to be signed
   * @param mac the hashing algorithm to be used
   * @return the signature
   * @throws IOException if unable to carry out signature
   */
  public byte[] sign(byte[] b, Algorithm mac) throws IOException {
    Signature signature;
    try {
      KeyPair key = getKeyPair();
      signature = getSignature(mac);
      signature.initSign(key.getPrivate());
      signature.update(b);
      return signature.sign();
    } catch (SignatureException | InvalidKeySpecException | NoSuchAlgorithmException
            | NoSuchProviderException | InvalidKeyException e) {
      throw new IOException("Exception while encrypting", e);
    }
  }
  
  /***
   * <p>Verifies a given signature accourding to the objects public key.</p>
   *
   * @param b the byte array representing the message
   * @param sig the byte array representing the signature
   * @return true if signature could be verified successfully
   * @throws IOException if signature processing failed
   */
  public boolean verify(byte[] b, byte[] sig) throws IOException {
    return verify(b, sig, Algorithm.getDefault(AlgorithmType.HASHING));
  }
  
  /***
   * <p>Verifies a given signature accourding to the objects public key.</p>
   *
   * @param b the byte array representing the message
   * @param sig the byte array representing the signature
   * @param mac the mac algorithm to verify the signature
   * @return true if signature could be verified successfully
   * @throws IOException if signature processing failed
   */
  public boolean verify(byte[] b, byte[] sig, Algorithm mac) throws IOException {
    Signature signature;
    try {
      KeyPair key = getKeyPair();
      signature = getSignature(mac);
      signature.initVerify(key.getPublic());
      signature.update(b);
      return signature.verify(sig);
    } catch (SignatureException | InvalidKeySpecException | NoSuchAlgorithmException
            | NoSuchProviderException | InvalidKeyException e) {
      throw new IOException("Exception while verifying signature", e);
    }
  }
  
  private KeyFactory getKeyFactory() throws NoSuchAlgorithmException, NoSuchProviderException {
    if (parameters.get(Parameter.ALGORITHM).startsWith(Algorithm.EC.toString())) {
      return KeyFactory.getInstance("ECDSA", "BC");
    } else {
      Algorithm alg = Algorithm.getByString(parameters.get(Parameter.ALGORITHM));
      return KeyFactory.getInstance(alg.toString(), alg.getProvider());
    }
  }
  
  /***
   * <p>Sets the probability of reusing a precalculated key again.</p>
   *
   * <p>This capability is used to reduce cpu load during tests.</p>
   *
   * @param probability the new probability to be set
   * @return the previously set probability
   */
  public static double setDequeueProbability(double probability) {
    return AsymmetricKeyPreCalculator.setDequeueProbability(probability);
  }
  
  /***
   * <p>Gets the current probability for dequeing a used key (nolrmally 1.0)</p>
   *
   * @return the current probability set
   */
  public static double getDequeueProbability() {
    return AsymmetricKeyPreCalculator.getDequeueProbability();
  }
  
  private KeyPair getKeyPair() throws NoSuchAlgorithmException, NoSuchProviderException,
          InvalidKeySpecException {
    
    KeyFactory kf = getKeyFactory();
    
    // Getting public key
    PublicKey localPublicKey = kf.generatePublic(new X509EncodedKeySpec(this.publicKey));
    
    // getting stored private key
    PrivateKey localPrivateKey = null;
    if (this.privateKey != null) {
      localPrivateKey = kf.generatePrivate(new PKCS8EncodedKeySpec(this.privateKey));
    }
    
    // build result and return
    return new KeyPair(localPublicKey, localPrivateKey);
  }
  
  private Cipher getCipher() throws NoSuchPaddingException, NoSuchAlgorithmException,
          NoSuchProviderException {
    Algorithm alg = getAlgorithm();
    if (alg == Algorithm.EC) {
      return Cipher.getInstance(alg.getAlgorithmFamily(), alg.getProvider());
    } else {
      return Cipher.getInstance(alg + "/" + getMode() + "/" + getPadding(), alg.getProvider());
    }
  }
  
  private Signature getSignature(Algorithm a)
          throws NoSuchAlgorithmException, NoSuchProviderException {
    Algorithm alg = getAlgorithm();
    
    if (alg == Algorithm.EC) {
      return Signature.getInstance(a + "WithECDSA", alg.getProvider());
    } else {
      return Signature.getInstance(a + "With" + alg, alg.getProvider());
    }
  }
  
  /***
   * <p>Sets the public key.</p>
   *
   * @param b the byte array representing the public key
   * @return the previously set public key
   * @throws NullPointerException if key was tried to set to null
   */
  public byte[] setPublicKey(byte[] b) {
    if (b == null) {
      throw new NullPointerException("Public key may not be null");
    }
    byte[] old =  Arrays.copyOf(publicKey,publicKey.length);
    publicKey = Arrays.copyOf(b, b.length);
    return old;
  }
  
  /***
   * <p>Gets the public key in binary representation.</p>
   *
   * @return the public key
   */
  public byte[] getPublicKey() {
    return Arrays.copyOf(publicKey, publicKey.length);
  }
  
  /***
   * <p>Sets the private key of this object.</p>
   *
   * @param b the byte representation of the key to be set.
   * @return the previously set private key
   */
  public byte[] setPrivateKey(byte[] b) {
    byte[] old = privateKey;
    if (b == null) {
      privateKey = null;
    } else {
      privateKey = Arrays.copyOf(b, b.length);
    }
    return old;
  }
  
  /***
   * <p>Gets the private key of this object.</p>
   *
   * @return the pyte representation of the private key
   */
  public byte[] getPrivateKey() {
    return Arrays.copyOf(privateKey, privateKey.length);
  }
  
  /***
   * <p>Gets the algorithm of this key type.</p>
   *
   * @return the algorithm used for generation
   */
  public Algorithm getAlgorithm() {
    return Algorithm.getByString(parameters.get(Parameter.ALGORITHM));
  }
  
  /***
   * <p>Gets the full algorithm parameters of this key.</p>
   *
   * @return the algorithm parameters used for generation
   */
  public AlgorithmParameter getAlgorithmParameter() {
    try {
      return new AlgorithmParameter(parameters.toAsn1Object(DumpType.INTERNAL));
    } catch (IOException ex) {
      throw new IllegalStateException("parameter structure not clonable", ex);
    }
  }
  
  /***
   * <p>Gets the padding used for encryption.</p>
   *
   * @return the padding which is used for encryption
   */
  public Padding getPadding() {
    Padding padding = Padding.getByString(parameters.get(Parameter.PADDING));
    return padding == null ? Padding.getDefault(AlgorithmType.ASYMMETRIC) : padding;
  }
  
  /***
   * <p>Sets the padding used for encryption.</p>
   *
   * @param p the padding to be set
   * @return the previously set padding
   */
  public Padding setPadding(Padding p) {
    Padding old = getPadding();
    parameters.put(Parameter.PADDING.getId(), p.toString());
    return old;
  }
  
  /***
   * <p>Gets the size of the key stored in this object.</p>
   *
   * @return the key size in bits
   */
  public int getKeySize() {
    return Integer.parseInt(parameters.get(Parameter.KEYSIZE));
  }
  
  /***
   * <p>Gets the size of the key stored in this object.</p>
   *
   * @return the key size in bits
   */
  public int getBlockSize() {
    int bs = 0;
    try {
      bs = Integer.parseInt(parameters.get(Parameter.BLOCKSIZE));
      
      // get keysize if blocksize is to small/invalid
      if (bs < 128) {
        throw new NumberFormatException();
      }
    } catch (NumberFormatException nfe) {
      bs = Integer.parseInt(parameters.get(Parameter.KEYSIZE));
      if (bs < 128) {
        bs = -1;
      }
    }
    return bs;
  }
  
  /***
   * <p>Gets the mode used for encryption.</p>
   *
   * @return the mode set used for encryption
   */
  public Mode getMode() {
    return Mode.getByString(parameters.get(Parameter.MODE));
  }
  
  /***
   * <p>Sets the mode used for encryption.</p>
   *
   * @param m the mode to be set
   * @return the mode previously set
   */
  public Mode setMode(Mode m) {
    Mode old = getMode();
    parameters.put(Parameter.MODE.getId(), m.toString());
    return old;
  }
  
  /***
   * <p>tests two asymmetric keys for equality.</p>
   *
   * <p>Two keys are considered equal if they contain the same parameters and
   * the same keys (public and private)</p>
   *
   * @param key the other key
   * @return true if both keys are considered equivalent
   */
  @Override
  public boolean equals(Object key) {
    // make sure object is not null
    if (key == null) {
      return false;
    }
    
    //make sure object is of right type
    if (key.getClass() != this.getClass()) {
      return false;
    }
    
    // compare public keys
    AsymmetricKey o = (AsymmetricKey) key;
    return dumpValueNotation("",
            DumpType.ALL_UNENCRYPTED).equals(o.dumpValueNotation("", DumpType.ALL_UNENCRYPTED)
    );
    
  }
  
  /***
   * <p>returns the hashcode of the dump representation.</p>
   */
  @Override
  public int hashCode() {
    return dumpValueNotation("", DumpType.ALL_UNENCRYPTED).hashCode();
  }
  
  /***
   * <p>Gets a textual representation of the objects parameters (without the keys).</p>
   *
   * @return the string
   */
  @Override
  public String toString() {
    return "([AsymmetricKey]hash=" + (privateKey != null ? Arrays.hashCode(privateKey) : "null")
            + "/" + (publicKey != null ? Arrays.hashCode(publicKey) : "null") + ";" + parameters
            + ")";
  }
  
}
