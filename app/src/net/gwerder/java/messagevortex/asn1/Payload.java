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

import org.bouncycastle.asn1.*;

import java.io.IOException;

public class Payload extends AbstractBlock {

    PayloadChunk[] payloads = null;

    public Payload(byte[] p) throws IOException {
        ASN1InputStream aIn = new ASN1InputStream( p );
        parse( aIn.readObject() );
    }

    public Payload() {
        payloads=new PayloadChunk[] {new PayloadChunk()};
    }

    public Payload(ASN1Encodable to) throws IOException {
        parse( to );
    }

    public PayloadChunk[] getPayloadChunks() { return payloads; }

    public PayloadChunk[] setPayloadChunks(PayloadChunk[] nplc) {
        PayloadChunk[] oplc=payloads;
        payloads=nplc;
        return oplc;
    }

    @Override
    protected void parse(ASN1Encodable to) throws IOException {
        ASN1Sequence s1 = ASN1Sequence.getInstance(to);
        payloads=new PayloadChunk[s1.size()];
        for(int i=0;i< s1.size();i++) {
            payloads[i] = new PayloadChunk( s1.getObjectAt( i ) );
        }
    }

    public ASN1Object toASN1Object() throws IOException {
        ASN1EncodableVector v =new ASN1EncodableVector();
        for(PayloadChunk pc:payloads) {
            v.add(pc.toASN1Object());
        }
        return new DERSequence(v);
    }

    public String dumpValueNotation(String prefix) {
        StringBuilder sb=new StringBuilder();
        sb.append(" {"+CRLF);
        sb.append(prefix+"  -- FIXME dumping of Payload object not yet supported"+CRLF);
        sb.append(prefix+"}");
        return sb.toString();
    }

}
