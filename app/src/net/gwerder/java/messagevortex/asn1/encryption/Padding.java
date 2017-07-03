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

import org.bouncycastle.asn1.ASN1Enumerated;

import java.util.*;

/**
 * Enumeration listing all available padding types for encryption.
 */
public enum Padding {

    /*NONE            ( 1000, "NoPadding", new AlgorithmType[]{AlgorithmType.ASYMMETRIC}, new SizeCalc() {
        public int maxSize(int s) {
            return s / 8;
        }
    } ),*/
    PKCS1           ( 1001, "PKCS1Padding", new AlgorithmType[]{AlgorithmType.ASYMMETRIC}, new SizeCalc() {
        public int maxSize(int s) {
            return (s / 8) - 11;
        }
    } ),
    OAEP_SHA256_MGF1( 1100, "OAEPWithSHA256AndMGF1Padding", new AlgorithmType[]{AlgorithmType.ASYMMETRIC}, new SizeCalc() {
        public int maxSize(int s) {
            return (s / 8) - 2 - (256 / 4);
        }
    } ),
    OAEP_SHA384_MGF1( 1101, "OAEPWithSHA384AndMGF1Padding", new AlgorithmType[]{AlgorithmType.ASYMMETRIC}, new SizeCalc() {
        public int maxSize(int s) {
            return s / 8 - 2 - 384 / 4;
        }
    } ),
    OAEP_SHA512_MGF1( 1102, "OAEPWithSHA512AndMGF1Padding", new AlgorithmType[]{AlgorithmType.ASYMMETRIC}, new SizeCalc() {
        public int maxSize(int s) {
            return s / 8 - 2 - 512 / 4;
        }
    } ),
    PKCS7           ( 1007, "PKCS7Padding", new AlgorithmType[]{AlgorithmType.SYMMETRIC},  new SizeCalc() {
        public int maxSize(int s) {
            return s / 8 - 1;
        }
    } );

    private static final Map<AlgorithmType,Padding> DEFAULT_PADDING =new HashMap<AlgorithmType,Padding>(  ) {
        {
            put( AlgorithmType.ASYMMETRIC, Padding.PKCS1 );
            put( AlgorithmType.SYMMETRIC,  Padding.PKCS7 );
        }
    };

    private int id;
    private String txt;
    private Set<AlgorithmType> at;
    private SizeCalc s;
    final ASN1Enumerated asn;

    Padding(int id, String txt, AlgorithmType[] at, SizeCalc s) {
        this.id=id;
        this.txt=txt;
        this.at = new HashSet<>();
        this.at.addAll( Arrays.asList(at) );
        this.s=s;
        this.asn=new ASN1Enumerated(id);
    }

    public static Padding[] getAlgorithms(AlgorithmType at) {
        List<Padding> v = new ArrayList<>();
        for (Padding val : values()) {
            if (val.at.contains( at )) {
                v.add( val );
            }
        }
        return v.toArray(new Padding[v.size()]);
    }

    public static Padding getById(int id) {
        for(Padding e : values()) {
            if(e.id==id) {
                return e;
            }
        }
        return null;
    }

    public static Padding getByString(String name) {
        for(Padding e : values()) {
            if(e.txt.equals(name)) {
                return e;
            }
        }
        return null;
    }

    public static Padding getDefault(AlgorithmType at) {
        Padding p=DEFAULT_PADDING.get(at);
        if(p==null) {
            throw new NullPointerException("no default padding for "+at);
        }
        return p;
    }

    public int getId() {
        return id;
    }

    public String toString() {
        return txt;
    }

    public int getMaxSize(int keysize) {
        return s.maxSize( keysize );
    }

    /***
     * returns the corresponding ASN1 enumeration
     * @return the ASN1 enumeration representing this padding
     */
    public ASN1Enumerated toASN1() {
        return asn;
    }


}
