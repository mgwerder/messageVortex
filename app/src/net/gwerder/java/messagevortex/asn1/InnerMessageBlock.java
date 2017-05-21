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


import net.gwerder.java.messagevortex.MessageVortexLogger;
import net.gwerder.java.messagevortex.asn1.encryption.Algorithm;
import net.gwerder.java.messagevortex.asn1.encryption.DumpType;
import net.gwerder.java.messagevortex.asn1.encryption.Padding;
import org.bouncycastle.asn1.*;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;

/***
 * represents the inner encrypted part of a VortexMessage.
 *
 * This part is specified as InnerMessageBlock in the file asn.1/messageBlocks.asn1
 */
public class InnerMessageBlock extends AbstractBlock {

    public static final int PREFIX_PLAIN      = 11011;
    public static final int PREFIX_ENCRYPTED  = 11012;

    public static final int IDENTITY_PLAIN    = 11021;
    public static final int IDENTITY_ENCRYPTED= 11022;

    public static final int ROUTING_PLAIN     = 11031;
    public static final int ROUTING_ENCRYPTED = 11032;

    private static final int PAYLOAD          = 11002;

    private byte[] padding=new byte[0];
    private PrefixBlock prefix;
    private IdentityBlock identity;
    private byte[] identitySignature=null;
    private RoutingBlock routing;
    private PayloadChunk[] payload;

    private static final java.util.logging.Logger LOGGER;
    static {
        LOGGER = MessageVortexLogger.getLogger((new Throwable()).getStackTrace()[0].getClassName());
        MessageVortexLogger.setGlobalLogLevel( Level.ALL);
    }

    public InnerMessageBlock() throws IOException {
        this(new PrefixBlock(),new IdentityBlock(), new RoutingBlock() );
    }

    public InnerMessageBlock(Algorithm sym,AsymmetricKey asym) throws IOException {
        this(new PrefixBlock(new SymmetricKey(sym)),new IdentityBlock(asym), new RoutingBlock() );
    }

    public InnerMessageBlock(PrefixBlock prefix,IdentityBlock i, RoutingBlock routing) throws IOException {
        this.prefix=prefix;
        identity=i;
        this.routing=routing;
        payload=new PayloadChunk[0];
    }

    public InnerMessageBlock(byte[] b,AsymmetricKey decryptionKey) throws IOException {
        this(null,null,null);
        parse( b,decryptionKey );
    }

    protected void parse(byte[] p, AsymmetricKey decryptionKey ) throws IOException {
        ASN1InputStream aIn=new ASN1InputStream( p );
        parse(aIn.readObject(),decryptionKey);
        if( identity==null ) {
            throw new NullPointerException( "IdentityBlock may not be null" );
        }
    }

    protected void parse(ASN1Encodable o ) throws IOException {
        parse(o);
    }

    protected void parse(ASN1Encodable o,AsymmetricKey decryptionKey ) throws IOException {
        LOGGER.log(Level.FINER,"Executing parse()");
        int i = 0;
        ASN1Sequence s1 = ASN1Sequence.getInstance( o );

        // get padding
        padding = ASN1OctetString.getInstance(s1.getObjectAt( i++ ) ).getOctets();

        // get prefix
        ASN1TaggedObject ato = ASN1TaggedObject.getInstance(s1.getObjectAt( i++ ));
        switch(ato.getTagNo()) {
            case PREFIX_PLAIN:
                prefix=new PrefixBlock(ato.getObject(),null);
                break;
            case PREFIX_ENCRYPTED:
                prefix=new PrefixBlock(ASN1OctetString.getInstance(ato.getObject()).getOctets(),decryptionKey);
                break;
            default:
                throw new IOException( "got unexpected tag (expect: " + PREFIX_PLAIN + " or "+PREFIX_ENCRYPTED+"; got: " + ato.getTagNo() + ")" );
        }

        // get identity
        ato = ASN1TaggedObject.getInstance(s1.getObjectAt( i++ ));
        byte[] identityEncoded;
        switch(ato.getTagNo()) {
            case IDENTITY_PLAIN:
                identityEncoded = toDER(ato.getObject());
                identity = new IdentityBlock( identityEncoded );
                break;
            case IDENTITY_ENCRYPTED:
                identityEncoded = ASN1OctetString.getInstance(ato.getObject()).getOctets();
                identity = new IdentityBlock( prefix.getKey().decrypt(identityEncoded) );
                break;
            default:
                throw new IOException( "got unexpected tag (expect: " + PREFIX_PLAIN + " or "+PREFIX_ENCRYPTED+"; got: " + ato.getTagNo() + ")" );
        }


        // get signature
        identitySignature=ASN1OctetString.getInstance(s1.getObjectAt( i++ ) ).getOctets();
        if(!identity.getIdentityKey().verify(identityEncoded,identitySignature)) {
            throw new IOException( "failed verifying signature (signature length:"+identitySignature.length+"; signedBlock:"+identityEncoded+")" );
        }

        // getting routing block
        ASN1TaggedObject ae = ASN1TaggedObject.getInstance( s1.getObjectAt( i++ ) );
        byte[] routingBlockArray;
        switch(ae.getTagNo()) {
            case ROUTING_PLAIN:
                routing=new RoutingBlock(ae.getObject());
                break;
            case ROUTING_ENCRYPTED:
                try {
                    routing = new RoutingBlock(ASN1Sequence.getInstance(prefix.getKey().decrypt(ASN1OctetString.getInstance(ae.getObject()).getOctets())));
                } catch(IOException ioe) {
                    throw new IOException("error while decrypting routing block",ioe);
                }
                break;
            default:
                throw new IOException( "got unexpected tag (expect: " + ROUTING_PLAIN + " or "+ROUTING_ENCRYPTED+"; got: " + ASN1TaggedObject.getInstance( ae ).getTagNo() + ")" );
        }

        // getting payload blocks
        List<PayloadChunk> p2=new ArrayList<>();
        for (Iterator<ASN1Encodable> iter= ASN1Sequence.getInstance( s1.getObjectAt( i++ ) ).iterator(); iter.hasNext(); ) {
            ASN1Encodable tr = iter.next();
            p2.add(new PayloadChunk(tr));
        }
        payload = p2.toArray(new PayloadChunk[p2.size()]);

    }

    public ASN1Object toASN1Object() throws IOException,NoSuchAlgorithmException,ParseException {
        return toASN1Object( DumpType.PUBLIC_ONLY );
    }

    @Override
    public ASN1Object toASN1Object(DumpType dumpType) throws IOException {
        // Prepare encoding
        LOGGER.log(Level.FINER,"Executing toASN1Object()");

        ASN1EncodableVector v=new ASN1EncodableVector();
        v.add( new DEROctetString(padding));
        switch(dumpType) {
            case ALL_UNENCRYPTED:
                v.add( new DERTaggedObject(PREFIX_PLAIN,prefix.toASN1Object(dumpType)));
                break;
            case ALL:
            case PUBLIC_ONLY:
            case PRIVATE_COMMENTED:
                if(prefix.getDecryptionKey()==null || !prefix.getDecryptionKey().hasPrivateKey()) {
                    throw new IOException( "need a decryption key to encrypt prefix block ("+prefix.getDecryptionKey()+")" );
                }
                v.add( new DERTaggedObject(PREFIX_ENCRYPTED,new DEROctetString(prefix.toEncBytes())));
                break;
            default:
        }
        if(identity==null) {
            throw new IOException("identity may not be null when encoding");
        }
        LOGGER.log(Level.FINER,"adding identity");
        byte[] o=null;
        switch(dumpType) {
            case ALL_UNENCRYPTED:
                ASN1Object t=identity.toASN1Object(dumpType);
                o= toDER(t);
                v.add( new DERTaggedObject(IDENTITY_PLAIN,t) );
                break;
            case ALL:
            case PUBLIC_ONLY:
            case PRIVATE_COMMENTED:
                o= prefix.getKey().encrypt(identity.toBytes(dumpType));
                v.add( new DERTaggedObject(IDENTITY_ENCRYPTED,new DEROctetString(o)) );
                break;
            default:
        }
        LOGGER.log(Level.FINER,"adding signature");
        if(identity.getIdentityKey()==null || ! identity.getIdentityKey().hasPrivateKey()) {
            throw new IOException( "identity needs private key to sign request ("+identity.getIdentityKey()+")" );
        }
        try {
            v.add(new DEROctetString(identity.getIdentityKey().sign(o)));
        } catch(IOException ioe) {
            throw new IOException("exception while signing identity",ioe);
        }

        // Writing encoded Routing Block
        if(routing==null) {
            throw new NullPointerException("routing may not be null when encoding");
        }
        switch(dumpType) {
            case ALL_UNENCRYPTED:
                v.add( new DERTaggedObject( true,ROUTING_PLAIN ,routing.toASN1Object(dumpType)));
                break;
            case ALL:
            case PUBLIC_ONLY:
            case PRIVATE_COMMENTED:
                v.add( new DERTaggedObject( true,ROUTING_ENCRYPTED ,new DEROctetString( prefix.getKey().encrypt(toDER(routing.toASN1Object(dumpType))) )));
                break;
            default:
                throw new IOException("got unknown dump type "+dumpType.name());
        }

        // Writing encoded Payload Blocks
        ASN1EncodableVector v2=new ASN1EncodableVector();
        if(payload!=null && payload.length>0) {
            for(PayloadChunk p:payload) {
                v2.add( p.toASN1Object(dumpType) );
            }
        }
        v.add( new DERSequence( v2 ));

        ASN1Sequence seq=new DERSequence(v);

        LOGGER.log(Level.FINER,"done toASN1Object()");
        return seq;
    }

    public IdentityBlock getIdentity() { return identity; }

    public RoutingBlock getRouting() { return routing; }

    public PayloadChunk[] getPayload() { return payload; }

    public PrefixBlock getPrefix() { return prefix; }

    @Override
    public String dumpValueNotation(String prefix, DumpType dt) throws IOException {
        StringBuilder sb=new StringBuilder();
        sb.append(  "  {" + CRLF );
        sb.append( prefix +"  padding "+toHex(padding)+CRLF);

        sb.append( prefix + "  -- Dumping IdentityBlock" + CRLF );
        sb.append( prefix + "  identity ");
        if(DumpType.ALL_UNENCRYPTED.equals(dt)) {
            // dumping plain identity
            sb.append( "plain " + identity.dumpValueNotation( prefix + "  ",DumpType.ALL_UNENCRYPTED ) + "," + CRLF );
        } else {
            // dumping encrypted identity
            sb.append( "encrypted " + identity.dumpValueNotation( prefix + "  ", dt ) + "," + CRLF );
        }

        sb.append( prefix + "  routing "+routing.dumpValueNotation( prefix+"  ",dt)+","+CRLF);

        sb.append( prefix + "  payload {" );
        int i=0;
        if(payload!=null){
            for(PayloadChunk p:payload) {
                if(i>0) {
                    sb.append( "," );
                }
                sb.append( CRLF );
                p.dumpValueNotation( prefix+"  ",dt);
            }
        }
        sb.append( CRLF + prefix + "  }" + CRLF );

        sb.append( prefix + "}"  );
        return sb.toString();
    }

    @Override
    public boolean equals(Object o) {
        if(o==null) {
            return false;
        }
        if(! (o instanceof InnerMessageBlock)) {
            return false;
        }
        InnerMessageBlock ib=(InnerMessageBlock)o;
        try {
            return toBytes(DumpType.ALL_UNENCRYPTED).equals(ib.toBytes(DumpType.ALL_UNENCRYPTED));
        } catch(IOException ioe) {
            return false;
        }
    }

}
