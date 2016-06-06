package net.gwerder.java.mailvortex.asn1.encryption;

import java.util.HashMap;
import java.util.Map;
import java.util.Vector;

/**
 * Enumeration listing all available padding types for encryption.
 *
 * Created by martin.gwerder on 31.05.2016.
 */
public enum Padding {
    PKCS1            ( 1000, "PKCS1Padding"                 , new SizeCalc(){public int maxSize(int s) {return s/8-11;}}            ),
    OAEP_SHA384_MGF1 ( 1100, "OAEPWithSHA384AndMGF1Padding" , new SizeCalc(){public int maxSize(int s) {return s/8-2-384/4;}}       ),
    PKCS5            ( 2000, "PKCS5Padding"                 , new SizeCalc(){public int maxSize(int keySize) {return keySize/8-1;}} );

    private static final long serialVersionUID = 1000000000L;
    private static Map<AlgorithmType,Padding> def=new HashMap<AlgorithmType,Padding>(  ) {
        {
            put( AlgorithmType.ASYMMETRIC, Padding.PKCS1 );
            put( AlgorithmType.SYMMETRIC, Padding.PKCS5 );
        }};
    private int id;
    private String txt;
    private SizeCalc s;
    Padding(int id,String txt,SizeCalc s) {
        this.id=id;
        this.txt=txt;
        this.s=s;
    }

    public static Padding[] getAlgorithms() {
        Vector<Padding> v = new Vector<>();
        for(Padding e : values()) {
            v.add(e);
        }
        return v.toArray(new Padding[v.size()]);
    }

    public static Padding getById(int id) {
        for(Padding e : values()) {
            if(e.id==id) return e;
        }
        return null;
    }

    public static Padding getByString(String s) {
        for(Padding e : values()) {
            if(e.toString().equals(s.toLowerCase())) return e;
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
