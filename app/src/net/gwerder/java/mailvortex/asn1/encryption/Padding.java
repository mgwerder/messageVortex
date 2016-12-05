package net.gwerder.java.mailvortex.asn1.encryption;

import java.util.*;

/**
 * Enumeration listing all available padding types for encryption.
 *
 * Created by martin.gwerder on 31.05.2016.
 */
public enum Padding {
    PKCS1( 1000, "PKCS1Padding", new AlgorithmType[]{AlgorithmType.ASYMMETRIC}, new SizeCalc() {
        public int maxSize(int s) {
            return s / 8 - 11;
        }
    } ),
    OAEP_SHA256_MGF1( 1100, "OAEPWithSHA256AndMGF1Padding", new AlgorithmType[]{AlgorithmType.ASYMMETRIC}, new SizeCalc() {
        public int maxSize(int s) {
            return s / 8 - 2 - 256 / 4;
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
    PKCS7( 2000, "PKCS7Padding", new AlgorithmType[]{AlgorithmType.SYMMETRIC}, new SizeCalc() {
        public int maxSize(int keySize) {
            return keySize / 8 - 1;
        }
    } );

    private static Map<AlgorithmType,Padding> def=new HashMap<AlgorithmType,Padding>(  ) {
        private static final long serialVersionUID = 121321383445L;
        {
            put( AlgorithmType.ASYMMETRIC, Padding.PKCS1 );
            put( AlgorithmType.SYMMETRIC, Padding.PKCS7 );
        }};
    private int id;
    private String txt;
    private HashSet<AlgorithmType> at;
    private SizeCalc s;

    Padding(int id, String txt, AlgorithmType[] at, SizeCalc s) {
        this.id=id;
        this.txt=txt;
        this.at = new HashSet<>();
        this.at.addAll( Arrays.asList(at) );
        this.s=s;
    }

    public static Padding[] getAlgorithms(AlgorithmType at) {
        Vector<Padding> v = new Vector<>();
        for (Padding val : values()) {
            if (val.at.contains( at )) v.add( val );
        }
        return v.toArray(new Padding[v.size()]);
    }

    public static Padding getById(int id) {
        for(Padding e : values()) {
            if(e.id==id) return e;
        }
        return null;
    }

    public static Padding getByName(String name) {
        for(Padding e : values()) {
            if(e.txt.equals(name)) return e;
        }
        return null;
    }

    public static Padding getDefault(AlgorithmType at) {
        return def.get(at);
    }

    public static Padding setDefault(AlgorithmType at,Padding ndef) {
        Padding old=def.get(at);
        def.put(at,ndef);
        return old;
    }

    public int getId() {
        return id;
    }

    public String getPadding() {
        return txt;
    }

    public int getMaxSize(int keysize) {
        return s.maxSize( keysize );
    }

    @Override
    public String toString() {
        return super.toString().toLowerCase();
    }

    private static abstract class SizeCalc {
        public abstract int maxSize(int keySize);
    }

}
