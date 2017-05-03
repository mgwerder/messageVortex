package net.gwerder.java.messagevortex.asn1;

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
