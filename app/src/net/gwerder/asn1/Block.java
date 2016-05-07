package net.gwerder.asn1;

import org.bouncycastle.asn1.*;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.text.ParseException;

/**
 * Created by martin.gwerder on 14.04.2016.
 */
public abstract class Block {

    protected static final String CRLF="\r\n";

    public static enum AlgorithmType {
        SYMETRIC,
        ASYMETRIC,
        HASHING
    };

    public static enum Parameter {
        KEYSIZE   (10000,"keySize"),
        CURVETYPE (10001,"curveType");

        int id=-1;
        String txt=null;

        Parameter(int id,String txt) {
            this.id=id;
            this.txt=txt;
        }

        public static Parameter getById(int id) {
            for(Parameter e : values()) {
                if(e.id==id) return e;
            }
            return null;
        }

        public static Parameter getByString(String s) {
            for(Parameter e : values()) {
                if(e.toString().equals(s)) return e;
            }
            return null;
        }

        public int getId() {return id;};

        public String toString() {
            return txt;
        }
    }

    public static enum Algorithm {
        AES128    (1000,AlgorithmType.SYMETRIC,"aes128"),
        AES192    (1001,AlgorithmType.SYMETRIC,"aes192"),
        AES256    (1002,AlgorithmType.SYMETRIC,"aes256"),
        RSA       (2000,AlgorithmType.ASYMETRIC,"rsa"),
        DSA       (2100,AlgorithmType.ASYMETRIC,"dsa"),
        SECP384R1 (2500,AlgorithmType.ASYMETRIC,"secp384R1"),
        SECT409K1 (2501,AlgorithmType.ASYMETRIC,"sect409K1"),
        SECP521R1 (2502,AlgorithmType.ASYMETRIC,"secp521r1"),
        SHA384    (3000,AlgorithmType.HASHING,"sha384"),
        SHA512    (3001,AlgorithmType.HASHING,"sha512"),
        TIGER192  (3100,AlgorithmType.HASHING,"tiger192");


        private int id;
        private AlgorithmType t;
        private String txt;

        Algorithm(int id,AlgorithmType t,String txt) {
            this.id=id;
            this.t=t;
            this.txt=txt;
        }

        public int getId() {return id;};

        public static Algorithm getById(int id) {
            for(Algorithm e : values()) {
                if(e.id==id) return e;
            }
            return null;
        }

        public static Algorithm getByString(String s) {
            for(Algorithm e : values()) {
                if(e.toString().equals(s.toLowerCase())) return e;
            }
            return null;
        }

        @Override
        public String toString() {
            return super.toString().toLowerCase();
        }
    }

    public static String toHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            sb.append(String.format("%02X", b));
        }
        return "'"+(sb.toString())+"'H";
    }

    public static String toBitString(ASN1BitString bs) {
        if(bs==null) return "''B";
        String ret="'";
        int i=bs.getBytes().length*8-bs.getPadBits();
        int j=0;
        byte k=0;
        byte[] b=bs.getBytes();
        while(i>0) {
            ret+=""+(((b[j]>>(7-k))&1)>0?"1":"0");
            k++;
            if(k>7) {
                k=0;
                j++;
            }
            i--;
        }
        return ret+"'B";
    }

    public static String toBitString(byte[] bs) {
        if(bs==null || bs.length==0) return "''B";
        return toBitString(new DLBitString(bs,1));
    }

    abstract public ASN1Encodable encodeDER();

    protected void parse(byte[] b) throws IOException,ParseException,NoSuchAlgorithmException {
        parse( ASN1Sequence.getInstance( b ) );
    }

    abstract protected void parse(ASN1Encodable to) throws IOException,ParseException,NoSuchAlgorithmException;

}
