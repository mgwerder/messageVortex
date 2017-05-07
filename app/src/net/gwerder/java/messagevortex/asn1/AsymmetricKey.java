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
import java.security.spec.InvalidParameterSpecException;
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
    private static int PUBLIC_KEY = 1;
    private static int PRIVATE_KEY = 2;

    static {
        Security.addProvider(new org.bouncycastle.jce.provider.BouncyCastleProvider());
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
     * @param params   the parameters to be used
     * @throws IOException if the key can not be generated with the given parameters
     */
    //public AsymmetricKey(Algorithm alg, Map<String,Object> params) throws IOException {
    public AsymmetricKey(AlgorithmParameter params) throws IOException {
        Algorithm alg=Algorithm.getByString(params.get(Parameter.ALGORITHM.getId()));
        if(this.parameters==null) {
            this.parameters=new AlgorithmParameter();
        }
        if(params!=null) {
            this.parameters=params;
        }
        if(params.get(Parameter.ALGORITHM)==null) {
            throw new IOException( "Algorithm null is not encodable by the system" );
        }

        try {
            createKey( parameters );
        } catch (NoSuchAlgorithmException | NoSuchProviderException | InvalidAlgorithmParameterException | InvalidParameterSpecException e) {
            throw new IOException( "Exception while creating key", e );
        }
        selftest();
    }

    private void selftest() throws IOException {
        if(publicKey==null) {
            throw new IOException( "selftest failed: Public key may not be null");
        }
    }

    /***
     * tests two asymmetric keys for equality.
     *
     * Two keys are considered equal if they contain the same parameters and the same keys (public and private)
     *
     * @param key the other key
     * @return true if both keys are considered equivalent
     */
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
        if(!Arrays.equals(o.publicKey,publicKey)) {
            return false;
        }

        // compare private keys
        if(!Arrays.equals(o.privateKey,privateKey)) {
            return false;
        }

        // compare padding
        if(!o.getPadding().equals(getPadding())) {
            return false;
        }

        return true;
    }

    @Override
    /***
     * returns the hashcode of the dump representation.
     */
    public int hashCode() {
        return dumpValueNotation("",DumpType.ALL).hashCode();
    }

    private void createKey(AlgorithmParameter params) throws NoSuchAlgorithmException, NoSuchProviderException, InvalidAlgorithmParameterException, InvalidParameterSpecException {
        if(params!=null) {
            this.parameters=params;
        }

        // create key pair
        Algorithm alg=Algorithm.getByString(params.get(Parameter.ALGORITHM.getId()));
        if(alg==null) {
            throw new NoSuchAlgorithmException("unknown algorithm is set in parameter list ("+params.get(Parameter.ALGORITHM.getId())+")");
        }
        if("RSA".equals(alg.name()) ) {
            int keySize=getKeySize();
            KeyPairGenerator keyGen = KeyPairGenerator.getInstance(alg.toString(),alg.getProvider());
            keyGen.initialize(keySize);
            KeyPair pair;
            try {
                pair = keyGen.genKeyPair();
            } catch (IllegalStateException ise) {
                throw new IllegalStateException( "unable to generate keys with " + alg.toString() + "/" + params.get(Parameter.MODE) + "/" + params.get(Parameter.PADDING) + " (size "+keySize+")", ise );
            }
            publicKey  = pair.getPublic().getEncoded();
            privateKey = pair.getPrivate().getEncoded();
        } else if (alg==Algorithm.EC) {
            if(params.get(Parameter.CURVETYPE)==null) {
                throw new NoSuchAlgorithmException( "curve type is not set" );
            }
            ECParameterSpec parameters = ECNamedCurveTable.getParameterSpec( params.get(Parameter.CURVETYPE) );
            KeyPairGenerator g = KeyPairGenerator.getInstance( alg.getAlgorithmFamily(), "BC" );
            g.initialize( parameters, esr.getSecureRandom() );
            KeyPair pair = g.generateKeyPair();
            publicKey  = pair.getPublic().getEncoded();
            privateKey = pair.getPrivate().getEncoded();
        } else {
            throw new NoSuchAlgorithmException("Encountered unsupported algorithm \""+alg.toString()+"\"");
        }
    }

    protected void parse(ASN1Encodable to) throws IOException{
        ASN1Sequence s1 = ASN1Sequence.getInstance(to);
        // parsing asymmetric key identifier
        int i=0;
        parseKeyParameter(ASN1Sequence.getInstance( s1.getObjectAt(i++) ));
        DERTaggedObject tagged=(DERTaggedObject)(s1.getObjectAt(i++));
        if(tagged.getTagNo()!=PUBLIC_KEY) {
            throw new IOException("encountered wrong tag number (expected: "+PUBLIC_KEY+"; got:"+tagged.getTagNo()+")");
        }
        publicKey=ASN1OctetString.getInstance(tagged.getObject()).getOctets();
        if(s1.size()>i) {
            tagged=(DERTaggedObject)(s1.getObjectAt(i++));
            if(tagged.getTagNo()!=PRIVATE_KEY) {
                throw new IOException("encountered wrong tag number (expected: "+PRIVATE_KEY+"; got:"+tagged.getTagNo()+")");
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
     * @param dt     the dump type to be used (normally DumpType.PUBLIC_ONLY)
     * @return the string representation of the ASN1 dump
     */
    public String dumpValueNotation(String prefix,DumpType dt) {
        StringBuilder sb=new StringBuilder();
        sb.append("{"+CRLF);
        sb.append( dumpKeyTypeValueNotation( prefix ) );
        sb.append( "," ).append(CRLF);
        if(publicKey!=null && dt.dumpPublicKey()) {
            String s = toHex( publicKey );
            sb.append( prefix ).append( "  publicKey " + s );
            if (dt.dumpPrivateKey()) {
                sb.append( "," );
            }
            sb.append( CRLF );
        }

        // dump private key
        if(privateKey!=null && (dt==DumpType.PRIVATE_COMMENTED || dt.dumpPrivateKey())) {
            String s=toHex(privateKey);
            sb.append(prefix).append("  ");
            if(dt==DumpType.PRIVATE_COMMENTED) {
                sb.append("-- ");
            }
            sb.append("privateKey ").append(s).append(CRLF);
        } else {
            sb.append( CRLF );
        }
        sb.append(prefix).append("}");
        return sb.toString();
    }

    /***
     * Dumps the key as ASN1 object.
     *
     * @return the ASN1 object suitable for encoding
     * @throws IOException if not encodable
     */
    public ASN1Object toASN1Object() throws IOException {
        return toASN1Object( DumpType.ALL );
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
        addToASN1Parameter(v);
        // add public key
        addToASN1PublicKey(v,dt);
        // add private key
        addToASN1PrivateKey(v,dt);

        return new DERSequence( v );
    }

    private void addToASN1Parameter(ASN1EncodableVector v ) throws IOException {
        v.add(encodeKeyParameter());
    }

    private void addToASN1PublicKey(ASN1EncodableVector v, DumpType dt ) {
        if(dt.dumpPublicKey()) {
            v.add(new DERTaggedObject( true,PUBLIC_KEY,new DEROctetString( publicKey )));
        }
    }

    private void addToASN1PrivateKey(ASN1EncodableVector v, DumpType dt ) {
        if (privateKey != null && dt.dumpPrivateKey()) {
            v.add(new DERTaggedObject( true,PRIVATE_KEY,new DEROctetString( privateKey )));
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
            throw new IOException( "Exception while encrypting", e );
        } catch (InvalidKeyException e) {
            throw new IOException( "Exception while init of cipher", e );
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
            throw new IOException( "Exception while decrypting", e );
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
            return Cipher.getInstance( alg.getAlgorithmFamily(),"BC");
        } else {
            return Cipher.getInstance(alg+"/"+getMode()+"/"+getPadding().toString());
        }
    }

    private Signature getSignature(Algorithm a) throws NoSuchPaddingException, NoSuchAlgorithmException, NoSuchProviderException {
        Algorithm alg=getAlgorithm();
        if ( alg==Algorithm.EC ) {
            return Signature.getInstance( a.toString() + "WithECDSA", "BC" );
        } else {
            return Signature.getInstance( a.toString() + "With" + alg.toString() );
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
     * Gets a textual representation of the objects parameters (without the keys)
     * @return the string
     */
    public String toString() {
        return "([AsymmetricKey]"+getAlgorithm()+"/"+getKeySize()+"/"+getMode()+"/"+getPadding().toString()+")";
    }

}
