package net.gwerder.java.messagevortex.asn1.encryption;

/**
 * <p>Specifies random number generator.</p>
 */
public enum PrngType {
  FIXME(-1);

  int id;

  PrngType(int id) {
    this.id = id;
  }

  public int getId() {
    return this.id;
  }

  public static PrngType getById(int id) {
    for (PrngType e : values()) {
      if (e.id == id) {
        return e;
      }
    }
    return null;
  }
}
