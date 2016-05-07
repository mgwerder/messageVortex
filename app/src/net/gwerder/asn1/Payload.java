package net.gwerder.asn1;

import org.bouncycastle.asn1.ASN1Encodable;

public class Payload extends Block {

    public Payload() {
    }

    public Payload(ASN1Encodable to) {

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
        sb.append(" {"+CRLF);
        sb.append(prefix+"  -- FIXME dumping of Payload object not yet supported"+CRLF);
        sb.append(prefix+"}");
        return sb.toString();
    }

}
