package net.gwerder.java.mailvortex.asn1;


import org.bouncycastle.asn1.*;

import javax.crypto.spec.SecretKeySpec;
import java.io.IOException;
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

    @Override
    public byte[] encrypt(byte[] b) {
        // FIXME
        return null;
    }

    @Override
    public byte[] decrypt(byte[] b) {
        // FIXME
        return null;
    }

    protected void parse(ASN1Encodable to) {
        // preparing parsing
        int i=0;
        ASN1Sequence s1 = ASN1Sequence.getInstance(to);

        // parsing Symetric Key Idetifier
        parseKeyParameter(ASN1Sequence.getInstance( s1.getObjectAt(i++) ));

        // getting key
        System.out.println("getting THE key ");
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
        sb.append(prefix+"  key "+toBitString(key)+CRLF);
        sb.append(prefix+"}");
        return sb.toString();
    }


}
