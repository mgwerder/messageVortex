package net.gwerder.java.mailvortex.asn1;

import net.gwerder.java.mailvortex.asn1.encryption.Algorithm;
import net.gwerder.java.mailvortex.asn1.encryption.AlgorithmType;
import net.gwerder.java.mailvortex.asn1.encryption.Padding;
import org.bouncycastle.asn1.*;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.text.ParseException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created by martin.gwerder on 26.05.2016.
 */
public class IdentityStoreBlock extends Block {

    public enum IdentityType{
        OWNED_IDENTITY,
        NODE_IDENTITY,
        RECIPIENT_IDENTITY
    }

    UsagePeriod   valid         = null;
    int           messageQuota  = 0;
    int           transferQuota = 0;
    AsymmetricKey identityKey   = null;
    String        nodeAddress   = null;
    AsymmetricKey nodeKey       = null;


    public IdentityStoreBlock() {
        super();
    }

    public IdentityStoreBlock(ASN1Encodable ae) throws ParseException,NoSuchAlgorithmException,IOException {
        parse(ae);
    }

    public static IdentityStoreBlock getIdentityStoreBlockDemo(IdentityType it,boolean complete) throws IOException {
        SecureRandom r=new SecureRandom(  );
        IdentityStoreBlock ret= new IdentityStoreBlock();
        ret.setValid(new UsagePeriod(3600*24*30));
        ret.setTransferQuota(r.nextInt( 1024*1024*1024 ));
        ret.setMessageQuota(r.nextInt( 1024*1024 ));
        switch(it) {
            case OWNED_IDENTITY:
                try {
                    ret.setIdentityKey( new AsymmetricKey() );
                    byte [] b=new byte[r.nextInt(20)+3];
                    r.nextBytes( b );
                    ret.setNodeAddress( "smtp:"+toHex( b )+"@localhost" );
                    ret.setNodeKey(null);
                } catch(Exception e) {
                    throw new IOException("Exception while generating identity",e);
                }
                break;
            case NODE_IDENTITY:
                try {
                    ret.setIdentityKey( null );
                    byte [] b=new byte[r.nextInt(20)+3];
                    r.nextBytes( b );
                    ret.setNodeAddress( "smtp:"+toHex( b )+"@demo"+r.nextInt( 3 ) );
                    AsymmetricKey ak=new AsymmetricKey();
                    if(!complete) ak.setPrivateKey(null);
                    ret.setNodeKey(ak);
                } catch(Exception e) {
                    throw new IOException("Exception while generating identity",e);
                }
                break;
            case RECIPIENT_IDENTITY:
                try {
                    AsymmetricKey ak=new AsymmetricKey();
                    if(!complete) ak.setPrivateKey(null);
                    ret.setIdentityKey( ak );
                    byte [] b=new byte[r.nextInt(20)+3];
                    r.nextBytes( b );
                    ret.setNodeAddress( "smtp:"+toHex( b )+"@demo"+r.nextInt( 3 ) );
                    ak=new AsymmetricKey();
                    if(!complete) ak.setPrivateKey(null);
                    ret.setNodeKey(ak);
                } catch(Exception e) {
                    throw new IOException("Exception while generating identity",e);
                }
                break;
        }
        return ret;
    }

    public AsymmetricKey setIdentityKey(AsymmetricKey k) {
        AsymmetricKey old=identityKey;
        identityKey=k;
        return old;
    }

    public AsymmetricKey getIdentityKey() { return identityKey; }

    public UsagePeriod setValid(UsagePeriod np) {
        UsagePeriod old=valid;
        valid=np;
        return old;
    }

    public UsagePeriod getValid() { return valid; }

    public int setMessageQuota(int nq) {
        int old=messageQuota;
        messageQuota=nq;
        return old;
    }

    public int getMessageQuota() { return messageQuota; }

    public int setTransferQuota(int tq) {
        int old=transferQuota;
        transferQuota=tq;
        return old;
    }

    public int getTransferQuota() { return transferQuota; }

    public String setNodeAddress(String na) {
        String old=nodeAddress;
        nodeAddress=na;
        return old;
    }

    public String getNodeAddress() { return nodeAddress; }

    public AsymmetricKey setNodeKey(AsymmetricKey k) {
        AsymmetricKey old=nodeKey;
        nodeKey=k;
        return old;
    }

    public AsymmetricKey getNodeKey() { return nodeKey; }

    protected void parse(ASN1Encodable p) throws IOException,ParseException,NoSuchAlgorithmException {
        Logger.getLogger( "IdentityStoreBlock" ).log( Level.FINER, "Executing parse()" );
        ASN1Sequence s1 = ASN1Sequence.getInstance( p );
        int i=0;
        valid = new UsagePeriod( s1.getObjectAt( i++ ) );
        messageQuota=ASN1Integer.getInstance( s1.getObjectAt( i++ ) ).getValue().intValue();
        transferQuota=ASN1Integer.getInstance( s1.getObjectAt( i++ ) ).getValue().intValue();
        Logger.getLogger( "IdentityStoreBlock" ).log( Level.FINER, "Finished parse()" );
        for(;i<s1.size();i++) {
            ASN1TaggedObject to = ASN1TaggedObject.getInstance( s1.getObjectAt( i ) );
            switch(to.getTagNo()) {
                case 1001:
                    identityKey=new AsymmetricKey( to.getObject() );
                    break;
                case 1002:
                    nodeAddress=((ASN1String)(to.getObject())).getString();
                    break;
                case 1003:
                    nodeKey=new AsymmetricKey( to.getObject() );
                    break;
            }
        }
    }

    public ASN1Object toASN1Object() throws IOException {
        // Prepare encoding
        Logger.getLogger("IdentityStoreBlock").log( Level.FINER,"Executing toASN1Object()");

        ASN1EncodableVector v=new ASN1EncodableVector();

        v.add(valid.toASN1Object());
        v.add(new ASN1Integer( messageQuota ));
        v.add(new ASN1Integer( transferQuota ));

        if(identityKey!=null) v.add(new DERTaggedObject( true,1001,identityKey.toASN1Object() ));
        if(nodeAddress!=null) v.add(new DERTaggedObject( true,1002, new DERIA5String(nodeAddress)));
        if(nodeKey!=null)     v.add(new DERTaggedObject( true,1003,nodeKey.toASN1Object() ));

        ASN1Sequence seq=new DERSequence(v);
        Logger.getLogger("IdentityStoreBlock").log(Level.FINER,"done toASN1Object()");
        return seq;
    }

    public String dumpValueNotation(String prefix) throws IOException {
        StringBuilder sb=new StringBuilder();
        sb.append( "{" + CRLF );
        sb.append( prefix + "  valid "+valid.dumpValueNotation( prefix+"    " ) +","+CRLF );
        sb.append( prefix + "  messageQuota "+messageQuota+","+CRLF );
        sb.append( prefix + "  transferQuota "+transferQuota );
        if(identityKey!=null) sb.append( ","+CRLF+prefix + "  identity "+identityKey.dumpValueNotation( prefix+"    " ) );
        if(nodeAddress!=null) sb.append( ","+CRLF+prefix + "  nodeAddress \""+nodeAddress+"\"" );
        if(nodeKey!=null)     sb.append( ","+CRLF+prefix + "  nodeKey "+nodeKey.dumpValueNotation( prefix+"    " ) );
        sb.append( CRLF );
        sb.append( prefix + "}" );
        return sb.toString();
    }

}
