package net.gwerder.java.mailvortex.asn1;


import net.gwerder.java.mailvortex.ExtendedSecureRandom;
import net.gwerder.java.mailvortex.asn1.encryption.Algorithm;
import net.gwerder.java.mailvortex.asn1.encryption.AlgorithmType;
import net.gwerder.java.mailvortex.asn1.encryption.Mode;
import net.gwerder.java.mailvortex.asn1.encryption.Padding;
import org.bouncycastle.asn1.*;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.IOException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;

/**
 * Created by martin.gwerder on 19.04.2016.
 */
public class SymmetricKey extends Key {

    private static ExtendedSecureRandom secureRandom = new ExtendedSecureRandom();

    protected byte[] key= null;

    public SymmetricKey() throws IOException,NoSuchAlgorithmException {
        this(Algorithm.getDefault( AlgorithmType.SYMMETRIC ));
    }

    public SymmetricKey(Algorithm sk ,Padding pad, Mode mode) throws IOException {
        keytype=sk;
        this.padding=pad;
        this.mode=mode;
        if(sk.toString().toLowerCase().startsWith("aes")) {
            createAES( sk.getKeySize() );
        } else if(sk.toString().toLowerCase().startsWith("camellia")) {
            createCamellia( sk.getKeySize() );
        } else {
            throw new IOException( "Algorithm "+sk+" is not encodable by the system" );
        }
    }

    public SymmetricKey(Algorithm sk) throws IOException {
        mode = Mode.getDefault(AlgorithmType.SYMMETRIC);
        padding = Padding.getDefault( AlgorithmType.SYMMETRIC );
        keytype=sk;
        if(sk.toString().toLowerCase().startsWith("aes")) {
           createAES( sk.getKeySize() );
        } else if(sk.toString().toLowerCase().startsWith("camellia")) {
            createCamellia( sk.getKeySize() );
        } else {
            throw new IOException( "Algorithm "+sk+" is not encodable by the system" );
        }
    }

    public byte[] setIV(byte[] b) {
        byte[] old=initialisationVector;
        if(b==null || b.length==0) {
            // generate random IV
            initialisationVector=new byte[16];
            secureRandom.nextBytes( initialisationVector );
        } else {
            initialisationVector=b;
        }
        return old;
    }

    public byte[] getIV() {
        return initialisationVector;
    }

    public Padding getPadding() {
        return this.padding;
    }

    public Mode getMode() {
        return this.mode;
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
        if(mode.getRequiresIV()) {
            initialisationVector=new byte[16];
            setIV( initialisationVector );
        }
        SecretKeySpec aeskey = new SecretKeySpec( keyBytes, "AES" );
        key = aeskey.getEncoded();
    }

    private void createCamellia(int keysize) {
        byte[] keyBytes = new byte[keysize / 8];
        secureRandom.nextBytes( keyBytes );
        if(mode.getRequiresIV()) {
            initialisationVector=new byte[16];
            setIV( initialisationVector );
        }
        SecretKeySpec camelliakey = new SecretKeySpec( keyBytes, "Camellia" );
        key = camelliakey.getEncoded();
    }

    private Cipher getCipher() throws NoSuchAlgorithmException,NoSuchPaddingException {
        setIV(initialisationVector);
        return Cipher.getInstance( keytype.getAlgorithmFamily()+"/"+mode.getMode()+"/"+padding.getPadding() );
    }

    @Override
    public byte[] encrypt(byte[] b) throws IOException {
        try {
            Cipher c = getCipher();
            SecretKeySpec ks = new SecretKeySpec( key, keytype.getAlgorithmFamily() );
            if(mode.getRequiresIV()) {
                setIV( initialisationVector );
                c.init( Cipher.ENCRYPT_MODE, ks, new IvParameterSpec( initialisationVector ) );
            } else {
                c.init( Cipher.ENCRYPT_MODE, ks );
            }
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
        } catch (InvalidAlgorithmParameterException e) {
            throw new IOException( "Exception while encrypting ("+keytype.getAlgorithmFamily()+"/"+initialisationVector.length+")", e );
        }
    }

    @Override
    public byte[] decrypt(byte[] b) throws IOException {
        try {
            Cipher c = getCipher();
            SecretKeySpec ks = new SecretKeySpec( key, keytype.getAlgorithmFamily().toUpperCase() );
            if(mode.getRequiresIV()) {
                c.init( Cipher.DECRYPT_MODE, ks, new IvParameterSpec( initialisationVector ) );
            } else {
                c.init( Cipher.DECRYPT_MODE, ks );
            }
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
        } catch (InvalidAlgorithmParameterException e) {
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

    public byte[] setKey(byte[] b) {
        byte[] old=key;
        key=b;
        return old;
    }

    @Override
    public ASN1Object toASN1Object() throws IOException {
        ASN1EncodableVector ret = new ASN1EncodableVector();
        ret.add(encodeKeyParameter());
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

        // compare public keys
        SymmetricKey o=(SymmetricKey)t;
        if(!Arrays.equals(o.key,key)) {
            return false;
        }

        // compare initialisationVectors
        if(!Arrays.equals(o.initialisationVector,initialisationVector)) {
            return false;
        }

        // compare mode (CBC, ECB et al)
        if(o.mode!=mode) {
            return false;
        }

        // compare padding
        if(o.padding!=padding) {
            return false;
        }

        return true;
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
