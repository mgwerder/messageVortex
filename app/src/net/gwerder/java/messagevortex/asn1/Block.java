package net.gwerder.java.messagevortex.asn1;

import org.bouncycastle.asn1.*;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.text.ParseException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Abstract class collecting all ASN1 Block parser classes.
 *
 * Created by martin.gwerder on 14.04.2016.
 */
public abstract class Block {

    protected static final String CRLF="\r\n";


    public static String toHex(byte[] data) {
        byte[] bytes=data;
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
        if(bs==null) {
            return "''B";
        }
        int i=bs.getBytes().length*8-bs.getPadBits();
        if(i%8==0) {
            return toHex(bs.getOctets());
        }
        String ret="'";
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

    protected void parse(byte[] b) throws IOException,ParseException,NoSuchAlgorithmException {
        parse( ASN1Sequence.getInstance( b ) );
    }

    protected abstract void parse(ASN1Encodable to) throws IOException,ParseException,NoSuchAlgorithmException;

    public abstract String dumpValueNotation(String prefix) throws IOException;

    protected byte[] toDER(ASN1Object a) {
        if(a==null) {
            throw new NullPointerException( "null object may not be encoded in DER" );
        }
        ByteArrayOutputStream bOut = new ByteArrayOutputStream();
        DEROutputStream       dOut = new DEROutputStream(bOut);
        try {
            dOut.writeObject( a );
        } catch (IOException ioe) {
            // should never occur as we have no IO
            Logger.getLogger("VortexMessage").log( Level.SEVERE,"Exception while encoding object",ioe);
        }
        return bOut.toByteArray();
    }

    abstract ASN1Object toASN1Object() throws IOException,NoSuchAlgorithmException,ParseException;

    public byte[] toBytes() throws IOException {
        try {
            ASN1Object o = toASN1Object();
            if (o == null) {
                throw new IOException("Got a null reply from toASN1Object ... get coding man");
            }
            return toDER(o);
        } catch(NoSuchAlgorithmException|ParseException e) {
            throw new IOException("exception while getting ASN.1 object",e);
        }
    }

}
