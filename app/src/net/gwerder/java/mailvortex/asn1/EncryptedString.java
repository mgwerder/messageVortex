package net.gwerder.java.mailvortex.asn1;

import org.bouncycastle.asn1.*;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import java.io.IOException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.spec.InvalidKeySpecException;
import java.text.ParseException;

/**
 * Created by martin.gwerder on 07.05.2016.
 */
public class EncryptedString extends Block {

    protected Key key;
    protected ASN1String encStr = null;
    protected Block b=null;
    protected boolean stdAsymetricEncrypt=true;

    public EncryptedString(ASN1String to, Key k) throws IOException, ParseException, NoSuchAlgorithmException {
        key = k;
        encStr = to;
        parse( (ASN1Encodable) to );
    }

    public byte[] getDecryptedBytes() throws NoSuchAlgorithmException,NoSuchPaddingException,NoSuchProviderException,IllegalBlockSizeException,InvalidKeyException,BadPaddingException,InvalidKeySpecException,InvalidAlgorithmParameterException {
        byte[] as=null;
        if(AsymmetricKey.class.isAssignableFrom(key.getClass())) {
            // decrypt symetric
            as = key.decrypt( encStr.getString().getBytes() );
        } else {
            // decrypt asymetric
            as=((AsymmetricKey)(key)).decrypt( encStr.getString().getBytes(),false);
        }
        return as;
    }

    public ASN1OctetString getDecryptedString()  throws NoSuchAlgorithmException,NoSuchPaddingException,NoSuchProviderException,IllegalBlockSizeException,InvalidKeyException,BadPaddingException,InvalidKeySpecException,InvalidAlgorithmParameterException {
        return new DEROctetString( getDecryptedBytes() );
    }

    @Override
    protected void parse(ASN1Encodable to) throws IOException, ParseException, NoSuchAlgorithmException {
        // FIXME
    }

    public ASN1Object toASN1Object() {
        return (ASN1Object)encStr;
    }

    public String dumpValueNotation(String prefix) throws IOException {
        StringBuilder sb = new StringBuilder();
        sb.append( CRLF );
        if( b != null ) {
            sb.append( prefix + "  --" + b.getClass().getSimpleName() );
        }
        return sb.toString();
    }
}
