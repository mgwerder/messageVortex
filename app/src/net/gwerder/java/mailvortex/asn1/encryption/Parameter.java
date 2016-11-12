package net.gwerder.java.mailvortex.asn1.encryption;

/**
 * Enumeration of all supported Parameters.
 *
 * Created by martin.gwerder on 31.05.2016.
 */
public enum Parameter {
    KEYSIZE   (10000,"keySize"),
    CURVETYPE (10001,"curveType");

    int id=-1;
    String txt=null;

    Parameter(int id,String txt) {
        this.id=id;
        this.txt=txt;
    }

    public static Parameter getById(int id) {
        for(Parameter e : values()) {
            if(e.id==id) return e;
        }
        return null;
    }

    public static Parameter getByString(String s) {
        for(Parameter e : values()) {
            if(e.toString().equals(s)) return e;
        }
        return null;
    }

    public int getId() {return id;}

    public String toString() {
        return txt;
    }
}