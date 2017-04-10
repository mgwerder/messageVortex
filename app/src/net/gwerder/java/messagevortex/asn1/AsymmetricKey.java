package net.gwerder.java.messagevortex.asn1;

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
import java.util.HashMap;
import java.util.Map;

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

    final static String ECC="ECIES";

    static {
        Security.addProvider(new org.bouncycastle.jce.provider.BouncyCastleProvider());
    }

    protected byte[]  publicKey  = null;
    protected byte[]  privateKey = null;
    protected Algorithm mac=Algorithm.getDefault( AlgorithmType.HASHING );

    public AsymmetricKey(byte[] b) throws IOException {
        this(ASN1Sequence.getInstance( b ));
        selftest();
    }

    public AsymmetricKey(ASN1Encodable to) throws IOException  {
        parse(to);
        selftest();
    }

    public AsymmetricKey() throws IOException {
        this( Algorithm.getDefault( AlgorithmType.ASYMMETRIC ), Algorithm.getDefault( AlgorithmType.ASYMMETRIC ).getKeySize(),null );
        selftest();
    }

    public AsymmetricKey(Algorithm alg, int keySize ,Map<String,Object> params) throws IOException {
        if(this.parameters==null) {
            this.parameters=new HashMap<>();
        }
        if(params!=null) {
            this.parameters.putAll( params );
        }
        if(alg==null) {
            throw new IOException( "Algorithm null is not encodable by the system" );
        }
        this.mode=null;
        this.padding=null;
        this.parameters.remove(Parameter.PADDING.toString()+"_0");
        this.parameters.put(Parameter.KEYSIZE.toString()+"_0",keySize);
        this.parameters.remove(Parameter.MODE.toString()+"_0");
        try {
            createKey( alg, parameters );
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

    public boolean equals(Object t) {
        // make sure object is not null
        if(t==null) {
            return false;
        }

        //make sure object is of right type
        if(! (t instanceof AsymmetricKey)) {
            return false;
        }

        // compare public keys
        AsymmetricKey o=(AsymmetricKey)t;
        if(!Arrays.equals(o.publicKey,publicKey)) {
            return false;
        }

        // compare private keys
        if(!Arrays.equals(o.privateKey,privateKey)) {
            return false;
        }

        // compare mode (CBC, ECB et al)
        if(o.mode!=mode) {
            return false;
        }

        // compare padding
        if(!o.getPadding().equals(getPadding())) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        return super.hashCode();
    }

    private void createKey(Algorithm alg, Map<String, Object> params) throws NoSuchAlgorithmException, NoSuchProviderException, InvalidAlgorithmParameterException, InvalidParameterSpecException {
        this.keytype=alg;
        if(params!=null) {
            this.parameters.putAll(params);
        }

        // create key pair
        if("RSA".equals(alg.getAlgorithm()) ) {
            int keySize=(Integer)(parameters.get("" + Parameter.KEYSIZE.toString() + "_0"));
            KeyPairGenerator keyGen = KeyPairGenerator.getInstance(alg.toString().toUpperCase(),alg.getProvider());
            keyGen.initialize(keySize);
            KeyPair pair;
            try {
                pair = keyGen.genKeyPair();
            } catch (IllegalStateException ise) {
                throw new IllegalStateException( "unable to generate keys with " + alg.getAlgorithm() + "/" + mode + "/" + padding.getPadding() + " (size "+keySize+")", ise );
            }
            publicKey  = pair.getPublic().getEncoded();
            privateKey = pair.getPrivate().getEncoded();
        } else if (ECC.equals( alg.getAlgorithm() )) {
            if(params.get("curveType_0")==null) {
                throw new NoSuchAlgorithmException( "curve type is not set" );
            }
            ECParameterSpec parameters = ECNamedCurveTable.getParameterSpec( (String)(params.get("curveType_0")) );
            KeyPairGenerator g = KeyPairGenerator.getInstance( ECC, "BC" );
            g.initialize( parameters, esr.getSecureRandom() );
            KeyPair pair = g.generateKeyPair();
            publicKey  = pair.getPublic().getEncoded();
            privateKey = pair.getPrivate().getEncoded();
        } else {
            throw new NoSuchAlgorithmException("Encountered unknown parameter \""+alg.getAlgorithm()+"\" ("+getParameterString()+")");
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

    public boolean hasPrivateKey() { return privateKey!=null; }

    public String dumpValueNotation(String prefix) {
        return dumpValueNotation(prefix,DumpType.PUBLIC_COMMENTED);
    }

    public String dumpValueNotation(String prefix,DumpType dt) {
        StringBuilder sb=new StringBuilder();
        sb.append("{"+CRLF);
        if(publicKey!=null && (dt.dumpPublicKey() || DumpType.PUBLIC_COMMENTED==dt)) {
            sb.append( dumpKeyTypeValueNotation( prefix ) );
            String s = toHex( publicKey );
            sb.append( prefix ).append( "  " );
            if(dt==DumpType.PUBLIC_COMMENTED) {
                sb.append( "-- " );
            }
            sb.append( "publicKey " + s );
            if (dt == DumpType.ALL) {
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

    public ASN1Object toASN1Object() throws IOException{
        return toASN1Object( DumpType.ALL );
    }

    public ASN1Object toASN1Object(DumpType dt) throws IOException {
        if(publicKey==null) {
            throw new IOException("publicKey may not be null when dumping");
        }
        ASN1EncodableVector v =new ASN1EncodableVector();
        v.add(encodeKeyParameter());
        if(dt.dumpPublicKey()) {
            v.add(new DERTaggedObject( true,PUBLIC_KEY,new DEROctetString( publicKey )));
        }
        if (privateKey != null && dt.dumpPrivateKey()) {
            v.add(new DERTaggedObject( true,PRIVATE_KEY,new DEROctetString( privateKey )));
        }
        return new DERSequence( v );
    }

    @Override
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

    public byte[] sign(byte[] b) throws IOException {
        return sign( b, Algorithm.getDefault( AlgorithmType.HASHING ) );
    }

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

    public boolean verify(byte[] b, byte[] sig) throws IOException {
        return verify( b, sig, Algorithm.getDefault( AlgorithmType.HASHING ) );
    }

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
        if(keytype.getAlgorithm().startsWith( Algorithm.EC.toString() )) {
            return KeyFactory.getInstance( "ECDSA", "BC" );
        } else {
            return KeyFactory.getInstance( keytype.getAlgorithm() );
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
        if(keytype.getAlgorithm().equals(Algorithm.EC.getAlgorithm())) {
            return Cipher.getInstance( ECC,"BC");
        } else {
            return Cipher.getInstance(keytype.getAlgorithm()+"/"+getMode()+"/"+getPadding().getPadding());
        }
    }

    private Signature getSignature(Algorithm a) throws NoSuchPaddingException, NoSuchAlgorithmException, NoSuchProviderException {
        if (keytype.getAlgorithm().equals( ECC )) {
            return Signature.getInstance( mac.getAlgorithm() + "WithECDSA", "BC" );
        } else {
            return Signature.getInstance( a.getAlgorithm() + "With" + keytype.getAlgorithm() );
        }
    }

    public byte[] setPublicKey(byte[] b) throws InvalidKeyException {
        if(b==null) {
            throw new NullPointerException( "Public key may not be null" );
        }
        byte[] old=publicKey;
        publicKey=b;
        return old;
    }

    public byte[] getPublicKey() {return publicKey; }

    public byte[] setPrivateKey(byte[] b) throws InvalidKeyException {
        byte[] old=privateKey;
        privateKey=b;
        return old;
    }

    public byte[] getPrivateKey() {return privateKey; }

    public Algorithm getAlgorithm() {return keytype; }

    public Padding getPadding() {
        return padding==null?Padding.getDefault( AlgorithmType.ASYMMETRIC ):padding;
    }

    public Padding setPadding(Padding p) {
        Padding old=padding;
        padding=p;
        return old;
    }

    public int getKeySize() {
        return (Integer)(this.parameters.get(Parameter.KEYSIZE.toString()+"_0"));
    }

    public Mode getMode() {
        return mode==null?Mode.getDefault( AlgorithmType.ASYMMETRIC ):mode;
    }

    public String toString() {
        return "([AsymmetricKey]"+keytype+"/"+parameters.get(""+ Parameter.KEYSIZE.toString()+"_0")+"/"+mode+"/"+getPadding().toString()+")";
    }

}