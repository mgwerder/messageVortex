package net.gwerder.java.mailvortex.asn1.encryption;

import java.util.Vector;

/**
 * Created by martin.gwerder on 31.05.2016.
 */
public enum Algorithm {
    AES128    (1000, AlgorithmType.SYMMETRIC,"aes128"),
    AES192    (1001, AlgorithmType.SYMMETRIC,"aes192"),
    AES256    (1002, AlgorithmType.SYMMETRIC,"aes256"),
    RSA       (2000, AlgorithmType.ASYMMETRIC,"rsa"),
    SECP384R1 (2500, AlgorithmType.ASYMMETRIC,"secp384r1"),
    SECT409K1 (2501, AlgorithmType.ASYMMETRIC,"sect409k1"),
    SECP521R1 (2502, AlgorithmType.ASYMMETRIC,"secp521r1"),
    SHA384    (3000, AlgorithmType.HASHING,"sha384"),
    SHA512    (3001, AlgorithmType.HASHING,"sha512"),
    TIGER192  (3100, AlgorithmType.HASHING,"tiger192");


    private int id;
    private AlgorithmType t;
    private String txt;

    Algorithm(int id, AlgorithmType t, String txt) {
        this.id=id;
        this.t=t;
        this.txt=txt;
    }

    public int getId() {return id;}

    public static Algorithm[] getAlgorithms(AlgorithmType at) {
        Vector<Algorithm> v=new Vector<Algorithm>();
        for(Algorithm e : values()) {
            if(e.t==at) v.add(e);
        }
        return v.toArray(new Algorithm[v.size()]);
    }

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

    public String getAlgorithmFamily() {
        return txt.replaceAll("[0-9]*$","");
    }

    public String getAlgorithm() {
        return txt;
    }

    @Override
    public String toString() {
        return super.toString().toLowerCase();
    }
}

