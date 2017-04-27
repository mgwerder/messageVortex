package net.gwerder.java.messagevortex.asn1;

import net.gwerder.java.messagevortex.asn1.encryption.DumpType;
import org.bouncycastle.asn1.*;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.NoSuchAlgorithmException;
import java.text.ParseException;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;

public class InnerMessage extends Block {

    private static final int ROUTING          = 11001;
    private static final int PAYLOAD          = 11002;

    private Identity identity;
    private Routing[] routing;
    private Payload[] payload;

    public InnerMessage() {
        identity=null;
        routing=null;
        payload=null;
    }

    public InnerMessage(File f,SymmetricKey messageKey) throws IOException,ParseException,NoSuchAlgorithmException {
        this();
        Path asn1DataPath = Paths.get(f.getAbsolutePath());
        byte[] p = Files.readAllBytes(asn1DataPath);
        if(messageKey!=null) {
            p=messageKey.decrypt(p);
        }
        parse( p );
    }

    public InnerMessage(byte[] b,SymmetricKey messageKey) throws IOException,ParseException,NoSuchAlgorithmException {
        this();
        if(messageKey!=null) {
            b=messageKey.decrypt(b);
        }
        parse( b );
    }

    protected void parse(byte[] p) throws IOException,ParseException,NoSuchAlgorithmException {
        ASN1InputStream aIn=new ASN1InputStream( p );
        parse(aIn.readObject());
        if( identity==null ) {
            throw new NullPointerException( "Identity may not be null" );
        }
        if( payload==null ) {
            throw new NullPointerException( "Payload may not be null" );
        }
    }

    protected void parse(ASN1Encodable o) throws IOException,ParseException,NoSuchAlgorithmException {
        Logger.getLogger("VortexMessage").log(Level.FINER,"Executing parse()");
        int i = 0;
        ASN1Sequence s1 = ASN1Sequence.getInstance( o );
        identity = new Identity( s1.getObjectAt( i++ ) );

        // check tag number of routing block
        ASN1TaggedObject ae = ASN1TaggedObject.getInstance( s1.getObjectAt( i++ ) );
        if(ASN1TaggedObject.getInstance(ae).getTagNo()!=ROUTING) {
            throw new ParseException( "got unexpected tag (expect: " + ROUTING + "; got: " + ASN1TaggedObject.getInstance( ae ).getTagNo() + ")", 0 );
        }

        // getting routing blocks
        List<Routing> v=new Vector<>();
        for (Iterator<ASN1Encodable> iter= ASN1Sequence.getInstance( ASN1TaggedObject.getInstance(ae).getObject() ).iterator(); iter.hasNext(); ) {
            ASN1Encodable tr = iter.next();
            v.add(new Routing(tr));
        }
        routing=v.toArray(new Routing[v.size()]);

        // check tag number of payloadblock
        ae = ASN1TaggedObject.getInstance( s1.getObjectAt( i++ ) );
        if(ASN1TaggedObject.getInstance(ae).getTagNo()!=PAYLOAD) {
            throw new ParseException( "got unexpected tag (expect: " + PAYLOAD+ "; got: " + ASN1TaggedObject.getInstance( ae ).getTagNo() + ")", 0 );
        }

        // getting routing blocks
        List<Payload> p=new Vector<>();
        for (Iterator<ASN1Encodable> iter= ASN1Sequence.getInstance( ASN1TaggedObject.getInstance(ae).getObject() ).iterator(); iter.hasNext(); ) {
            ASN1Encodable tr = iter.next();
            p.add(new Payload(tr));
        }
        payload=p.toArray(new Payload[p.size()]);

    }

    public ASN1Object toASN1Object() throws IOException,NoSuchAlgorithmException,ParseException {
        return toASN1Object( null, DumpType.PUBLIC_ONLY );
    }

    public ASN1Object toASN1Object(SymmetricKey messageKey,DumpType dt) throws IOException,NoSuchAlgorithmException,ParseException {
        // Prepare encoding
        Logger.getLogger("VortexMessage").log(Level.FINER,"Executing toASN1Object()");

        ASN1EncodableVector v=new ASN1EncodableVector();
        if(identity==null) throw new IOException("identity may not be null when encoding");
        Logger.getLogger("VortexMessage").log(Level.FINER,"adding identity");
        ASN1Encodable o=identity.toASN1Object();
        if (o == null) {
            throw new IOException( "returned identity object may not be null" );
        }
        v.add( o );

        // Writing encoded Routing Blocks
        ASN1EncodableVector v2=new ASN1EncodableVector();
        for(Routing r:routing) {
            v2.add( r.toASN1Object() );
        }
        v.add( new DERTaggedObject( true,ROUTING    ,new DERSequence( v2 )));

        // Writing encoded Payload Blocks
        v2=new ASN1EncodableVector();
        for(Payload p:payload) {
            v2.add( p.toASN1Object() );
        }
        v.add( new DERTaggedObject( true,PAYLOAD    ,new DERSequence( v2 )));

        ASN1Sequence seq=new DERSequence(v);

        Logger.getLogger("VortexMessage").log(Level.FINER,"done toASN1Object()");
        return seq;
    }

    public Identity getIdentity() { return identity; }

    public Routing[] getRouting() { return routing; }

    public Payload[] getPayload() { return payload; }

    public String dumpValueNotation(String prefix) throws IOException {
        return dumpValueNotation( prefix, null, DumpType.PUBLIC_ONLY );
    }

    public String dumpValueNotation(String prefix, SymmetricKey sessionKey,DumpType dt) throws IOException {
        StringBuilder sb=new StringBuilder();
        sb.append(  "  {" + CRLF );
        // FIXME dump padding

        sb.append( prefix + "  -- Dumping Identity" + CRLF );
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

    /***
     * converts an unsigned long value into a byte array representation (LSB).
     *
     * @param i the long value to be converted
     * @param num the number of bytes to be returned
     * @return the unsigned byte array representation
     */
    public static byte[] getLongAsBytes(long i, int num) {
        byte[] ret = new byte[num];
        for (int j = 0; j < num; j++) {
            ret[j]=(byte)((i>>(j*8))&0xFF);
        }
        return ret;
    }

    /***
     * converts an unsigned long value into a 32 bit byte array representation (LSB).
     *
     * @param i the long value to be converted
     * @return the unsigned byte array of length 4 representation
     */
    public static byte[] getLongAsBytes(long i) {
        return getLongAsBytes(i,4);
    }

    /***
     * converts a number of bytes into a long representation (LSB)
     *
     * @param b the byte array to be converted to long
     * @return the long representation of the byte array
     */
    public static long getBytesAsInteger(byte[] b) {
        if(b==null || b.length<1 || b.length>8) throw new IllegalArgumentException( "byte array must contain exactly four bytes" );
        long ret=0;
        for(int i=0;i<b.length;i++) {
            ret|=((long)(b[i]&0xFF))<<(i*8);
        }
        return ret;
    }

}
