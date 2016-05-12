package net.gwerder.asn1;

import org.bouncycastle.asn1.*;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.NoSuchAlgorithmException;
import java.text.ParseException;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Message extends Block {

    private static final int ROUTING          = 0;
    private static final int ROUTINGLOG       = 1;
    private static final int REPLY            = 2;
    private static final int HEADER_ENCRYPTED = 1001;
    private static final int HEADER_PLAIN     = 1002;
    private static final int BLOCKS_ENCRYPTED = 1101;
    private static final int BLOCKS_PLAIN     = 1102;


    private Identity identity;
    private Routing routing;
    private RoutingLog routingLog ;
    private HeaderReply headerReply;
    private Payload payload;

    private Message() {
        identity=null;
        routing=null;
        routingLog=null;
        headerReply=null;
        payload=null;
    }

    public Message(File f) throws IOException,ParseException,NoSuchAlgorithmException {
        this();
        Path asn1DataPath = Paths.get(f.getAbsolutePath());
        byte[] p = Files.readAllBytes(asn1DataPath);
        parse( p );
    }

    public Message(byte[] b) throws IOException,ParseException,NoSuchAlgorithmException {
        this();
        parse( b );
    }

    public Message(Identity i,Payload p) {
        this();
        if( i==null ) throw new NullPointerException( "Identity may not be null" );
        if( p==null ) throw new NullPointerException( "Payload may not be null" );
        identity=i;
        payload=p;
    }

    protected void parse(byte[] p) throws IOException,ParseException,NoSuchAlgorithmException {
        ASN1InputStream aIn=new ASN1InputStream( p );
        parse(aIn.readObject());
        if( identity==null ) throw new NullPointerException( "Identity may not be null" );
        if( payload==null ) throw new NullPointerException( "Payload may not be null" );
    }

    protected void parse(ASN1Encodable p) throws IOException,ParseException,NoSuchAlgorithmException {
        Logger.getLogger("Message").log(Level.FINER,"Executing parse()");

        ASN1Sequence s1 = ASN1Sequence.getInstance( p );
        identity = new Identity( ((ASN1TaggedObject)(s1.getObjectAt( 0 ))).getObject(),null );
        int i = 1;
        try {
            // get tagged block (if any)
            ASN1TaggedObject tmp= ASN1TaggedObject.getInstance( s1.getObjectAt( i ) ); //optional block;

            // testing for routingBlock
            if (tmp.getTagNo() == ROUTING) {
                routing = new Routing( tmp.getObject() ); //optional block
                tmp = ASN1TaggedObject.getInstance( s1.getObjectAt( ++i ) ); //optional block
            }

            // testing for routingLogBlock
            if (tmp.getTagNo() == ROUTINGLOG) {
                routingLog = new RoutingLog( tmp.getObject() ); //optional block
                tmp = ASN1TaggedObject.getInstance( s1.getObjectAt( ++i ) ); //optional block
            }

            // Testing for reply block
            if (tmp.getTagNo() == REPLY) {
                headerReply = new HeaderReply( tmp.getObject() ); //optional block
                // codeblock obsoleted as it is the last possible block
                // tmp = ASN1TaggedObject.getInstance( s1.getObjectAt( ++i ) ); //optional block
            }

        } catch (IllegalArgumentException iae) {
            // if this happens we have reached the payload block
        }
        try {
            // getting payload
            payload = new Payload( s1.getObjectAt( i ) ); // payloadblock
        } catch (IllegalArgumentException iae) {
            Logger.getLogger( "Message" ).log( Level.WARNING, "Error while parsing payload block in message", iae );
            throw iae;
        }
        if( identity==null ) throw new NullPointerException( "Identity may not be null" );
        if( payload==null ) throw new NullPointerException( "Payload may not be null" );
    }

    public ASN1Object toASN1Object(AsymmetricKey targetNodeIdentity, SymmetricKey blocksKey) throws IOException {
        ASN1EncodableVector v=new ASN1EncodableVector();
        if(blocksKey==null) blocksKey=identity.decryptionKey;
        if(targetNodeIdentity==null) {
            v.add(new DERTaggedObject( false, HEADER_PLAIN, identity.toASN1Object()));
        } else {
            try{
                v.add( new DERTaggedObject( false, HEADER_ENCRYPTED, new DEROctetString( targetNodeIdentity.encrypt( identity.toBytes(),true )) ) );
            } catch(Exception e) {
                throw new IOException("Exception while encrypting identity",e);
            }
        }
        ASN1EncodableVector v3=new ASN1EncodableVector();
        if(routing!=null)      v3.add( new DERTaggedObject( false,ROUTING    ,routing.toASN1Object()));
        if(routingLog!=null)   v3.add( new DERTaggedObject( false,ROUTINGLOG ,routingLog.toASN1Object()));
        if(headerReply!=null)  v3.add( new DERTaggedObject( false,REPLY      ,headerReply.toASN1Object()));
        v3.add(payload.toASN1Object());
        v.add(new DERTaggedObject( false, BLOCKS_ENCRYPTED, new DEROctetString( blocksKey.encrypt((new DERSequence( v3 )).getEncoded()))));
        return new DERSequence( v );
    }

    public ASN1Object toASN1Object() throws IOException {
        Logger.getLogger("Message").log(Level.FINER,"Executing toASN1Object()");
        // FIXME adapt encrypted/decrypted structure
        ASN1EncodableVector v2=new ASN1EncodableVector();
        if(identity==null) throw new IOException("identity may not be null when encoding");
        Logger.getLogger("Message").log(Level.FINER,"adding identity");
        ASN1Encodable o=identity.toASN1Object();
        if(o==null) throw new IOException("returned identity object may not be null");
        v2.add(new DERTaggedObject( false, HEADER_PLAIN, o ));
        ASN1EncodableVector v3=new ASN1EncodableVector();
        if(routing!=null)      v3.add( new DERTaggedObject( false,ROUTING    ,routing.toASN1Object()));
        if(routingLog!=null)   v3.add( new DERTaggedObject( false,ROUTINGLOG ,routingLog.toASN1Object()));
        if(headerReply!=null)  v3.add( new DERTaggedObject( false,REPLY      ,headerReply.toASN1Object()));
        if(payload !=null)     v3.add(payload.toASN1Object());
        Logger.getLogger("Message").log(Level.FINER,"adding blocks");
        v2.add(new DERTaggedObject( false, BLOCKS_PLAIN, new DERSequence( v3 )));
        ASN1Sequence seq=new DERSequence(v2);
        Logger.getLogger("Message").log(Level.FINER,"done toASN1Object()");
        return seq;
    }

    public Identity getIdentity() { return identity; }
    public Routing getRouting() { return routing; }
    public RoutingLog getRoutingLog() { return routingLog; }
    public HeaderReply getHeaderReply() { return headerReply; }
    public Payload getPayload() { return payload; }

    public String dumpValueNotation(String prefix) throws IOException {
        return dumpValueNotation( prefix,null,null );
    }

    public String dumpValueNotation(String prefix, AsymmetricKey headerKey, SymmetricKey sessionKey) throws IOException {
        StringBuilder sb=new StringBuilder();
        sb.append( prefix + "m Message ::= {" + CRLF );
        if(identity!=null) {
            sb.append( prefix + "  -- Dumping Identity" + CRLF );
            if(headerKey==null) {
                sb.append( prefix + "  header plain " + identity.dumpValueNotation( prefix + "  " ) + "," + CRLF );
            } else {
                try {
                    sb.append( prefix + "  header encrypted " + toHex( headerKey.encrypt( identity.toBytes(), true ) ) + CRLF );
                } catch(Exception e) {
                    throw new IOException( "Exception while encrypting stream",e );
                }
            }
        } else {
            sb.append(prefix + "  -- NO Identity"+CRLF);
        }

        if(sessionKey==null) {
            sb.append( prefix + "  blocks plain {" + CRLF );

            if (routing != null) {
                sb.append( prefix + "    -- Dumping Routing" + CRLF );
                sb.append( prefix + "    routing " + routing.dumpValueNotation( prefix + "  " ) + "," + CRLF );
            } else {
                sb.append( prefix + "    -- NO Routing" + CRLF );
            }
            if (routingLog != null) {
                sb.append( prefix + "    -- Dumping RoutingLog" + CRLF );
                sb.append( prefix + "    routingLog " + routingLog.dumpValueNotation( prefix + "  " ) + "," + CRLF );
            } else {
                sb.append( prefix + "    -- NO RoutingLog" + CRLF );
            }
            if (headerReply != null) {
                sb.append( prefix + "    -- Dumping HeaderReply" + CRLF );
                sb.append( prefix + "    headerReply " + headerReply.dumpValueNotation( prefix + "  " ) + "," + CRLF );
            } else {
                sb.append( prefix + "    -- NO HeaderReply" + CRLF );
            }
            if (payload != null) {
                sb.append( prefix + "    -- Dumping Payload" + CRLF );
                sb.append( prefix + "    payload " + payload.dumpValueNotation( prefix + "    " ) + CRLF );
            } else {
                sb.append( prefix + "    -- NO Payload" + CRLF );
            }
            sb.append( prefix + "  }" + CRLF );
        } else {
            sb.append( prefix + "  blocks encrypted -- FIXME not yet supported" + CRLF );
        }
        sb.append( prefix + "}" + CRLF );
        return sb.toString();
    }

    public static void main(String[] args) throws Exception {
        System.out.println("\n;;; -------------------------------------------");
        System.out.println(";;; creating blank dump");
        Message m=new Message(new Identity(),new Payload());
        System.out.println(m.dumpValueNotation(""));

        System.out.println("\n;;; -------------------------------------------");
        System.out.println(";;; Reading from File");
        Message msg=new Message(new File("example1.PDU.der"));
        System.out.println(";;; dumping");
        System.out.println(msg.dumpValueNotation(""));

        System.out.println("\n;;; -------------------------------------------");
        System.out.println(";;; simple building message");
        Message m2=new Message(new Identity(),new Payload());
        System.out.println(";;; dumping");
        System.out.println(m2.dumpValueNotation(""));
        System.out.println(";;; reencode check");
        System.out.println(";;;   getting DER stream");
        byte[] b1=m.toBytes();
        System.out.println(";;;   parsing DER stream");
        Message m3=new Message(b1);
        System.out.println(";;;   getting DER stream again");
        byte[] b2=m3.toBytes();
        System.out.println(";;;   comparing");
        if(Arrays.equals(b1,b2) ) {
            System.out.println( "Reencode success" );
        } else {
            System.out.println( "Reencode FAILED" );
        }
    }
}
