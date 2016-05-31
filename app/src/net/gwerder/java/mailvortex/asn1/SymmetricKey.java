package net.gwerder.java.mailvortex.asn1;


import net.gwerder.java.mailvortex.asn1.encryption.Algorithm;
import org.bouncycastle.asn1.*;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.SecretKeySpec;
import java.io.IOException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

/**
 * Created by martin.gwerder on 19.04.2016.
 */
public class SymmetricKey extends Key {

    protected byte[] key= null;

    public SymmetricKey(Algorithm sk) throws IOException,NoSuchAlgorithmException {
        keytype=sk;
        if(sk.toString().equals(Algorithm.AES256.toString())) {
           createAES( 256 );
        } else if(sk.toString().equals(Algorithm.AES192.toString())) {
            createAES( 192 );
        } else if(sk.toString().equals(Algorithm.AES128.toString())) {
            createAES( 128 );
        } else {
            throw new NoSuchAlgorithmException( "Algorithm "+sk+" is not encodable by the system" );
        }
    }

    private void createAES(int keysize) {
        SecureRandom random = new SecureRandom();
        byte[] keyBytes = new byte[keysize/8];
        random.nextBytes(keyBytes);
        SecretKeySpec aeskey = new SecretKeySpec(keyBytes, "AES");
        key=aeskey.getEncoded();
    }

    public SymmetricKey(byte[] sk) throws IOException {
        this(sk,null,false);
    }

    public SymmetricKey(byte[] sk, AsymmetricKey deckey, boolean decryptWithPublicKey) throws IOException {
        // decrypt and decode
        ASN1Primitive s;
        if(deckey!=null) {
            byte[] b;
            try {
                b=deckey.decrypt( sk, decryptWithPublicKey );
            } catch(Exception e) {
                throw new IOException( "Error while decrypting object", e );
            }
            s=DERSequence.fromByteArray(b);
        } else {
            s=DERSequence.fromByteArray(sk);
        }
        parse( s );
    }

    private Cipher getCipher() throws NoSuchAlgorithmException,NoSuchPaddingException {
        return Cipher.getInstance( keytype.getAlgorithmFamily().toUpperCase()+"/ECB/PKCS5Padding" );
    }

    @Override
    public byte[] encrypt(byte[] b) throws NoSuchAlgorithmException,NoSuchPaddingException,InvalidKeyException,InvalidAlgorithmParameterException,BadPaddingException,IllegalBlockSizeException {
        Cipher c=getCipher();
        SecretKeySpec ks=new SecretKeySpec( key,keytype.getAlgorithmFamily() );
        SecureRandom r=new SecureRandom();
        c.init(Cipher.ENCRYPT_MODE,ks );
        return c.doFinal( b );
    }

    @Override
    public byte[] decrypt(byte[] b) throws NoSuchAlgorithmException,NoSuchPaddingException,InvalidKeyException,InvalidAlgorithmParameterException,BadPaddingException,IllegalBlockSizeException {
        Cipher c=getCipher();
        SecretKeySpec ks=new SecretKeySpec( key,keytype.getAlgorithmFamily().toUpperCase() );
        SecureRandom r=new SecureRandom();
        c.init(Cipher.DECRYPT_MODE,ks );
        return c.doFinal( b );
    }

    protected void parse(ASN1Encodable to) {
        // preparing parsing
        int i=0;
        ASN1Sequence s1 = ASN1Sequence.getInstance(to);

        // parsing Symetric Key Idetifier
        parseKeyParameter(ASN1Sequence.getInstance( s1.getObjectAt(i++) ));

        // getting key
        key=ASN1OctetString.getInstance( s1.getObjectAt(i++)).getOctets();
    }

    public byte[] getKey() { return key; }

    @Override
    public ASN1Object toASN1Object() throws IOException {
        ASN1EncodableVector ret = new ASN1EncodableVector();
        ret.add(encodeKeyParameter());
        ret.add(new DEROctetString( key ));
        return new DERSequence(ret);
    }

    public String dumpValueNotation(String prefix) {
        StringBuilder sb=new StringBuilder();
        sb.append("{"+CRLF);
        sb.append(dumpKeyTypeValueNotation(prefix)+CRLF);
        sb.append(prefix+"  key "+toHex(key)+CRLF);
        sb.append(prefix+"}");
        return sb.toString();
    }


}
