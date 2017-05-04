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

/**
 * Enumeration of all supported Parameters.
 *
 * Created by martin.gwerder on 31.05.2016.
 */
public enum Parameter {
    KEYSIZE   (10000,"keySize"),
    CURVETYPE (10001,"curveType"),
    IV        (10002,"initialisationVector"),
    NONCE     (10003,"nonce"),
    MODE      (10004,"mode"),
    PADDING   (10005,"padding");

    int id=-1;
    String txt=null;

    Parameter(int id,String txt) {
        this.id=id;
        this.txt=txt;
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

    public String toString() {
        return txt;
    }
}
