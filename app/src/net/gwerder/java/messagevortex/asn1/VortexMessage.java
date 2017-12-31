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
import java.io.Serializable;
import java.security.NoSuchAlgorithmException;
import java.text.ParseException;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;

/***
 * The main calss representing the main message object
 *
 * This part is specified as vortexMessage in the file asn.1/messageBlocks.asn1
 */
public class VortexMessage extends AbstractBlock implements Serializable {

    public static final long serialVersionUID = 100000000018L;

    public static final int PREFIX_PLAIN            =  10011;
    public static final int PREFIX_ENCRYPTED        =  10012;
    public static final int INNER_MESSAGE_PLAIN     =  10021;
    public static final int INNER_MESSAGE_ENCRYPTED =  10022;

    private PrefixBlock prefix;
    private InnerMessageBlock innerMessage;
    private AsymmetricKey decryptionKey;

    private static final Logger LOGGER;
    static {
        LOGGER = MessageVortexLogger.getLogger((new Throwable()).getStackTrace()[0].getClassName());
        MessageVortexLogger.setGlobalLogLevel( Level.ALL);
    }

    /***
     * Creates an empty message with a new symmetric encrypption decryptionKey in the prefix.
     *
     * @throws IOException if the constructor was unable to build a message skeleton from the default values
     * @throws NoSuchAlgorithmException if the default values for symmetric encryption keys are invalid
     */
    private VortexMessage()  throws IOException {
        prefix=new PrefixBlock();
        innerMessage=new InnerMessageBlock();
        decryptionKey =null;
    }

    /***
     * Parses a byte array to a  VortexMessage.
     *
     * @param b  the byte array to be parsed
     * @param dk the decryptionKey required to decrypt the prefix
     * @throws IOException if there was a problem parsing or decrypting the object
     * @throws ParseException if there was a problem parsing the object
     * @throws NoSuchAlgorithmException if there was a problem decrypting the object
     */
    public VortexMessage(byte[] b,AsymmetricKey dk) throws IOException {
        setDecryptionKey(dk);
        parse( b );
    }

    /***
     * Creates a new message block.
     *
     * @param pre the prefix block to use
     * @param im  the inner message block to use
     * @throws IOException if there was an error generating the kay
     * @throws NoSuchAlgorithmException if the default values of a symmetric decryptionKey point to an invalid algorithm
     */
    public VortexMessage(PrefixBlock pre, InnerMessageBlock im)  throws IOException {
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
     * get the currently set encryption/decryption decryptionKey (asymmetric).
     *
     * @return the decryptionKey or null if not set
     */
    public AsymmetricKey getDecryptionKey() {
        return decryptionKey;
    }

    /***
     * Set the encryption/decryption decryptionKey.
     *
     * @return the decryptionKey which has been set previously or null if the decryptionKey ha not been set
     */
    public AsymmetricKey setDecryptionKey(AsymmetricKey dk) throws IOException {
        AsymmetricKey old=getDecryptionKey();
        this.decryptionKey = dk!=null?dk.clone():null;
        if(prefix!=null) {
            prefix.setDecryptionKey(dk);
        }
        if(innerMessage!=null && innerMessage.getPrefix()!=null) {
            innerMessage.getPrefix().setDecryptionKey(dk);
        }
        return old;
    }

    protected void parse(byte[] p) throws IOException {
        ASN1InputStream aIn=null;
        try {
            aIn = new ASN1InputStream(p);
            parse(aIn.readObject());
        } finally {
            if(aIn!=null) {
                aIn.close();
            }
        }
    }

    protected void parse(ASN1Encodable p) throws IOException {
        LOGGER.log(Level.FINER,"Executing parse()");
        int i = 0;
        ASN1Sequence s1 = ASN1Sequence.getInstance( p );

        // reading prefix
        ASN1TaggedObject to=ASN1TaggedObject.getInstance( s1.getObjectAt( i ) );
        i++;
        switch(to.getTagNo()) {
            case PREFIX_PLAIN:
                prefix = new PrefixBlock( to.getObject(),null );
                break;
            case PREFIX_ENCRYPTED:
                prefix = new PrefixBlock( ASN1OctetString.getInstance(to.getObject()).getOctets() , getDecryptionKey());
                break;
            default:
                throw new IOException( "got unexpected tag number when reading prefix ("+to.getTagNo()+")");
        }

        // reading inner message
        to=ASN1TaggedObject.getInstance( s1.getObjectAt( i ) );
        i++;
        switch(to.getTagNo()) {
            case INNER_MESSAGE_PLAIN:
                innerMessage = new InnerMessageBlock( toDER(to.getObject()),null );
                break;
            case INNER_MESSAGE_ENCRYPTED:
                innerMessage = new InnerMessageBlock( prefix.getKey().decrypt(ASN1OctetString.getInstance(to.getObject()).getOctets()),getDecryptionKey()  );
                break;
            default:
                throw new IOException( "got unexpected tag number when reading inner message ("+to.getTagNo()+")" );
        }

        // propagate decryption key to sub elements
        setDecryptionKey(getDecryptionKey());
    }

    /***
     * Dumps the object a ASN1Object.
     *
     * This method is mainly useful for diagnostic purposes.
     *
     * @param dt           the dumpType to apply
     * @return             the requested object as ASN1Object
     * @throws IOException if any object or subobject can not be dumped
     */
    public ASN1Object toASN1Object(DumpType dt) throws IOException {
        // Prepare encoding
        LOGGER.log(Level.FINER,"Executing toASN1Object()");

        ASN1EncodableVector v=new ASN1EncodableVector();

        // add prefix to structure
        addPrefixBlockToASN1(v,dt);
        // add inner message to structure
        addInnerMessageBlockToASN1(v,dt);

        ASN1Sequence seq=new DERSequence(v);
        LOGGER.log(Level.FINER,"done toASN1Object()");
        return seq;
    }

    private void addPrefixBlockToASN1(ASN1EncodableVector v,DumpType dumpType) throws IOException {
        ASN1Object o=getPrefix().toASN1Object(dumpType);
        if (o == null) {
            throw new IOException( "returned prefix object may not be null" );
        }
        if(dumpType==DumpType.ALL_UNENCRYPTED || getPrefix().getDecryptionKey()==null) {
            v.add(new DERTaggedObject(PREFIX_PLAIN, o));
        } else {
            v.add(new DERTaggedObject(PREFIX_ENCRYPTED, new DEROctetString(getPrefix().toEncBytes())));
        }
    }

    private void addInnerMessageBlockToASN1(ASN1EncodableVector v, DumpType dt) throws IOException {
        if(prefix.getKey()==null || DumpType.ALL_UNENCRYPTED.equals(dt)) {
            v.add( new DERTaggedObject( INNER_MESSAGE_PLAIN,getInnerMessage().toASN1Object(dt) ));
        } else {
            byte[] b=toDER(getInnerMessage().toASN1Object(dt));
            v.add( new DERTaggedObject( INNER_MESSAGE_ENCRYPTED,new DEROctetString( prefix.getKey().encrypt( b ))));
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
        ret+=prefix+"  innerMessage " + getInnerMessage().dumpValueNotation( prefix + "  ",dt ) + "," + CRLF;
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
    public static long getBytesAsLong(byte[] b) {
        if(b==null || b.length<1 || b.length>8) {
            throw new IllegalArgumentException( "byte array must contain exactly four bytes" );
        }
        long ret=0;
        for(int i=0;i<b.length;i++) {
            ret|=((long)(b[i]&0xFF))<<(i*8);
        }
        return ret;
    }

    @Override
    public boolean equals(Object o) {
        if(o==null) return false;
        if(!(o instanceof VortexMessage)) {
            return false;
        }
        VortexMessage vm=(VortexMessage)o;
        try{
            return Arrays.equals(vm.toBytes(DumpType.ALL_UNENCRYPTED),this.toBytes(DumpType.ALL_UNENCRYPTED));
        } catch (IOException ioe) {
            return false;
        }
    }

    @Override
    public int hashCode() {
        try {
            return prepareDump(dumpValueNotation("",DumpType.ALL_UNENCRYPTED)).hashCode();
        } catch(IOException ioe) {
            return -1;
        }
    }
}
