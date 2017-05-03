package net.gwerder.java.messagevortex.asn1;
/***
 * Copyright (c) 2018 Martin Gwerder (martin@gwerder.net)
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 ***/


import net.gwerder.java.messagevortex.MessageVortexLogger;
import net.gwerder.java.messagevortex.asn1.encryption.DumpType;
import org.bouncycastle.asn1.*;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.NoSuchAlgorithmException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;

/***
 * represents the inner encrypted part of a VortexMessage.
 */
public class InnerMessageBlock extends AbstractBlock {

    public static final int PLAIN_MESSAGE=10011;
    public static final int ENCRYPTED_MESSAGE=10012;

    private static final int ROUTING          = 11001;
    private static final int PAYLOAD          = 11002;

    private IdentityBlock identity;
    private RoutingBlock[] routing;
    private Payload[] payload;

    private static final java.util.logging.Logger LOGGER;
    static {
        LOGGER = MessageVortexLogger.getLogger((new Throwable()).getStackTrace()[0].getClassName());
        MessageVortexLogger.setGlobalLogLevel( Level.ALL);
    }

    public InnerMessageBlock(IdentityBlock i) {
        identity=i;
        routing=null;
        payload=null;
    }

    public InnerMessageBlock(File f) throws IOException,ParseException,NoSuchAlgorithmException {
        this((IdentityBlock)null);
        Path asn1DataPath = Paths.get(f.getAbsolutePath());
        byte[] p = Files.readAllBytes(asn1DataPath);
        parse( p );
    }

    public InnerMessageBlock(byte[] b) throws IOException,ParseException,NoSuchAlgorithmException {
        this((IdentityBlock)null);
        parse( b );
    }

    protected void parse(byte[] p) throws IOException,ParseException,NoSuchAlgorithmException {
        ASN1InputStream aIn=new ASN1InputStream( p );
        parse(aIn.readObject());
        if( identity==null ) {
            throw new NullPointerException( "IdentityBlock may not be null" );
        }
    }

    protected void parse(ASN1Encodable o) throws IOException,ParseException,NoSuchAlgorithmException {
        LOGGER.log(Level.FINER,"Executing parse()");
        int i = 0;
        ASN1Sequence s1 = ASN1Sequence.getInstance( o );
        identity = new IdentityBlock( s1.getObjectAt( i++ ) );

        // check tag number of routing block
        ASN1TaggedObject ae = ASN1TaggedObject.getInstance( s1.getObjectAt( i++ ) );
        if(ASN1TaggedObject.getInstance(ae).getTagNo()!=ROUTING) {
            throw new ParseException( "got unexpected tag (expect: " + ROUTING + "; got: " + ASN1TaggedObject.getInstance( ae ).getTagNo() + ")", 0 );
        }

        // getting routing blocks
        List<RoutingBlock> v=new ArrayList<>();
        for (Iterator<ASN1Encodable> iter= ASN1Sequence.getInstance( ASN1TaggedObject.getInstance(ae).getObject() ).iterator(); iter.hasNext(); ) {
            ASN1Encodable tr = iter.next();
            v.add(new RoutingBlock(tr));
        }
        routing=v.toArray(new RoutingBlock[v.size()]);

        // check tag number of payloadblock
        ae = ASN1TaggedObject.getInstance( s1.getObjectAt( i++ ) );
        if(ASN1TaggedObject.getInstance(ae).getTagNo()!=PAYLOAD) {
            throw new ParseException( "got unexpected tag (expect: " + PAYLOAD+ "; got: " + ASN1TaggedObject.getInstance( ae ).getTagNo() + ")", 0 );
        }

        // getting routing blocks
        List<Payload> p=new ArrayList<>();
        for (Iterator<ASN1Encodable> iter= ASN1Sequence.getInstance( ASN1TaggedObject.getInstance(ae).getObject() ).iterator(); iter.hasNext(); ) {
            ASN1Encodable tr = iter.next();
            p.add(new Payload(tr));
        }
        payload=p.toArray(new Payload[p.size()]);

    }

    public ASN1Object toASN1Object() throws IOException,NoSuchAlgorithmException,ParseException {
        return toASN1Object( DumpType.PUBLIC_ONLY );
    }

    public ASN1Object toASN1Object(DumpType dt) throws IOException {
        // Prepare encoding
        LOGGER.log(Level.FINER,"Executing toASN1Object()");

        ASN1EncodableVector v=new ASN1EncodableVector();
        if(identity==null) {
            throw new IOException("identity may not be null when encoding");
        }
        LOGGER.log(Level.FINER,"adding identity");
        ASN1Encodable o=null;
        o = identity.toASN1Object();
        if (o == null) {
            throw new IOException( "returned identity object may not be null" );
        }
        v.add( o );

        // Writing encoded Routing Blocks
        ASN1EncodableVector v2=new ASN1EncodableVector();
        if(routing!=null) {
            for(RoutingBlock r:routing) {
                v2.add( r.toASN1Object() );
            }
        }
        v.add( new DERTaggedObject( true,ROUTING    ,new DERSequence( v2 )));

        // Writing encoded Payload Blocks
        v2=new ASN1EncodableVector();
        if(payload!=null) {
            for(Payload p:payload) {
                v2.add( p.toASN1Object() );
            }
        }
        v.add( new DERTaggedObject( true,PAYLOAD    ,new DERSequence( v2 )));

        ASN1Sequence seq=new DERSequence(v);

        LOGGER.log(Level.FINER,"done toASN1Object()");
        return seq;
    }

    public IdentityBlock getIdentity() { return identity; }

    public RoutingBlock[] getRouting() { return routing; }

    public Payload[] getPayload() { return payload; }

    public String dumpValueNotation(String prefix) throws IOException {
        return dumpValueNotation( prefix, null, DumpType.PUBLIC_ONLY );
    }

    public String dumpValueNotation(String prefix, SymmetricKey sessionKey,DumpType dt) throws IOException {
        StringBuilder sb=new StringBuilder();
        sb.append(  "  {" + CRLF );
        // FIXME dump padding

        sb.append( prefix + "  -- Dumping IdentityBlock" + CRLF );
        sb.append( prefix + "  identity ");
        if(DumpType.ALL_UNENCRYPTED.equals(dt)) {
            // dumping plain identity
            sb.append( "plain " + identity.dumpValueNotation( prefix + "  ",DumpType.ALL_UNENCRYPTED ) + "," + CRLF );
        } else {
            // dumping encrypted identity
            sb.append( "encrypted " + identity.dumpValueNotation( prefix + "  ", DumpType.PUBLIC_ONLY ) + "," + CRLF );
        }

        // FIXME dump routing

        // FIXME dump payload

        sb.append( prefix + "}" + CRLF );
        return sb.toString();
    }

}
