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
 * Represents all supported EC named curves.
 *
 * Created by martin.gwerder on 31.05.2016.
 */
public enum ECCurveType {

    SECP384R1( 2500, "secp384r1", Algorithm.EC, SecurityLevel.MEDIUM ),
    SECT409K1( 2501, "sect409k1", Algorithm.EC, SecurityLevel.HIGH ),
    SECP521R1( 2502, "secp521r1", Algorithm.EC, SecurityLevel.QUANTUM );

    static ECCurveType def=SECP521R1;

    private int id;
    private ECCurveType t;
    private String txt;
    private Algorithm alg;
    private SecurityLevel secLevel;

    ECCurveType(int id, String txt, Algorithm alg, SecurityLevel level) {
        this.id=id;
        this.txt=txt;
        this.alg=alg;
        this.secLevel=level;
    }

    public static ECCurveType getById(int id) {
        for(ECCurveType e : values()) {
            if(e.id==id) {
                return e;
            }
        }
        return null;
    }

    public static ECCurveType getByString(String s) {
        for(ECCurveType e : values()) {
            if(e.toString().equals(s)) {
                return e;
            }
        }
        return null;
    }

    public int getId() {
        return id;
    }

    public String toString() {
        return txt;
    }

    public SecurityLevel getSecurityLevel() {
        return secLevel;
    }

    public Algorithm getAlgorithm() {
        return alg;
    }

    public int getKeySize() { return Integer.parseInt( txt.substring( 4,7 ) ); }

    public static ECCurveType getDefault() {
        return def;
    }
}

