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

    public enum DumpType {
        ALL,
        PUBLIC_ONLY,
        PUBLIC_COMMENTED,
        PRIVATE_ONLY,
        PRIVATE_COMMENTED,
    }

    static {
        Security.addProvider(new org.bouncycastle.jce.provider.BouncyCastleProvider());
    }

    protected byte[] publicKey = null;
    protected byte[] privateKey = null;


    public AsymmetricKey(byte[] b) {
        this(ASN1Sequence.getInstance( b ));
    }

    public AsymmetricKey(ASN1Encodable to) {
        parse(to);
    }

    public AsymmetricKey(Algorithm alg, int keysize) throws NoSuchAlgorithmException, NoSuchProviderException, InvalidAlgorithmParameterException {
        if(alg==null) throw new NoSuchAlgorithmException( "Algorithm null is not encodable by the system" );
        Map<String,Integer> pm= new HashMap<String,Integer>();
        pm.put(""+Parameter.KEYSIZE.getId()+"_0",keysize);
        createKey(alg,pm);
    }

    public AsymmetricKey(Algorithm alg, Map<String,Integer> params) throws NoSuchAlgorithmException, NoSuchProviderException, InvalidAlgorithmParameterException {
        // store algorithm and parameters
        createKey( alg, params );
    }

    private void createKey(Algorithm alg, Map<String,Integer> params) throws NoSuchAlgorithmException, NoSuchProviderException, InvalidAlgorithmParameterException  {
        this.keytype=alg;
        if(params!=null) this.parameters.putAll(params);

        // create key pair
        if(alg.toString().equals("rsa") || alg.toString().equals("dsa")) {
            KeyPairGenerator keyGen = KeyPairGenerator.getInstance(alg.toString().toUpperCase());
            keyGen.initialize(parameters.get("" + Parameter.KEYSIZE.getId() + "_0"));
            KeyPair pair = keyGen.genKeyPair();
            publicKey = pair.getPublic().getEncoded();
            privateKey = pair.getPrivate().getEncoded();
        } else if(alg.toString().startsWith("sec")) {
            ECParameterSpec ecSpec = ECNamedCurveTable.getParameterSpec(alg.getAlgorithm());
            KeyPairGenerator g = KeyPairGenerator.getInstance("ECDSA", "BC");
            g.initialize(ecSpec, new SecureRandom());
            KeyPair pair = g.generateKeyPair();
            publicKey = pair.getPublic().getEncoded();
            privateKey = pair.getPrivate().getEncoded();
        } else throw new NoSuchAlgorithmException("Encountered unknown parameter \""+alg.toString()+"\"");

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
            String s = null;
            try {
                s = toHex( publicKey );
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
                s=toHex(privateKey);
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
        v.add( new DEROctetString(publicKey) );
        if(privateKey!=null) v.add(new DERTaggedObject( true,0,new DEROctetString( privateKey )));
        return new DERSequence( v );
    }

    @Override
    public byte[] encrypt(byte[] b) throws NoSuchAlgorithmException,NoSuchPaddingException,InvalidKeyException,IllegalBlockSizeException,BadPaddingException,NoSuchProviderException,InvalidKeySpecException {
        return encrypt(b,true);
    }

    public byte[] encrypt(byte[] b,boolean withPublicKey) throws NoSuchAlgorithmException,NoSuchPaddingException,InvalidKeyException,IllegalBlockSizeException,BadPaddingException,NoSuchProviderException,InvalidKeySpecException {
        KeyPair key = getKeyPair();
        Cipher cipher=getCipher();
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
        Cipher cipher = getCipher();
        java.security.Key k=key.getPrivate();
        if(withPublicKey) k=key.getPublic();
        cipher.init(Cipher.DECRYPT_MODE,k);
        return cipher.doFinal(b);
    }

    private KeyFactory getKeyFactory() throws NoSuchAlgorithmException,NoSuchProviderException {
        if(keytype.getAlgorithm().startsWith( "sec" )) {
            return KeyFactory.getInstance( "ECDSA", "BC" );
        } else {
            return KeyFactory.getInstance( keytype.getAlgorithm(), "BC" );
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
            return Cipher.getInstance(keytype.getAlgorithm()+"/ECB/PKCS1Padding");
        }
    }

    public boolean equals(AsymmetricKey ak) {
        return publicKey.equals(ak.publicKey);
    }

    public byte[] setPublicKey(byte[] b) throws InvalidKeyException {
        byte[] old=publicKey;
        if( b!= null && b.length!=publicKey.length) throw new InvalidKeyException( "KeySizeMissmatch in key detected" );
        publicKey=b;
        return old;
    }

    public byte[] getPublicKey() {return publicKey; }

    public byte[] setPrivateKey(byte[] b) throws InvalidKeyException {
        byte[] old=privateKey;
        if( b!= null && b.length!=privateKey.length) throw new InvalidKeyException( "KeySizeMissmatch in key detected" );
        privateKey=b;
        return old;
    }

    public byte[] getPrivateKey() {return privateKey; }

}
