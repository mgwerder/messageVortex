package net.gwerder.asn1;

import org.bouncycastle.asn1.ASN1Encodable;
import org.bouncycastle.asn1.ASN1Primitive;

/**
 * Created by martin.gwerder on 14.04.2016.
 */
public class HeaderReply extends Block {

    // FIXME get HeaderReply
    public HeaderReply(ASN1Primitive to) {
    }

    @Override
    public ASN1Encodable encodeDER() {
        // FIXME
        return null;
    }

    @Override
    protected void parse(ASN1Encodable to) {
        // FIXME
    }

    public String dumpValueNotation(String prefix) {
        StringBuilder sb=new StringBuilder();
        sb.append("-- FIXME unsuported dump of HeaderReply"+CRLF);
        return sb.toString();
    }

}
