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
import net.gwerder.java.messagevortex.asn1.encryption.*;
import org.bouncycastle.asn1.*;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.IOException;
import java.io.Serializable;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.util.Arrays;

/**
 * Represents a Symmetric Key in the ASN.1 structure
 */
public class SymmetricKey extends Key implements Serializable {

    public static final long serialVersionUID = 100000000040L;

    private static ExtendedSecureRandom secureRandom = new ExtendedSecureRandom();

    protected byte[] key= null;

    public SymmetricKey() throws IOException {
        this(Algorithm.getDefault( AlgorithmType.SYMMETRIC ));
    }

    public SymmetricKey(Algorithm sk) throws IOException {
        this(sk,Padding.getDefault( AlgorithmType.SYMMETRIC ),Mode.getDefault(AlgorithmType.SYMMETRIC));
    }

    public SymmetricKey(Algorithm sk , Padding pad, Mode mode) throws IOException {
        if(pad==null) {
            throw new NullPointerException("padding may not be null");
        }
        if(mode==null) {
            throw new NullPointerException("mode may not be null");
        }
        parameters.put(Parameter.ALGORITHM,sk.toString());
        parameters.put(Parameter.PADDING,pad.toString());
        parameters.put(Parameter.MODE,mode.toString());
        if(sk.toString().toLowerCase().startsWith("aes")) {
            createAES( sk.getKeySize() );
        } else if(sk.toString().toLowerCase().startsWith("camellia")) {
            createCamellia( sk.getKeySize() );
        } else if(sk.toString().toLowerCase().startsWith("twofish")) {
            createTwofish( sk.getKeySize() );
        } else {
            throw new IOException( "Algorithm "+sk+" is not encodable by the system" );
        }
    }

    public SymmetricKey(byte[] sk) throws IOException {
        this( sk, null );
    }

    public SymmetricKey(byte[] sk, AsymmetricKey deckey) throws IOException {
        // decrypt and decode
        ASN1Primitive s;
        if(deckey!=null) {
            byte[] b;
            try {
                b = deckey.decrypt( sk );
            } catch(Exception e) {
                throw new IOException( "Error while decrypting object", e );
            }
            s=DERSequence.fromByteArray(b);
        } else {
            s=DERSequence.fromByteArray(sk);
        }
        parse( s );
    }

    public byte[] setIV(byte[] b) {
        String s=parameters.get(Parameter.IV);
        byte[] old;
        if(s==null) {
            old = null;
        } else {
            old=s.getBytes();
        }
        if(b==null || b.length==0) {
            parameters.put(Parameter.IV,toHex(secureRandom.generateSeed(16)));
        } else {
            parameters.put(Parameter.IV,toHex(b));
        }
        return old;
    }

    public byte[] getIV() {
        return fromHex(parameters.get(Parameter.IV));
    }

    public AlgorithmParameter getParameter() {
        return parameters.clone();
    }

    public Padding getPadding() {
        return Padding.getByString(parameters.get(Parameter.PADDING.toString()));
    }

    /***
     * gets the key size from the key generation parameters.
     *
     * @return the key size in bits or -1 if there is no key size set
     */
    public int getKeySize() {
        return parameters.get(Parameter.KEYSIZE)!=null?Integer.parseInt(parameters.get(Parameter.KEYSIZE)):getAlgorithm().getKeySize();
    }

    public Mode getMode() {
        return Mode.getByString(parameters.get(Parameter.MODE.toString()));
    }

    public Algorithm getAlgorithm() {
        return Algorithm.getByString(parameters.get(Parameter.ALGORITHM));
    }

    private void createAES(int keysize) {
        Mode mode=getMode();
        byte[] keyBytes = new byte[keysize / 8];
        secureRandom.nextBytes( keyBytes );
        if(mode.getRequiresIV() && (getIV()==null || getIV().length!=16)) {
            setIV( null );
        }
        SecretKeySpec aeskey = new SecretKeySpec( keyBytes, "AES" );
        key = aeskey.getEncoded();
    }

    private void createCamellia(int keysize) {
        Mode mode=getMode();
        byte[] keyBytes = new byte[keysize / 8];
        secureRandom.nextBytes( keyBytes );
        if(mode.getRequiresIV()) {
            setIV( null );
        }
        SecretKeySpec camelliakey = new SecretKeySpec( keyBytes, "Camellia" );
        key = camelliakey.getEncoded();
    }

    private void createTwofish(int keysize) {
        Mode mode=getMode();
        byte[] keyBytes = new byte[keysize / 8];
        secureRandom.nextBytes( keyBytes );
        if(mode.getRequiresIV()) {
            setIV( null );
        }
        SecretKeySpec twofishkey = new SecretKeySpec( keyBytes, "Twofish" );
        key = twofishkey.getEncoded();
    }

    private Cipher getCipher() throws NoSuchAlgorithmException,NoSuchPaddingException {
        setIV( getIV() );
        try {
            return Cipher.getInstance(getAlgorithm().getAlgorithmFamily().toUpperCase() + "/" + getMode().toString() + "/" + getPadding().toString(), getAlgorithm().getProvider());
        } catch(NoSuchProviderException e) {
            throw new NoSuchAlgorithmException("unknown provider",e);
        }
    }

    @Override
    public byte[] encrypt(byte[] b) throws IOException {
        try {
            Cipher c = getCipher();
            SecretKeySpec ks = new SecretKeySpec( key, getAlgorithm().getAlgorithmFamily().toUpperCase() );
            if(getMode().getRequiresIV()) {
                setIV( getIV() );
                c.init( Cipher.ENCRYPT_MODE, ks, new IvParameterSpec( getIV()) );
            } else {
                c.init( Cipher.ENCRYPT_MODE, ks );
            }
            return c.doFinal( b );
        } catch (NoSuchAlgorithmException|NoSuchPaddingException|IllegalBlockSizeException|BadPaddingException e) {
            throw new IOException( "Exception while encrypting", e );
        } catch (InvalidKeyException e) {
            throw new IOException( "Exception while init of cipher [possible limmited JCE installed] ("+getParameter()+"/"+getIV().length+"/"+(key.length*8)+")", e );
        } catch (InvalidAlgorithmParameterException e) {
            throw new IOException( "Exception while encrypting ("+getAlgorithm().getAlgorithmFamily()+"/"+getIV().length+")", e );
        }
    }

    @Override
    public byte[] decrypt(byte[] b) throws IOException {
        try {
            Cipher c = getCipher();
            SecretKeySpec ks = new SecretKeySpec( key, getAlgorithm().getAlgorithmFamily().toUpperCase() );
            if(getMode().getRequiresIV()) {
                c.init( Cipher.DECRYPT_MODE, ks, new IvParameterSpec( getIV()) );
            } else {
                c.init( Cipher.DECRYPT_MODE, ks );
            }
            return c.doFinal( b );
        } catch (NoSuchAlgorithmException|NoSuchPaddingException|IllegalBlockSizeException|InvalidAlgorithmParameterException|BadPaddingException e) {
            throw new IOException( "Exception while decrypting", e );
        } catch (InvalidKeyException e) {
            throw new IOException( "Exception while init of cipher", e );
        }
    }

    protected void parse(ASN1Encodable to) throws IOException {
        // preparing parsing
        int i=0;
        ASN1Sequence s1 = ASN1Sequence.getInstance(to);

        // parsing Symetric Key Idetifier
        parseKeyParameter(ASN1Sequence.getInstance( s1.getObjectAt(i++) ));

        // getting key
        key=ASN1OctetString.getInstance( s1.getObjectAt(i)).getOctets();
    }

    public byte[] getKey() { return key; }

    public byte[] setKey(byte[] b) {
        byte[] old=key;
        key=b;
        return old;
    }

    @Override
    public ASN1Object toASN1Object(DumpType dumpType) throws IOException {
        ASN1EncodableVector ret = new ASN1EncodableVector();
        ret.add(encodeKeyParameter(dumpType));
        ret.add(new DEROctetString( key ));
        return new DERSequence(ret);
    }

    public boolean equals(Object t) {
        // make sure object is not null
        if(t==null) {
            return false;
        }

        //make sure object is of right type
        if(! (t instanceof SymmetricKey)) {
            return false;
        }

        // compare  keys
        SymmetricKey o=(SymmetricKey)t;
        return o.dumpValueNotation("",DumpType.ALL_UNENCRYPTED).equals(dumpValueNotation("",DumpType.ALL_UNENCRYPTED));
   }

    @Override
    public int hashCode() {
        return dumpValueNotation("",DumpType.ALL_UNENCRYPTED).hashCode();
    }

    @Override
    public String dumpValueNotation(String prefix,DumpType dumpType) {
        StringBuilder sb=new StringBuilder();
        sb.append("{"+CRLF);
        sb.append(dumpKeyTypeValueNotation(prefix+"  ",dumpType)+","+CRLF);
        sb.append(prefix+"  key "+toHex(key)+CRLF);
        sb.append(prefix+"}");
        return sb.toString();
    }

    /***
     * Gets a textual representation of the objects parameters (without the keys)
     * @return the string
     */
    @Override
    public String toString() {
        return "([SymmetricKey]hash="+(key!=null? Arrays.hashCode(key):"null")+";"+parameters.toString()+")";
    }



}
