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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Represents all supported crypto algorithms.
 *
 */
public enum Algorithm implements Serializable {

    AES128     ( 1000, AlgorithmType.SYMMETRIC, "aes128"     , "BC", SecurityLevel.LOW ),
    AES192     ( 1001, AlgorithmType.SYMMETRIC, "aes192"     , "BC", SecurityLevel.MEDIUM ),
    AES256     ( 1002, AlgorithmType.SYMMETRIC, "aes256"     , "BC", SecurityLevel.QUANTUM ),
    CAMELLIA128( 1100, AlgorithmType.SYMMETRIC, "CAMELLIA128", "BC", SecurityLevel.LOW ),
    CAMELLIA192( 1101, AlgorithmType.SYMMETRIC, "CAMELLIA192", "BC", SecurityLevel.MEDIUM ),
    CAMELLIA256( 1102, AlgorithmType.SYMMETRIC, "CAMELLIA256", "BC", SecurityLevel.QUANTUM ),
    TWOFISH128 ( 1200, AlgorithmType.SYMMETRIC, "TWOFISH128" , "BC", SecurityLevel.LOW ),
    TWOFISH192 ( 1201, AlgorithmType.SYMMETRIC, "TWOFISH192" , "BC", SecurityLevel.MEDIUM ),
    TWOFISH256 ( 1202, AlgorithmType.SYMMETRIC, "TWOFISH256" , "BC", SecurityLevel.QUANTUM ),

    RSA        (2000, AlgorithmType.ASYMMETRIC, "RSA", "BC", getSecLevelList( getSecLevelList( getSecLevelList( getSecLevelList(
              SecurityLevel.LOW,     getParameterList(new String[] { Parameter.ALGORITHM+"=RSA",Parameter.KEYSIZE+"=1024",Parameter.BLOCKSIZE+"=1024",Parameter.MODE+"="+Mode.getDefault( AlgorithmType.ASYMMETRIC ),Parameter.PADDING+"="+Padding.getDefault( AlgorithmType.ASYMMETRIC ) } ) ),
              SecurityLevel.MEDIUM,  getParameterList(new String[] { Parameter.ALGORITHM+"=RSA",Parameter.KEYSIZE+"=2048",Parameter.BLOCKSIZE+"=2048",Parameter.MODE+"="+Mode.getDefault( AlgorithmType.ASYMMETRIC ),Parameter.PADDING+"="+Padding.getDefault( AlgorithmType.ASYMMETRIC ) } ) ),
              SecurityLevel.HIGH,    getParameterList(new String[] { Parameter.ALGORITHM+"=RSA",Parameter.KEYSIZE+"=4096",Parameter.BLOCKSIZE+"=4096",Parameter.MODE+"="+Mode.getDefault( AlgorithmType.ASYMMETRIC ),Parameter.PADDING+"="+Padding.getDefault( AlgorithmType.ASYMMETRIC ) } ) ),
              SecurityLevel.QUANTUM, getParameterList(new String[] { Parameter.ALGORITHM+"=RSA",Parameter.KEYSIZE+"=8192",Parameter.BLOCKSIZE+"=8192",Parameter.MODE+"="+Mode.getDefault( AlgorithmType.ASYMMETRIC ),Parameter.PADDING+"="+Padding.getDefault( AlgorithmType.ASYMMETRIC ) } ) )

    ),
    EC         ( 2100, AlgorithmType.ASYMMETRIC, "ECIES"    , "BC", getSecLevelList( getSecLevelList( getSecLevelList(
            ECCurveType.SECP384R1.getSecurityLevel(), getParameterList(new String[] { Parameter.ALGORITHM+"=ECIES",Parameter.KEYSIZE+"=384",Parameter.BLOCKSIZE+"=384",Parameter.CURVETYPE+"="+ECCurveType.SECP384R1,Parameter.MODE+"="+Mode.getDefault( AlgorithmType.ASYMMETRIC ),Parameter.PADDING+"="+Padding.getDefault( AlgorithmType.ASYMMETRIC) } ) ),
            ECCurveType.SECT409K1.getSecurityLevel(), getParameterList(new String[] { Parameter.ALGORITHM+"=ECIES",Parameter.KEYSIZE+"=409",Parameter.BLOCKSIZE+"=409",Parameter.CURVETYPE+"="+ECCurveType.SECT409K1,Parameter.MODE+"="+Mode.getDefault( AlgorithmType.ASYMMETRIC ),Parameter.PADDING+"="+Padding.getDefault( AlgorithmType.ASYMMETRIC) } ) ),
            ECCurveType.SECP521R1.getSecurityLevel(), getParameterList(new String[] { Parameter.ALGORITHM+"=ECIES",Parameter.KEYSIZE+"=521",Parameter.BLOCKSIZE+"=521",Parameter.CURVETYPE+"="+ECCurveType.SECP521R1,Parameter.MODE+"="+Mode.getDefault( AlgorithmType.ASYMMETRIC ),Parameter.PADDING+"="+Padding.getDefault( AlgorithmType.ASYMMETRIC) } ) )
    ),
    SHA384     ( 3000, AlgorithmType.HASHING, "sha384"   , "BC", SecurityLevel.HIGH ),
    SHA512     ( 3001, AlgorithmType.HASHING, "sha512"   , "BC", SecurityLevel.QUANTUM ),
    RIPEMD160  ( 3100, AlgorithmType.HASHING, "ripemd160", "BC", SecurityLevel.LOW ),
    //RIPEMD256  ( 3101, AlgorithmType.HASHING, "ripemd256", "BC", SecurityLevel.MEDIUM ),
    //RIPEMD320  ( 3102, AlgorithmType.HASHING, "ripemd320", "BC", SecurityLevel.HIGH ),
    ;

    public static final long serialVersionUID = 100000000039L;

    private static Map<AlgorithmType, Algorithm> def = new ConcurrentHashMap<AlgorithmType, Algorithm>();
    private java.util.logging.Logger LOGGER;

    private final int id;
    private final AlgorithmType t;
    private final String txt;
    private final String provider;
    private final Map<SecurityLevel,AlgorithmParameter> secLevel;

    Algorithm(int id, AlgorithmType t, String txt, String provider, SecurityLevel level) {
        LOGGER=MessageVortexLogger.getLogger( (new Throwable()).getStackTrace()[0].getClassName() );
        this.secLevel = new ConcurrentHashMap<>();
        synchronized(secLevel) {
            this.id = id;
            this.t = t;
            this.txt = txt;
            this.provider = provider;
            int blockSize=getKeySize();
            if( txt.toLowerCase().startsWith("aes") || txt.toLowerCase().startsWith("camellia") || txt.toLowerCase().startsWith("twofish") ) {
                // CAMELLIA, TWOFISH and AES do always have 128 bit block size
                blockSize=128;
            }
            secLevel.putAll(getSecLevelList( level, getParameterList( new String[] {
                    Parameter.ALGORITHM+"="+id,
                    Parameter.KEYSIZE+"="+getKeySize(),
                    Parameter.BLOCKSIZE+"="+blockSize
            } ) ) );
            if(t==AlgorithmType.SYMMETRIC) {
                secLevel.get(level).put(Parameter.PADDING.getId(),Padding.getDefault(t).toString());
                secLevel.get(level).put(Parameter.MODE.getId(),Mode.getDefault(t).toString());
            }
        }
    }

    Algorithm(int id, AlgorithmType t, String txt, String provider, Map<SecurityLevel, AlgorithmParameter> parameters) {
        LOGGER=MessageVortexLogger.getLogger( (new Throwable()).getStackTrace()[0].getClassName() );
        this.id = id;
        this.t = t;
        this.txt = txt;
        this.provider = provider;
        this.secLevel = parameters;
    }

    private static AlgorithmParameter getParameterList(String[] txt) {
        final Logger LOGGER = MessageVortexLogger.getLogger( (new Throwable()).getStackTrace()[0].getClassName() );
        AlgorithmParameter ret = new AlgorithmParameter();
        for( String s : txt ) {
            String[] kv=s.split( "=" );
            if( kv.length == 2 ) {
                ret.put( kv[0], kv[1] );
            } else {
                LOGGER.log( Level.WARNING, "split of \"" + s + "\" failed" );
            }
        }
        return ret;
    }

    private static  Map<SecurityLevel,AlgorithmParameter> getSecLevelList(SecurityLevel level ,AlgorithmParameter o) {
        Map<SecurityLevel,AlgorithmParameter> ret=new ConcurrentHashMap<>();
        return getSecLevelList( ret, level, o );
    }

    private static  Map<SecurityLevel,AlgorithmParameter> getSecLevelList(Map<SecurityLevel,AlgorithmParameter> lst, SecurityLevel level ,AlgorithmParameter o) {
        Map<SecurityLevel,AlgorithmParameter> ret = new HashMap<>();
        ret.putAll( lst );
        ret.put( level, o );
        return ret;
    }

    /***
     * Return a list of algorithms of the specified type.
     *
     * @param at   the algorithm type to generate a list for
     * @return     the list of algorithms
     */
    public static Algorithm[] getAlgorithms(AlgorithmType at) {
        Algorithm[] a= values();
        List<Algorithm> v = new ArrayList<>( a.length );
        for(Algorithm e : a ) {
            if( e.t == at ) {
                v.add( e );
            }
        }
        return v.toArray( new Algorithm[ v.size() ] );
    }

    /***
     * Look up an algorithm by id.
     *
     * @param id     the idto be looked up
     * @return       the algorithm or null if not known
     */
    public static Algorithm getById(int id) {
        for(Algorithm e : values() ) {
            if(e.id==id) {
                return e;
            }
        }
        return null;
    }

    /***
     * Look up an algorithm by its identification string.
     *
     * @param s  the identification string to be looked up
     * @return   the algorithm or null if not found
     */
    public static Algorithm getByString(String s) {
        for(Algorithm e : values()) {
            if(e.toString().equals(s)) {
                return e;
            }
        }
        return null;
    }

    /***
     * Look up the default algorithm to be used on the system.
     *
     * @param at   the type of algorithm to be looked for
     * @return     the default type
     */
    public static Algorithm getDefault( AlgorithmType at ) {
        // init map if not yet done
        if( def.isEmpty() ) {
            def.put( AlgorithmType.ASYMMETRIC, RSA );
            def.put( AlgorithmType.SYMMETRIC,  AES256 );
            def.put( AlgorithmType.HASHING,    SHA384 );
        };
        return def.get( at );
    }

    /***
     * Look up the current id.
     *
     * @return the current id
     */
    public int getId() {
        return id;
    }

    /***
     * The algorithm family.
     *
     * This is either the identification string without the key size or identical to the identification string
     *
     * @return  the identification string without key size (if any)
     */
    public String getAlgorithmFamily() {
        return txt.replaceAll("[0-9]*$","");
    }

    /***
     * Get the identification string.
     *
     * @return the identification string
     */
    public String toString() {
        return txt;
    }

    /***
     * Get the type of algorithm.
     *
     * @return the type of algorithm
     */
    public AlgorithmType getAlgorithmType() {
        return t;
    }

    /***
     * Get the provider string for this algorithm.
     *
     * @return the provider string for this algorithm
     */
    public String getProvider() {
        return provider;
    }


    /***
     * Get the default key size for this algorithm.
     *
     * @return the default key size in bits
     */
    public int getKeySize() {
        return getKeySize( SecurityLevel.getDefault() );
    }

    /***
     * Get the default key size for this algorithm.
     *
     * @return the default key size in bits
     */
    public int getBlockSize() {
        return getBlockSize( SecurityLevel.getDefault() );
    }

    /***
     * Get the key size for this algorithm and security level.
     *
     * @param sl   the security level
     * @return     the key size in bits for the security level specified
     */
    public int getKeySize(SecurityLevel sl) {
        final String[] KEYS = new String[] { "aes", "sha", "camellia", "twofish", "ripemd" };
        for( String i: KEYS ) {
            if( txt.toLowerCase().startsWith( i ) ) {
                return Integer.parseInt( txt.substring( i.length(), i.length()+3 ) );
            }
        }

        synchronized( secLevel ) {
            // get requested parameters
            AlgorithmParameter params = getParameters( sl );

            // get kesize from parameters
            if( params == null || ( Integer.parseInt( params.get( Parameter.KEYSIZE.getId() ) ) < 10 ) ) {
                LOGGER.log( Level.SEVERE, "Error fetching keysize for " + txt + "/" + sl + " (" + secLevel.get( sl ) + ")" );
                throw new IllegalArgumentException( "Error fetching key size for " + txt + "/" + sl + " (" + secLevel.get( sl ) + ")" );
            }
            if( params.get( Parameter.ALGORITHM ).toLowerCase().startsWith( "ecies" ) ) {
                // Extract key size from EC courve name
                return Integer.parseInt( params.get( Parameter.CURVETYPE ).substring( 4, 7 ) );
            } else {
                return Integer.parseInt( params.get( Parameter.KEYSIZE ) );
            }
        }
    }

    /***
     * Get the block size for this algorithm and security level.
     *
     * @param sl   the security level
     * @return     the key size in bits for the security level specified
     */
    public int getBlockSize( SecurityLevel sl ) {
        synchronized( secLevel ) {
            // get requested parameters
            AlgorithmParameter params = getParameters( sl );

            String bsparam = params.get( Parameter.BLOCKSIZE );
            if ( bsparam != null ) {
                // get kesize from parameters
                return Integer.parseInt( bsparam );
            } else {
                return getKeySize();
            }
        }
    }

    /***
     * Get default parameters for the security level specified.
     *
     * @param sl  the security level
     * @return    the default set of parameters for the security level specified
     */
    public AlgorithmParameter getParameters(SecurityLevel sl) {
        synchronized( secLevel ) {
            AlgorithmParameter params = null;

            // get next higher security level if not available
            while ( params == null ) {
                params = secLevel.get( sl );

                // if required repeat with next higher SecurityLevel
                if( params==null ) {
                    sl = sl.next();
                }
            }
            return new AlgorithmParameter( params );
        }
    }

    /***
     * Get a map of security levels and default parameters for this algorithm.
     *
     * @return The map containing the default parameters
     */
    public Map<SecurityLevel,AlgorithmParameter> getParameters() {
        synchronized( secLevel ) {
            Map<SecurityLevel,AlgorithmParameter> ret = new HashMap<>( secLevel.size() );
            for( Map.Entry<SecurityLevel,AlgorithmParameter> e : secLevel.entrySet() ) {
                ret.put( e.getKey(), new AlgorithmParameter( e.getValue() ) );
            }
            return ret;
        }
    }

}
