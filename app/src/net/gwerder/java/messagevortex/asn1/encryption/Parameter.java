package net.gwerder.java.messagevortex.asn1.encryption;
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

import org.bouncycastle.asn1.*;

import java.io.Serializable;
import java.nio.charset.StandardCharsets;

/**
 * Enumeration of all supported Parameters.
 *
 * Created by martin.gwerder on 31.05.2016.
 */
public enum Parameter implements Serializable {
    /* dummy id for internal use (store algorithm with parameter for key cache) */
    ALGORITHM (1,    "algorithm",null),

    /* Keysize parameter specifying the size of an encryption key in bits */
    KEYSIZE   (10000,"keySize",new Transcoder() {
        @Override
        public String fromASN1(ASN1Object o) {
            return ASN1Integer.getInstance(o).getPositiveValue().toString();
        }
        @Override
        public ASN1Encodable toASN1(String s) {
            return new ASN1Integer(Integer.parseInt(s));
        }
    }),

    /* Parameter for courve type name when using ECC */
    CURVETYPE(10001, "curveType", new Transcoder() {
        @Override
        public String fromASN1(ASN1Object o) {
            return ECCurveType.getById(ASN1Enumerated.getInstance(o).getValue().intValue()).toString();
        }
        @Override
        public ASN1Encodable toASN1(String s) {
            return new ASN1Enumerated(ECCurveType.getByString(s).getId());
        }
    }),

    /* Initialisation vector specification for IV based encryption modes */
    IV        (10002,"initialisationVector",new Transcoder() {
        public String fromASN1( ASN1Object o ) {
            return new String( ASN1OctetString.getInstance( o ).getOctets(), StandardCharsets.UTF_8 );
        }
        public ASN1Encodable toASN1( String s ) {
            return new DEROctetString( s.getBytes( StandardCharsets.UTF_8 ) );
        }
    }),

    /* nonce required for certain CR algorithms */
    NONCE     (10003,"nonce",new Transcoder() {
        public String fromASN1( ASN1Object o ) {
            return new String( ASN1OctetString.getInstance( o ).getOctets(), StandardCharsets.UTF_8 );
        }
        public ASN1Encodable toASN1( String s ) {
            return new DEROctetString( s.getBytes( StandardCharsets.UTF_8 ) );
        }
    }),

    /* Name of encryption mode to be used */
    MODE      (10004,"mode",new Transcoder() {
        public String fromASN1( ASN1Object o ) {
            return new String( ASN1OctetString.getInstance( o ).getOctets(), StandardCharsets.UTF_8 );
        }
        public ASN1Encodable toASN1( String s ) {
            return new DEROctetString( s.getBytes( StandardCharsets.UTF_8 ) );
        }
    }),

    /* name of padding to be used for encryption */
    PADDING   (10005,"padding",new Transcoder() {
        public String fromASN1( ASN1Object o ) {
            return new String( ASN1OctetString.getInstance( o ).getOctets(), StandardCharsets.UTF_8 );
        }
        public ASN1Encodable toASN1( String s ) {
            return new DEROctetString( s.getBytes( StandardCharsets.UTF_8 ) );
        }
    }),

    /* size of encryption block (When different from key size in Bit). The size is measured in bits */
    BLOCKSIZE (10100,"blockSize",new Transcoder() {
        @Override
        public String fromASN1( ASN1Object o ) {
            return ASN1Integer.getInstance( o ).getPositiveValue().toString();
        }
        @Override
        public ASN1Encodable toASN1( String s ) {
            return new ASN1Integer( Integer.parseInt( s ) );
        }
    });

    private interface Transcoder {
        String fromASN1(ASN1Object o);
        ASN1Encodable toASN1(String s);
    }

    final int id;
    final String txt;
    final Transcoder transcoder;

    Parameter(int id,String txt,Transcoder transcoder) {
        this.id=id;
        this.txt=txt;
        this.transcoder=transcoder;
    }

    /***
     * Get a paraameter by ASN.1 ID
     *
     * @param id    the ASN.1 ID
     * @return      the parameter or null if not known
     */
    public static Parameter getById(int id) {
        for(Parameter e : values()) {
            if(e.id==id) {
                return e;
            }
        }
        return null;
    }

    /***
     * Get a paraameter by ASN.1 name
     *
     * @param s     the ASN.1 name
     * @return      the parameter or null if not known
     */
    public static Parameter getByString(String s) {
        for(Parameter e : values()) {
            if(e.toString().equals(s)) {
                return e;
            }
        }
        return null;
    }

    /***
     * Get the ASN.1 ID
     *
     * @return  the ID
     */
    public int getId() {return id;}

    /***
     * Whether the parameter is encodable or not.
     *
     * @return returns true if the parameter is ASN.1 encodable
     */
    public boolean isEncodable() {
        return transcoder!=null;
    }

    /***
     * Obtain a string representation of the ASN.1 object
     *
     * @param o the ASN.1 encoded parameter content
     * @return  the string representation of the parameter
     */
    public String fromASN1Object(ASN1Object o) {
        return transcoder.fromASN1(o);
    }

    /***
     * Encode the string representation into the ASN.1 equivalent
     *
     * @param s the string representation of the parameter content
     * @return  the ASN.1 representation of the parameter
     */
    public ASN1Encodable toASN1Object(String s) {
        return transcoder.toASN1(s);
    }

    public String toString() {
        return txt;
    }
}
