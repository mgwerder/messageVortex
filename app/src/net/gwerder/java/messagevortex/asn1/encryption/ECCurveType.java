package net.gwerder.java.messagevortex.asn1.encryption;

/**
 * Represents all supported EC named curves.
 *
 * Created by martin.gwerder on 31.05.2016.
 */
public enum ECCurveType {

    SECP384R1( 2500, "secp384r1", Algorithm.EC, SecurityLevel.MEDIUM ),
    SECT409K1( 2501, "sect409k1", Algorithm.EC, SecurityLevel.HIGH ),
    SECP521R1( 2502, "secp521r1", Algorithm.EC, SecurityLevel.QUANTUM );
    //TIGER192  (3100, AlgorithmType.HASHING,"tiger","BC",SecurityLevel.LOW);

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
            if(e.id==id) return e;
        }
        return null;
    }

    public int getId() {
        return id;
    }

    public String getECCurveType() {
        return txt;
    }
    public SecurityLevel getSecurityLevel() {
        return secLevel;
    }
    public int getKeySize() { return Integer.parseInt( txt.substring( 4,7 ) ); }

    @Override
    public String toString() {
        return super.toString();
    }

    public static ECCurveType getDefault() {
        return def;
    }
}

