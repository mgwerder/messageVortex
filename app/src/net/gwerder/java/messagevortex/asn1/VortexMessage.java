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
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.NoSuchAlgorithmException;
import java.text.ParseException;
import java.util.logging.Level;

public class VortexMessage extends Block {

    private Prefix       prefix;
    private InnerMessage innerMessage;
    private AsymmetricKey key;

    private static final java.util.logging.Logger LOGGER;
    static {
        LOGGER = MessageVortexLogger.getLogger((new Throwable()).getStackTrace()[0].getClassName());
        MessageVortexLogger.setGlobalLogLevel( Level.ALL);
    }

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
        }
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
        LOGGER.log(Level.FINER,"Executing parse()");
        int i = 0;
        ASN1Sequence s1 = ASN1Sequence.getInstance( p );

        // reading prefix
        ASN1TaggedObject to=ASN1TaggedObject.getInstance( s1.getObjectAt( i++ ) );
        switch(to.getTagNo()) {
            case Prefix.PLAIN_PREFIX:
                prefix = new Prefix( to.getObject(),null );
                break;
            case Prefix.ENCRYPTED_PREFIX:
                prefix = new Prefix( to.getObject() , getDecryptionKey());
                break;
            default:
                throw new ParseException( "got unexpected tag number when reading prefix ("+to.getTagNo()+")",0 );
        }

        // reading inner message
        to=ASN1TaggedObject.getInstance( s1.getObjectAt( i++ ) );
        switch(to.getTagNo()) {
            case InnerMessage.PLAIN_MESSAGE:
                innerMessage = new InnerMessage( to.getObject().getEncoded() );
                break;
            case InnerMessage.ENCRYPTED_MESSAGE:
                innerMessage = new InnerMessage( prefix.getKey().decrypt(ASN1OctetString.getInstance(to.getObject()).getOctets())  );
                break;
            default:
                throw new ParseException( "got unexpected tag number when reading inner message ("+to.getTagNo()+")",0 );
        }
    }

    public ASN1Object toASN1Object() throws IOException {
        return toASN1Object( null, DumpType.PUBLIC_ONLY );
    }

    public ASN1Object toASN1Object(AsymmetricKey identityKey, DumpType dt) throws IOException {
        // Prepare encoding
        LOGGER.log(Level.FINER,"Executing toASN1Object()");

        if(identityKey!=null || DumpType.ALL_UNENCRYPTED==dt) {
            getPrefix().setDecryptionKey(null);
        } else {
            getPrefix().setDecryptionKey( identityKey );
        }
        ASN1EncodableVector v=new ASN1EncodableVector();

        // add prefix to structure
        ASN1Encodable o=getPrefix().toASN1Object();
        if (o == null) {
            throw new IOException( "returned prefix object may not be null" );
        }
        v.add( new DERTaggedObject( getPrefix().getDecryptionKey()==null?Prefix.PLAIN_PREFIX:Prefix.ENCRYPTED_PREFIX,o ));

        if(prefix.getKey()==null || DumpType.ALL_UNENCRYPTED.equals(dt)) {
            v.add( new DERTaggedObject( InnerMessage.PLAIN_MESSAGE,getInnerMessage().toASN1Object(dt) ));
        } else {
            v.add( new DERTaggedObject( InnerMessage.ENCRYPTED_MESSAGE,new DEROctetString( prefix.getKey().encrypt(getInnerMessage().toASN1Object(dt).getEncoded() ))));
        }
        ASN1Sequence seq=new DERSequence(v);
        LOGGER.log(Level.FINER,"done toASN1Object()");
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
        if(b==null || b.length<1 || b.length>8) {
            throw new IllegalArgumentException( "byte array must contain exactly four bytes" );
        }
        long ret=0;
        for(int i=0;i<b.length;i++) {
            ret|=((long)(b[i]&0xFF))<<(i*8);
        }
        return ret;
    }

}
