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

import net.gwerder.java.messagevortex.asn1.encryption.DumpType;
import org.bouncycastle.asn1.*;

import java.io.IOException;

public class PayloadChunk extends AbstractBlock {
    private enum PayloadType {
        /* ASN1 tag number for a contained payload */
        PAYLOAD(100),
        /* ASN1 tag number for a contained reply block */
        REPLY(101);

        private final int id;

        PayloadType(int id) {
            this.id=id;
        }

        public int getId() {
            return id;
        }

    }

    /* the minimum required id in order to allow dumping to der */
    public static final int MIN_VALID_ID = 100;

    int    id = 0;
    byte[] payload=null;
    PayloadType payloadType = PayloadType.PAYLOAD;

    /***
     * Creates an empty payload block.
     */
    public PayloadChunk() {
        id=0;
        payload=new byte[0];
        payloadType=PayloadType.PAYLOAD;
    }

    /***
     * Creates a payload block from a ASN1 stream.
     */
    public PayloadChunk(ASN1Encodable to) throws IOException {
        parse(to);
    }

    public PayloadChunk(int id,byte[] payload) throws IOException {
        setId(id);
        setPayload(payload);
    }

    /***
     * Creates a der encoded ASN1 representation of the payload chunk.
     *
     * @param  dumpType    the dump type to be used
     * @return             the ASN.1 object
     * @throws IOException if id is too low or the payload has not been set
     */
    public ASN1Object toASN1Object(DumpType dumpType) throws IOException{
        ASN1EncodableVector v=new ASN1EncodableVector();
        if(id<MIN_VALID_ID) {
            throw new IOException("illegal dump id is set");
        }
        v.add( new ASN1Integer( id ) );

        if(payloadType==PayloadType.PAYLOAD) {
            v.add(new DERTaggedObject(true, PayloadType.PAYLOAD.getId(), new DEROctetString(payload)));
        } else if(payloadType==PayloadType.REPLY) {
            v.add(new DERTaggedObject(true, PayloadType.REPLY.getId(), new DEROctetString(payload)));
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
        payloadType=PayloadType.PAYLOAD;
        return opl;
    }

    /***
     * Gets the the currently set payload.
     *
     * @return the payload as byte array or null if a replyblock has been set
     */
    public byte[] getPayload() {
        if(payloadType!=PayloadType.PAYLOAD) {
            return null;
        }
        if(payload==null) {
            return null;
        } else {
            return payload.clone();
        }
    }

    /***
     * set a byte array as reply block.
     *
     * @param reply the reply block to be set
     * @return the previously set reply block (may have been a payload block)
     */
    public byte[] setReplyBlock(byte[] reply) throws IOException{
        byte[] opl=payload;
        payload=reply;
        payloadType=PayloadType.REPLY;
        return opl;
    }

    /***
     * Gets the the currently set reply block.
     *
     * @return the reply block as byte array or null if a payload block has been set
     */
    public byte[] getReplyBlock() {
        if(payloadType!=PayloadType.REPLY) {
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
        if(dto.getTagNo()==PayloadType.PAYLOAD.getId()) {
            setPayload(ASN1OctetString.getInstance( dto.getObject() ).getOctets());
        } else if(dto.getTagNo()==PayloadType.REPLY.getId()) {
                setReplyBlock(ASN1OctetString.getInstance( dto.getObject() ).getOctets());
        } else {
            throw new IOException( "got bad tag number (expected:"+PayloadType.REPLY.getId()+" or "+PayloadType.PAYLOAD.getId()+";got:"+dto.getTagNo()+")" );
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
     * @param prefix       the prefix to be used (normally used for indentation)
     * @param dumpType     the dump type to be used (@see DumpType)
     * @return             the string representation of the ASN1 object
     * @throws IOException if the payload id is below MIN_VALID_ID or no payload/reply block has been set
     */
    @Override
    public String dumpValueNotation(String prefix,DumpType dumpType) throws IOException {
        StringBuilder sb=new StringBuilder();
        sb.append(" {"+CRLF);
        sb.append(prefix+"  id "+id+","+CRLF);
        sb.append(prefix+"  content ");
        if(payloadType==PayloadType.PAYLOAD) {
            sb.append("payload " + toHex(payload) + CRLF);
        } else if(payloadType==PayloadType.REPLY) {
            sb.append("reply " + toHex(payload) + CRLF);
        } else {
            throw new IOException( "unable to determine payload type (expected:"+PayloadType.REPLY.getId()+" or "+PayloadType.PAYLOAD.getId()+";got:"+payloadType+")" );
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
            return dumpValueNotation("",DumpType.ALL_UNENCRYPTED).equals(pl.dumpValueNotation("",DumpType.ALL_UNENCRYPTED));
        } catch(IOException ioe) {
            return false;
        }
    }

    @Override
    public int hashCode() {
        try {
            return dumpValueNotation("",DumpType.ALL_UNENCRYPTED).hashCode();
        } catch(IOException ioe) {
            return 0;
        }
    }

}
