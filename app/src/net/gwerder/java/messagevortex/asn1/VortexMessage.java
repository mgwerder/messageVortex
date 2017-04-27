package net.gwerder.java.messagevortex.asn1;

import net.gwerder.java.messagevortex.asn1.encryption.DumpType;
import org.bouncycastle.asn1.*;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.NoSuchAlgorithmException;
import java.text.ParseException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class VortexMessage extends Block {

    private Prefix       prefix;
    private InnerMessage innerMessage;
    private AsymmetricKey key;

    private VortexMessage() {
        prefix=null;
        innerMessage=null;
        key=null;
    }

    public VortexMessage(File f,AsymmetricKey dk) throws IOException,ParseException,NoSuchAlgorithmException {
        this();
        setDecryptionKey(dk);
        Path asn1DataPath = Paths.get(f.getAbsolutePath());
        byte[] p = Files.readAllBytes(asn1DataPath);
        parse( p );
    }

    public VortexMessage(byte[] b,AsymmetricKey dk) throws IOException,ParseException,NoSuchAlgorithmException {
        this();
        setDecryptionKey(dk);
        parse( b );
    }

    public VortexMessage(Prefix pre, InnerMessage im) {
        this();
        setPrefix(pre);
        setInnerMessage(im);
    }

    public InnerMessage getInnerMessage() {return innerMessage;}
    public InnerMessage setInnerMessage(InnerMessage im) {
        if( im==null ) {
            throw new NullPointerException( "InnerMessage may not be null" );
        }
        InnerMessage old=getInnerMessage();
        innerMessage=im;
        return old;
    }

    public Prefix getPrefix() {return prefix;}
    public Prefix setPrefix(Prefix pre) {
        if( pre==null ) {
            throw new NullPointerException( "Prefix may not be null" );
        };
        Prefix old=getPrefix();
        prefix=pre;
        return old;
    }

    public AsymmetricKey getDecryptionKey() {return key;}
    public AsymmetricKey setDecryptionKey(AsymmetricKey dk) {
        AsymmetricKey old=getDecryptionKey();
        this.key=dk;
        return old;
    }

    protected void parse(byte[] p) throws IOException,ParseException,NoSuchAlgorithmException {
        ASN1InputStream aIn=new ASN1InputStream( p );
        parse(aIn.readObject());
    }

    protected void parse(ASN1Encodable p) throws IOException,ParseException,NoSuchAlgorithmException {
        Logger.getLogger("VortexMessage").log(Level.FINER,"Executing parse()");
        int i = 0;
        ASN1Sequence s1 = ASN1Sequence.getInstance( p );
        prefix = new Prefix( s1.getObjectAt( i++ ).toASN1Primitive().getEncoded(),getDecryptionKey() );
        innerMessage = new InnerMessage( s1.getObjectAt( i++ ).toASN1Primitive().getEncoded(),prefix.getKey() );
    }

    public ASN1Object toASN1Object() throws IOException,NoSuchAlgorithmException,ParseException {
        return toASN1Object( null, null,DumpType.PUBLIC_ONLY );
    }

    public ASN1Object toASN1Object(AsymmetricKey identityKey, SymmetricKey messageKey, DumpType dt) throws IOException,NoSuchAlgorithmException,ParseException {
        // Prepare encoding
        Logger.getLogger("VortexMessage").log(Level.FINER,"Executing toASN1Object()");
        if(messageKey==null) {
        } else if(getPrefix().getKey()!=null) {
            messageKey=getPrefix().getKey();
        }
        getPrefix().setDecryptionKey( identityKey );

        ASN1EncodableVector v=new ASN1EncodableVector();

        // add prefix to structure
        ASN1Encodable o=getPrefix().toASN1Object();
        if (o == null) {
            throw new IOException( "returned prefix object may not be null" );
        }
        v.add( o );

        v.add( getInnerMessage().toASN1Object( messageKey,dt ) );
        ASN1Sequence seq=new DERSequence(v);
        Logger.getLogger("VortexMessage").log(Level.FINER,"done toASN1Object()");
        return seq;
    }

    public String dumpValueNotation(String prefix) throws IOException {
        return dumpValueNotation( prefix, DumpType.PUBLIC_ONLY);
    }

    public String dumpValueNotation(String prefix, DumpType dt) throws IOException {
        StringBuilder sb=new StringBuilder();
        sb.append( prefix + "m VortexMessage ::= {" + CRLF );
        sb.append( prefix + "  -- Dumping prefix" + CRLF );
        sb.append( prefix + "  prefix " + getPrefix().dumpValueNotation( prefix + "  ",dt ) + "," + CRLF );
        sb.append( prefix + "  -- Dumping innerMessage" + CRLF );
        sb.append( prefix + "  innerMessage " + getInnerMessage().dumpValueNotation( prefix + "  ",DumpType.PUBLIC_ONLY.equals(dt)?this.prefix.getKey():null,dt ) + "," + CRLF );
        sb.append( prefix + "}" + CRLF );
        return sb.toString();
    }

    public byte[] toBinary(DumpType dt) {
        //FIXME
        throw new NotImplementedException();
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
