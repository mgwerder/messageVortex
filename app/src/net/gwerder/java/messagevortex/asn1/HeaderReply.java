package net.gwerder.java.messagevortex.asn1;

import org.bouncycastle.asn1.ASN1Encodable;
import org.bouncycastle.asn1.ASN1Object;
import org.bouncycastle.asn1.ASN1Primitive;

import java.io.IOException;

/**
 * ASN1 parser class for header reply.
 *
 * Created by martin.gwerder on 14.04.2016.
 */
public class HeaderReply extends AbstractBlock {

    // FIXME get HeaderReply
    public HeaderReply(ASN1Primitive to) {
    }

    @Override
    protected void parse(ASN1Encodable to) {
        // FIXME
    }

    public ASN1Object toASN1Object() throws IOException {
        throw new IOException( "not implemented" ); //FIXME
    }

    public String dumpValueNotation(String prefix) {
        StringBuilder sb=new StringBuilder();
        sb.append("-- FIXME unsuported dump of HeaderReply").append(CRLF);
        return sb.toString();
    }

}
