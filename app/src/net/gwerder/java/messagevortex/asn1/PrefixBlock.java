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

import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.text.ParseException;
import java.util.logging.Level;

/**
 * ASN1 parser class for header reply.
 */
public class PrefixBlock extends AbstractBlock {

    byte[] encrypted=null;
    AsymmetricKey decryptionKey=null;

    /** The key used for decryption of the rest of the VortexMessage **/
    SymmetricKey key=null;

    private static final java.util.logging.Logger LOGGER;
    static {
        LOGGER = MessageVortexLogger.getLogger((new Throwable()).getStackTrace()[0].getClassName());
        MessageVortexLogger.setGlobalLogLevel( Level.ALL);
    }

    /**
     * Creates an empty prefix
     */
    public PrefixBlock() throws IOException  {
       this(null);
    }

    /***
     * Creates a prefix with the given key.
     *
     * @param sk
     */
    public PrefixBlock(SymmetricKey sk) throws IOException {
        if(sk==null) {
            key=new SymmetricKey();
        } else {
            key=sk;
        }
    }

    /***
     * Creates a prefix by parsing to in plan (unencrypted)
     *
     * @param to The primitive to be parsed
     *
     * @throws IOException if parsing fails
     */
    public PrefixBlock(ASN1Primitive to, AsymmetricKey ak) throws IOException {
        this(toDER(to),ak);
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
    public PrefixBlock(byte[] to, AsymmetricKey ak) throws IOException {
        if(ak!=null) {
            setDecryptionKey(ak);
        }
        AsymmetricKey decrypt=getDecryptionKey();
        if(decrypt!=null && decrypt.hasPrivateKey()) {
            parse( ASN1Sequence.getInstance(decrypt.decrypt( to )));
        } else {
            try {
                parse(ASN1Sequence.getInstance(to));
            } catch(IOException|RuntimeException ioe) {
                LOGGER.log(Level.WARNING, "Parsing of prefix block failed", ioe);
                setDecryptionKey(null);
                key=null;
                encrypted=to;
            }
        }
    }

    @Override
    protected void parse(ASN1Encodable to) throws IOException {
        encrypted=null;
        LOGGER.log( Level.FINER,"Executing parse()");
        int i = 0;
        ASN1Sequence s1 = ASN1Sequence.getInstance( to );

        // getting key
        key = new SymmetricKey( toDER(s1.getObjectAt(i++).toASN1Primitive()) ,null );
        if(key==null) {
            throw new IOException("symmetric key may not be null when decoding");
        }
    }

    public AsymmetricKey setDecryptionKey(AsymmetricKey dk) throws IOException {
        AsymmetricKey old=getDecryptionKey();
        decryptionKey=dk;
        if(isEncrypted()) {
            parse(dk.decrypt(encrypted));
        }
        return old;
    }

    public AsymmetricKey getDecryptionKey() {
        return decryptionKey;
    }

    public SymmetricKey setKey(SymmetricKey dk) {
        if(dk==null) {
            throw new NullPointerException("symmetric key may not be null");
        }
        SymmetricKey old=getKey();
        key=dk;
        encrypted=null;
        return old;
    }

    public SymmetricKey getKey() {
        return key;
    }

    @Override
    public ASN1Object toASN1Object(DumpType dumpType) throws IOException {
        if (getKey() == null && isEncrypted()) {
            throw new IOException( "only encrypted form may be dumped without providing a valid decryption key" );
        }
        ASN1EncodableVector v=new ASN1EncodableVector();
        ASN1Encodable o=getKey().toASN1Object(dumpType);
        if (o == null) {
            throw new IOException( "returned symmetric object may not be null" );
        }
        v.add( o );

        LOGGER.log(Level.FINER,"done toASN1Object() of PrefixBlock");
        return new DERSequence(v);
    }

    @Override
    public String dumpValueNotation(String prefix, DumpType dumpType) throws IOException {
        StringBuilder sb=new StringBuilder();
        if(DumpType.ALL==dumpType || DumpType.PUBLIC_ONLY==dumpType) {
            // dump standard block as octet string
            sb.append( "encrypted "+toHex(toEncBytes()) );
        } else {
            // dump as unecrypted structure
            sb.append( "plain  {"+CRLF );
            sb.append( prefix+"  key "+key.dumpValueNotation(prefix+"  ",dumpType)+CRLF);
            sb.append( prefix+"}" );
        }
        return sb.toString();
    }

    @Override
    public boolean equals(Object o) {
        // must be equivalent in type
        if(!(o instanceof PrefixBlock)) {
            return false;
        }

        // do casting
        PrefixBlock p=(PrefixBlock)(o);

        // look for not equal keys
        if((p.getKey()==null && getKey()!=null) || (p.getKey()!=null && getKey()==null)) {
            return false;
        }
        if(p.getKey()!=null && !p.getKey().equals(getKey())) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        return getKey().hashCode();
    }

    public boolean isEncrypted() {
        return encrypted!=null;
    }

    public byte[] toEncBytes() throws IOException {
        if(decryptionKey != null && encrypted == null) {
            return decryptionKey.encrypt(toBytes(DumpType.PUBLIC_ONLY));
        } else {
            return encrypted;
        }
    }
}
