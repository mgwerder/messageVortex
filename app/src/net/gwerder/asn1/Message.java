package net.gwerder.asn1;

import org.bouncycastle.asn1.*;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.NoSuchAlgorithmException;
import java.text.ParseException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Message extends Block {

    private static final int ROUTING=0;
    private static final int ROUTINGLOG=1;
    private static final int REPLY=2;

    protected Identity identity = null;
    protected Routing routing = null;
    protected RoutingLog routingLog = null;
    protected HeaderReply headerReply = null;
    protected Payload payload = null;

    public Message(File f) throws IOException,ParseException,NoSuchAlgorithmException {
        Path asn1DataPath = Paths.get(f.getAbsolutePath());
        byte[] p = Files.readAllBytes(asn1DataPath);
        parse( p );
    }

    public Message(Identity i,Payload p) {
        if(i==null) throw new NullPointerException( "Identity may not be null" );
        if(p==null) throw new NullPointerException( "Payload may not be null" );
        identity=i;
        payload=p;
    }

    protected void parse(ASN1Encodable p) throws IOException,ParseException,NoSuchAlgorithmException {
        Logger.getLogger("Message").log(Level.FINER,"Executing parse()");

        ASN1Sequence s1 = ASN1Sequence.getInstance( p );
        identity = new Identity( s1.getObjectAt( 0 ) );
        int i = 1;
        try {
            ASN1TaggedObject tmp = null;
            // get tagged block (if any)
            tmp = ASN1TaggedObject.getInstance( s1.getObjectAt( i ) ); //optional block

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
                tmp = ASN1TaggedObject.getInstance( s1.getObjectAt( ++i ) ); //optional block
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
    }

    @Override
    public ASN1Encodable encodeDER() {
        ASN1EncodableVector v2=new ASN1EncodableVector();
        if(identity==null) return null;
        v2.add(identity.encodeDER());
        if(routing!=null) v2.add( new DERTaggedObject( false,ROUTING,routing.encodeDER()));
        if(routingLog!=null) v2.add( new DERTaggedObject( false,ROUTINGLOG,routingLog.encodeDER()));
        if(headerReply!=null) v2.add( new DERTaggedObject( false,REPLY,headerReply.encodeDER()));
        v2.add(payload.encodeDER());
        ASN1Sequence seq=(ASN1Sequence)new DERSequence(v2);
        return seq;
    }

    public Identity getIdentity() { return identity; }
    public Routing getRouting() { return routing; }
    public RoutingLog getRoutingLog() { return routingLog; }
    public HeaderReply getHeaderReply() { return headerReply; }
    public Payload getPayload() { return payload; }

    public String dumpValueNotation(String prefix) throws IOException {
        StringBuilder sb=new StringBuilder();
        sb.append(prefix+"m Message ::= {"+CRLF);
        if(identity!=null) {
            sb.append(prefix + "  -- Dumping Identity"+CRLF);
            sb.append(prefix + "  header "+identity.dumpValueNotation(prefix+"  ")+","+CRLF);
        } else {
            sb.append(prefix + "  -- NO Identity"+CRLF);
        }

        if(routing!=null) {
            sb.append(prefix + "  -- Dumping Routing"+CRLF);
            sb.append(prefix + "  routing "+routing.dumpValueNotation(prefix+"  ")+","+CRLF);
        } else {
            sb.append(prefix + "  -- NO Routing"+CRLF);
        }
        if(routingLog!=null) {
            sb.append(prefix + "  -- Dumping RoutingLog"+CRLF);
            sb.append(prefix + "  routingLog "+routingLog.dumpValueNotation(prefix+"  ")+","+CRLF);
        } else {
            sb.append(prefix + "  -- NO RoutingLog"+CRLF);
        }
        if(headerReply!=null) {
            sb.append(prefix + "  -- Dumping HeaderReply"+CRLF);
            sb.append(prefix + "  headerReply "+headerReply.dumpValueNotation(prefix+"  ")+","+CRLF);
        } else {
            sb.append(prefix + "  -- NO HeaderReply"+CRLF);
        }
        if(payload!=null) {
            sb.append(prefix + "  -- Dumping Payload"+CRLF);
            sb.append(prefix+"  payload "+payload.dumpValueNotation(prefix+"  ")+CRLF);
        } else {
            sb.append(prefix + "  -- NO Payload"+CRLF);
        }
        sb.append(prefix+"}");
        return sb.toString();
    }

    public static void main(String[] args) throws Exception {
        System.out.println("\n;;; -------------------------------------------");
        System.out.println(";;; simple building message");
        System.out.println(";;; dumping");
        Message m=new Message(new Identity(),new Payload());
        System.out.println(m.dumpValueNotation(""));
        System.out.println("\n;;; -------------------------------------------");
        System.out.println(";;; Reading from File");
        Message msg=new Message(new File("example1.PDU.der"));
        System.out.println(";;; dumping");
        System.out.println(msg.dumpValueNotation(""));
    }
}
