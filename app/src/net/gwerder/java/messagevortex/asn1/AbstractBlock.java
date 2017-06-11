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

import net.gwerder.java.messagevortex.asn1.encryption.DumpType;
import org.bouncycastle.asn1.ASN1BitString;
import org.bouncycastle.asn1.ASN1Encodable;
import org.bouncycastle.asn1.ASN1Object;
import org.bouncycastle.asn1.ASN1Sequence;

import java.io.IOException;
import java.io.Serializable;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Abstract class collecting all ASN1 Block parser classes.
 *
 * Created by martin.gwerder on 14.04.2016.
 */
public abstract class AbstractBlock implements Serializable,Block {

    protected static final String CRLF="\r\n";

    public static byte[] fromHex(String s) {
        if(s==null) {
            return null;
        }
        int len = s.length();
        byte[] data = new byte[Math.max(0,(len-3) / 2)];
        for (int i = 1; i < len-2; i += 2) {
            data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4)
                    + Character.digit(s.charAt(i+1), 16));
        }
        return data;
    }

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

    protected void parse(byte[] b) throws IOException {
        parse( ASN1Sequence.getInstance( b ) );
    }

    protected abstract void parse(ASN1Encodable to) throws IOException;

    public abstract String dumpValueNotation(String prefix,DumpType dumptype) throws IOException;

    protected static byte[] toDER(ASN1Object a) {
        if(a==null) {
            throw new NullPointerException( "null object may not be encoded in DER" );
        }
        try {
            return a.getEncoded();
        } catch (IOException ioe) {
            // should never occur as we have no IO
            Logger.getLogger("VortexMessage").log( Level.SEVERE,"Exception while encoding object",ioe);
            return null;
        }
    }

    public abstract ASN1Object toASN1Object(DumpType dumpType) throws IOException;

    /***
     * Dumps the object as ASN.1 der encoded byte array.
     *
     * @param dumpType  the dump type to be used (@see DumpType)
     * @return          the der encoded byte array
     * @throws IOException if encoding was unsuccessful
     */
    public byte[] toBytes(DumpType dumpType) throws IOException {
        ASN1Object o = toASN1Object(dumpType);
        if (o == null) {
            throw new IOException("Got a null reply from toASN1Object ... get coding man");
        }
        return toDER(o);
    }


    protected String prepareDump(String s) {
        return s.replaceAll( "encrypted *'[^']*'H","encrypted '<encryptedString>'H");
    }

}
