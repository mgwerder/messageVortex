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

import net.gwerder.java.messagevortex.ExtendedSecureRandom;
import net.gwerder.java.messagevortex.MessageVortexLogger;
import net.gwerder.java.messagevortex.asn1.encryption.Algorithm;
import net.gwerder.java.messagevortex.asn1.encryption.DumpType;
import net.gwerder.java.messagevortex.asn1.encryption.SecurityLevel;
import org.bouncycastle.asn1.*;

import java.io.IOException;
import java.util.logging.Level;

/**
 * This class represents one block of an identity store for storage.
 */
public class IdentityStoreBlock extends AbstractBlock {

    public enum IdentityType {
        OWNED_IDENTITY,
        NODE_IDENTITY,
        RECIPIENT_IDENTITY
    }

    private static final java.util.logging.Logger LOGGER;
    static {
        LOGGER = MessageVortexLogger.getLogger((new Throwable()).getStackTrace()[0].getClassName());
        MessageVortexLogger.setGlobalLogLevel( Level.ALL);
    }

    private static ExtendedSecureRandom secureRandom = new ExtendedSecureRandom();

    private UsagePeriod   valid         = null;
    private int           messageQuota  = 0;
    private int           transferQuota = 0;
    private AsymmetricKey identityKey   = null;
    private String        nodeAddress   = null;
    private AsymmetricKey nodeKey       = null;
    private IdentityType  iType         = null;

    public IdentityStoreBlock() {super();}

    public IdentityStoreBlock(ASN1Encodable ae) throws IOException {
        parse(ae);
    }

    @Override
    public int hashCode() {
        try {
            return dumpValueNotation("", DumpType.ALL_UNENCRYPTED).hashCode();
        } catch(IOException e) {
            return "".hashCode();
        }
    }

    public static IdentityStoreBlock getIdentityStoreBlockDemo(IdentityType it,boolean complete) throws IOException {
        IdentityStoreBlock ret= new IdentityStoreBlock();
        ret.setValid(new UsagePeriod(3600*24*30));
        ret.setTransferQuota( secureRandom.nextInt( 1024 * 1024 * 1024 ) );
        ret.setMessageQuota( secureRandom.nextInt( 1024 * 1024 ) );
        ret.iType=it;
        switch(it) {
            case OWNED_IDENTITY:
                // my own identity to decrypt everything
                try {
                    ret.setIdentityKey( new AsymmetricKey(Algorithm.RSA.getParameters(SecurityLevel.LOW)) );
                    byte[] b = new byte[secureRandom.nextInt( 20 ) + 3];
                    secureRandom.nextBytes( b );
                    ret.setNodeAddress( "smtp:"+toHex( b )+"@localhost" );
                    ret.setNodeKey(null);
                } catch(Exception e) {
                    throw new IOException("Exception while generating owned identity",e);
                }
                break;
            case NODE_IDENTITY:
                // My identities I have on remote nodes
                try {
                    ret.setIdentityKey( null );
                    byte[] b = new byte[secureRandom.nextInt( 20 ) + 3];
                    secureRandom.nextBytes( b );
                    ret.setNodeAddress( "smtp:" + toHex( b ) + "@demo" + secureRandom.nextInt( 3 ) );
                    AsymmetricKey ak=new AsymmetricKey();
                    if(!complete) {
                        ak.setPrivateKey(null);
                    }
                    ret.setNodeKey(ak);
                } catch(Exception e) {
                    throw new IOException("Exception while generating node identity",e);
                }
                break;
            case RECIPIENT_IDENTITY:
                // Identities for receiving mails
                try {
                    AsymmetricKey ak=new AsymmetricKey(Algorithm.EC.getParameters(SecurityLevel.LOW));
                    if(!complete) {
                        ak.setPrivateKey(null);
                    }
                    ret.setIdentityKey( ak );
                    byte[] b = new byte[secureRandom.nextInt( 20 ) + 3];
                    secureRandom.nextBytes( b );
                    ret.setNodeAddress( "smtp:" + toHex( b ) + "@demo" + secureRandom.nextInt( 3 ) );
                    ak=new AsymmetricKey();
                    if(!complete) {
                        ak.setPrivateKey(null);
                    }
                    ret.setNodeKey(ak);
                } catch(Exception e) {
                    throw new IOException("Exception while generating recipient identity",e);
                }
                break;
            default:
                // Unknown type just ignore it
                return null;
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

    protected void parse(ASN1Encodable p) throws IOException {
        LOGGER.log( Level.FINER, "Executing parse()" );
        ASN1Sequence s1 = ASN1Sequence.getInstance( p );
        int i=0;
        valid = new UsagePeriod( s1.getObjectAt( i++ ) );
        messageQuota=ASN1Integer.getInstance( s1.getObjectAt( i++ ) ).getValue().intValue();
        transferQuota=ASN1Integer.getInstance( s1.getObjectAt( i++ ) ).getValue().intValue();
        LOGGER.log( Level.FINER, "Finished parse()" );
        for(;i<s1.size();i++) {
            ASN1TaggedObject to = ASN1TaggedObject.getInstance( s1.getObjectAt( i ) );
            switch(to.getTagNo()) {
                case 1001:
                    identityKey=new AsymmetricKey( toDER(to.getObject()) );
                    break;
                case 1002:
                    nodeAddress=((ASN1String)(to.getObject())).getString();
                    break;
                case 1003:
                    nodeKey=new AsymmetricKey( toDER(to.getObject()) );
                    break;
                default:
                    throw new IOException("unknown tag encountered");
            }
        }
    }

    @Override
    public ASN1Object toASN1Object(DumpType dumpType) throws IOException {
        // Prepare encoding
        LOGGER.log( Level.FINER,"Executing toASN1Object()");

        ASN1EncodableVector v=new ASN1EncodableVector();

        v.add(valid.toASN1Object(dumpType));
        v.add(new ASN1Integer( messageQuota ));
        v.add(new ASN1Integer( transferQuota ));

        if (identityKey != null) {
            v.add( new DERTaggedObject( true, 1001, identityKey.toASN1Object( dumpType ) ) );
        }
        if(nodeAddress!=null) {
            v.add( new DERTaggedObject( true, 1002, new DERIA5String(nodeAddress)));
        }
        if (nodeKey != null){
            v.add( new DERTaggedObject( true, 1003, nodeKey.toASN1Object( dumpType ) ) );
        }

        ASN1Sequence seq=new DERSequence(v);
        LOGGER.log(Level.FINER,"done toASN1Object()");
        return seq;
    }

    @Override
    public String dumpValueNotation(String prefix,DumpType dumpType) throws IOException {
        StringBuilder sb=new StringBuilder();
        sb.append( "{" ).append( CRLF );
        sb.append( prefix ).append( "  valid " ).append(valid.dumpValueNotation( prefix+"    ",dumpType )  ).append("," ).append(CRLF );
        sb.append( prefix ).append( "  messageQuota " ).append(messageQuota ).append("," ).append(CRLF );
        sb.append( prefix ).append( "  transferQuota " ).append(transferQuota );
        if(identityKey!=null) {
            sb.append( "," ).append(CRLF);
            sb.append(prefix ).append( "  identity " ).append( identityKey.dumpValueNotation( prefix+"    ",dumpType ) );
        }
        if(nodeAddress!=null) {
            sb.append( "," ).append(CRLF);
            sb.append(prefix ).append( "  nodeAddress \"" ).append(nodeAddress ).append("\"" );
        }
        if(nodeKey!=null)     {
            sb.append( "," ).append(CRLF);
            sb.append(prefix ).append( "  nodeKey " ).append( nodeKey.dumpValueNotation( prefix+"    ",dumpType ) );
        }
        sb.append( CRLF );
        sb.append( prefix ).append( "}" );
        return sb.toString();
    }

    public IdentityType getType() {
        if(iType!=null) {
            return iType;
        }
        if(nodeKey==null) {
            return IdentityType.OWNED_IDENTITY;
        }
        return identityKey==null?IdentityType.NODE_IDENTITY:IdentityType.RECIPIENT_IDENTITY;
    }

    public boolean equals(Object t) {
        if(t==null) {
            return false;
        }
        if(!(t instanceof IdentityStoreBlock)) {
            return false;
        }
        IdentityStoreBlock isb=(IdentityStoreBlock)t;
        if(!valid.equals(isb.valid)) {
            return false;
        }
        if(messageQuota!=isb.messageQuota) {
            return false;
        }
        if(transferQuota!=isb.transferQuota) {
            return false;
        }
        if((identityKey==null && isb.identityKey!=null) || (identityKey!=null && !identityKey.equals(isb.identityKey))) {
            return false;
        }
        if((nodeAddress!=null && !nodeAddress.equals(isb.nodeAddress)) || (nodeAddress==null && isb.nodeAddress!=null)) {
            return false;
        }
        return nodeKey.equals(isb.nodeKey);
    }

}
