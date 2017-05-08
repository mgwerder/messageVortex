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
import net.gwerder.java.messagevortex.asn1.AlgorithmParameter;
import org.bouncycastle.jcajce.util.AlgorithmParametersUtils;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;

/**
 * Represents all supported crypto algorithms.
 *
 * FIXME: Add tiger192 support for final version
 * FIXME: concurrency problem at startup for static map
 */
public enum Algorithm implements Serializable {

    AES128     ( 1000, AlgorithmType.SYMMETRIC, "aes128", "BC", SecurityLevel.LOW ),
    AES192     ( 1001, AlgorithmType.SYMMETRIC, "aes192", "BC", SecurityLevel.MEDIUM ),
    AES256     ( 1002, AlgorithmType.SYMMETRIC, "aes256", "BC", SecurityLevel.QUANTUM ),
    CAMELLIA128( 1100, AlgorithmType.SYMMETRIC, "CAMELLIA128", "BC", SecurityLevel.LOW ),
    CAMELLIA192( 1101, AlgorithmType.SYMMETRIC, "CAMELLIA192", "BC", SecurityLevel.MEDIUM ),
    CAMELLIA256( 1102, AlgorithmType.SYMMETRIC, "CAMELLIA256", "BC", SecurityLevel.QUANTUM ),

    RSA        (2000, AlgorithmType.ASYMMETRIC, "RSA", "BC", getSecLevelList( getSecLevelList( getSecLevelList( getSecLevelList(
              SecurityLevel.LOW,     getParameterList(new String[] { Parameter.ALGORITHM+"=RSA",Parameter.KEYSIZE.toString()+"=1024",Parameter.MODE.toString()+"="+Mode.getDefault(AlgorithmType.ASYMMETRIC).toString(),Parameter.PADDING.toString()+"="+Padding.getDefault(AlgorithmType.ASYMMETRIC).toString()})),
              SecurityLevel.MEDIUM,  getParameterList(new String[] { Parameter.ALGORITHM+"=RSA",Parameter.KEYSIZE.toString()+"=2048",Parameter.MODE.toString()+"="+Mode.getDefault(AlgorithmType.ASYMMETRIC).toString(),Parameter.PADDING.toString()+"="+Padding.getDefault(AlgorithmType.ASYMMETRIC).toString()})),
              SecurityLevel.HIGH,    getParameterList(new String[] { Parameter.ALGORITHM+"=RSA",Parameter.KEYSIZE.toString()+"=4096",Parameter.MODE.toString()+"="+Mode.getDefault(AlgorithmType.ASYMMETRIC).toString(),Parameter.PADDING.toString()+"="+Padding.getDefault(AlgorithmType.ASYMMETRIC).toString()})),
              SecurityLevel.QUANTUM, getParameterList(new String[] { Parameter.ALGORITHM+"=RSA",Parameter.KEYSIZE.toString()+"=8192",Parameter.MODE.toString()+"="+Mode.getDefault(AlgorithmType.ASYMMETRIC).toString(),Parameter.PADDING.toString()+"="+Padding.getDefault(AlgorithmType.ASYMMETRIC).toString()}))

    ),
    EC         ( 2100, AlgorithmType.ASYMMETRIC, "ECIES"    , "BC", getSecLevelList( getSecLevelList( getSecLevelList(
            ECCurveType.SECP384R1.getSecurityLevel(), getParameterList(new String[] { Parameter.ALGORITHM+"=ECIES",Parameter.KEYSIZE.toString()+"=384",Parameter.CURVETYPE.toString()+"="+ECCurveType.SECP384R1.toString(),Parameter.MODE.toString()+"="+Mode.getDefault(AlgorithmType.ASYMMETRIC).toString(),Parameter.PADDING.toString()+"="+Padding.getDefault(AlgorithmType.ASYMMETRIC).toString()})),
            ECCurveType.SECT409K1.getSecurityLevel(), getParameterList(new String[] { Parameter.ALGORITHM+"=ECIES",Parameter.KEYSIZE.toString()+"=409",Parameter.CURVETYPE.toString()+"="+ECCurveType.SECT409K1.toString(),Parameter.MODE.toString()+"="+Mode.getDefault(AlgorithmType.ASYMMETRIC).toString(),Parameter.PADDING.toString()+"=",Padding.getDefault(AlgorithmType.ASYMMETRIC).toString()})),
            ECCurveType.SECP521R1.getSecurityLevel(), getParameterList(new String[] { Parameter.ALGORITHM+"=ECIES",Parameter.KEYSIZE.toString()+"=521",Parameter.CURVETYPE.toString()+"="+ECCurveType.SECP521R1.toString(),Parameter.MODE.toString()+"="+Mode.getDefault(AlgorithmType.ASYMMETRIC).toString(),Parameter.PADDING.toString()+"=",Padding.getDefault(AlgorithmType.ASYMMETRIC).toString()}))
    ),
    SHA384     ( 3000, AlgorithmType.HASHING, "sha384", "BC",  SecurityLevel.HIGH ),
    SHA512     ( 3001, AlgorithmType.HASHING, "sha512", "BC", SecurityLevel.QUANTUM );

    private static final java.util.logging.Logger LOGGER;
    private static Map<AlgorithmType, Algorithm> def = new ConcurrentHashMap<AlgorithmType, Algorithm>() {
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

    private final int id;
    private final AlgorithmType t;
    private final String txt;
    private final String provider;
    private final Map<SecurityLevel,AlgorithmParameter> secLevel;

    Algorithm(int id, AlgorithmType t, String txt, String provider, SecurityLevel level) {
        this.secLevel = new ConcurrentHashMap<>();
        synchronized(secLevel) {
            this.id = id;
            this.t = t;
            this.txt = txt;
            this.provider = provider;
            secLevel.putAll(getSecLevelList( level, getParameterList( new String[] { Parameter.ALGORITHM+"="+id,Parameter.KEYSIZE+"="+getKeySize()} ) ) );
            if(t==AlgorithmType.SYMMETRIC) {
                secLevel.get(level).put(Parameter.PADDING.getId(),Padding.getDefault(t).toString());
                secLevel.get(level).put(Parameter.MODE.getId(),Mode.getDefault(t).toString());
            }
        }
    }

    Algorithm(int id, AlgorithmType t, String txt, String provider, Map<SecurityLevel, AlgorithmParameter> parameters) {
        this.id = id;
        this.t = t;
        this.txt = txt;
        this.provider = provider;
        this.secLevel = parameters;
    }

    private static AlgorithmParameter getParameterList(String[] txt) {
        AlgorithmParameter ret=new AlgorithmParameter();
        for(String s:txt) {
            String[] kv=s.split("=");
            if(kv.length==2) {
                ret.put(kv[0], kv[1]);
            }
        }
        return ret;
    }

    private static  Map<SecurityLevel,AlgorithmParameter> getSecLevelList(SecurityLevel level ,AlgorithmParameter o) {
        Map<SecurityLevel,AlgorithmParameter> ret=new ConcurrentHashMap<>();
        return getSecLevelList(ret, level, o);
    }

    private static  Map<SecurityLevel,AlgorithmParameter> getSecLevelList(Map<SecurityLevel,AlgorithmParameter> lst, SecurityLevel level ,AlgorithmParameter o) {
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

    public static Algorithm getByString(String s) {
        for(Algorithm e : values()) {
            if(e.toString().equals(s)) {
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

    public String toString() {
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
        if (txt.toLowerCase().startsWith( "aes" ) || txt.startsWith( "sha" ) ) {
            // Extract key size from AES and SHA name
            return Integer.parseInt( txt.substring( 3, 6 ) );
        } else if (txt.toLowerCase().startsWith( "camellia" )  ) {
            return Integer.parseInt( txt.substring( 8, 11 ) );
        }

        synchronized(secLevel) {
            // get requested parameters
            AlgorithmParameter params = getParameters(sl);

            // get kesize from parameters
            if (params == null || (Integer.parseInt(params.get(Parameter.KEYSIZE.getId())) < 10)) {
                LOGGER.log(Level.SEVERE, "Error fetching keysize for " + txt + "/" + sl.toString() + " (" + secLevel.get(sl) + ")");
                throw new IllegalArgumentException("Error fetching key size for " + txt + "/" + sl.toString() + " (" + secLevel.get(sl) + ")");
            }
            if (params.get(Parameter.ALGORITHM).toLowerCase().startsWith("ecies")) {
                // Extract key size from EC courve name
                return Integer.parseInt(params.get(Parameter.CURVETYPE).substring(4, 7));
            } else {
                return Integer.parseInt(params.get(Parameter.KEYSIZE));
            }
        }
    }

    public AlgorithmParameter getParameters(SecurityLevel sl) {
        synchronized(secLevel) {
            AlgorithmParameter params = null;

            // get next higher security level if not available
            while (params == null) {
                params = secLevel.get(sl);

                // if required repeat with next higher SecurityLevel
                if(params==null) {
                    sl = sl.next();
                }
            }
            return params.clone();
        }
    }

    public Map<SecurityLevel,AlgorithmParameter> getParameters() {
        synchronized(secLevel) {
            Map<SecurityLevel,AlgorithmParameter> ret=new HashMap<>();
            for(Map.Entry<SecurityLevel,AlgorithmParameter> e:secLevel.entrySet()) {
                ret.put(e.getKey(),e.getValue().clone());
            }
            return ret;
        }
    }

}

