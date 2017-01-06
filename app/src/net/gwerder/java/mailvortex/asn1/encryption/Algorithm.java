package net.gwerder.java.mailvortex.asn1.encryption;

import net.gwerder.java.mailvortex.MailvortexLogger;

import java.util.*;
import java.util.logging.Level;

/**
 * Represents all supported crypto algorithms.
 * FIXME: Add Camelia support for final version
 * FIXME: Add tiger192 support for final version
 * Created by martin.gwerder on 31.05.2016.
 */
public enum Algorithm {

    AES128     ( 1000, AlgorithmType.SYMMETRIC, "aes128", "BC", SecurityLevel.LOW ),
    AES192     ( 1001, AlgorithmType.SYMMETRIC, "aes192", "BC", SecurityLevel.MEDIUM ),
    AES256     ( 1002, AlgorithmType.SYMMETRIC, "aes256", "BC", SecurityLevel.QUANTUM ),
    CAMELLIA128( 1100, AlgorithmType.SYMMETRIC, "CAMELLIA128", "BC", SecurityLevel.LOW ),
    CAMELLIA192( 1101, AlgorithmType.SYMMETRIC, "CAMELLIA192", "BC", SecurityLevel.MEDIUM ),
    CAMELLIA256( 1102, AlgorithmType.SYMMETRIC, "CAMELLIA256", "BC", SecurityLevel.QUANTUM ),

    RSA        (2000, AlgorithmType.ASYMMETRIC, "RSA", "BC", getSecLevelList( getSecLevelList( getSecLevelList( getSecLevelList(
              SecurityLevel.LOW,     getParameterList("keySize_0",1024)),
              SecurityLevel.MEDIUM,  getParameterList("keySize_0",2048)),
              SecurityLevel.HIGH,    getParameterList("keySize_0",4096)),
              SecurityLevel.QUANTUM, getParameterList("keySize_0",8192))

    ),
    EC         ( 2100, AlgorithmType.ASYMMETRIC, "ECIES"    , "BC", getSecLevelList( getSecLevelList( getSecLevelList(
            ECCurveType.SECP384R1.getSecurityLevel(), getParameterList(getParameterList("keySize_0",ECCurveType.SECP384R1.getKeySize()),"curveType_0",ECCurveType.SECP384R1.getECCurveType())),
            ECCurveType.SECT409K1.getSecurityLevel(), getParameterList(getParameterList("keySize_0",ECCurveType.SECT409K1.getKeySize()),"curveType_0",ECCurveType.SECT409K1.getECCurveType())),
            ECCurveType.SECP521R1.getSecurityLevel(), getParameterList(getParameterList("keySize_0",ECCurveType.SECP521R1.getKeySize()),"curveType_0",ECCurveType.SECP521R1.getECCurveType()))
    ),
    SHA384     ( 3000, AlgorithmType.HASHING, "sha384", "BC",  SecurityLevel.HIGH ),
    SHA512     ( 3001, AlgorithmType.HASHING, "sha512", "BC", SecurityLevel.QUANTUM );
    //TIGER192  (3100, AlgorithmType.HASHING,"tiger","BC",SecurityLevel.LOW);

    private static final java.util.logging.Logger LOGGER;
    static {
        LOGGER = MailvortexLogger.getLogger((new Throwable()).getStackTrace()[0].getClassName());
        MailvortexLogger.setGlobalLogLevel(Level.ALL);
    }

    private static Map<AlgorithmType, Algorithm> def = new HashMap<AlgorithmType, Algorithm>() {
        private static final long serialVersionUID = 12132324789789L;

        {
            put(AlgorithmType.ASYMMETRIC, RSA);
            put(AlgorithmType.SYMMETRIC, AES256);
            put(AlgorithmType.HASHING, SHA384);
        }
    };

    private int id;
    private AlgorithmType t;
    private String txt;
    private String provider;
    private Map<SecurityLevel,Map<String,Object>> secLevel;

    private static HashMap<String,Object> getParameterList(String txt,Object o) {
        HashMap<String,Object> ret=new HashMap<String,Object>();
        return getParameterList( ret,txt,o );
    }

    private static HashMap<String,Object> getParameterList(HashMap<String,Object> ret, String txt,Object o) {
        ret.put(txt,o);
        return ret;
    }

    private static  Map<SecurityLevel,Map<String,Object>> getSecLevelList(SecurityLevel level ,Map<String,Object> o) {
        Map<SecurityLevel,Map<String,Object>> ret=new HashMap<SecurityLevel,Map<String,Object>>();
        return getSecLevelList(ret, level, o);
    }

    private static  Map<SecurityLevel,Map<String,Object>> getSecLevelList(Map<SecurityLevel,Map<String,Object>> lst, SecurityLevel level ,Map<String,Object> o) {
        lst.put(level,o);
        return lst;
    }

    Algorithm(int id, AlgorithmType t, String txt, String provider, SecurityLevel level) {
        this(id,t,txt,provider,(Map<SecurityLevel,Map<String,Object>>) null);
        secLevel = getSecLevelList( level, getParameterList("keySize_0",getKeySize()) );
    }

    Algorithm(int id, AlgorithmType t, String txt, String provider, Map<SecurityLevel,Map<String, Object>> parameters) {
        this.id=id;
        this.t=t;
        this.txt=txt;
        this.provider=provider;
        this.secLevel=parameters;
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
        if (txt.toLowerCase().startsWith( "sec" )) {
            return Integer.parseInt( txt.substring( 4, 7 ) );
        } else if (txt.toLowerCase().startsWith( "aes" ) || txt.startsWith( "sha" ) ) {
            return Integer.parseInt( txt.substring( 3, 6 ) );
        } else if (txt.toLowerCase().startsWith( "camellia" )  ) {
            return Integer.parseInt( txt.substring( 8, 11 ) );
        } else if (txt.toLowerCase().startsWith( "tiger" )) {
            return 192;
        }
        if(secLevel==null || secLevel.get(sl)==null || (! (secLevel.get(sl).get(Parameter.KEYSIZE.toString()+"_0") instanceof Integer))) {
            LOGGER.log( Level.SEVERE, "Error fetching keysize for " + txt + "/" +sl.toString()+" ("+ secLevel.get(sl) + ")");
        }
        return ((Integer)(secLevel.get(sl).get(Parameter.KEYSIZE.toString()+"_0"))).intValue();
    }

    public Map<String,Object> getParameters(SecurityLevel sl) {
        return secLevel.get(sl);
    }

    public Map<SecurityLevel,Map<String,Object>> getParameters() {
        return secLevel;
    }

    @Override
    public String toString() {
        return super.toString();
    }
}

