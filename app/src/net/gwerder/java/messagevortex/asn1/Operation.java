package net.gwerder.java.messagevortex.asn1;

import org.bouncycastle.asn1.ASN1Encodable;
import org.bouncycastle.asn1.ASN1Object;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import java.io.IOException;

/**
 * Represents a the Blending specification of the routing block.
 *
 */
public class Operation extends Block {

    /* constructor */
    public Operation() {}

    protected void parse(ASN1Encodable to) {
        throw new NotImplementedException();
    }

    public ASN1Object toASN1Object() throws IOException{
        throw new UnsupportedOperationException( "not yet implemented" ); //FIXME
    }

    public String dumpValueNotation(String prefix) {
        StringBuilder sb=new StringBuilder();
        sb.append(prefix).append("-- FIXME dumping of BlendingSpec object not yet supported").append(CRLF); //FIXME
        return sb.toString();
    }

}
