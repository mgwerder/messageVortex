package net.gwerder.java.messagevortex.asn1;

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

    public PayloadChunk(byte[] b) throws IOException {
        ASN1InputStream aIn=new ASN1InputStream( b );
        parse(aIn.readObject());
    }

    public PayloadChunk(ASN1Encodable to) throws IOException {
        parse(to);
    }

    public ASN1Object toASN1Object() throws IOException{
        ASN1EncodableVector v=new ASN1EncodableVector();
        v.add( new ASN1Integer( offset ) );

        // FIXME RBI dummy only
        v.add( new DERTaggedObject( true,0,new DERSequence() ));

        v.add( new DERTaggedObject( true, 100, new DEROctetString(payload) ));
        return new DERSequence( v );
    }

    public byte[] setPayload(byte[] b) {
        byte[] opl=payload;
        payload=b;
        return opl;
    }

    @Override
    protected void parse(ASN1Encodable to) throws IOException {
        ASN1Sequence s1 = ASN1Sequence.getInstance(to);
        int i=0;
        offset=ASN1Integer.getInstance( s1.getObjectAt(i++)).getValue().longValue();

        //FIXME routingBlockIdentifier parsing missing
        // The following line is a dummy
        i++;

        ASN1TaggedObject dto=ASN1TaggedObject.getInstance( s1.getObjectAt(i++) );
        if(dto.getTagNo()!=100) {
            throw new IOException( "got bad tag number (expected:10;got:"+dto.getTagNo()+")" );
        }
        payload=ASN1OctetString.getInstance( dto.getObject() ).getOctets();
        if(offset<0) {
            throw new IOException("illegal offset parsed");
        }
    }

    public String dumpValueNotation(String prefix) {
        StringBuilder sb=new StringBuilder();
        sb.append(" {"+CRLF);
        sb.append(prefix+"  -- FIXME dumping of PayloadChunk object not yet supported"+CRLF);
        sb.append(prefix+"}");
        return sb.toString();
    }

}
