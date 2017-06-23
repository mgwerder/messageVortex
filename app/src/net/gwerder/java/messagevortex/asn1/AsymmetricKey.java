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

import net.gwerder.java.messagevortex.ExtendedSecureRandom;
import net.gwerder.java.messagevortex.asn1.encryption.*;
import org.bouncycastle.asn1.*;
import org.bouncycastle.jce.ECNamedCurveTable;
import org.bouncycastle.jce.spec.ECParameterSpec;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import java.io.IOException;
import java.security.*;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Arrays;

/**
 * Asymmetric Key Handling.
 *
 * This class parses and encodes Asymmetric keys from/to ASN.1.
 * It furthermore handles encoding and decoding of encrypted material.
 *
 * Created by martin.gwerder on 19.04.2016.
 */
public class AsymmetricKey extends Key {

    private static ExtendedSecureRandom esr = new ExtendedSecureRandom();
    private static int PUBLIC_KEY_TAG  = 2;
    private static int PRIVATE_KEY_TAG = 3;

    static {
        Security.addProvider(new org.bouncycastle.jce.provider.BouncyCastleProvider());
    }

    static{
        // start key precalculator
        AsymmetricKey.setCacheFileName("AsymmetricKey.cache");
    }

    protected byte[]  publicKey  = null;
    protected byte[]  privateKey = null;
    protected Algorithm mac=Algorithm.getDefault( AlgorithmType.HASHING );

    /***
     * Creates an asymmetric key based on the byte sequence.
     *
     * @param b             the byte array containing the key.
     * @throws IOException  if an error occures during parsing
     */
    public AsymmetricKey(byte[] b) throws IOException {
        this(ASN1Sequence.getInstance( b ));
        selftest();
    }

    private AsymmetricKey(ASN1Encodable to) throws IOException  {
        parse(to);
        selftest();
    }

    /***
     * creates a new Asymmetric key based on the default values.
     *
     * @throws IOException if an error happens during generation
     */
    public AsymmetricKey() throws IOException {
        this( Algorithm.getDefault( AlgorithmType.ASYMMETRIC ).getParameters(SecurityLevel.MEDIUM) );
        selftest();
    }

    /***
     * creates a new asymmetric key based on the parameters given.
     *
     * If available a precalculated key will be offered.
     *
     * @param params   the parameters to be used
     * @throws IOException if the key can not be generated with the given parameters
     */
    public AsymmetricKey(AlgorithmParameter params) throws IOException {
        this(params,true);
    }

    /***
     * creates a new asymmetric key based on the parameters given.
     *
     * This call is mainly used by the cache manager to enforce new calculation of a key.
     *
     * @param params             the parameters to be used
     * @param allowPrecalculated true if a precalculated key is allowed
     * @throws IOException if the key can not be generated with the given parameters
     */
    public AsymmetricKey(AlgorithmParameter params,boolean allowPrecalculated) throws IOException {
        if(params==null) {
            throw new NullPointerException("parameters may not be null");
        }
        this.parameters=params;

        if(params.get(Parameter.ALGORITHM)==null) {
            throw new IOException( "Algorithm null is not encodable by the system" );
        }

        createKey(allowPrecalculated);

        selftest();
    }

    public static String setCacheFileName(String name) {
        return AsymmetricKeyPreCalculator.setCacheFileName(name);
    }

    private void selftest() throws IOException {
        if(publicKey==null) {
            throw new IOException( "selftest failed: Public key may not be null");
        }
    }

    private void createKey(boolean allowPrecomputed) throws IOException {
        // Selfcheck
        assert parameters!=null;
        Algorithm alg=Algorithm.getByString(parameters.get(Parameter.ALGORITHM));
        assert alg!=null;

        // check for precomputed key
        if(allowPrecomputed) {
            AsymmetricKey tk = AsymmetricKeyPreCalculator.getPrecomputedAsymmetricKey(parameters);
            if (tk != null) {
                // set precomputed values
                assert getKeySize()==tk.getKeySize();
                publicKey = tk.publicKey;
                privateKey = tk.privateKey;
                return;
            }
        }
        // create key pair
        if("RSA".equals(alg.name()) ) {
            createRSAKey();
        } else if (alg==Algorithm.EC) {
            createECKey();
        } else {
            throw new IOException("Encountered unsupported algorithm \""+alg.toString()+"\"");
        }
    }

    private void createRSAKey() throws IOException {
        Algorithm alg=Algorithm.getByString(parameters.get(Parameter.ALGORITHM));
        int keySize = getKeySize();
        try {
            KeyPairGenerator keyGen = KeyPairGenerator.getInstance(alg.toString(), alg.getProvider());
            keyGen.initialize(keySize);
            KeyPair pair;
            pair = keyGen.genKeyPair();
            publicKey = pair.getPublic().getEncoded();
            privateKey = pair.getPrivate().getEncoded();
        } catch (IllegalStateException ise) {
            throw new IllegalStateException("unable to generate keys with " + alg.toString() + "/" + parameters.get(Parameter.MODE) + "/" + parameters.get(Parameter.PADDING) + " (size " + keySize + ")", ise);
        } catch(NoSuchAlgorithmException|NoSuchProviderException e) {
            throw new IOException("Exception while generating key pair",e);
        }
    }


    private void createECKey() throws IOException {
        Algorithm alg=Algorithm.getByString(parameters.get(Parameter.ALGORITHM));
        try {
            if (parameters.get(Parameter.CURVETYPE) == null) {
                throw new IOException("curve type is not set");
            }
            ECParameterSpec ecpara = ECNamedCurveTable.getParameterSpec(parameters.get(Parameter.CURVETYPE));
            KeyPairGenerator g = KeyPairGenerator.getInstance(alg.getAlgorithmFamily(), "BC");
            g.initialize(ecpara, esr.getSecureRandom());
            KeyPair pair = g.generateKeyPair();
            publicKey = pair.getPublic().getEncoded();
            privateKey = pair.getPrivate().getEncoded();
        } catch(InvalidAlgorithmParameterException|NoSuchAlgorithmException|NoSuchProviderException e) {
            throw new IOException("Exception while initializing key generation",e);
        }
    }

    protected void parse(ASN1Encodable to) throws IOException{
        ASN1Sequence s1 = ASN1Sequence.getInstance(to);
        // parsing asymmetric key identifier
        int i=0;

        // parse parameters
        parseKeyParameter(ASN1Sequence.getInstance( s1.getObjectAt(i++) ));

        //parse public key
        DERTaggedObject tagged=(DERTaggedObject)(s1.getObjectAt(i++));
        if(tagged.getTagNo()!= PUBLIC_KEY_TAG) {
            throw new IOException("encountered wrong tag number when parsing public key (expected: "+ PUBLIC_KEY_TAG +"; got:"+tagged.getTagNo()+")");
        }
        publicKey=ASN1OctetString.getInstance(tagged.getObject()).getOctets();

        // parse private key
        if(s1.size()>i) {
            tagged=(DERTaggedObject)(s1.getObjectAt(i++));
            if(tagged.getTagNo()!= PRIVATE_KEY_TAG) {
                throw new IOException("encountered wrong tag number when parsing private key (expected: "+ PRIVATE_KEY_TAG +"; got:"+tagged.getTagNo()+")");
            }
            privateKey=ASN1OctetString.getInstance(tagged.getObject()).getOctets();
        }

    }

    /***
     * Checks if the object contains a private key
     *
     * @return true if the object contains a private key
     */
    public boolean hasPrivateKey() { return privateKey!=null; }

    /***
     * Generates the ASN1 notation of the object.
     *
     * @param prefix the line prefix to be used (normally &quot;&quot;)
     * @return the string representation of the ASN1 dump
     */
    public String dumpValueNotation(String prefix) {
        return dumpValueNotation(prefix,DumpType.PUBLIC_ONLY);
    }

    /***
     * Generates the ASN1 notation of the object.
     *
     * @param prefix the line prefix to be used (normally &quot;&quot;)
     * @param dumpType     the dump type to be used (normally DumpType.PUBLIC_ONLY)
     * @return the string representation of the ASN1 dump
     */
    public String dumpValueNotation(String prefix,DumpType dumpType) {
        StringBuilder sb=new StringBuilder();
        sb.append("{"+CRLF);
        sb.append( dumpKeyTypeValueNotation( prefix,dumpType ) );
        sb.append( "," ).append(CRLF);
        String s = toHex( publicKey );
        sb.append( prefix ).append( "  publicKey " + s );
        switch(dumpType) {
            case ALL:
            case ALL_UNENCRYPTED:
            case PRIVATE_COMMENTED:
                dumpPrivateKey(sb,dumpType,prefix);
                sb.append( CRLF );
                break;
            default:
                sb.append( CRLF );
        }

        sb.append(prefix).append("}");
        return sb.toString();
    }

    private void dumpPrivateKey(StringBuilder sb, DumpType dumpType,String prefix) {
        if(dumpType!=DumpType.PRIVATE_COMMENTED) sb.append( "," );
        sb.append( CRLF );
        String s=toHex(privateKey);
        sb.append(prefix).append("  ");
        if(dumpType==DumpType.PRIVATE_COMMENTED) {
            sb.append("-- ");
        }
        sb.append("privateKey ").append(s).append(CRLF);
    }

    /***
     * Dumps the key as ASN1 object.
     *
     * @param dt the dump type to be used
     * @return the ASN1 object suitable for encoding
     * @throws IOException if not encodable
     */
    public ASN1Object toASN1Object(DumpType dt) throws IOException {
        // at least a public key must be set
        if(publicKey==null) {
            throw new IOException("publicKey may not be null when dumping");
        }
        ASN1EncodableVector v =new ASN1EncodableVector();

        // add key parameters
        addToASN1Parameter(v,dt);

        // add public key
        addToASN1PublicKey(v,dt);

        // add private key
        addToASN1PrivateKey(v,dt);

        return new DERSequence( v );
    }

    private void addToASN1Parameter(ASN1EncodableVector v,DumpType dumpType ) throws IOException {
        v.add(encodeKeyParameter(dumpType));
    }

    private void addToASN1PublicKey(ASN1EncodableVector v, DumpType dt ) {
        v.add(new DERTaggedObject( true, PUBLIC_KEY_TAG,new DEROctetString( publicKey )));
    }

    private void addToASN1PrivateKey(ASN1EncodableVector v, DumpType dt ) {
        if (privateKey != null && (dt==DumpType.ALL || dt==DumpType.ALL_UNENCRYPTED)) {
            v.add(new DERTaggedObject( true, PRIVATE_KEY_TAG,new DEROctetString( privateKey )));
        }
    }

    @Override
    /***
     * Encrypts a byte array using the key contained in this object.
     *
     * @param b the plain text byte array to encrypt
     * @return the encrypted byte array including padding
     */
    public byte[] encrypt(byte[] b) throws IOException {
        try {
            KeyPair key = getKeyPair();
            Cipher cipher = getCipher();
            java.security.Key k = key.getPublic();
            cipher.init( Cipher.ENCRYPT_MODE, k );
            return cipher.doFinal( b );
        } catch (InvalidKeySpecException e) {
            throw new IOException( "Exception while getting key pair", e );
        } catch (NoSuchAlgorithmException|NoSuchPaddingException|NoSuchProviderException|IllegalBlockSizeException|BadPaddingException e) {
            throw new IOException( "Exception while encrypting (len: "+b.length+")", e );
        } catch (InvalidKeyException e) {
            throw new IOException( "Exception while init of cipher ", e );
        }
    }

    @Override
    /***
     * Decrypts a byte array using the key contained in this object.
     *
     * @param b the encrypted byte array
     * @return the plain text byte array
     */
    public byte[] decrypt(byte[] b) throws IOException {
        try {
            KeyPair key = getKeyPair();
            Cipher cipher = getCipher();
            java.security.Key k = key.getPrivate();
            cipher.init( Cipher.DECRYPT_MODE, k );
            return cipher.doFinal( b );
        } catch (InvalidKeySpecException | NoSuchAlgorithmException | NoSuchPaddingException | NoSuchProviderException | InvalidKeyException | IllegalBlockSizeException | BadPaddingException e) {
            throw new IOException( "Exception while decrypting (len: "+b.length+")", e );
        }

    }

    /***
     * Signs a byte array.
     *
     * This method uses the default hashing algorithm.
     *
     * @param b the byte array to be signed
     * @return the signature
     * @throws IOException if unable to carry out signature
     */
    public byte[] sign(byte[] b) throws IOException {
        return sign( b, Algorithm.getDefault( AlgorithmType.HASHING ) );
    }

    /***
     * Signs a byte array.
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
            signature = getSignature( mac );
            signature.initSign( key.getPrivate() );
            signature.update( b );
            return signature.sign();
        } catch (SignatureException | InvalidKeySpecException | NoSuchAlgorithmException | NoSuchPaddingException | NoSuchProviderException | InvalidKeyException e) {
            throw new IOException( "Exception while encrypting", e );
        }
    }

    /***
     * Verifies a given signature accourding to the objects public key.
     *
     * @param b the byte array representing the message
     * @param sig the byte array representing the signature
     * @return true if signature could be verified successfully
     * @throws IOException if signature processing failed
     */
    public boolean verify(byte[] b, byte[] sig) throws IOException {
        return verify( b, sig, Algorithm.getDefault( AlgorithmType.HASHING ) );
    }

    /***
     * Verifies a given signature accourding to the objects public key.
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
            signature = getSignature( mac );
            signature.initVerify( key.getPublic() );
            signature.update( b );
            return signature.verify( sig );
        } catch (SignatureException | InvalidKeySpecException | NoSuchAlgorithmException | NoSuchPaddingException | NoSuchProviderException | InvalidKeyException e) {
            throw new IOException( "Exception while verifying signature", e );
        }
    }

    private KeyFactory getKeyFactory() throws NoSuchAlgorithmException,NoSuchProviderException {
        if(parameters.get(Parameter.ALGORITHM).startsWith( Algorithm.EC.toString() )) {
            return KeyFactory.getInstance( "ECDSA", "BC" );
        } else {
            Algorithm alg=Algorithm.getByString(parameters.get(Parameter.ALGORITHM));
            return KeyFactory.getInstance( alg.toString(),alg.getProvider() );
        }
    }

    /***
     * Sets the probability of reusing a precalculated key again.
     *
     * This capability is used to reduce cpu load during tests.
     *
     * @param probability the new probability to be set
     * @return the previously set probability
     */
    public static double setDequeueProbability(double probability) {
        return AsymmetricKeyPreCalculator.setDequeueProbability(probability);
    }

    /***
     * Gets the current probability for dequeing a used key (nolrmally 1.0)
     * @return the current probability set
     */
    public static double getDequeueProbability() {
        return AsymmetricKeyPreCalculator.getDequeueProbability();
    }

    private KeyPair getKeyPair() throws NoSuchAlgorithmException,NoSuchProviderException,InvalidKeySpecException {
        KeyFactory kf=getKeyFactory();

        // Getting public key
        PublicKey lPublicKey = kf.generatePublic(new X509EncodedKeySpec(this.publicKey));

        // getting stored private key
        PrivateKey lPrivateKey = null;
        if(this.privateKey!=null) {
            lPrivateKey = kf.generatePrivate(new PKCS8EncodedKeySpec(this.privateKey));
        }

        // build result and return
        return new KeyPair( lPublicKey,lPrivateKey );
    }

    private Cipher getCipher() throws NoSuchPaddingException,NoSuchAlgorithmException,NoSuchProviderException {
        Algorithm alg=getAlgorithm();
        if(alg==Algorithm.EC) {
            return Cipher.getInstance( alg.getAlgorithmFamily(),alg.getProvider());
        } else {
            return Cipher.getInstance(alg+"/"+getMode()+"/"+getPadding().toString(),alg.getProvider());
        }
    }

    private Signature getSignature(Algorithm a) throws NoSuchPaddingException, NoSuchAlgorithmException, NoSuchProviderException {
        Algorithm alg=getAlgorithm();
        if ( alg==Algorithm.EC ) {
            return Signature.getInstance( a.toString() + "WithECDSA", alg.getProvider() );
        } else {
            return Signature.getInstance( a.toString() + "With" + alg.toString(),alg.getProvider() );
        }
    }

    /***
     * Sets the public key.
     *
     * @param b the byte array representing the public key
     * @return the previously set public key
     * @throws NullPointerException if key was tried to set to null
     */
    public byte[] setPublicKey(byte[] b) throws InvalidKeyException {
        if(b==null) {
            throw new NullPointerException( "Public key may not be null" );
        }
        byte[] old=publicKey;
        publicKey=b;
        return old;
    }

    /***
     * Gets the public key in binary representation.
     * @return the public key
     */
    public byte[] getPublicKey() {return publicKey; }

    /***
     * Sets the private key of this object.
     *
     * @param b the byte representation of the key to be set.
     * @return the previously set private key
     */
    public byte[] setPrivateKey(byte[] b) {
        byte[] old=privateKey;
        privateKey=b;
        return old;
    }

    /***
     * Gets the private key of this object.
     *
     * @return the pyte representation of the private key
     */
    public byte[] getPrivateKey() {return privateKey; }

    /***
     * Gets the algorithm of this key type.
     *
     * @return the algorithm used for generation
     */
    public Algorithm getAlgorithm() {return Algorithm.getByString(parameters.get(Parameter.ALGORITHM)); }

    /***
     * Gets the padding used for encryption.
     *
     * @return the padding which is used for encryption
     */
    public Padding getPadding() {
        Padding padding=Padding.getByString(parameters.get(Parameter.PADDING));
        return padding==null?Padding.getDefault( AlgorithmType.ASYMMETRIC ):padding;
    }

    /***
     * Sets the padding used for encryption.
     *
     * @param p the padding to be set
     * @return the previously set padding
     */
    public Padding setPadding(Padding p) {
        Padding old=getPadding();
        parameters.put(Parameter.PADDING.getId(),p.toString());
        return old;
    }

    /***
     * Gets the size of the key stored in this object.
     * @return the key size in bits
     */
    public int getKeySize() {
        return Integer.parseInt(parameters.get(Parameter.KEYSIZE));
    }

    /***
     * Gets the mode used for encryption.
     *
     * @return the mode set used for encryption
     */
    public Mode getMode() {
        return Mode.getByString(parameters.get(Parameter.MODE));
    }

    /***
     * Sets the mode used for encryption.
     *
     * @param m the mode to be set
     * @return the mode previously set
     */
    public Mode setMode(Mode m) {
        Mode old=getMode();
        parameters.put(Parameter.MODE.getId(),m.toString());
        return old;
    }

    /***
     * tests two asymmetric keys for equality.
     *
     * Two keys are considered equal if they contain the same parameters and the same keys (public and private)
     *
     * @param key the other key
     * @return true if both keys are considered equivalent
     */
    @Override
    public boolean equals(Object key) {
        // make sure object is not null
        if(key==null) {
            return false;
        }

        //make sure object is of right type
        if(! (key instanceof AsymmetricKey)) {
            return false;
        }

        // compare public keys
        AsymmetricKey o=(AsymmetricKey)key;
        return dumpValueNotation("",DumpType.ALL_UNENCRYPTED).equals(o.dumpValueNotation("",DumpType.ALL_UNENCRYPTED));

    }

    public AsymmetricKey clone() {
        try {
            return new AsymmetricKey(this.toASN1Object(DumpType.ALL));
        } catch( IOException ioe) {
            return null;
        }
    }

    /***
     * returns the hashcode of the dump representation.
     */
    @Override
    public int hashCode() {
        return dumpValueNotation("",DumpType.ALL_UNENCRYPTED).hashCode();
    }

    /***
     * Gets a textual representation of the objects parameters (without the keys)
     * @return the string
     */
    @Override
    public String toString() {
        return "([AsymmetricKey]hash="+(privateKey!=null?Arrays.hashCode(privateKey):"null")+"/"+(publicKey!=null?Arrays.hashCode(publicKey):"null")+";"+parameters.toString()+")";
    }

}
