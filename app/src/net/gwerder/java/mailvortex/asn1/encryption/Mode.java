package net.gwerder.java.mailvortex.asn1.encryption;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Enumeration to list available encryption modes.
 *
 * @FIXME add sensible modes
 *
 * Created by martin.gwerder on 31.05.2016.
 */
public enum Mode {
    ECB       (10000,"ECB" ,false,new Algorithm[] { Algorithm.RSA,Algorithm.CAMELLIA128, Algorithm.CAMELLIA192,Algorithm.CAMELLIA256 }, new Padding[] { Padding.NONE,Padding.PKCS7 }),
    CBC       (10001,"CBC" ,true ,new Algorithm[] { Algorithm.AES128, Algorithm.AES192,Algorithm.AES256,Algorithm.CAMELLIA128, Algorithm.CAMELLIA192,Algorithm.CAMELLIA256 }, new Padding[] { Padding.NONE,Padding.PKCS7 }),
    //EAX       (10002,"EAX" ,true ,new Algorithm[] { Algorithm.CAMELLIA128, Algorithm.CAMELLIA192,Algorithm.CAMELLIA256}),
    //CTR       (10002,"EAX" ,true ,new Algorithm[] { Algorithm.AES128, Algorithm.AES192,Algorithm.AES256,Algorithm.CAMELLIA128, Algorithm.CAMELLIA192,Algorithm.CAMELLIA256 }),
    //CCM       (10002,"EAX" ,true ,new Algorithm[] { Algorithm.AES128, Algorithm.AES192,Algorithm.AES256,Algorithm.CAMELLIA128, Algorithm.CAMELLIA192,Algorithm.CAMELLIA256 }),
    //GCM       (10003,"GCM" ,true ,new Algorithm[] { Algorithm.AES128, Algorithm.AES192,Algorithm.AES256,Algorithm.CAMELLIA128, Algorithm.CAMELLIA192,Algorithm.CAMELLIA256 }),
    //OCB       (10004,"OCB" ,true ,new Algorithm[] { Algorithm.AES128, Algorithm.AES192,Algorithm.AES256,Algorithm.CAMELLIA128, Algorithm.CAMELLIA192,Algorithm.CAMELLIA256 }),
    //OFB       (10005,"OFB" ,true ,new Algorithm[] {Algorithm.CAMELLIA128, Algorithm.CAMELLIA192,Algorithm.CAMELLIA256}),
    NONE      (10010,"NONE",false,new Algorithm[] { Algorithm.RSA }, new Padding[] { Padding.NONE,Padding.PKCS7 });

    private static Map<AlgorithmType,Mode> def=new HashMap<>();

    static {
        def.put(AlgorithmType.ASYMMETRIC,Mode.ECB);
        def.put(AlgorithmType.SYMMETRIC,Mode.CBC);
    }

    int id=-1;
    String txt=null;
    boolean requiresIV=false;
    Algorithm[] alg;
    Padding[] pad;

    Mode(int id,String txt, boolean iv,Algorithm[] alg,Padding[] pad) {
        this.id=id;
        this.txt=txt;
        this.requiresIV=iv;
        this.alg=alg;
        this.pad=pad;
    }

    public boolean getRequiresIV() {
        return this.requiresIV;
    }

    public static Mode getById(int id) {
        for(Mode e : values()) {
            if(e.id==id) return e;
        }
        return null;
    }

    public static Mode getByName(String name) {
        for(Mode e : values()) {
            if(e.txt.equals(name)) return e;
        }
        return null;
    }

    public static Mode getDefault(AlgorithmType t) {
        return def.get(t);
    }

    public static Mode setDefault(AlgorithmType t,Mode ndef) {
        Mode old=def.get(t);
        def.put(t,ndef);
        return old;
    }

    public int getId() {
        return id;
    }

    public String getMode() {
        return txt;
    }

    public Padding[] getPaddings() {
        return pad;
    }
}

