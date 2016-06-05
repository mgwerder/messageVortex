package net.gwerder.java.mailvortex.asn1;

import de.flexiprovider.core.FlexiCoreProvider;
import de.flexiprovider.ec.FlexiECProvider;
import de.flexiprovider.ec.parameters.CurveParams;
import de.flexiprovider.ec.parameters.CurveRegistry;
import net.gwerder.java.mailvortex.asn1.encryption.*;
import org.bouncycastle.asn1.*;

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
 * Asymmetic Key Handling.
 *
 * This class parses and encodes Asymmetric keys from/to ASN.1.
 * It furthermore handles encoding and decoding of encrypted material.
 *
 * Created by martin.gwerder on 19.04.2016.
 */
public class AsymmetricKey extends Key {

    private static SecureRandom secureRandom = new SecureRandom();

    static {
        Security.addProvider(new org.bouncycastle.jce.provider.BouncyCastleProvider());
        Security.addProvider( new FlexiCoreProvider() );
        Security.addProvider( new FlexiECProvider() );
    }

    protected byte[]  publicKey  = null;
    protected byte[]  privateKey = null;
    private Mode mode = Mode.getDefault();
    private Padding padding = Padding.getDefault( AlgorithmType.ASYMMETRIC );
    public AsymmetricKey(byte[] b) {
        this(ASN1Sequence.getInstance( b ));
    }


    public AsymmetricKey(ASN1Encodable to) {
        parse(to);
    }

    public AsymmetricKey() throws IOException {
        this( Algorithm.getDefault( AlgorithmType.ASYMMETRIC ), Padding.getDefault( AlgorithmType.ASYMMETRIC ), Algorithm.getDefault( AlgorithmType.ASYMMETRIC ).getKeySize() );
    }

    public AsymmetricKey(Algorithm alg, Padding p, int keysize) throws IOException {
        if(alg==null) {
            throw new IOException( "Algorithm null is not encodable by the system" );
        }
        Map<String,Integer> pm= new HashMap<>();
        pm.put(""+ Parameter.KEYSIZE.getId()+"_0",keysize);
        padding=p;
        try {
            createKey( alg, pm );
        } catch (NoSuchAlgorithmException | NoSuchProviderException | InvalidAlgorithmParameterException | InvalidParameterSpecException e) {
            throw new IOException( "Exception while creating key", e );
        }
    }

    public AsymmetricKey(Algorithm alg, Map<String, Integer> params) throws NoSuchAlgorithmException, NoSuchProviderException, InvalidAlgorithmParameterException, InvalidParameterSpecException {
        // store algorithm and parameters
        createKey( alg, params );
    }

    private void createKey(Algorithm alg, Map<String, Integer> params) throws NoSuchAlgorithmException, NoSuchProviderException, InvalidAlgorithmParameterException, InvalidParameterSpecException {
        this.keytype=alg;
        if(params!=null) {
            this.parameters.putAll(params);
        }

        // create key pair
        if("RSA".equals(alg.getAlgorithm()) || "EC".equals(alg.getAlgorithm())) {
            int keysize=parameters.get("" + Parameter.KEYSIZE.getId() + "_0");
            KeyPairGenerator keyGen = KeyPairGenerator.getInstance(alg.toString().toUpperCase(),alg.getProvider());
            keyGen.initialize(keysize);
            KeyPair pair;
            try {
                pair = keyGen.genKeyPair();
            } catch (IllegalStateException ise) {
                throw new IllegalStateException( "unable to generate keys with " + alg.getAlgorithm() + "/" + mode + "/" + padding.getPadding() + " (size "+keysize+")", ise );
            }
            publicKey = pair.getPublic().getEncoded();
            privateKey = pair.getPrivate().getEncoded();
        } else if (alg.toString().toLowerCase().startsWith( "sec" )) {
            CurveParams parameters = new CurveRegistry.Secp384r1();
            KeyPairGenerator g = KeyPairGenerator.getInstance( "EC", "FlexiEC" );
            g.initialize( parameters, secureRandom );
            KeyPair pair = g.generateKeyPair();
            publicKey = pair.getPublic().getEncoded();
            privateKey = pair.getPrivate().getEncoded();
        } else {
            throw new NoSuchAlgorithmException("Encountered unknown parameter \""+alg.getAlgorithm()+"\"");
        }

    }

    protected void parse(ASN1Encodable to) {
        ASN1Sequence s1 = ASN1Sequence.getInstance(to);
        // parsing asymetric Key Idetifier
        int i=0;
        parseKeyParameter(ASN1Sequence.getInstance( s1.getObjectAt(i++) ));
        publicKey=((ASN1OctetString)(s1.getObjectAt(i++))).getOctets();
        if(s1.size()>i) {
            privateKey=((ASN1OctetString)(((DERTaggedObject)(s1.getObjectAt(i++))).getObject())).getOctets();
        }
    }

    public boolean hasPrivateKey() { return privateKey!=null; }

    public String dumpValueNotation(String prefix) {
        return dumpValueNotation(prefix,DumpType.PUBLIC_ONLY);
    }

    public String dumpValueNotation(String prefix,DumpType dt) {
        StringBuilder sb=new StringBuilder();
        sb.append("{"+CRLF);
        if(publicKey!=null && (dt==DumpType.ALL || dt==DumpType.PUBLIC_ONLY || dt==DumpType.PUBLIC_COMMENTED || dt==DumpType.PRIVATE_COMMENTED)) {
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
        if(privateKey!=null && (dt==DumpType.PRIVATE_COMMENTED || dt==DumpType.PRIVATE_ONLY || dt==DumpType.ALL)) {
            String s=toHex(privateKey);
            sb.append(prefix).append("  ");
            if(dt==DumpType.PRIVATE_COMMENTED) {
                sb.append("-- ");
            }
            sb.append("privateKey ").append(s).append(CRLF);
        } else sb.append(CRLF);
        sb.append(prefix).append("}");
        return sb.toString();
    }

    public ASN1Object toASN1Object() throws IOException{
        if(publicKey==null) {
            throw new IOException("publicKey may not be null when dumping");
        }
        ASN1EncodableVector v =new ASN1EncodableVector();
        v.add(encodeKeyParameter());
        v.add( new DEROctetString(publicKey) );
        if(privateKey!=null) {
            v.add(new DERTaggedObject( true,0,new DEROctetString( privateKey )));
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
            throw new IOException( "Exception while encrypting", e );
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
        /*try {
            KeyPair key = getKeyPair();
            MessageDigest dig = MessageDigest.getInstance( mac.getAlgorithm() );
            byte[] hash = dig.digest(b);
            Signature s;
            if(keytype.getAlgorithm().startsWith("sec")) {
                s=Signature.getInstance( "NONEWithECDSA","BC" );
            } else {
                s=Signature.getInstance("NONEWith"+keytype.getAlgorithm());
            }
            s.initVerify( key.getPublic() );
            s.update(hash);
            return s.verify(sig);
        } catch(NoSuchAlgorithmException|InvalidKeyException|NoSuchProviderException|InvalidKeySpecException|SignatureException e) {
            throw new IOException(e);
        }*/
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
        if(keytype.getAlgorithm().startsWith( "sec" )) {
            return KeyFactory.getInstance( "ECDSA", "BC" );
        } else {
            return KeyFactory.getInstance( keytype.getAlgorithm() );
        }
    }

    private KeyPair getKeyPair() throws NoSuchAlgorithmException,NoSuchProviderException,InvalidKeySpecException {
        KeyFactory kf=getKeyFactory();
        PKCS8EncodedKeySpec rks = null;
        PrivateKey priv = null;
        if(privateKey!=null) {
            rks  = new PKCS8EncodedKeySpec(privateKey);
            priv = kf.generatePrivate(rks);
        }
        X509EncodedKeySpec uks=null;
        PublicKey pub = null;
        if(publicKey!=null) {
            uks = new X509EncodedKeySpec(publicKey);
            pub = kf.generatePublic(uks);
        }
        return new KeyPair( pub,priv );
    }

    private Cipher getCipher() throws NoSuchPaddingException,NoSuchAlgorithmException,NoSuchProviderException {
        if(keytype.getAlgorithm().startsWith("sec")) {
            return Cipher.getInstance( "ECIES","BC" );
        } else {
            return Cipher.getInstance(keytype.getAlgorithm()+"/"+mode+"/"+padding.getPadding());
        }
    }

    private Signature getSignature(Algorithm a) throws NoSuchPaddingException, NoSuchAlgorithmException, NoSuchProviderException {
        if (keytype.getAlgorithm().startsWith( "sec" )) {
            return Signature.getInstance( a.getAlgorithm() + "WithECDSA", "BC" );
        } else {
            return Signature.getInstance( a.getAlgorithm() + "With" + keytype.getAlgorithm() );
        }
    }

    public boolean equals(AsymmetricKey ak) {
        return Arrays.equals(publicKey,ak.publicKey);
    }

    public byte[] setPublicKey(byte[] b) throws InvalidKeyException {
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

    public Padding getPadding() {return padding; }

    public enum DumpType {
        ALL,
        PUBLIC_ONLY,
        PUBLIC_COMMENTED,
        PRIVATE_ONLY,
        PRIVATE_COMMENTED,
    }

}
