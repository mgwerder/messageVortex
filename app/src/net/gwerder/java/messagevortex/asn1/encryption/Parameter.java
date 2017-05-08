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

import java.io.IOException;
import java.io.Serializable;

/**
 * Enumeration of all supported Parameters.
 *
 * Created by martin.gwerder on 31.05.2016.
 */
public enum Parameter implements Serializable {
    ALGORITHM (1,    "algorithm",null),
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
    IV        (10002,"initialisationVector",new Transcoder() {
        public String fromASN1(ASN1Object o) {
            return new String(ASN1OctetString.getInstance(o).getOctets());
        }
        public ASN1Encodable toASN1(String s) {
            return new DEROctetString(s.getBytes());
        }
    }),
    NONCE     (10003,"nonce",new Transcoder() {
        public String fromASN1(ASN1Object o) {
            return new String(ASN1OctetString.getInstance(o).getOctets());
        }
        public ASN1Encodable toASN1(String s) {
            return new DEROctetString(s.getBytes());
        }
    }),
    MODE      (10004,"mode",new Transcoder() {
        public String fromASN1(ASN1Object o) {
            return new String(ASN1OctetString.getInstance(o).getOctets());
        }
        public ASN1Encodable toASN1(String s) {
            return new DEROctetString(s.getBytes());
        }
    }),
    PADDING   (10005,"padding",new Transcoder() {
        public String fromASN1(ASN1Object o) {
            return new String(ASN1OctetString.getInstance(o).getOctets());
        }
        public ASN1Encodable toASN1(String s) {
            return new DEROctetString(s.getBytes());
        }
    });

    private static abstract class Transcoder {
        public abstract String fromASN1(ASN1Object o);
        public abstract ASN1Encodable toASN1(String s);
    }

    final int id;
    final String txt;
    final Transcoder transcoder;

    Parameter(int id,String txt,Transcoder transcoder) {
        this.id=id;
        this.txt=txt;
        this.transcoder=transcoder;
    }

    public static Parameter getById(int id) {
        for(Parameter e : values()) {
            if(e.id==id) {
                return e;
            }
        }
        return null;
    }

    public static Parameter getByString(String s) {
        for(Parameter e : values()) {
            if(e.toString().equals(s)) {
                return e;
            }
        }
        return null;
    }

    public int getId() {return id;}

    public boolean isEncodable() {
        return transcoder!=null;
    }

    public String fromASN1Object(ASN1Object o) {
        return transcoder.fromASN1(o);
    }

    public ASN1Encodable toASN1Object(String s) throws IOException {
        return transcoder.toASN1(s);
    }

    public String toString() {
        return txt;
    }
}
