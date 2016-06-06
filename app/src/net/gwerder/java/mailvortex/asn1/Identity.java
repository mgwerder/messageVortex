package net.gwerder.java.mailvortex.asn1;

import net.gwerder.java.mailvortex.asn1.encryption.Algorithm;
import net.gwerder.java.mailvortex.asn1.encryption.AlgorithmType;
import net.gwerder.java.mailvortex.asn1.encryption.Padding;
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

    private static final int ENCRYPTED_HEADER_KEY = 1000;
    private static final int PLAIN_IDENTITY_BLOCK = 1002;
    private static final int ENCRYPTED_IDENTITY_BLOCK = 1001;

    private static final Request[] NULLREQUESTS = new Request[0];


    private SymmetricKey headerKey;
    private byte[] encryptedHeaderKey = null;
    private AsymmetricKey identityKey = null;
    private long serial;
    private int maxReplays;
    private UsagePeriod valid = null;
    private int[] forwardSecret = null;
    private byte[] decryptionKeyRaw = null;
    private SymmetricKey decryptionKey;
    private MacAlgorithm hash = new MacAlgorithm( Algorithm.getDefault( AlgorithmType.HASHING ) );
    private Request[] requests;
    private long identifier=-1;
    private byte[] padding = null;
    private byte[] encryptedIdentityBlock = null;

    private AsymmetricKey ownIdentity = null;

    public Identity() throws NoSuchAlgorithmException, InvalidAlgorithmParameterException, IOException, NoSuchPaddingException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException, NoSuchProviderException, InvalidKeySpecException {
        this.identityKey = new AsymmetricKey(Algorithm.RSA, Padding.getDefault(AlgorithmType.ASYMMETRIC), 2048);
        this.serial = (long) (Math.random() * 4294967295L);
        this.maxReplays = 1;
        this.valid = new UsagePeriod(3600);
        this.decryptionKey = new SymmetricKey(Algorithm.AES256);
        this.decryptionKeyRaw = this.identityKey.encrypt(this.toDER(decryptionKey.toASN1Object()));
        this.requests = Identity.NULLREQUESTS;
    }

    public Identity(byte[] b, AsymmetricKey ownIdentity) throws ParseException, IOException, NoSuchAlgorithmException {
        this.ownIdentity = ownIdentity;
        ASN1Encodable s = ASN1Sequence.getInstance( b );
        parse( s );
    }

    public Identity(byte[] b) throws ParseException, IOException, NoSuchAlgorithmException {
        ASN1Encodable s = ASN1Sequence.getInstance( b );
        parse(s);
    }

    public Identity(ASN1Encodable to) throws ParseException, IOException, NoSuchAlgorithmException {
        this( to, null );
    }

    public Identity(ASN1Encodable to, AsymmetricKey ownIdentity) throws ParseException, IOException, NoSuchAlgorithmException {
        super();
        this.ownIdentity = ownIdentity;
        parse( to );
    }

    @Override
    protected void parse(ASN1Encodable o) throws ParseException, IOException, NoSuchAlgorithmException {
        ASN1Sequence s = ASN1Sequence.getInstance( o );
        ASN1Sequence s1;
        int j = 0;
        ASN1TaggedObject to = ASN1TaggedObject.getInstance( s.getObjectAt( j++ ) );
        if (to.getTagNo() == ENCRYPTED_HEADER_KEY) {
            if (ownIdentity == null) {
                encryptedHeaderKey = to.getObject().getEncoded();
                to = DERTaggedObject.getInstance( s.getObjectAt( j++ ) );
            } else {
                headerKey = new SymmetricKey( ownIdentity.decrypt( to.getObject().getEncoded() ) );
                // FIXME check tag
                to = DERTaggedObject.getInstance( s.getObjectAt( j++ ) );
            }
        }
        byte[] signVerifyObject = to.getObject().getEncoded();
        if ((headerKey != null && to.getTagNo() == ENCRYPTED_HEADER_KEY) || to.getTagNo() == PLAIN_IDENTITY_BLOCK) {
            if (headerKey != null && to.getTagNo() == ENCRYPTED_HEADER_KEY) {
                s1 = ASN1Sequence.getInstance( headerKey.decrypt( to.getObject().getEncoded() ) );
            } else {
                s1 = ASN1Sequence.getInstance( to.getObject() );
            }
            int i = 0;
            ASN1Encodable s3 = s1.getObjectAt( i++ );
            identityKey = new AsymmetricKey( s3 );
            serial = ASN1Integer.getInstance( s1.getObjectAt( i++ ) ).getValue().longValue();
            maxReplays = ASN1Integer.getInstance( s1.getObjectAt( i++ ) ).getValue().intValue();
            valid = new UsagePeriod( s1.getObjectAt( i++ ) );
            try {
                ASN1TaggedObject ato = ASN1TaggedObject.getInstance( s1.getObjectAt( i++ ) );
                if (ato.getTagNo() != 0) {
                    throw new IOException( "not a forward secret" );
                }
                ASN1Sequence s2 = ASN1Sequence.getInstance( ato );
                forwardSecret = new int[s2.size()];
                for (int y = 0; y < s2.size(); y++) {
                    forwardSecret[y] = ((ASN1Integer) (s2.getObjectAt( y ))).getValue().intValue();
                }
            } catch (Exception e) {
                // redo this line if not optional forwardSecret
                i--;
            }
            decryptionKeyRaw = ASN1OctetString.getInstance( s1.getObjectAt( i++ ) ).getOctets();
            decryptionKey = new SymmetricKey( decryptionKeyRaw, identityKey );
            hash = new MacAlgorithm( s1.getObjectAt( i++ ) );
            ASN1Sequence s2 = ASN1Sequence.getInstance( s1.getObjectAt( i++ ) );
            requests = new Request[s2.size()];
            for (int y = 0; y < s2.size(); y++) {
                requests[y] = new Request( s2.getObjectAt( y ) );
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
            encryptedIdentityBlock = to.getObject().getEncoded();
        }

        byte[] signature = ASN1OctetString.getInstance( s.getObjectAt( j++ ) ).getOctets();
        if (!identityKey.verify( signVerifyObject, signature, hash.getAlgorithm() ))
            throw new IOException( "Exception while verifying signature of identity block" );
    }

    public SymmetricKey getDecryptionKey() {
        return decryptionKey;
    }

    public byte[] getRawDecryptionKey() throws IOException {
        if(decryptionKeyRaw==null && identityKey.hasPrivateKey()) {
            try {
                decryptionKeyRaw = identityKey.encrypt( decryptionKey.toBytes() );
            } catch(Exception e) {
                throw new IOException("Error while encoding decryption key",e);
            }
        }
        return decryptionKeyRaw;
    }

    public ASN1Object toASN1Object() throws IOException {
        return toASN1Object( ownIdentity, null );
    }

    public AsymmetricKey getOwnIdentity() {
        return ownIdentity;
    }

    public AsymmetricKey setOwnIdentity(AsymmetricKey oid) {
        AsymmetricKey old = ownIdentity;
        ownIdentity = oid;
        return old;
    }

    private void sanitizeHeaderKey() throws IOException {
        if (headerKey == null) {
            if (encryptedHeaderKey == null) {
                try {
                    headerKey = new SymmetricKey();
                } catch (NoSuchAlgorithmException nae) {
                    throw new IOException( "exception while generating header key", nae );
                }
            }
        }
    }

    public ASN1Object toASN1Object(AsymmetricKey ownIdentity, AsymmetricKey targetIdentity) throws IOException, NullPointerException {
        sanitizeHeaderKey();
        //if(ownIdentity==null) throw new NullPointerException( "ownIdentity must not be null" );
        if (headerKey == null && encryptedHeaderKey == null)
            throw new NullPointerException( "headerKey may not be null" );
        ASN1EncodableVector v1 = new ASN1EncodableVector();
        boolean encryptIdentity = false;
        if (headerKey != null && targetIdentity != null) {
            v1.add( new DERTaggedObject( true, ENCRYPTED_HEADER_KEY, new DEROctetString( targetIdentity.encrypt( headerKey.toBytes() ) ) ) );
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
            ASN1Object o = identityKey.toASN1Object();
            if (o == null) throw new IOException( "identityKey did return null object" );
            v.add( o );
            v.add( new ASN1Integer( serial ) );
            v.add( new ASN1Integer( maxReplays ) );
            o = valid.toASN1Object();
            if (o == null) throw new IOException( "validity did return null object" );
            v.add( o );
            // FIXME missing forward secrets
            try {
                v.add( new DEROctetString( (decryptionKeyRaw != null ? decryptionKeyRaw : identityKey.encrypt( decryptionKey.getKey() )) ) );
            } catch (Exception e) {
                throw new IOException( "Error while encrypting decryptionKey", e );
            }
            v.add( hash.toASN1Object() );
            ASN1EncodableVector s = new ASN1EncodableVector();
            for (Request r : requests) {
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
            // store identity encrypted
            o = new DEROctetString( headerKey.encrypt( ae.toASN1Primitive().getEncoded() ) );
            v1.add( new DERTaggedObject( true, ENCRYPTED_IDENTITY_BLOCK, o ) );
        } else {
            // store identity plain
            o = ae.toASN1Primitive();
            v1.add( new DERTaggedObject( true, PLAIN_IDENTITY_BLOCK, o ) );
        }
        v1.add( new DEROctetString( identityKey.sign( o.getEncoded() ) ) );
        return new DERSequence( v1 );
    }

    public String dumpValueNotation(String prefix) throws IOException {
        StringBuilder sb=new StringBuilder();
        sb.append("{"+CRLF);
        if (encryptedHeaderKey != null) {
            sb.append( prefix + "  headerKey " + toHex( encryptedHeaderKey ) );
        }
        if (encryptedIdentityBlock != null) {
            sb.append( prefix + "  blocks encrypted [" + ENCRYPTED_IDENTITY_BLOCK + "]" + toHex( encryptedIdentityBlock ) );
        } else {
            sb.append( prefix + "  blocks plain [" + PLAIN_IDENTITY_BLOCK + "]" + toHex( encryptedIdentityBlock ) );
            sb.append( prefix + "    identityKey " + identityKey.dumpValueNotation( prefix + "  ", AsymmetricKey.DumpType.PRIVATE_COMMENTED ) + "," + CRLF );
            sb.append( prefix + "    serial " + serial + "," + CRLF );
            sb.append( prefix + "    maxReplays " + maxReplays + "," + CRLF );
            sb.append( prefix + "    valid " + valid.dumpValueNotation( prefix + "  " ) + "," + CRLF );
            if (forwardSecret != null) {
                sb.append( prefix + "    forwardSecret { " + CRLF );
                for (int i = 0; i < forwardSecret.length; i++) {
                    sb.append( forwardSecret[i] + (i < forwardSecret.length - 1 ? "," : "") + CRLF );
                }
                sb.append( " }," + CRLF );
            }
            byte[] a = decryptionKey.toBytes();
            if (a == null) {
                sb.append( prefix + "    decryptionKey ''B," + CRLF );
            } else {
                // this key is
                try {
                    sb.append( prefix + "    decryptionKey " + toHex( getRawDecryptionKey() ) + "," + CRLF );
                } catch (Exception e) {
                    throw new IOException( "unable to sign decryptionKey", e );
                }
            }
            sb.append( prefix + "    requests {" + CRLF );
            for (Request r : requests) {
                sb.append( valid.dumpValueNotation( prefix + "  " ) + CRLF );
                sb.append( r.dumpValueNotation( prefix + "  " ) + CRLF );
            }
            sb.append( prefix + "    }" );
            if (padding != null) {
                sb.append( "," + CRLF );
                sb.append( prefix + "    padding " + toHex( padding ) + CRLF );
            } else sb.append( CRLF );
            sb.append( prefix + "  }," );
        }
        sb.append(prefix+"}");
        return sb.toString();
    }

}
