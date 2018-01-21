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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Enumeration to list available encryption modes.
 */
public enum Mode {
    ECB       (10000,"ECB" ,false,new String[] { "ECIES","RSA","CAMELLIA128","CAMELLIA192","CAMELLIA256", "Twofish128", "Twofish192","Twofish256" }, new Padding[] { Padding.PKCS7 }),
    CBC       (10001,"CBC" ,true ,new String[] { "aes128", "aes192","aes256","CAMELLIA128", "CAMELLIA192","CAMELLIA256", "Twofish128", "Twofish192","Twofish256" }, new Padding[] { Padding.PKCS7 }),
    EAX       (10002,"EAX" ,true ,new String[] { "CAMELLIA128", "CAMELLIA192","CAMELLIA256", "Twofish128", "Twofish192","Twofish256"},new Padding[] { Padding.PKCS7 }),
    CTR       (10003,"CTR" ,true ,new String[] { "aes128", "aes192","aes256","CAMELLIA128", "CAMELLIA192","CAMELLIA256", "Twofish128", "Twofish192","Twofish256" },new Padding[] { Padding.PKCS7 }),
    CCM       (10004,"CCM" ,true ,new String[] { "aes128", "aes192","aes256","CAMELLIA128", "CAMELLIA192","CAMELLIA256", "Twofish128", "Twofish192","Twofish256" },new Padding[] { Padding.PKCS7 }),
    GCM       (10005,"GCM" ,true ,new String[] { "aes128", "aes192","AES256","CAMELLIA128", "CAMELLIA192","CAMELLIA256", "Twofish128", "Twofish192","Twofish256" },new Padding[] { Padding.PKCS7 }),
    OCB       (10006,"OCB" ,true ,new String[] { "aes128", "aes192","AES256","CAMELLIA128", "CAMELLIA192","CAMELLIA256", "Twofish128", "Twofish192","Twofish256" },new Padding[] { Padding.PKCS7 }),
    OFB       (10007,"OFB" ,true ,new String[] { "CAMELLIA128", "CAMELLIA192","CAMELLIA256", "Twofish128", "Twofish192","Twofish256"},new Padding[] { Padding.PKCS7 }),
    NONE      (10100,"NONE",false,new String[] { "ECIES","RSA" }, new Padding[] { Padding.PKCS7 });

    private static Map<AlgorithmType,Mode> def=new HashMap<>();

    static {
        def.put(AlgorithmType.ASYMMETRIC,Mode.ECB);
        def.put(AlgorithmType.SYMMETRIC,Mode.CBC);
    }

    final int id;
    final String txt;
    final boolean requiresIV;
    final String[] alg;
    final Padding[] pad;
    final ASN1Enumerated asn;

    Mode(int id,String txt, boolean iv,String[] alg,Padding[] pad) {
        this.id=id;
        this.txt=txt;
        this.requiresIV=iv;
        this.alg=alg;
        this.pad=pad;
        this.asn=new ASN1Enumerated(id);
    }

    public boolean getRequiresIV() {
        return this.requiresIV;
    }

    public static Mode getById(int id) {
        for(Mode e : values()) {
            if(e.id==id) {
                return e;
            }
        }
        return null;
    }

    public static Mode getByString(String name) {
        for(Mode e : values()) {
            if(e.txt.equals(name)) {
                return e;
            }
        }
        return null;
    }

    /***
     * Gets the currently set default value for the given type
     * @param type the type for which the default value is required
     * @return     the default value requested
     */
    public static Mode getDefault(AlgorithmType type) {
        return def.get(type);
    }

    /***
     * Sets the default encryption mode for a specific algorithm type.
     *
     * @param t     the type for which the default value should be set
     * @param ndef  the new default value
     * @return      the previously set default value
     */
    public static Mode setDefault(AlgorithmType t,Mode ndef) {
        Mode old=def.get(t);
        def.put(t,ndef);
        return old;
    }

    public int getId() {
        return id;
    }

    /***
     * Gets the mode identifier as required by the encryption provider.
     *
     * This value is returned regardless of the support of the provider classes.
     *
     * @return the mode identifier
     */
    public String toString() {
        return txt;
    }

    /***
     * Gets all known paddings regardless of their support.
     *
     * @return an array of all paddings
     */
    public Padding[] getPaddings() {
        return pad;
    }

    public static Mode[] getModes(Algorithm alg) {
        ArrayList<Mode> l=new ArrayList<>();
        for(Mode m:values()) {
            for(String a:m.alg) {
                if(alg==Algorithm.getByString(a)) {
                    l.add(m);
                }
            }
        }
        return l.toArray(new Mode[0]);
    }

    /**
     * Gets the corresponding ASN1 enumeration.
     *
     * @return the corresponding ASN1 enumeration
     */
    public ASN1Enumerated toASN1() {
        return asn;
    }

}

