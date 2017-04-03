package net.gwerder.java.messagevortex.asn1;

import org.bouncycastle.asn1.ASN1Encodable;
import org.bouncycastle.asn1.ASN1Object;

import java.io.IOException;

public class RoutingLog extends Block {

    public RoutingLog(ASN1Encodable to) {
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
        sb.append("-- FIXME unsuported dump of RoutingLog"+CRLF);
        return sb.toString();
    }

}
