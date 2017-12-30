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

import net.gwerder.java.messagevortex.asn1.encryption.Algorithm;
import net.gwerder.java.messagevortex.asn1.encryption.AlgorithmType;
import net.gwerder.java.messagevortex.asn1.encryption.DumpType;
import net.gwerder.java.messagevortex.asn1.encryption.SecurityLevel;
import org.bouncycastle.asn1.*;

import java.io.IOException;
import java.io.Serializable;

/***
 * represents an identity block of a vortexMessage.
 */
public class IdentityBlock extends AbstractBlock  implements Serializable {

    public static final long serialVersionUID = 100000000008L;

    private static int nextID=0;

    private static final int ENCRYPTED_HEADER_KEY     = 1000;
    private static final int ENCRYPTED_BLOCK          = 1001;
    private static final int PLAIN_BLOCK              = 1002;
    private static final int ENCRYPTED_HEADER         = 1101;

    private static final HeaderRequest[] NULLREQUESTS = new HeaderRequest[0];


    private SymmetricKey headerKey;
    private byte[] encryptedHeaderKey = null;
    private AsymmetricKey identityKey = null;
    private long serial;
    private int maxReplays;
    private UsagePeriod valid = null;
    private int forwardSecret = -1;
    private MacAlgorithm hash = new MacAlgorithm( Algorithm.getDefault( AlgorithmType.HASHING ) );
    private HeaderRequest[] requests;
    private long identifier=-1;
    private byte[] padding = null;
    private byte[] encryptedIdentityBlock = null;

    private int id;

    private AsymmetricKey ownIdentity = null;

    public IdentityBlock() throws IOException {
        this(new AsymmetricKey(Algorithm.getDefault(AlgorithmType.ASYMMETRIC).getParameters(SecurityLevel.MEDIUM)));
    }

    public IdentityBlock(AsymmetricKey key) throws IOException {
        this.identityKey = key;
        this.serial = (long) (Math.random() * 4294967295L);
        this.maxReplays = 1;
        this.valid = new UsagePeriod(3600);
        this.requests = IdentityBlock.NULLREQUESTS;
        id=nextID++;
    }

    public IdentityBlock(byte[] b, AsymmetricKey ownIdentity) throws IOException {
        this.ownIdentity = ownIdentity;
        ASN1Encodable s = ASN1Sequence.getInstance( b );
        id=nextID++;
        parse( s );
    }

    public IdentityBlock(byte[] b) throws IOException {
        ASN1Encodable s = ASN1Sequence.getInstance( b );
        id=nextID++;
        parse(s);
    }

    public IdentityBlock(ASN1Encodable to) throws IOException {
        this( to, null );
    }

    public IdentityBlock(ASN1Encodable to, AsymmetricKey ownIdentity) throws IOException {
        super();
        this.ownIdentity = ownIdentity;
        parse( to );
        id=nextID++;
    }

    public void setRequests(HeaderRequest[] hr) {
        this.requests=hr;
    }

    @Override
    protected void parse(ASN1Encodable o) throws IOException  {
        ASN1Sequence s = ASN1Sequence.getInstance( o );
        ASN1Sequence s1;
        int j = 0;
        ASN1TaggedObject to = ASN1TaggedObject.getInstance( s.getObjectAt( j++ ) );
        if (to.getTagNo() == ENCRYPTED_HEADER_KEY) {
            if (ownIdentity == null) {
                encryptedHeaderKey = toDER(to.getObject());
                to = DERTaggedObject.getInstance( s.getObjectAt( j++ ) );
            } else {
                headerKey = new SymmetricKey( ownIdentity.decrypt( toDER(to.getObject()) ) );
                to = DERTaggedObject.getInstance( s.getObjectAt( j++ ) );
            }
        }
        byte[] signVerifyObject = toDER(to.getObject());
        if ((headerKey != null && to.getTagNo() == ENCRYPTED_HEADER) || to.getTagNo() == PLAIN_BLOCK) {
            if (headerKey != null && to.getTagNo() == ENCRYPTED_BLOCK) {
                s1 = ASN1Sequence.getInstance( headerKey.decrypt( toDER(to.getObject()) ) );
            } else {
                s1 = ASN1Sequence.getInstance( to.getObject() );
            }
            int i = 0;
            ASN1Encodable s3 = s1.getObjectAt( i++ );
            identityKey = new AsymmetricKey( toDER(s3.toASN1Primitive()) );
            serial = ASN1Integer.getInstance( s1.getObjectAt( i++ ) ).getValue().longValue();
            maxReplays = ASN1Integer.getInstance( s1.getObjectAt( i++ ) ).getValue().intValue();
            valid = new UsagePeriod( s1.getObjectAt( i++ ) );
            forwardSecret = ASN1Integer.getInstance(s1.getObjectAt( i++ )).getValue().intValue();
            hash = new MacAlgorithm( s1.getObjectAt( i++ ) );
            ASN1Sequence s2 = ASN1Sequence.getInstance( s1.getObjectAt( i++ ) );
            requests = new HeaderRequest[s2.size()];
            for (int y = 0; y < s2.size(); y++) {
                requests[y] = HeaderRequest.createRequest( s2.getObjectAt( y ) );
            }
            while (s1.size() > i) {
                to = ASN1TaggedObject.getInstance( s1.getObjectAt( i++ ) );
                if (to.getTagNo() == 1) {
                    identifier = ASN1Integer.getInstance( to.getObject() ).getValue().longValue();
                } else if (to.getTagNo() == 2) {
                    padding = ASN1OctetString.getInstance( s1.getObjectAt( i ) ).getOctets();
                }
            }
        } else {
            encryptedIdentityBlock = toDER(to.getObject());
        }

        byte[] signature = ASN1OctetString.getInstance( s.getObjectAt( j++ ) ).getOctets();
        if (!identityKey.verify( signVerifyObject, signature, hash.getAlgorithm() )) {
            throw new IOException( "Exception while verifying signature of identity block" );
        }
    }

    /***
     * Gets the maximum number of replays for this block.
     *
     * @return    the currently set maximum number of replays
     */
    public int getReplay() {
        return maxReplays;
    }

    /***
     * Sets the maximum number of replays for this block.
     *
     * @param maxReplay the maximum nuber of replays to be set
     * @return          the previously set maximum
     */
    public int setReplay(int maxReplay) {
        int old = getReplay();
        this.maxReplays=maxReplay;
        return old;
    }

    /***
     * Gets the currently set validity period of the block.
     *
     * @return    the previously set validity period
     */
    public UsagePeriod getUsagePeriod() {
        return valid.clone();
    }

    /***
     * Sets the maximum usage period of the block.
     *
     * @param valid the new usage period to be set
     * @return      the previously set usage period
     */
    public UsagePeriod setUsagePeriod(UsagePeriod valid) {
        UsagePeriod old= getUsagePeriod();
        this.valid = valid.clone();
        return old;
    }

    /***
     * Gets the identity representation (asymmetric key) of the block.
     *
     * @return    the previously set identity
     */
    public AsymmetricKey getOwnIdentity() {
        return ownIdentity;
    }

    /***
     * Sets the identity representation (asymmetric key) of the block.
     *
     * @param oid the identity key
     * @return    the previously set identity
     */
    public AsymmetricKey setOwnIdentity(AsymmetricKey oid) {
        AsymmetricKey old = ownIdentity;
        ownIdentity = oid;
        return old;
    }

    /***
     * Gets the identity representation (asymmetric key) of the block.
     *
     * @return    the previously set identity
     */
    public AsymmetricKey getIdentityKey() {
        if(identityKey==null) {
            return null;
        }
        return identityKey.clone();
    }

    /***
     * Sets the identity representation (asymmetric key) of the block.
     *
     * @param oid the identity key
     * @return    the previously set identity
     */
    public AsymmetricKey setIdentityKey(AsymmetricKey oid) {
        AsymmetricKey old = identityKey;
        this.identityKey = oid;
        return old;
    }

    private void sanitizeHeaderKey() throws IOException {
        if (headerKey == null && encryptedHeaderKey == null) {
            headerKey = new SymmetricKey();
        }
    }

    /***
     * Dumps the identity block as ASN.1 der encoded object.
     *
     * @return               the block as der encodable object
     * @throws IOException   if the block is not encodable
     */
    public ASN1Object toASN1Object(DumpType dumpType) throws IOException {
        return toASN1Object( dumpType,null );
    }

    /***
     * Dumps the identity block as ASN.1 der encoded object.
     *
     * @param targetIdentity the identity to be used to secure the Identity block (target identity)
     * @return               the block as der encodable object
     * @throws IOException   if the block is not encodable
     */
    public ASN1Object toASN1Object(DumpType dumpType,AsymmetricKey targetIdentity) throws IOException {
        sanitizeHeaderKey();
        if (headerKey == null && encryptedHeaderKey == null) {
            throw new NullPointerException( "headerKey may not be null" );
        }
        ASN1EncodableVector v1 = new ASN1EncodableVector();
        boolean encryptIdentity = false;
        if (headerKey != null && targetIdentity != null) {
            v1.add( new DERTaggedObject( true, ENCRYPTED_HEADER_KEY, new DEROctetString( targetIdentity.encrypt( headerKey.toBytes(dumpType) ) ) ) );
            encryptIdentity = true;
        } else if (encryptedHeaderKey != null) {
            v1.add( new DERTaggedObject( true, ENCRYPTED_HEADER_KEY, new DEROctetString( encryptedHeaderKey ) ) );
            encryptIdentity = true;
        }
        ASN1Encodable ae;
        if (encryptedIdentityBlock != null) {
            ae = new DEROctetString( encryptedIdentityBlock );
        } else {
            ASN1EncodableVector v = new ASN1EncodableVector();
            ASN1Object o = identityKey.toASN1Object( DumpType.ALL );
            if (o == null) {
                throw new IOException( "identityKey did return null object" );
            }
            v.add( o );
            v.add( new ASN1Integer( serial ) );
            v.add( new ASN1Integer( maxReplays ) );
            o = valid.toASN1Object(dumpType);
            if (o == null) {
                throw new IOException( "validity did return null object" );
            }
            v.add( o );
            v.add(new ASN1Integer(forwardSecret));
            v.add( hash.toASN1Object(dumpType) );
            ASN1EncodableVector s = new ASN1EncodableVector();
            for (HeaderRequest r : requests) {
                s.add( r.toASN1Object() );
            }
            v.add( new DERSequence( s ) );
            if (identifier > -1) {
                v.add( new DERTaggedObject( true, 1, new ASN1Integer( identifier ) ) );
            }
            if (padding != null) {
                v.add( new DERTaggedObject( true, 2, new DEROctetString( padding ) ) );
            }
            ae = new DERSequence( v );
        }
        ASN1Object o;
        if (encryptIdentity) {
            if(headerKey!=null) {
                throw new IOException("header key is empty but block should be encrypted");
            }
            // store identity encrypted
            o = new DEROctetString( headerKey.encrypt( toDER(ae.toASN1Primitive()) ) );
            v1.add( new DERTaggedObject( true, ENCRYPTED_BLOCK, o ) );
        } else {
            // store identity plain
            o = ae.toASN1Primitive();
            v1.add( new DERTaggedObject( true, PLAIN_BLOCK, o ) );
        }
        v1.add( new DEROctetString( identityKey.sign( toDER(o) ) ) );
        return new DERSequence( v1 );
    }

    public String dumpValueNotation(String prefix) throws IOException {
        return dumpValueNotation( prefix, DumpType.PUBLIC_ONLY );
    }

    /***
     * Dumps the current block state in ASN.1 value notation.
     *
     * @param prefix the prefix to be prepended to each line (whitespaces for indentation)
     * @param dumpType     the type of dump to be used
     * @return       a String representing the ASN.1 value notation of the Block
     * @throws IOException if the block is not encodable
     */
    public String dumpValueNotation(String prefix, DumpType dumpType) throws IOException {
        StringBuilder sb=new StringBuilder();
        sb.append("{"+CRLF);
        if (encryptedHeaderKey != null) {
            sb.append( prefix + "  headerKey " + toHex( encryptedHeaderKey ) );
        }
        if (encryptedIdentityBlock != null) {
            sb.append( prefix + "  blocks encrypted " + toHex( encryptedIdentityBlock ) );
        } else {
            sb.append( prefix + "  blocks plain {" + CRLF);
            sb.append( prefix + "    identityKey " + identityKey.dumpValueNotation( prefix + "  ", DumpType.PRIVATE_COMMENTED ) + "," + CRLF );
            sb.append( prefix + "    serial " + serial + "," + CRLF );
            sb.append( prefix + "    maxReplays " + maxReplays + "," + CRLF );
            sb.append( prefix + "    valid " + valid.dumpValueNotation( prefix + "  ",dumpType ) + "," + CRLF );
            sb.append( prefix + "    forwardSecret "+forwardSecret + CRLF );
            sb.append( prefix + "    decryptionKey ''B," + CRLF );
            sb.append( prefix + "    requests {" + CRLF );
            for (HeaderRequest r : requests) {
                sb.append( valid.dumpValueNotation( prefix + "  ",dumpType ) + CRLF );
                sb.append( r.dumpValueNotation( prefix + "  " ) + CRLF );
            }
            sb.append( prefix + "    }" );
            if (padding != null) {
                sb.append( "," + CRLF );
                sb.append( prefix + "    padding " + toHex( padding ) + CRLF );
            } else {
                sb.append( CRLF );
            }
            sb.append( prefix + "  }," );
        }
        sb.append(prefix+"}");
        return sb.toString();
    }

    /***
     * Get the serial of the identity block.
     *
     * @return the currently set serial number
     */
    public long getSerial() {
        return serial;
    }

    /***
     * Set the serial of the identity block.
     *
     * @return the previously set serial number
     */
    public long setSerial(long serial) {
        long ret=getSerial();
        this.serial=serial;
        return ret;
    }



    @Override
    public boolean equals(Object t) {
        if(t==null) {
            return false;
        }
        if(! (t instanceof IdentityBlock) ) {
            return false;
        }
        IdentityBlock o=(IdentityBlock)t;
        try {
            return dumpValueNotation("", DumpType.ALL).equals(o.dumpValueNotation("", DumpType.ALL));
        } catch(IOException ioe) {
            return false;
        }
    }

    @Override
    public int hashCode() {
        // this methode is required for code sanity
        try{
            return dumpValueNotation("",DumpType.ALL).hashCode();
        } catch(IOException ioe) {
            return "FAILED".hashCode();
        }
    }

    public String toString() {
        return "Identity"+id;
    }

}
