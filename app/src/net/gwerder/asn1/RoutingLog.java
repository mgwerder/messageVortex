package net.gwerder.asn1;

import org.bouncycastle.asn1.ASN1Encodable;
import org.bouncycastle.asn1.ASN1Primitive;

public class RoutingLog extends Block {

    public RoutingLog(ASN1Encodable to) {
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
        sb.append("-- FIXME unsuported dump of RoutingLog"+CRLF);
        return sb.toString();
    }

}
