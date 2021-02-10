package net.messagevortex.asn1.encryption;

import java.util.ArrayList;
import java.util.List;

/**
 * <p>Represents all supported EC named curves.</p>
 */
public enum EllipticCurveType {
  
  SECP384R1(2500, "secp384r1", 384, SecurityLevel.MEDIUM),
  SECT409K1(2501, "sect409k1", 409, SecurityLevel.HIGH),
  SECP521R1(2502, "secp521r1", 521, SecurityLevel.QUANTUM);
  
  private static final EllipticCurveType def = SECP521R1;
  
  private final int id;
  private final String txt;
  private final SecurityLevel secLevel;
  private final int keySize;
  
  EllipticCurveType(int id, String txt, int keySize, SecurityLevel level) {
    this.id = id;
    this.txt = txt;
    this.secLevel = level;
    this.keySize = keySize;
  }
  
  /***
   * <p>Gets en elliptic curve by id.</p>
   *
   * @param id  the id to look up
   * @return the enum or null if not found
   */
  public static EllipticCurveType getById(int id) {
    for (EllipticCurveType e : values()) {
      if (e.id == id) {
        return e;
      }
    }
    return null;
  }
  
  /***
   * <p>Gets en elliptic curve by keySize.</p>
   *
   * @param ks  the keysize to look up
   * @return an array of suitable enums
   */
  public static EllipticCurveType[] getByKeySize(int ks) {
    List<EllipticCurveType> l = new ArrayList<>();
    for (EllipticCurveType e : values()) {
      if (e.getKeySize() == ks) {
        l.add(e);
      }
    }
    return l.toArray(new EllipticCurveType[0]);
  }
  
  /***
   * <p>Gets en elliptic enum curve by name.</p>
   *
   * @param s  the name to look up
   * @return the enum or null if not found
   */
  public static EllipticCurveType getByString(String s) {
    for (EllipticCurveType e : values()) {
      if (e.toString().equals(s)) {
        return e;
      }
    }
    return null;
  }
  
  public int getId() {
    return id;
  }
  
  public String toString() {
    return txt;
  }
  
  public SecurityLevel getSecurityLevel() {
    return secLevel;
  }

  public int getKeySize() {
    return keySize;
  }
  
  public static EllipticCurveType getDefault() {
    return def;
  }
}

