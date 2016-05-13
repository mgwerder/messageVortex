package net.gwerder.java.mailvortex.asn1;

import org.bouncycastle.asn1.*;

import java.io.IOException;

public class PayloadChunk extends Block {

    long offset = -1;
    int[]  rbi = null;
    byte[] payload=null;

    public PayloadChunk() {
        offset=0;
        rbi=new int[] { 0 };
        payload=new byte[0];
    }

    public PayloadChunk(ASN1Encodable to) {

    }

    public ASN1Object toASN1Object() throws IOException{
        ASN1EncodableVector v=new ASN1EncodableVector();
        v.add( new ASN1Integer( offset ) );

        // FIXME RBI dummy only
        v.add( new DERTaggedObject( true,0,new DERSequence() ));

        v.add( new DERTaggedObject( true, 100, new DEROctetString(payload) ));
        return new DERSequence( v );
    }

    @Override
    protected void parse(ASN1Encodable to) throws IOException {
        ASN1Sequence s1 = ASN1Sequence.getInstance(to);
        int i=0;
        offset=((ASN1Integer)(s1.getObjectAt(i++))).getValue().longValue();

        //FIXME routingBlockIdentifier parsing missing
        s1.getObjectAt(i++);// FIXME this is a dummy

        payload=((ASN1OctetString)(s1.getObjectAt(i++))).getOctets();
        if(offset<0) throw new IOException("illegal offset parsed");
    }

    public String dumpValueNotation(String prefix) {
        StringBuilder sb=new StringBuilder();
        sb.append(" {"+CRLF);
        sb.append(prefix+"  -- FIXME dumping of Payload object not yet supported"+CRLF);
        sb.append(prefix+"}");
        return sb.toString();
    }

}
