package net.gwerder.java.messagevortex.asn1;

import net.gwerder.java.messagevortex.asn1.encryption.DumpType;
import org.bouncycastle.asn1.*;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.text.ParseException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * ASN1 parser class for header reply.
 *
 * Created by martin.gwerder on 14.04.2016.
 */
public class Prefix extends Block {

    public static final int PLAIN_PREFIX=100001;
    public static final int ENCRYPTED_PREFIX=100002;

    byte[] encrypted=null;
    AsymmetricKey decryptionKey=null;

    /** The key used for decryption of the rest of the VortexMessage **/
    SymmetricKey key=null;

    /**
     * Creates an empty prefix
     */
    public Prefix() {
        // nothing to be done here
    }

    public Prefix(SymmetricKey sk) {
        key=sk;
    }

    /***
     * Creates a prefix by parsing to in plan (unencrypted)
     *
     * @param to The primitive to be parsed
     *
     * @throws IOException if parsing fails
     */
    public Prefix(ASN1Primitive to,AsymmetricKey ak) throws IOException,NoSuchAlgorithmException,ParseException {
        this(to.getEncoded(),ak);
    }

    /***
     * Creates a prefix from the provided octet stream by decyphering it with the provided key.
     *
     * @param to the ASN1 OCTET STRING containing the encrypted prefix
     * @param ak the host key
     *
     * @throws IOException
     * @throws NoSuchAlgorithmException
     * @throws ParseException
     */
    public Prefix(ASN1OctetString to, AsymmetricKey ak) throws IOException,NoSuchAlgorithmException,ParseException {
        this(to.getOctets(),ak);
    }


    /***
     * Creates a prefix from the provided byte array by decyphering it with the provided key.
     *
     * @param to the ASN1 OCTET STRING containing the encrypted prefix
     * @param ak the host key
     *
     * @throws IOException
     * @throws NoSuchAlgorithmException
     * @throws ParseException
     */
    public Prefix(byte[] to,AsymmetricKey ak) throws IOException,NoSuchAlgorithmException,ParseException {
        encrypted=to;
        setDecryptionKey(ak);
        if(getDecryptionKey()!=null && getDecryptionKey().hasPrivateKey()) {
            parse( ASN1OctetString.getInstance(decryptionKey.decrypt( encrypted )));
        } else {
            parse( ASN1Sequence.getInstance(to) );
        }
    }

    @Override
    protected void parse(ASN1Encodable to) throws IOException {
        Logger.getLogger("VortexMessage").log( Level.FINER,"Executing parse()");
        int i = 0;
        ASN1Sequence s1 = ASN1Sequence.getInstance( to );

        // getting key
        key = new SymmetricKey( s1.getObjectAt(0).toASN1Primitive().getEncoded() ,null );
        if(key==null) throw new IOException("symmetric key may not be null when decoding");
    }

    public AsymmetricKey setDecryptionKey(AsymmetricKey dk) {
        AsymmetricKey old=getDecryptionKey();
        decryptionKey=dk;
        return old;
    }

    public AsymmetricKey getDecryptionKey() {
        return decryptionKey;
    }

    public SymmetricKey setKey(SymmetricKey dk) {
        SymmetricKey old=getKey();
        key=dk;
        return old;
    }

    public SymmetricKey getKey() {
        return key;
    }

    @Override
    public ASN1Object toASN1Object() throws IOException {
        return toASN1Object( null );
    }

    public ASN1Object toASN1Object(AsymmetricKey ak) throws IOException {
        Logger.getLogger("Prefix").log(Level.FINER,"adding symmetric key");
        if(ak!=null) setDecryptionKey(ak);
        if(key==null) throw new IOException("symmetric key may not be null when encoding");

        ASN1EncodableVector v=new ASN1EncodableVector();
        ASN1Encodable o=key.toASN1Object();
        if (o == null) {
            throw new IOException( "returned symmetric object may not be null" );
        }
        v.add( o );
        ASN1Object seq=new DERSequence(v);

        // encrypt and embedd if requested
        if(ak!=null) {
            // encrypt and embedd in OCTET STREAM
            Logger.getLogger("Prefix").log(Level.FINER,"encrypting prefix contend to octet string in Prefix");
            seq=new DEROctetString( ak.encrypt(seq.getEncoded()) );
        }
        Logger.getLogger("Prefix").log(Level.FINER,"done toASN1Object() of Prefix");
        return seq;
    }

    public String dumpValueNotation(String prefix) throws IOException {
        return dumpValueNotation( prefix,DumpType.PUBLIC_ONLY );
    }

    public String dumpValueNotation(String prefix, DumpType dt) throws IOException {
        StringBuilder sb=new StringBuilder();
        if(DumpType.ALL.equals(dt) || DumpType.PUBLIC_ONLY.equals(dt)) {
            // dump standard block as octet string
            sb.append( "  {"+CRLF );
            sb.append( prefix+"key "+toHex( key.toASN1Object().getEncoded() ));
            sb.append( "  }"+CRLF );
        } else {
            // dump as unecrypted structure
            sb.append( "  {"+CRLF );
            sb.append( prefix+"key "+key.dumpValueNotation( prefix+"  " ) );
            sb.append( "  }"+CRLF );
        }
        return sb.toString();
    }

    @Override
    public boolean equals(Object o) {
        // must be equivalent in type
        if(!(o instanceof Prefix)) return false;

        // do casting
        Prefix p=(Prefix)(o);

        // look for not equal keys
        if((p.getKey()==null && getKey()!=null) || (p.getKey()!=null && getKey()==null)) return false;
        if(p.getKey()!=null && !p.getKey().equals(getKey())) return false;

        return true;
    }

}
