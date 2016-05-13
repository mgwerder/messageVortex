package net.gwerder.java.mailvortex.asn1;

import org.bouncycastle.asn1.*;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.text.ParseException;
import java.util.logging.Level;
import java.util.logging.Logger;

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

    public static String toHex(byte[] bytes) {
        if(bytes==null) {
            bytes = new byte[0];
        }
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

    protected void parse(byte[] b) throws IOException,ParseException,NoSuchAlgorithmException {
        parse( ASN1Sequence.getInstance( b ) );
    }

    abstract protected void parse(ASN1Encodable to) throws IOException,ParseException,NoSuchAlgorithmException;

    abstract public String dumpValueNotation(String prefix) throws IOException;

    protected byte[] toDER(ASN1Object a) {
        if(a==null) throw new NullPointerException( "null object may not be encoded in DER" );
        ByteArrayOutputStream bOut = new ByteArrayOutputStream();
        DEROutputStream       dOut = new DEROutputStream(bOut);
        try {
            dOut.writeObject( a );
        } catch (IOException ioe) {
            // should never occur as we have no IO
            Logger.getLogger("Message").log( Level.SEVERE,"Exception while encoding object",ioe);
        }
        return bOut.toByteArray();
    }

    abstract ASN1Object toASN1Object() throws IOException;

    public byte[] toBytes() throws IOException {
        ASN1Object o=toASN1Object();
        if(o==null) throw new IOException( "Got a null reply from toASN1Object ... get coding man" );
        return toDER( o );
    }
}
