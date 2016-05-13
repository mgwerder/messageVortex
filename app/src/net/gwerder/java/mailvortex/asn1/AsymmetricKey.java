package net.gwerder.java.mailvortex.asn1;

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
import java.util.HashMap;
import java.util.Map;

/**
 * Created by martin.gwerder on 19.04.2016.
 */
public class AsymmetricKey extends Key {

    public static enum DumpType {
        ALL,
        PUBLIC_ONLY,
        PUBLIC_COMMENTED,
        PRIVATE_ONLY,
        PRIVATE_COMMENTED,
    };

    static {
        Security.addProvider(new org.bouncycastle.jce.provider.BouncyCastleProvider());
    }

    protected ASN1BitString publicKey = null;
    protected ASN1BitString privateKey = null;


    public AsymmetricKey(Algorithm alg, int keysize) throws NoSuchAlgorithmException, NoSuchProviderException, InvalidAlgorithmParameterException {
        if(alg==null) throw new NoSuchAlgorithmException( "Algorithm null is not encodable by the system" );
        Map<String,Integer> pm= new HashMap();
        pm.put(""+Parameter.KEYSIZE.getId()+"_0",keysize);
        createKey(alg,pm);
    }

    public AsymmetricKey(Algorithm alg, Map<String,Integer> params) throws NoSuchAlgorithmException, NoSuchProviderException, InvalidAlgorithmParameterException {
        // store algorithm and parameters
        createKey( alg, params );
    }

    private void createKey(Algorithm alg, Map<String,Integer> params) throws NoSuchAlgorithmException, NoSuchProviderException, InvalidAlgorithmParameterException  {
        this.keytype=alg;
        this.parameters.putAll(params);

        // create key pair
        if(alg.toString().equals("rsa") || alg.toString().equals("dsa")) {
            KeyPairGenerator keyGen = KeyPairGenerator.getInstance(alg.toString().toUpperCase());
            keyGen.initialize(parameters.get("" + Parameter.KEYSIZE.getId() + "_0"));
            KeyPair pair = keyGen.genKeyPair();
            publicKey = new DLBitString(pair.getPublic().getEncoded(), 0);
            privateKey = new DLBitString(pair.getPrivate().getEncoded(), 0);
        } else if(alg.toString().startsWith("sec")) {
            ECParameterSpec ecSpec = ECNamedCurveTable.getParameterSpec(alg.toString());
            KeyPairGenerator g = KeyPairGenerator.getInstance("ECDSA", "BC");
            g.initialize(ecSpec, new SecureRandom());
            KeyPair pair = g.generateKeyPair();
            publicKey = new DLBitString(pair.getPublic().getEncoded(), 0);
            privateKey = new DLBitString(pair.getPrivate().getEncoded(), 0);
        } else throw new NoSuchAlgorithmException("Encountered unknown parameter \""+alg.toString()+"\"");

    }

    public AsymmetricKey(byte[] b) {
        this(ASN1Sequence.getInstance( b ));
    }
    public AsymmetricKey(ASN1Encodable to) {
        parse(to);
    }

    protected void parse(ASN1Encodable to) {
        ASN1Sequence s1 = ASN1Sequence.getInstance(to);
        // parsing asymetric Key Idetifier
        int i=0;
        parseKeyParameter(ASN1Sequence.getInstance( s1.getObjectAt(i++) ));
        publicKey=((ASN1BitString)(s1.getObjectAt(i++)));
        if(s1.size()>i) {
            privateKey=((ASN1BitString)(((DERTaggedObject)(s1.getObjectAt(i++))).getObject()));
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
            String s = null;
            try {
                s = toHex( publicKey.getOctets() );
            } catch (IllegalStateException ise) {
                s = toBitString( publicKey );
            }
            sb.append( prefix + "  " );
            if(dt==DumpType.PUBLIC_COMMENTED) sb.append( "-- " );
            sb.append( "publicKey " + s );
            if (dt == DumpType.ALL) {
                sb.append( "," );
            }
            sb.append( CRLF );
        }
        if(privateKey!=null && (dt==DumpType.PRIVATE_COMMENTED || dt==DumpType.PRIVATE_ONLY || dt==DumpType.ALL)) {
            String s=null;
            try{
                s=toHex(privateKey.getOctets());
            } catch(IllegalStateException ise) {
                s=toBitString(privateKey);
            }
            sb.append(prefix+"  ");
            if(dt==DumpType.PRIVATE_COMMENTED) {
                sb.append("-- ");
            }
            sb.append("privateKey "+s+CRLF);
        } else sb.append(CRLF);
        sb.append(prefix+"}");
        return sb.toString();
    }

    public ASN1Object toASN1Object() throws IOException{
        if(publicKey==null) throw new IOException("publicKey may not be null when dumping");
        ASN1EncodableVector v =new ASN1EncodableVector();
        v.add(encodeKeyParameter());
        v.add(publicKey);
        if(privateKey!=null) v.add(new DERTaggedObject( true,0,privateKey));
        return new DERSequence( v );
    }

    @Override
    public byte[] encrypt(byte[] b) throws NoSuchAlgorithmException,NoSuchPaddingException,InvalidKeyException,IllegalBlockSizeException,BadPaddingException,NoSuchProviderException,InvalidKeySpecException {
        return encrypt(b,false);
    }

    public byte[] encrypt(byte[] b,boolean withPublicKey) throws NoSuchAlgorithmException,NoSuchPaddingException,InvalidKeyException,IllegalBlockSizeException,BadPaddingException,NoSuchProviderException,InvalidKeySpecException {
        KeyPair key = getKeyPair();
        Cipher cipher = Cipher.getInstance(keytype.toString().toUpperCase()+"/ECB/PKCS1Padding");
        java.security.Key k=key.getPrivate();
        if(withPublicKey) k=key.getPublic();
        cipher.init(Cipher.ENCRYPT_MODE,k);
        return cipher.doFinal(b);
    }

    @Override
    public byte[] decrypt(byte[] b) throws NoSuchAlgorithmException,NoSuchPaddingException,InvalidKeyException,IllegalBlockSizeException,BadPaddingException,NoSuchProviderException,InvalidKeySpecException {
        return decrypt(b,false);
    }

    public byte[] decrypt(byte[] b,boolean withPublicKey) throws NoSuchAlgorithmException,NoSuchPaddingException,InvalidKeyException,IllegalBlockSizeException,BadPaddingException,NoSuchProviderException,InvalidKeySpecException {
        KeyPair key = getKeyPair();
        Cipher cipher = Cipher.getInstance(keytype.toString().toUpperCase()+"/ECB/PKCS1Padding");
        java.security.Key k=key.getPrivate();
        if(withPublicKey) k=key.getPublic();
        cipher.init(Cipher.DECRYPT_MODE,k);
        return cipher.doFinal(b);
    }

    private KeyFactory getKeyFactory() throws NoSuchAlgorithmException,NoSuchProviderException {
        return KeyFactory.getInstance(keytype.toString().toUpperCase(), "BC");
    }

    private KeyPair getKeyPair() throws NoSuchAlgorithmException,NoSuchProviderException,InvalidKeySpecException {
        KeyFactory kf=getKeyFactory();
        PKCS8EncodedKeySpec rks = null;
        PrivateKey priv = null;
        if(privateKey!=null) {
            rks  = new PKCS8EncodedKeySpec(privateKey.getBytes());
            priv = kf.generatePrivate(rks);
        }
        X509EncodedKeySpec uks=null;
        PublicKey pub = null;
        if(publicKey!=null) {
            uks = new X509EncodedKeySpec(publicKey.getBytes());
            pub = kf.generatePublic(uks);
        }
        return new KeyPair( pub,priv );
    }

    public boolean equals(AsymmetricKey ak) {
        return publicKey.equals(ak.publicKey);
    }
}
