package net.gwerder.asn1;

import org.bouncycastle.asn1.*;
import org.bouncycastle.jce.ECNamedCurveTable;
import org.bouncycastle.jce.spec.ECParameterSpec;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import java.security.*;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by martin.gwerder on 19.04.2016.
 */
public class AsymetricKey extends Key {

    static {
        Security.addProvider(new org.bouncycastle.jce.provider.BouncyCastleProvider());
    }

    protected ASN1BitString publicKey = null;
    protected ASN1BitString privateKey = null;


    public AsymetricKey(Algorithm alg,int keysize) throws NoSuchAlgorithmException, NoSuchProviderException, InvalidAlgorithmParameterException {
        if(alg==null) throw new NoSuchAlgorithmException( "Algorithm null is not encodable by the system" );
        Map<String,Integer> pm= new HashMap();
        pm.put(""+Parameter.KEYSIZE.getId()+"_0",keysize);
        createKey(alg,pm);
    }

    public AsymetricKey(Algorithm alg,Map<String,Integer> params) throws NoSuchAlgorithmException, NoSuchProviderException, InvalidAlgorithmParameterException {
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

    public AsymetricKey(ASN1Encodable to) {
        parse(to);
    }

    protected void parse(ASN1Encodable to) {
        int i=0;
        ASN1Sequence s1 = ASN1Sequence.getInstance(to);
        // parsing asymetric Key Idetifier
        ASN1Sequence s2=ASN1Sequence.getInstance(s1.getObjectAt(i++));
        parseKeyParameter(s2.getObjectAt(0),s2.getObjectAt(1));
        publicKey=((ASN1BitString)(s1.getObjectAt(i++)));
        if(s1.size()>i) {
            privateKey=((ASN1BitString)(s1.getObjectAt(i++)));
        }
    }

    @Override
    public ASN1Encodable encodeDER() {
        // FIXME
        return null;
    }

    public String dumpValueNotation(String prefix,boolean dumpPrivateKey) {
        StringBuilder sb=new StringBuilder();
        sb.append("{"+CRLF);
        sb.append(dumpKeyTypeValueNotation(prefix));
        String s=null;
        try{
            s=toHex(publicKey.getOctets());
        } catch(IllegalStateException ise) {
            s=toBitString(publicKey);
        }
        sb.append(prefix+"  publicKey "+s);
        if(privateKey!=null && dumpPrivateKey) {
            sb.append(","+CRLF);
            s=null;
            try{
                s=toHex(privateKey.getOctets());
            } catch(IllegalStateException ise) {
                s=toBitString(privateKey);
            }
            sb.append(prefix+"  privateKey "+s+CRLF);
        } else sb.append(CRLF);
        sb.append(prefix+"}");
        return sb.toString();
    }

    public byte[] decrypt(byte[] b,boolean withPublicKey) throws NoSuchAlgorithmException,NoSuchPaddingException,InvalidKeyException,IllegalBlockSizeException,BadPaddingException,NoSuchProviderException,InvalidKeySpecException {
        KeyPair key = getKeyPair();
        Cipher cipher = Cipher.getInstance(keytype.toString().toUpperCase()+"/ECB/PKCS1Padding");
        java.security.Key k=key.getPrivate();
        if(withPublicKey) k=key.getPublic();
        cipher.init(Cipher.DECRYPT_MODE,k);
        byte[] newText = cipher.doFinal(b);
        return newText;
    }

    private KeyFactory getKeyFactory() throws NoSuchAlgorithmException,NoSuchProviderException {
        return KeyFactory.getInstance(keytype.toString().toUpperCase(), "BC");
    }

    private KeyPair getKeyPair() throws NoSuchAlgorithmException,NoSuchProviderException,InvalidKeySpecException {
        KeyFactory kf=getKeyFactory();
        PKCS8EncodedKeySpec rks = new PKCS8EncodedKeySpec(privateKey.getBytes());
        PrivateKey priv = kf.generatePrivate(rks);
        X509EncodedKeySpec uks=new X509EncodedKeySpec(publicKey.getBytes());
        PublicKey pub = kf.generatePublic(uks);
        return new KeyPair( pub,priv );
    }

    public byte[] encrypt(byte[] b,boolean withPublicKey) throws NoSuchAlgorithmException,NoSuchPaddingException,InvalidKeyException,IllegalBlockSizeException,BadPaddingException,NoSuchProviderException,InvalidKeySpecException {
        KeyPair key = getKeyPair();
        Cipher cipher = Cipher.getInstance(keytype.toString().toUpperCase()+"/ECB/PKCS1Padding");
        java.security.Key k=key.getPrivate();
        if(withPublicKey) k=key.getPublic();
        cipher.init(Cipher.ENCRYPT_MODE,k);
        byte[] newText = cipher.doFinal(b);
        return newText;
    }

    public boolean equals(AsymetricKey ak) {
        return publicKey.equals(ak.publicKey);
    }
}
