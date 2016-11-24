package net.gwerder.java.mailvortex.asn1.encryption;

/**
 * Enumeration to list available encryption modes.
 *
 * @FIXME add sensible modes
 *
 * Created by martin.gwerder on 31.05.2016.
 */
public enum Mode {
    ECB       (10000,"ECB"),
    //CBC       (10001,"CBC"),
    //EAX       (10002,"EAX"),
    //GCM       (10003,"GCM"),
    OCB       (10004,"OCB"),
    NONE      (10010,"NONE");

    private static Mode def=Mode.ECB;

    int id=-1;
    String txt=null;

    Mode(int id,String txt) {
        this.id=id;
        this.txt=txt;
    }

    public static Mode getById(int id) {
        for(Mode e : values()) {
            if(e.id==id) return e;
        }
        return null;
    }

    public static Mode getDefault() {
        return def;
    }

    public static Mode setDefault(Mode ndef) {
        Mode old=def;
        def=ndef;
        return old;
    }

    public int getId() {
        return id;
    }

    public String getMode() {
        return txt;
    }
}

