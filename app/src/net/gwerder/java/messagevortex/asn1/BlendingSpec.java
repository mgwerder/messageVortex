package net.gwerder.java.messagevortex.asn1;

import org.bouncycastle.asn1.ASN1Encodable;
import org.bouncycastle.asn1.ASN1Object;

import java.io.IOException;

/**
 * Represents a Request in the identity block.
 *
 * Created by martin.gwerder on 19.04.2016.
 */
public class BlendingSpec extends Block {

    private String blendingEndpointAddress=null;

    public BlendingSpec(ASN1Encodable to) {
        parse(to);
    }

    protected void parse(ASN1Encodable to) {
    }

    public ASN1Object toASN1Object() throws IOException{
        throw new IOException( "not implemented" ); //FIXME
    }

    public String dumpValueNotation(String prefix) {
        StringBuilder sb=new StringBuilder();
        sb.append(prefix+"-- FIXME dumping of BlendingSpec object not yet supported"+CRLF);
        return sb.toString();
    }

    public String getBlendingEndpointAddress() { return blendingEndpointAddress; }

    public String setBlendingEndpointAddress(String blendingEndpointAddress) {
        String old=blendingEndpointAddress;
        this.blendingEndpointAddress=blendingEndpointAddress;
        return old;
    }

}
