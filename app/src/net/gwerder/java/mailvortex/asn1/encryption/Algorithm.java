package net.gwerder.java.mailvortex.asn1.encryption;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;

/**
 * Represents all supported crypto algorithms.
 *
 * Created by martin.gwerder on 31.05.2016.
 */
public enum Algorithm {

    AES128( 1000, AlgorithmType.SYMMETRIC, "aes128", "BC", SecurityLevel.LOW ),
    AES192( 1001, AlgorithmType.SYMMETRIC, "aes192", "BC" ),
    AES256( 1002, AlgorithmType.SYMMETRIC, "aes256", "BC" ),
    RSA(2000, AlgorithmType.ASYMMETRIC, "RSA", "BC", new HashMap<SecurityLevel, Integer>() {
        private static final long serialVersionUID = 12132345345L;

        {
            put(SecurityLevel.LOW, 1024);
            put(SecurityLevel.MEDIUM, 2048);
            put(SecurityLevel.HIGH, 4096);
            put(SecurityLevel.QUANTUM, 8192);
        }
    }), //available as well under "SunJCE"
    //EC        (2100, AlgorithmType.ASYMMETRIC,"EC"   ,"SunEC",null),
    SECP384R1( 2500, AlgorithmType.ASYMMETRIC, "secp384r1", "BC" ),
    SECT409K1( 2501, AlgorithmType.ASYMMETRIC, "sect409k1", "BC" ),
    SECP521R1( 2502, AlgorithmType.ASYMMETRIC, "secp521r1", "BC" ),
    SHA384( 3000, AlgorithmType.HASHING, "sha384", "BC" ),
    SHA512( 3001, AlgorithmType.HASHING, "sha512", "BC" );
    //TIGER192  (3100, AlgorithmType.HASHING,"tiger192","BC");

    private static Map<AlgorithmType, Algorithm> def = new HashMap<AlgorithmType, Algorithm>() {
        private static final long serialVersionUID = 12132324789789L;

        {
            put(AlgorithmType.ASYMMETRIC, RSA);
            put(AlgorithmType.SYMMETRIC, AES256);
            put(AlgorithmType.HASHING, SHA512);
        }
    };

    private int id;
    private AlgorithmType t;
    private String txt;
    private String provider;
    private Map<SecurityLevel,Integer> secLevel;

    Algorithm(int id, AlgorithmType t, String txt, String provider) {
        this( id, t, txt, provider, (Map<SecurityLevel, Integer>) null );
    }

    Algorithm(int id, AlgorithmType t, String txt, String provider, SecurityLevel level) {
        this( id, t, txt, provider, (Map<SecurityLevel, Integer>) null );
        secLevel = new HashMap<>();
        secLevel.put( level, getKeySize() );
    }

    Algorithm(int id, AlgorithmType t, String txt, String provider, Map<SecurityLevel, Integer> level) {
        this.id=id;
        this.t=t;
        this.txt=txt;
        this.provider=provider;
        this.secLevel=level;
    }

    public static Algorithm[] getAlgorithms(AlgorithmType at) {
        List<Algorithm> v=new Vector<>();
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

    public static Algorithm getDefault(AlgorithmType at) {
        return def.get( at );
    }

    public int getId() {
        return id;
    }

    public String getAlgorithmFamily() {
        return txt.replaceAll("[0-9]*$","");
    }

    public String getAlgorithm() {
        return txt;
    }

    public AlgorithmType getAlgorithmType() {
        return t;
    }

    public String getProvider() {
        return provider;
    }

    public int getKeySize() {
        return getKeySize(SecurityLevel.getDefault());
    }

    public int getKeySize(SecurityLevel sl) {
        if (txt.startsWith( "sec" )) {
            return Integer.parseInt( txt.substring( 4, 7 ) );
        } else if (txt.startsWith( "aes" )) {
            return Integer.parseInt( txt.substring( 3, 6 ) );
        }
        return secLevel.get(sl);
    }

    @Override
    public String toString() {
        return super.toString();
    }
}

