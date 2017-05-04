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


import net.gwerder.java.messagevortex.MessageVortexLogger;
import net.gwerder.java.messagevortex.asn1.encryption.DumpType;
import org.bouncycastle.asn1.*;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.text.ParseException;
import java.util.logging.Level;
import java.util.logging.Logger;

/***
 * The main calss representing the main message object
 *
 * This part is specified as vortexMessage in the file asn.1/messageBlocks.asn1
 */
public class VortexMessage extends AbstractBlock {

    private PrefixBlock prefix;
    private InnerMessageBlock innerMessage;
    private AsymmetricKey key;

    private static final Logger LOGGER;
    static {
        LOGGER = MessageVortexLogger.getLogger((new Throwable()).getStackTrace()[0].getClassName());
        MessageVortexLogger.setGlobalLogLevel( Level.ALL);
    }

    /***
     * Creates an empty message with a new symmetric encrypption key in the prefix.
     *
     * @throws IOException if the constructor was unable to build a message skeleton from the default values
     * @throws NoSuchAlgorithmException if the default values for symmetric encryption keys are invalid
     */
    private VortexMessage()  throws IOException,NoSuchAlgorithmException {
        prefix=new PrefixBlock();
        innerMessage=new InnerMessageBlock();
        key=null;
    }

    /***
     * Parses a byte array to a  VortexMessage.
     *
     * @param b  the byte array to be parsed
     * @param dk the key required to decrypt the prefix
     * @throws IOException if there was a problem parsing or decrypting the object
     * @throws ParseException if there was a problem parsing the object
     * @throws NoSuchAlgorithmException if there was a problem decrypting the object
     */
    public VortexMessage(byte[] b,AsymmetricKey dk) throws IOException,ParseException,NoSuchAlgorithmException {
        this();
        setDecryptionKey(dk);
        parse( b );
    }

    /***
     * Creates a new message block.
     *
     * @param pre the prefix block to use
     * @param im  the inner message block to use
     * @throws IOException if there was an error generating the kay
     * @throws NoSuchAlgorithmException if the default values of a symmetric key point to an invalid algorithm
     */
    public VortexMessage(PrefixBlock pre, InnerMessageBlock im)  throws IOException,NoSuchAlgorithmException {
        this();
        if(pre==null) {
            setPrefix( new PrefixBlock( new SymmetricKey() ) );
        } else {
            setPrefix( pre );
        }
        setInnerMessage(im);
    }

    /***
     * gets the embedded inner message block
     *
     * @return the message block
     */
    public InnerMessageBlock getInnerMessage() {return innerMessage;}
    public InnerMessageBlock setInnerMessage(InnerMessageBlock im) {
        if( im==null ) {
            throw new NullPointerException( "InnerMessage may not be null" );
        }
        InnerMessageBlock old=getInnerMessage();
        innerMessage=im;
        return old;
    }

    /**
     * Gets the embedded prefix block.
     *
     * @return the prefix block
     */
    public PrefixBlock getPrefix() {return prefix;}

    /***
     * Sets the embedded prefix block.
     *
     * @param pre the new prefix block
     * @return the prefix block which was set prior to the operation
     */
    public PrefixBlock setPrefix(PrefixBlock pre) {
        if( pre==null ) {
            throw new NullPointerException( "Prefix may not be null" );
        }
        PrefixBlock old=getPrefix();
        prefix=pre;
        return old;
    }

    /***
     * get the currently set encryption/decryption key (asymmetric).
     *
     * @return the key or null if not set
     */
    public AsymmetricKey getDecryptionKey() {return key;}

    /***
     * Set the encryption/decryption key.
     *
     * @return the key which has been set previously or null if the key ha not been set
     */
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
        ASN1TaggedObject to=ASN1TaggedObject.getInstance( s1.getObjectAt( i ) );
        i++;
        switch(to.getTagNo()) {
            case PrefixBlock.PLAIN_PREFIX:
                prefix = new PrefixBlock( to.getObject(),null );
                break;
            case PrefixBlock.ENCRYPTED_PREFIX:
                prefix = new PrefixBlock( to.getObject() , getDecryptionKey());
                break;
            default:
                throw new ParseException( "got unexpected tag number when reading prefix ("+to.getTagNo()+")",0 );
        }

        // reading inner message
        to=ASN1TaggedObject.getInstance( s1.getObjectAt( i ) );
        i++;
        switch(to.getTagNo()) {
            case InnerMessageBlock.PLAIN_MESSAGE:
                innerMessage = new InnerMessageBlock( to.getObject().getEncoded() );
                break;
            case InnerMessageBlock.ENCRYPTED_MESSAGE:
                innerMessage = new InnerMessageBlock( prefix.getKey().decrypt(ASN1OctetString.getInstance(to.getObject()).getOctets())  );
                break;
            default:
                throw new ParseException( "got unexpected tag number when reading inner message ("+to.getTagNo()+")",0 );
        }
    }

    /***
     * Dumps the object a ASN1Object.
     *
     * This method is mainly useful for diagnostic purposes.
     *
     * @return             the requested object as ASN1Object
     * @throws IOException if any object or subobject can not be dumped
     */
    public ASN1Object toASN1Object() throws IOException {
        return toASN1Object( null, DumpType.PUBLIC_ONLY );
    }

    /***
     * Dumps the object a ASN1Object.
     *
     * This method is mainly useful for diagnostic purposes.
     *
     * @param identityKey  the identity key to apply to the message
     * @param dt           the dumpType to apply
     * @return             the requested object as ASN1Object
     * @throws IOException if any object or subobject can not be dumped
     */
    public ASN1Object toASN1Object(AsymmetricKey identityKey, DumpType dt) throws IOException {
        // Prepare encoding
        LOGGER.log(Level.FINER,"Executing toASN1Object()");

        if(identityKey==null || DumpType.ALL_UNENCRYPTED==dt) {
            getPrefix().setDecryptionKey(null);
        } else {
            getPrefix().setDecryptionKey( identityKey );
        }
        ASN1EncodableVector v=new ASN1EncodableVector();

        // add prefix to structure
        addPrefixBlockToASN1(v);
        // add inner message to structure
        addInnerMessageBlockToASN1(v,dt);

        ASN1Sequence seq=new DERSequence(v);
        LOGGER.log(Level.FINER,"done toASN1Object()");
        return seq;
    }

    private void addPrefixBlockToASN1(ASN1EncodableVector v) throws IOException {
        ASN1Encodable o=getPrefix().toASN1Object();
        if (o == null) {
            throw new IOException( "returned prefix object may not be null" );
        }
        v.add( new DERTaggedObject( getPrefix().getDecryptionKey()==null? PrefixBlock.PLAIN_PREFIX: PrefixBlock.ENCRYPTED_PREFIX,o ));
    }

    private void addInnerMessageBlockToASN1(ASN1EncodableVector v, DumpType dt) throws IOException {
        if(prefix.getKey()==null || DumpType.ALL_UNENCRYPTED.equals(dt)) {
            v.add( new DERTaggedObject( InnerMessageBlock.PLAIN_MESSAGE,getInnerMessage().toASN1Object(dt) ));
        } else {
            v.add( new DERTaggedObject( InnerMessageBlock.ENCRYPTED_MESSAGE,new DEROctetString( prefix.getKey().encrypt(getInnerMessage().toASN1Object(dt).getEncoded() ))));
        }
    }

    /***
     * Dumps an ASN.1 value notation of a vortexMessage (PUBLIC_ONLY dump type).
     *
     * @param prefix       the line prefix to be used (typically &quot;&quot;)
     * @return             an ASN.1  representation of the vortexMessage
     * @throws IOException if message i not encodable due to an incomplete/invalid object state
     */
    public String dumpValueNotation(String prefix) throws IOException {
        return dumpValueNotation( prefix, DumpType.PUBLIC_ONLY);
    }

    /***
     * Dumps a ASN.1 value notation of a vortexMessage.
     *
     * @param prefix       the line prefix to be used (typically &quot;&quot;)
     * @param dt           specifies the type of dump.
     * @return             an ASN.1  representation of the vortexMessage
     * @throws IOException if message i not encodable due to an incomplete/invalid object state
     */
    public String dumpValueNotation(String prefix, DumpType dt) throws IOException {
        String ret;
        ret =prefix+"m VortexMessage ::= {"+CRLF ;
        ret+=prefix+"  -- Dumping prefix"+CRLF;
        ret+=prefix+"  prefix " + getPrefix().dumpValueNotation( prefix + "  ",dt ) + "," + CRLF;
        ret+=prefix+"  -- Dumping innerMessage" + CRLF ;
        ret+=prefix+"  innerMessage " + getInnerMessage().dumpValueNotation( prefix + "  ",DumpType.PUBLIC_ONLY.equals(dt)?this.prefix.getKey():null,dt ) + "," + CRLF;
        ret+=prefix+"}"+CRLF;
        return ret;
    }

    /***
     * Build the binary represenattion for a vortexMessage.
     *
     * This mthod returns the binary (length prefixed) representation of a vortex message.
     *
     * @param dt specifies the type of dump. for sending use PUBLIC_ONLY
     * @return the binary representation of the vortexMessage
     */
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
