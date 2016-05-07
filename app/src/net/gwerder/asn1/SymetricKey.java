package net.gwerder.asn1;


import org.bouncycastle.asn1.*;

import javax.crypto.spec.SecretKeySpec;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.text.ParseException;

/**
 * Created by martin.gwerder on 19.04.2016.
 */
public class SymetricKey extends Key {

    protected ASN1BitString key= null;

    public SymetricKey(Algorithm sk) throws IOException,NoSuchAlgorithmException {
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
        key=new DERBitString(aeskey.getEncoded(),0);
    }

    public SymetricKey(byte[] sk,AsymetricKey deckey,boolean withPublicKey) throws IOException {
        // FIXME decrypt and decode (key must be added to params)
        if(deckey==null) {
            try {
                parse( deckey.decrypt( sk, withPublicKey ) );
            } catch (Exception e) {
                throw new IOException("Error while parsing/decrypting object",e);
            }
        }
    }

    protected void parse(ASN1Encodable to) {
        int i=0;
        ASN1Sequence s1 = ASN1Sequence.getInstance(to);
        // parsing asymetric Key Idetifier
        ASN1Sequence s2 = ASN1Sequence.getInstance(s1.getObjectAt(i++));
        parseKeyParameter(s2.getObjectAt(0),s2.getObjectAt(1));
        key=((ASN1BitString)(s1.getObjectAt(i++)));
    }

    @Override
    public ASN1Encodable encodeDER() {
        ASN1EncodableVector ret = new ASN1EncodableVector();
        ASN1Encodable e=super.encodeDER();
        if(e==null) return null;
        ret.add(e);
        ret.add(key);
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
