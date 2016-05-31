package net.gwerder.java.mailvortex.asn1;

import net.gwerder.java.mailvortex.asn1.Key.Algorithm;
import org.bouncycastle.asn1.*;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import java.io.IOException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.spec.InvalidKeySpecException;
import java.text.ParseException;

public class Identity extends Block {

    private AsymmetricKey identityDecryptionKey=null;
    private AsymmetricKey identityKey = null;
    private long serial;
    private int maxReplays;
    private UsagePeriod valid = null;
    private int[] forwardSecret = null;
    private byte[] decryptionKeyRaw = null;
    private SymmetricKey decryptionKey;
    private Request[] requests;
    private long identifier=-1;
    private String padding=null;

    public Identity() throws NoSuchAlgorithmException, NoSuchProviderException, InvalidAlgorithmParameterException,IOException,NoSuchPaddingException,InvalidKeyException,IllegalBlockSizeException,BadPaddingException,NoSuchProviderException,InvalidKeySpecException {
        identityKey=new AsymmetricKey(Algorithm.RSA,1024);
        serial = (long)(Math.random()*4294967295L);
        maxReplays=1;
        valid=new UsagePeriod(3600);
        decryptionKey=new SymmetricKey(Algorithm.AES256);
        decryptionKeyRaw=identityKey.encrypt(toDER(decryptionKey.toASN1Object()),false);
        requests=new Request[0];
    }

    public Identity(byte[] b,AsymmetricKey dk) throws ParseException,IOException,NoSuchAlgorithmException  {
        identityDecryptionKey=dk;
        ASN1Encodable s;
        if(dk!=null) {
            s=new DEROctetString(b);
        } else {
            s=ASN1Sequence.getInstance(b);
        }
        parse(s);
    }

    public Identity(ASN1Encodable to,AsymmetricKey dk) throws ParseException,IOException,NoSuchAlgorithmException  {
        identityDecryptionKey=dk;
        parse(to);
    }

    @Override
    protected void parse(ASN1Encodable to) throws ParseException,IOException,NoSuchAlgorithmException {
        ASN1Sequence s1;
        if( identityDecryptionKey!=null ) {
            // we got an encrypted string ... lets unpack it
            try {
                s1 = ASN1Sequence.getInstance( identityDecryptionKey.decrypt(((ASN1OctetString)(to)).getOctets()));
            } catch(BadPaddingException e) {
                throw new IOException("Exception while decrypting content",e);
            } catch(InvalidKeyException e) {
                throw new IOException("Exception while decrypting content",e);
            } catch(InvalidKeySpecException e) {
                throw new IOException("Exception while decrypting content",e);
            } catch(IllegalBlockSizeException e) {
                throw new IOException("Exception while decrypting content",e);
            } catch(NoSuchPaddingException e) {
                throw new IOException("Exception while decrypting content",e);
            } catch(NoSuchProviderException e) {
                throw new IOException("Exception while decrypting content",e);
            }
        } else {
            s1=ASN1Sequence.getInstance(to);
        }
        int i=0;
        ASN1Encodable s3=s1.getObjectAt(i++);
        identityKey= new AsymmetricKey(s3);
        serial     = ASN1Integer.getInstance( s1.getObjectAt(i++)).getValue().longValue();
        maxReplays = ASN1Integer.getInstance( s1.getObjectAt(i++)).getValue().intValue();
        valid=new UsagePeriod(s1.getObjectAt(i++));
        try {
            ASN1TaggedObject ato=ASN1TaggedObject.getInstance( s1.getObjectAt(i++) );
            if(ato.getTagNo()!=0) {
                throw new IOException("not a forward secret");
            }
            ASN1Sequence s2 = ASN1Sequence.getInstance( ato );
            forwardSecret = new int[s2.size()];
            for(int y=0;y<s2.size();y++) {
                forwardSecret[y] = ((ASN1Integer)(s2.getObjectAt(y))).getValue().intValue();
            }
        } catch(Exception e) {
            // redo this line if not optional forwardSecret
            i--;
        }
        decryptionKeyRaw = ASN1OctetString.getInstance(s1.getObjectAt(i++)).getOctets();
        decryptionKey=new SymmetricKey(decryptionKeyRaw,identityKey,true);
        ASN1Sequence s2=ASN1Sequence.getInstance( s1.getObjectAt(i++));
        requests= new Request[s2.size()];
        for(int y=0;y<s2.size();y++) {
            requests[y]=new Request(s2.getObjectAt(y));
        }
        while(s1.size()>i) {
            ASN1TaggedObject o=(ASN1TaggedObject)(s1.getObjectAt(i++));
            if(o.getTagNo()==1) {
                identifier=((ASN1Integer)(o.getObject())).getValue().longValue();
            } else if(o.getTagNo()==2) {
                padding=((ASN1String)(s1.getObjectAt(i))).getString();
            }
        }
    }

    public SymmetricKey getDecryptionKey() {
        return decryptionKey;
    }

    public byte[] getRawDecryptionKey() throws IOException {
        if(decryptionKeyRaw==null && identityKey.hasPrivateKey()) {
            try {
                decryptionKeyRaw = identityKey.encrypt( decryptionKey.toBytes(), false );
            } catch(Exception e) {
                throw new IOException("Error while encoding decryption key",e);
            }
        }
        return decryptionKeyRaw;
    }

    public ASN1Object toASN1Object() throws IOException {
        ASN1EncodableVector v =new ASN1EncodableVector();
        ASN1Object o=identityKey.toASN1Object();
        if(o==null) throw new IOException("identityKey did return null object");
        v.add(o);
        v.add(new ASN1Integer( serial ));
        v.add(new ASN1Integer( maxReplays ));
        o=valid.toASN1Object();
        if(o==null) throw new IOException("validity did return null object");
        v.add(o);
        // FIXME missing forward secrets
        try {
            v.add( new DEROctetString( (decryptionKeyRaw!=null?decryptionKeyRaw:identityKey.encrypt( decryptionKey.getKey(), false ) ) ) );
        } catch(Exception e) {
            throw new IOException( "Error while encrypting decryptionKey",e );
        }
        ASN1EncodableVector s=new ASN1EncodableVector();
        for(Request r:requests) {
            s.add( r.toASN1Object() );
        }
        v.add(new DERSequence( s ));
        if(identifier>-1) {
            v.add( new DERTaggedObject( true,1,new ASN1Integer(identifier)) );
        }
        if(padding!=null) {
            v.add( new DERTaggedObject( true, 2, new DEROctetString( padding.getBytes() ) ) );
        }
        return new DERSequence( v );
    }

    public String dumpValueNotation(String prefix) throws IOException {
        StringBuilder sb=new StringBuilder();
        sb.append("{"+CRLF);
        sb.append(prefix+"  identityKey "+identityKey.dumpValueNotation(prefix+"  ", AsymmetricKey.DumpType.PRIVATE_COMMENTED)+","+CRLF);
        sb.append(prefix+"  serial "+serial+","+CRLF);
        sb.append(prefix+"  maxReplays "+maxReplays+","+CRLF);
        sb.append(prefix+"  valid "+valid.dumpValueNotation(prefix+"  ")+","+CRLF);
        if(forwardSecret!=null) {
            sb.append(prefix + "  forwardSecret { "+CRLF);
            for (int i = 0; i < forwardSecret.length; i++) {
                sb.append(forwardSecret[i] + (i < forwardSecret.length - 1 ? "," : "")+CRLF);
            }
            sb.append(" },"+CRLF);
        }
        byte[] a=decryptionKey.toBytes();
        if(a==null) {
            sb.append(prefix + "  decryptionKey ''B,"+CRLF);
        } else {
            // this key is
            try {
                sb.append( prefix + "  decryptionKey " + toHex( getRawDecryptionKey() ) + "," + CRLF );
            } catch( Exception e) {
                throw new IOException( "unable to sign decryptionKey",e );
            }
        }
        sb.append(prefix+"  requests {"+CRLF);
        for(Request r:requests) {
            sb.append(valid.dumpValueNotation(prefix + "  ")+CRLF);
            sb.append(r.dumpValueNotation(prefix + "  ")+CRLF);
        }
        sb.append(prefix+"  }");
        if(padding!=null) {
            sb.append(","+CRLF);
            sb.append(prefix+"  padding "+toHex(padding.getBytes())+CRLF);
        } else sb.append(CRLF);
        sb.append(prefix+"}");
        return sb.toString();
    }

}
