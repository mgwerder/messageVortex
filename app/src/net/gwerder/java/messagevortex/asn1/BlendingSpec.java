package net.gwerder.java.messagevortex.asn1;
// ************************************************************************************
// * Copyright (c) 2018 Martin Gwerder (martin@gwerder.net)
// *
// * Permission is hereby granted, free of charge, to any person obtaining a copy
// * of this software and associated documentation files (the "Software"), to deal
// * in the Software without restriction, including without limitation the rights
// * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
// * copies of the Software, and to permit persons to whom the Software is
// * furnished to do so, subject to the following conditions:
// *
// * The above copyright notice and this permission notice shall be included in all
// * copies or substantial portions of the Software.
// *
// * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
// * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
// * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
// * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
// * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
// * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
// * SOFTWARE.
// ************************************************************************************

import org.bouncycastle.asn1.ASN1Encodable;
import org.bouncycastle.asn1.ASN1Object;

import java.io.IOException;

/**
 * Represents a the Blending specification of the routing block.
 *
 */
public class BlendingSpec extends AbstractBlock {

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
