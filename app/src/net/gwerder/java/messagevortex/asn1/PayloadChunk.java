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

public class PayloadChunk extends AbstractBlock {

    /* ASN1 tag number for a contained payload */
    public static final int PAYLOAD = 100;
    /* ASN1 tag number for a contained reply block */
    public static final int REPLY   = 101;

    /* the minimum required id in order to allow dumping to der */
    public static final int MIN_VALID_ID = 100;

    int    id = 0;
    byte[] payload=null;
    int    payloadType = PAYLOAD;

    /***
     * Creates an empty payload block.
     */
    public PayloadChunk() {
        id=0;
        payload=new byte[0];
        payloadType=-1;
    }

    /***
     * Creates a payload block from a ASN1 stream.
     */
    public PayloadChunk(ASN1Encodable to) throws IOException {
        parse(to);
    }

    /***
     * Creates a der encoded ASN1 representation of the payload chunk.
     *
     * @return
     * @throws IOException if id is too low or the payload has not been set
     */
    public ASN1Object toASN1Object() throws IOException{
        ASN1EncodableVector v=new ASN1EncodableVector();
        if(id<MIN_VALID_ID) {
            throw new IOException("illegal dump id is set");
        }
        v.add( new ASN1Integer( id ) );

        if(payloadType==PAYLOAD) {
            v.add(new DERTaggedObject(true, PAYLOAD, new DEROctetString(payload)));
        } else if(payloadType==REPLY) {
            v.add(new DERTaggedObject(true, REPLY, new DEROctetString(payload)));
        } else {
            throw new IOException( "unable to dump payload block as payload and reply are empty" );
        }
        return new DERSequence( v );
    }

    /***
     * set a byte array as payload.
     *
     * @param b the payload to be set
     * @return the previously set payload (may have been a reply block)
     */
    public byte[] setPayload(byte[] b) {
        byte[] opl=payload;
        payload=b;
        payloadType=PAYLOAD;
        return opl;
    }

    /***
     * Gets the the currently set payload.
     *
     * @return the payload as byte array or null if a replyblock has been set
     */
    public byte[] getPayload() {
        if(payloadType!=PAYLOAD) {
            return null;
        }
        return payload.clone();
    }

    /***
     * set a byte array as reply block.
     *
     * @param b the reply block to be set
     * @return the previously set reply block (may have been a payload block)
     */
    public byte[] setReplyBlock(byte[] b) {
        byte[] opl=payload;
        payload=b;
        payloadType=REPLY;
        return opl;
    }

    /***
     * Gets the the currently set reply block.
     *
     * @return the reply block as byte array or null if a payload block has been set
     */
    public byte[] getReplyBlock() {
        if(payloadType!=REPLY) {
            return null;
        }
        return payload.clone();
    }

    @Override
    protected void parse(ASN1Encodable to) throws IOException {
        ASN1Sequence s1 = ASN1Sequence.getInstance(to);
        int i=0;
        id=ASN1Integer.getInstance( s1.getObjectAt(i++)).getValue().intValue();

        ASN1TaggedObject dto=ASN1TaggedObject.getInstance( s1.getObjectAt(i++) );
        if(dto.getTagNo()==PAYLOAD) {
            setPayload(ASN1OctetString.getInstance( dto.getObject() ).getOctets());
        } else if(dto.getTagNo()==REPLY) {
                setReplyBlock(ASN1OctetString.getInstance( dto.getObject() ).getOctets());
        } else {
            throw new IOException( "got bad tag number (expected:"+REPLY+" or "+PAYLOAD+";got:"+dto.getTagNo()+")" );
        }
    }

    /***
     * Sets the id of the payload chunk.
     *
     * @param id the id to be set
     * @return the previously set id
     */
    public int setId(int id) {
        int ret=this.id;
        this.id=id;
        return ret;
    }

    /***
     * Gets the id of the payload chunk.
     *
     * @return the id currently set
     */
    public int getId() {
        return this.id;
    }

    /***
     * Dumps the current object as a value representation.
     *
     * @param prefix the prefix to be used (nurmally used for indentation
     * @return       the string representation of the ASN1 object
     * @throws IOException if the payload id is below MIN_VALID_ID or no payload/reply block has been set
     */
    public String dumpValueNotation(String prefix) throws IOException {
        StringBuilder sb=new StringBuilder();
        sb.append(" {"+CRLF);
        sb.append(prefix+"  id "+id+","+CRLF);
        sb.append(prefix+"  content ");
        if(payloadType==PAYLOAD) {
            sb.append("payload " + toHex(payload) + CRLF);
        } else if(payloadType==REPLY) {
            sb.append("reply " + toHex(payload) + CRLF);
        } else {
            throw new IOException( "unable to determine payload type (expected:"+REPLY+" or "+PAYLOAD+";got:"+payloadType+")" );
        }
        sb.append(prefix+"}");
        return sb.toString();
    }

    public boolean isInUsagePeriod() {
        // FIXME not yet implemented
        return true;
    }

    @Override
    public boolean equals(Object o) {
        if(o==null) {
            return false;
        }

        if(! (o instanceof PayloadChunk)) {
            return false;
        }
        PayloadChunk pl=(PayloadChunk)o;

        try {
            return dumpValueNotation("").equals(pl.dumpValueNotation(""));
        } catch(IOException ioe) {
            return false;
        }
    }

    @Override
    public int hashCode() {
        try {
            return dumpValueNotation("").hashCode();
        } catch(IOException ioe) {
            return 0;
        }
    }

}
