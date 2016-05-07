package net.gwerder.asn1;

import org.bouncycastle.asn1.ASN1Encodable;
import java.util.Vector;

/**
 * Represents a Request in the identity block.
 *
 * Created by martin.gwerder on 19.04.2016.
 */
public class Request extends Block {

    protected Vector<HeaderRequest> requests = new Vector<HeaderRequest>();

    public Request(ASN1Encodable to) {
        parse(to);
    }

    protected void parse(ASN1Encodable to) {
    }

    @Override
    public ASN1Encodable encodeDER() {
        // FIXME
        return null;
    }

    public String dumpValueNotation(String prefix) {
        StringBuilder sb=new StringBuilder();
        sb.append(prefix+"-- FIXME dumping of Request object not yet supported"+CRLF);
        return sb.toString();
    }

}
