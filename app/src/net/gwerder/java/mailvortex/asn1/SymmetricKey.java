package net.gwerder.java.mailvortex.asn1;


import net.gwerder.java.mailvortex.asn1.encryption.Algorithm;
import net.gwerder.java.mailvortex.asn1.encryption.AlgorithmType;
import net.gwerder.java.mailvortex.asn1.encryption.Mode;
import net.gwerder.java.mailvortex.asn1.encryption.Padding;
import org.bouncycastle.asn1.*;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.SecretKeySpec;
import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

/**
 * Created by martin.gwerder on 19.04.2016.
 */
public class SymmetricKey extends Key {

    private static SecureRandom secureRandom = new SecureRandom();

    protected byte[] key= null;
    private Mode mode=Mode.getDefault();
    private Padding padding= Padding.getDefault(AlgorithmType.SYMMETRIC);

    public SymmetricKey() throws IOException,NoSuchAlgorithmException {
        this(Algorithm.getDefault( AlgorithmType.SYMMETRIC ));
    }

    public SymmetricKey(Algorithm sk) throws IOException,NoSuchAlgorithmException {
        keytype=sk;
        if(sk.toString().equals(Algorithm.AES256.toString())) {
           createAES( 256 );
        } else if(sk.toString().equals(Algorithm.AES192.toString())) {
            createAES( 192 );
        } else if(sk.toString().equals(Algorithm.AES128.toString())) {
            createAES( 128 );
        } else {
            throw new NoSuchAlgorithmException( "Algorithm "+sk+" is not encodable by the system" );
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

    private void createAES(int keysize) {
        byte[] keyBytes = new byte[keysize / 8];
        secureRandom.nextBytes( keyBytes );
        SecretKeySpec aeskey = new SecretKeySpec( keyBytes, "AES" );
        key = aeskey.getEncoded();
    }

    private Cipher getCipher() throws NoSuchAlgorithmException,NoSuchPaddingException {
        return Cipher.getInstance( keytype.getAlgorithmFamily()+"/"+mode.getMode()+"/"+padding.getPadding() );
    }

    @Override
    public byte[] encrypt(byte[] b) throws IOException {
        try {
            Cipher c = getCipher();
            SecretKeySpec ks = new SecretKeySpec( key, keytype.getAlgorithmFamily() );
            c.init( Cipher.ENCRYPT_MODE, ks );
            return c.doFinal( b );
        } catch (NoSuchAlgorithmException e) {
            throw new IOException( "Exception while encrypting", e );
        } catch (NoSuchPaddingException e) {
            throw new IOException( "Exception while encrypting", e );
        } catch (InvalidKeyException e) {
            throw new IOException( "Exception while init of cipher", e );
        } catch (IllegalBlockSizeException e) {
            throw new IOException( "Exception while encrypting", e );
        } catch (BadPaddingException e) {
            throw new IOException( "Exception while encrypting", e );
        }
    }

    @Override
    public byte[] decrypt(byte[] b) throws IOException {
        try {
            Cipher c = getCipher();
            SecretKeySpec ks = new SecretKeySpec( key, keytype.getAlgorithmFamily().toUpperCase() );
            c.init( Cipher.DECRYPT_MODE, ks );
            return c.doFinal( b );
        } catch (NoSuchAlgorithmException e) {
            throw new IOException( "Exception while encrypting", e );
        } catch (NoSuchPaddingException e) {
            throw new IOException( "Exception while encrypting", e );
        } catch (InvalidKeyException e) {
            throw new IOException( "Exception while init of cipher", e );
        } catch (IllegalBlockSizeException e) {
            throw new IOException( "Exception while encrypting", e );
        } catch (BadPaddingException e) {
            throw new IOException( "Exception while encrypting", e );
        }
    }

    protected void parse(ASN1Encodable to) {
        // preparing parsing
        int i=0;
        ASN1Sequence s1 = ASN1Sequence.getInstance(to);

        // parsing Symetric Key Idetifier
        parseKeyParameter(ASN1Sequence.getInstance( s1.getObjectAt(i++) ));

        // getting key
        key=ASN1OctetString.getInstance( s1.getObjectAt(i++)).getOctets();
    }

    public byte[] getKey() { return key; }

    @Override
    public ASN1Object toASN1Object() throws IOException {
        ASN1EncodableVector ret = new ASN1EncodableVector();
        ret.add(encodeKeyParameter());
        ret.add(new DEROctetString( key ));
        return new DERSequence(ret);
    }

    public String dumpValueNotation(String prefix) {
        StringBuilder sb=new StringBuilder();
        sb.append("{"+CRLF);
        sb.append(dumpKeyTypeValueNotation(prefix)+CRLF);
        sb.append(prefix+"  key "+toHex(key)+CRLF);
        sb.append(prefix+"}");
        return sb.toString();
    }


}
