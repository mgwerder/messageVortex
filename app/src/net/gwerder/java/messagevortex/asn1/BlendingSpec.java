package net.gwerder.java.messagevortex.asn1;

import org.bouncycastle.asn1.ASN1Encodable;
import org.bouncycastle.asn1.ASN1Object;

import java.io.IOException;

/**
 * Represents a the Blending specification of the routing block.
 *
 */
public class BlendingSpec extends Block {

    /* The endpoint address to be used */
    private String blendingEndpointAddress=null;

    /* constructor */
    public BlendingSpec(ASN1Encodable to) {
        parse(to);
    }

    public BlendingSpec(String blendingEndpointAddress) {
        this.blendingEndpointAddress = blendingEndpointAddress;
    }

    protected void parse(ASN1Encodable to) {
        throw new UnsupportedOperationException( "not yet implemented" ); //FIXME
    }

    public ASN1Object toASN1Object() throws IOException{
        throw new UnsupportedOperationException( "not yet implemented" ); //FIXME
    }

    public String dumpValueNotation(String prefix) {
        StringBuilder sb=new StringBuilder();
        sb.append(prefix).append("-- FIXME dumping of BlendingSpec object not yet supported").append(CRLF); //FIXME
        return sb.toString();
    }

    public String getBlendingEndpointAddress() { return blendingEndpointAddress; }

    public String setBlendingEndpointAddress(String blendingEndpointAddress) {
        String old=this.blendingEndpointAddress;
        this.blendingEndpointAddress=blendingEndpointAddress;
        return old;
    }

}
