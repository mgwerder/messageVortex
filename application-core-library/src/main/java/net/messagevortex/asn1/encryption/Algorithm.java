package net.messagevortex.asn1.encryption;

import net.messagevortex.MessageVortexLogger;
import net.messagevortex.asn1.AlgorithmParameter;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/**
 * Represents all supported crypto algorithms.
 */
public enum Algorithm implements Serializable {
  /* AES fixed (block sized) enumerations */
  AES128(1000, AlgorithmType.SYMMETRIC, "aes128", "BC", SecurityLevel.LOW),
  AES192(1001, AlgorithmType.SYMMETRIC, "aes192", "BC", SecurityLevel.MEDIUM),
  AES256(1002, AlgorithmType.SYMMETRIC, "aes256", "BC", SecurityLevel.QUANTUM),
  /* CAMELLIA fixed (block sized) enumerations */
  CAMELLIA128(1100, AlgorithmType.SYMMETRIC, "CAMELLIA128", "BC", SecurityLevel.LOW),
  CAMELLIA192(1101, AlgorithmType.SYMMETRIC, "CAMELLIA192", "BC", SecurityLevel.MEDIUM),
  CAMELLIA256(1102, AlgorithmType.SYMMETRIC, "CAMELLIA256", "BC", SecurityLevel.QUANTUM),
  EC(2600, AlgorithmType.ASYMMETRIC, "ECIES", "BC",
      getSecLevelList(getSecLevelList(getSecLevelList(
          EllipticCurveType.SECP384R1.getSecurityLevel(), getParameterList(
              new String[] {
                  Parameter.ALGORITHM + "=ECIES",
                  Parameter.KEYSIZE + "=384",
                  Parameter.BLOCKSIZE + "=384",
                  Parameter.CURVETYPE + "=" + EllipticCurveType.SECP384R1,
                  Parameter.MODE + "=" + Mode.getDefault(AlgorithmType.ASYMMETRIC),
                  Parameter.PADDING + "=" + Padding.getDefault(AlgorithmType.ASYMMETRIC)
              })),
          EllipticCurveType.SECT409K1.getSecurityLevel(), getParameterList(
              new String[] {
                  Parameter.ALGORITHM + "=ECIES",
                  Parameter.KEYSIZE + "=409",
                  Parameter.BLOCKSIZE + "=409",
                  Parameter.CURVETYPE + "=" + EllipticCurveType.SECT409K1,
                  Parameter.MODE + "=" + Mode.getDefault(AlgorithmType.ASYMMETRIC),
                  Parameter.PADDING + "=" + Padding.getDefault(AlgorithmType.ASYMMETRIC)
              })),
          EllipticCurveType.SECP521R1.getSecurityLevel(), getParameterList(
              new String[] {
                  Parameter.ALGORITHM + "=ECIES",
                  Parameter.KEYSIZE + "=521",
                  Parameter.BLOCKSIZE + "=521",
                  Parameter.CURVETYPE + "=" + EllipticCurveType.SECP521R1,
                  Parameter.MODE + "=" + Mode.getDefault(AlgorithmType.ASYMMETRIC),
                  Parameter.PADDING + "=" + Padding.getDefault(AlgorithmType.ASYMMETRIC)
              })
      )
  )/* Elliptic curve (name represented) enumerations */,
  RIPEMD160(3100, AlgorithmType.HASHING, "ripemd160", "BC", SecurityLevel.LOW),
  //RIPEMD256  ( 3101, AlgorithmType.HASHING, "ripemd256", "BC", SecurityLevel.MEDIUM ),
  //RIPEMD320  ( 3102, AlgorithmType.HASHING, "ripemd320", "BC", SecurityLevel.HIGH ),
  /* RSA (variable sized) enumerations */
  RSA(2000, AlgorithmType.ASYMMETRIC, "RSA", "BC",
      getSecLevelList(getSecLevelList(getSecLevelList(getSecLevelList(
          SecurityLevel.LOW, getParameterList(
              new String[] {
                  Parameter.ALGORITHM + "=RSA",
                  Parameter.KEYSIZE + "=1024",
                  Parameter.BLOCKSIZE + "=1024",
                  Parameter.MODE + "=" + Mode.getDefault(AlgorithmType.ASYMMETRIC),
                  Parameter.PADDING + "=" + Padding.getDefault(AlgorithmType.ASYMMETRIC)
              })),
          SecurityLevel.MEDIUM, getParameterList(
              new String[] {
                  Parameter.ALGORITHM + "=RSA",
                  Parameter.KEYSIZE + "=2048",
                  Parameter.BLOCKSIZE + "=2048",
                  Parameter.MODE + "=" + Mode.getDefault(AlgorithmType.ASYMMETRIC),
                  Parameter.PADDING + "=" + Padding.getDefault(AlgorithmType.ASYMMETRIC)
              })),
          SecurityLevel.HIGH, getParameterList(
              new String[] {Parameter.ALGORITHM + "=RSA",
                  Parameter.KEYSIZE + "=4096",
                  Parameter.BLOCKSIZE + "=4096",
                  Parameter.MODE + "=" + Mode.getDefault(AlgorithmType.ASYMMETRIC),
                  Parameter.PADDING + "=" + Padding.getDefault(AlgorithmType.ASYMMETRIC)
              })),
          SecurityLevel.QUANTUM, getParameterList(
              new String[] {
                  Parameter.ALGORITHM + "=RSA",
                  Parameter.KEYSIZE + "=8192",
                  Parameter.BLOCKSIZE + "=8192",
                  Parameter.MODE + "=" + Mode.getDefault(AlgorithmType.ASYMMETRIC),
                  Parameter.PADDING + "=" + Padding.getDefault(AlgorithmType.ASYMMETRIC)
              }))

  ),

  /* NTRU */
  /*NTRU      ( 2500, AlgorithmType.ASYMMETRIC, "NTRU", "BC", getSecLevelList(
          SecurityLevel.QUANTUM, getParameterList(
                    new String[] {
                            Parameter.ALGORITHM+"=NTRU",
                            Parameter.KEYSIZE+"=256",
                            Parameter.BLOCKSIZE+"=8192",
                            Parameter.MODE+"="+Mode.getDefault( AlgorithmType.ASYMMETRIC ),
                            Parameter.PADDING+"="+Padding.getDefault( AlgorithmType.ASYMMETRIC )
                    }))
  ),*/

  /* Hash algorithm enumerations */
  SHA256(3000, AlgorithmType.HASHING, "sha256", "BC", SecurityLevel.MEDIUM),
  SHA384(3001, AlgorithmType.HASHING, "sha384", "BC", SecurityLevel.HIGH),
  SHA512(3002, AlgorithmType.HASHING, "sha512", "BC", SecurityLevel.QUANTUM),

  /* TWOFISH fixed (block sized) enumerations */
  TWOFISH128(1200, AlgorithmType.SYMMETRIC, "TWOFISH128", "BC", SecurityLevel.LOW),
  TWOFISH192(1201, AlgorithmType.SYMMETRIC, "TWOFISH192", "BC", SecurityLevel.MEDIUM),
  TWOFISH256(1202, AlgorithmType.SYMMETRIC, "TWOFISH256", "BC", SecurityLevel.QUANTUM);

  public static final long serialVersionUID = 100000000039L;

  /* create a class specific logger */
  private static final Logger LOGGER;
  static {
    LOGGER = MessageVortexLogger.getLogger((new Throwable()).getStackTrace()[0].getClassName());
  }

  private final Set<String> fixed_sizes = Arrays.stream(new String[] {"aes", "sha", "camellia", "twofish", "ripemd"}).collect(Collectors.toSet());

  /* contains the ASN.1 based ID for the algorithm */
  private final int id;

  /* contains the class of the algorithm */
  private final AlgorithmType algType;

  /* contains a textual representation */
  private final String txt;

  /* contains a textual representation of the security provider to be used */
  private final String provider;

  /* contains a representative parameter set for the security levels */
  private final Map<SecurityLevel, AlgorithmParameter> secLevel;

  /***
   * <p>constructor for the internal loading of parameters for the Algorithm enum.</p>
   *
   * <p>This contructor is required for block sized Algorithms without parameter sets.</p>
   *
   * @param id        the ASN.1 based numerical ID
   * @param algType         the class the algorithm belongs to
   * @param txt       a textual representation
   * @param provider  the name of the cryptographic provider to be used
   * @param level     the security level of this Algorithm type
   */
  Algorithm(int id, AlgorithmType algType, String txt, String provider, SecurityLevel level) {
    if (txt == null) {
      throw new NullPointerException(" textual representation may not be null");
    }
    this.secLevel = new ConcurrentHashMap<>();
    synchronized (secLevel) {
      this.id = id;
      this.algType = algType;
      this.txt = txt;
      this.provider = provider;
      int blockSize = getKeySize();
      if (txt.toLowerCase().startsWith("aes") || txt.toLowerCase().startsWith("camellia")
          || txt.toLowerCase().startsWith("twofish")) {
        // CAMELLIA, TWOFISH and AES do always have 128 bit block size
        blockSize = 128;
      }
      secLevel.putAll(getSecLevelList(level, getParameterList(new String[] {
          Parameter.ALGORITHM + "=" + id,
          Parameter.KEYSIZE + "=" + getKeySize(),
          Parameter.BLOCKSIZE + "=" + blockSize
      })));
      if (algType == AlgorithmType.SYMMETRIC) {
        secLevel.get(level).put(Parameter.PADDING.getId(),
            Padding.getDefault(algType).toString());
        secLevel.get(level).put(Parameter.MODE.getId(),
            Mode.getDefault(algType).toString());
      }
    }
  }

  /***
   * <p>constructor for the internal loading of parameters for the Algorithm enum.</p>
   *
   * @param id            the ASN.1 based numerical ID
   * @param algType             the class the algorithm belongs to
   * @param txt           a textual representation
   * @param provider      the name of the cryptographic provider to be used
   * @param parameters    set of parameter sets including the respective security level
   */
  Algorithm(int id, AlgorithmType algType, String txt, String provider,
            Map<SecurityLevel, AlgorithmParameter> parameters) {
    this.id = id;
    this.algType = algType;
    this.txt = txt;
    this.provider = provider;

    // make a deep copy of the hashmap to avoid later modification of the content
    this.secLevel = new HashMap<>();
    for (Map.Entry<SecurityLevel, AlgorithmParameter> e : parameters.entrySet()) {
      try {
        this.secLevel.put(e.getKey(),
            new AlgorithmParameter(e.getValue().toAsn1Object(DumpType.INTERNAL)));
      } catch (IOException ex) {
        throw new IllegalAccessError("unable to clone parameter map");
      }
    }
  }

  private static AlgorithmParameter getParameterList(String[] txt) {
    AlgorithmParameter ret = new AlgorithmParameter();
    for (String s : txt) {
      String[] kv = s.split("=");
      if (kv.length == 2) {
        ret.put(kv[0], kv[1]);
      } else {
        LOGGER.log(Level.WARNING, "split of \"" + s + "\" failed");
      }
    }
    return ret;
  }

  private static Map<SecurityLevel, AlgorithmParameter> getSecLevelList(
      SecurityLevel level, AlgorithmParameter o) {
    Map<SecurityLevel, AlgorithmParameter> ret = new ConcurrentHashMap<>();
    return getSecLevelList(ret, level, o);
  }

  private static Map<SecurityLevel, AlgorithmParameter> getSecLevelList(
      Map<SecurityLevel, AlgorithmParameter> lst, SecurityLevel level,
      AlgorithmParameter o) {

    Map<SecurityLevel, AlgorithmParameter> ret = new EnumMap<>(SecurityLevel.class);
    ret.putAll(lst);
    ret.put(level, o);
    return ret;
  }

  /***
   * <p>Return a list of algorithms of the specified type.</p>
   *
   * @param at   the algorithm type to generate a list for
   * @return the list of algorithms
   */
  public static Algorithm[] getAlgorithms(AlgorithmType at) {
    Algorithm[] a = values();
    List<Algorithm> v = new ArrayList<>(a.length);
    for (Algorithm e : a) {
      if (e.algType == at) {
        v.add(e);
      }
    }
    return v.toArray(new Algorithm[v.size()]);
  }

  /***
   * <p>Look up an algorithm by id.</p>
   *
   * @param id     the idto be looked up
   * @return the algorithm or null if not known
   */
  public static Algorithm getById(int id) {
    for (Algorithm e : values()) {
      if (e.id == id) {
        return e;
      }
    }
    return null;
  }

  /***
   * <p>Look up an algorithm by its identification string.</p>
   *
   * <p>The string search is case insensitive.</p>
   *
   * @param s  the identification string to be looked up
   * @return the algorithm or null if not found
   */
  public static Algorithm getByString(String s) {
    for (Algorithm e : values()) {
      if (e.toString().equalsIgnoreCase(s)) {
        return e;
      }
    }
    return null;
  }

  /***
   * <p>Look up the default algorithm to be used on the system.</p>
   *
   * @param at   the type of algorithm to be looked for
   * @return the default type
   */
  public static Algorithm getDefault(AlgorithmType at) {
    // init map if not yet done
    switch (at) {
      case ASYMMETRIC:
        return RSA;
      case SYMMETRIC:
        return AES256;
      case HASHING:
        return SHA384;
      default:
        return null;
    }
  }

  /***
   * <p>Look up the current id.</p>
   *
   * @return the current id
   */
  public int getId() {
    return id;
  }

  /***
   * <p>The algorithm family.</p>
   *
   * <p>This is either the identification string without the key size or identical to the
   * identification string</p>
   *
   * @return the identification string without key size (if any)
   */
  public String getAlgorithmFamily() {
    return txt.replaceAll("[0-9]*$", "");
  }

  /***
   * <p>Get the identification string.</p>
   *
   * @return the identification string
   */
  public String toString() {
    return txt;
  }

  /***
   * <p>Get the type of algorithm.</p>
   *
   * @return the type of algorithm
   */
  public AlgorithmType getAlgorithmType() {
    return algType;
  }

  /***
   * <p>Get the provider string for this algorithm.</p>
   *
   * @return the provider string for this algorithm
   */
  public String getProvider() {
    return provider;
  }


  /***
   * <p>Get the default key size for this algorithm.</p>
   *
   * @return the default key size in bits
   */
  public int getKeySize() {
    return getKeySize(SecurityLevel.getDefault());
  }

  /***
   * <p>Get the key size for this algorithm and security level.</p>
   *
   * @param sl   the security level
   * @return the key size in bits for the security level specified
   */
  public int getKeySize(SecurityLevel sl) {

    for (String i : fixed_sizes) {
      if (txt.toLowerCase().startsWith(i)) {
        return Integer.parseInt(txt.substring(i.length(), i.length() + 3));
      }
    }

    synchronized (secLevel) {
      // get requested parameters
      AlgorithmParameter params = getParameters(sl);

      // get kesize from parameters
      if (params == null || (Integer.parseInt(params.get(Parameter.KEYSIZE.getId())) < 10)) {
        LOGGER.log(Level.SEVERE, "Error fetching keysize for " + txt + "/" + sl
            + " (" + secLevel.get(sl) + ")");
        throw new IllegalArgumentException("Error fetching key size for " + txt + "/" + sl
            + " (" + secLevel.get(sl) + ")");
      }
      if (params.get(Parameter.ALGORITHM).toUpperCase().startsWith("ECIES")) {
        // Extract key size from EC courve name
        return Integer.parseInt(params.get(Parameter.CURVETYPE).substring(4, 7));
      } else {
        return Integer.parseInt(params.get(Parameter.KEYSIZE));
      }
    }
  }

  /***
   * <p>Get the default key size for this algorithm.</p>
   *
   * @return the default key size in bits
   */
  public int getBlockSize() {
    return getBlockSize(SecurityLevel.getDefault());
  }

  /***
   * <p>Get the block size for this algorithm and security level.</p>
   *
   * @param sl   the security level
   * @return the key size in bits for the security level specified or -1 if not set
   */
  public int getBlockSize(SecurityLevel sl) {
    synchronized (secLevel) {
      // get requested parameters
      AlgorithmParameter params = getParameters(sl);
      if (params == null) {
        return -1;
      }

      String bsparam = params.get(Parameter.BLOCKSIZE);
      if (bsparam != null) {
        // get kesize from parameters
        return Integer.parseInt(bsparam);
      } else {
        return getKeySize();
      }
    }
  }

  /***
   * <p>Get default parameters for the security level specified.</p>
   *
   * @param sl  the security level
   * @return the default set of parameters for the security level specified
   */
  public AlgorithmParameter getParameters(SecurityLevel sl) {
    synchronized (secLevel) {
      AlgorithmParameter params = null;

      // get next higher security level if not available
      while (params == null) {
        params = secLevel.get(sl);

        // if required repeat with next higher SecurityLevel
        if (params == null) {
          sl = sl.next();
        }
      }
      try {
        return new AlgorithmParameter(params.toAsn1Object(DumpType.ALL));
      } catch (IOException exception) {
        return null;
      }
    }
  }

  /***
   * <p>Get a map of security levels and default parameters for this algorithm.</p>
   *
   * @return The map containing the default parameters
   */
  public Map<SecurityLevel, AlgorithmParameter> getParameters() {
    synchronized (secLevel) {
      Map<SecurityLevel, AlgorithmParameter> ret = new HashMap<>(secLevel.size());
      for (Map.Entry<SecurityLevel, AlgorithmParameter> e : secLevel.entrySet()) {
        ret.put(e.getKey(), new AlgorithmParameter(e.getValue()));
      }
      return ret;
    }
  }

}
