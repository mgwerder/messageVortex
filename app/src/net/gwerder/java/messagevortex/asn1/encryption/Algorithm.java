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

import net.gwerder.java.messagevortex.MessageVortexLogger;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

/**
 * Represents all supported crypto algorithms.
 *
 * FIXME: Add tiger192 support for final version
 */
public enum Algorithm {

    AES128     ( 1000, AlgorithmType.SYMMETRIC, "aes128", "BC", SecurityLevel.LOW ),
    AES192     ( 1001, AlgorithmType.SYMMETRIC, "aes192", "BC", SecurityLevel.MEDIUM ),
    AES256     ( 1002, AlgorithmType.SYMMETRIC, "aes256", "BC", SecurityLevel.QUANTUM ),
    CAMELLIA128( 1100, AlgorithmType.SYMMETRIC, "CAMELLIA128", "BC", SecurityLevel.LOW ),
    CAMELLIA192( 1101, AlgorithmType.SYMMETRIC, "CAMELLIA192", "BC", SecurityLevel.MEDIUM ),
    CAMELLIA256( 1102, AlgorithmType.SYMMETRIC, "CAMELLIA256", "BC", SecurityLevel.QUANTUM ),

    RSA        (2000, AlgorithmType.ASYMMETRIC, "RSA", "BC", getSecLevelList( getSecLevelList( getSecLevelList( getSecLevelList(
              SecurityLevel.LOW,     getParameterList(getParameterList(getParameterList(Algorithm.KEYSIZE,1024),Parameter.MODE.toString()+"_0",Mode.getDefault(AlgorithmType.ASYMMETRIC).toString()),Parameter.PADDING.toString()+"_0",Padding.getDefault(AlgorithmType.ASYMMETRIC).getPadding())),
              SecurityLevel.MEDIUM,  getParameterList(getParameterList(getParameterList(Algorithm.KEYSIZE,2048),Parameter.MODE.toString()+"_0",Mode.getDefault(AlgorithmType.ASYMMETRIC).toString()),Parameter.PADDING.toString()+"_0",Padding.getDefault(AlgorithmType.ASYMMETRIC).getPadding())),
              SecurityLevel.HIGH,    getParameterList(getParameterList(getParameterList(Algorithm.KEYSIZE,4096),Parameter.MODE.toString()+"_0",Mode.getDefault(AlgorithmType.ASYMMETRIC).toString()),Parameter.PADDING.toString()+"_0",Padding.getDefault(AlgorithmType.ASYMMETRIC).getPadding())),
              SecurityLevel.QUANTUM, getParameterList(getParameterList(getParameterList(Algorithm.KEYSIZE,8192),Parameter.MODE.toString()+"_0",Mode.getDefault(AlgorithmType.ASYMMETRIC).toString()),Parameter.PADDING.toString()+"_0",Padding.getDefault(AlgorithmType.ASYMMETRIC).getPadding()))

    ),
    EC         ( 2100, AlgorithmType.ASYMMETRIC, "ECIES"    , "BC", getSecLevelList( getSecLevelList( getSecLevelList(
            ECCurveType.SECP384R1.getSecurityLevel(), getParameterList(getParameterList(getParameterList(getParameterList(Algorithm.KEYSIZE,ECCurveType.SECP384R1.getKeySize()),Algorithm.CURVETYPE,ECCurveType.SECP384R1.getECCurveType()),Parameter.MODE.toString()+"_0",Mode.getDefault(AlgorithmType.ASYMMETRIC).toString()),Parameter.PADDING.toString()+"_0",Padding.getDefault(AlgorithmType.ASYMMETRIC).getPadding())),
            ECCurveType.SECT409K1.getSecurityLevel(), getParameterList(getParameterList(getParameterList(getParameterList(Algorithm.KEYSIZE,ECCurveType.SECT409K1.getKeySize()),Algorithm.CURVETYPE,ECCurveType.SECT409K1.getECCurveType()),Parameter.MODE.toString()+"_0",Mode.getDefault(AlgorithmType.ASYMMETRIC).toString()),Parameter.PADDING.toString()+"_0",Padding.getDefault(AlgorithmType.ASYMMETRIC).getPadding())),
            ECCurveType.SECP521R1.getSecurityLevel(), getParameterList(getParameterList(getParameterList(getParameterList(Algorithm.KEYSIZE,ECCurveType.SECP521R1.getKeySize()),Algorithm.CURVETYPE,ECCurveType.SECP521R1.getECCurveType()),Parameter.MODE.toString()+"_0",Mode.getDefault(AlgorithmType.ASYMMETRIC).toString()),Parameter.PADDING.toString()+"_0",Padding.getDefault(AlgorithmType.ASYMMETRIC).getPadding()))
    ),
    SHA384     ( 3000, AlgorithmType.HASHING, "sha384", "BC",  SecurityLevel.HIGH ),
    SHA512     ( 3001, AlgorithmType.HASHING, "sha512", "BC", SecurityLevel.QUANTUM );

    private static final String KEYSIZE   = "keySize_0";
    private static final String CURVETYPE = "curveType_0";

    private static final java.util.logging.Logger LOGGER;
    private static Map<AlgorithmType, Algorithm> def = new HashMap<AlgorithmType, Algorithm>() {

        private static final long serialVersionUID = 12132324789789L;

        {
            put(AlgorithmType.ASYMMETRIC, RSA);
            put(AlgorithmType.SYMMETRIC, AES256);
            put(AlgorithmType.HASHING, SHA384);
        }
    };

    static {
        LOGGER = MessageVortexLogger.getLogger( (new Throwable()).getStackTrace()[0].getClassName() );
        MessageVortexLogger.setGlobalLogLevel( Level.ALL );
    }

    private int id;
    private AlgorithmType t;
    private String txt;
    private String provider;
    private Map<SecurityLevel,Map<String,Object>> secLevel;

    Algorithm(int id, AlgorithmType t, String txt, String provider, SecurityLevel level) {
        this( id, t, txt, provider, (Map<SecurityLevel, Map<String, Object>>) null );
        secLevel = getSecLevelList( level, getParameterList( "keySize_0", getKeySize() ) );
    }

    Algorithm(int id, AlgorithmType t, String txt, String provider, Map<SecurityLevel, Map<String, Object>> parameters) {
        this.id = id;
        this.t = t;
        this.txt = txt;
        this.provider = provider;
        this.secLevel = parameters;
    }

    private static HashMap<String,Object> getParameterList(String txt,Object o) {
        HashMap<String,Object> ret=new HashMap<>();
        return getParameterList( ret,txt,o );
    }

    private static HashMap<String,Object> getParameterList(HashMap<String,Object> ret, String txt,Object o) {
        ret.put(txt,o);
        return ret;
    }

    private static  Map<SecurityLevel,Map<String,Object>> getSecLevelList(SecurityLevel level ,Map<String,Object> o) {
        Map<SecurityLevel,Map<String,Object>> ret=new HashMap<>();
        return getSecLevelList(ret, level, o);
    }

    private static  Map<SecurityLevel,Map<String,Object>> getSecLevelList(Map<SecurityLevel,Map<String,Object>> lst, SecurityLevel level ,Map<String,Object> o) {
        lst.put(level,o);
        return lst;
    }

    public static Algorithm[] getAlgorithms(AlgorithmType at) {
        List<Algorithm> v=new ArrayList<>();
        for(Algorithm e : values()) {
            if(e.t==at) {
                v.add(e);
            }
        }
        return v.toArray(new Algorithm[v.size()]);
    }

    public static Algorithm getById(int id) {
        for(Algorithm e : values()) {
            if(e.id==id) {
                return e;
            }
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
            // Extract key size from EC courve name
            return Integer.parseInt( txt.substring( 4, 7 ) );
        } else if (txt.toLowerCase().startsWith( "aes" ) || txt.startsWith( "sha" ) ) {
            // Extract key size from AES and SHA name
            return Integer.parseInt( txt.substring( 3, 6 ) );
        } else if (txt.toLowerCase().startsWith( "camellia" )  ) {
            return Integer.parseInt( txt.substring( 8, 11 ) );
        }
        if(secLevel==null || secLevel.get(sl)==null || (! (secLevel.get(sl).get(Parameter.KEYSIZE.toString()+"_0") instanceof Integer))) {
            LOGGER.log( Level.SEVERE, "Error fetching keysize for " + txt + "/" +sl.toString()+" ("+ secLevel.get(sl) + ")");
        }
        Map<String,Object> params=secLevel.get(sl);
        // get next higher security level if not available
        while(params==null) {
            sl=sl.next();
            params=secLevel.get(sl);
        }
        return (Integer)(params.get(Parameter.KEYSIZE.toString()+"_0"));
    }

    public Map<String,Object> getParameters(SecurityLevel sl) {
        Map<String,Object> params=secLevel.get(sl);
        // get next higher security level if not available
        while(params==null) {
            sl=sl.next();
            params=secLevel.get(sl);
        }
        return secLevel.get(sl);
    }

    public Map<SecurityLevel,Map<String,Object>> getParameters() {
        return secLevel;
    }

}

