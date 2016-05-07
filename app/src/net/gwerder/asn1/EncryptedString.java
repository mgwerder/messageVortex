package net.gwerder.asn1;

import org.bouncycastle.asn1.ASN1Encodable;
import org.bouncycastle.asn1.ASN1String;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.text.ParseException;

/**
 * Created by martin.gwerder on 07.05.2016.
 */
public class EncryptedString extends Block {

    protected Key key;
    protected ASN1String encStr = null;
    protected Block b=null;

    public EncryptedString(ASN1String to, Key k) throws IOException, ParseException, NoSuchAlgorithmException {
        key = k;
        encStr = to;
        parse( (ASN1Encodable) to );
    }

    @Override
    protected void parse(ASN1Encodable to) throws IOException, ParseException, NoSuchAlgorithmException {
        // FIXME
    }

    @Override
    public ASN1Encodable encodeDER() {
        return (ASN1Encodable) encStr;
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
