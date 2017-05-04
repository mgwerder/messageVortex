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
import java.util.Arrays;

public class PayloadChunk extends AbstractBlock {

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

    public byte[] getPayload() {
        return payload;
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

    public boolean isInUsagePeriod() {
        // FIXME not yet implemented
        return true;
    }

    public boolean equals(Object o) {
        if(o==null) return false;

        if(! (o instanceof PayloadChunk)) return false;
        PayloadChunk pl=(PayloadChunk)o;

        return Arrays.equals(getPayload(),pl.getPayload());
    }

}
