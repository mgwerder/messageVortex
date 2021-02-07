package net.messagevortex.asn1.encryption;

// FIXME enum not functional (must first match specified PRNGs)

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

  /***
   * <p>Gets a pseudo random number generator based on its id.</p>
   *
   * @param id  the ID to be looked up
   * @return the type or null if not found
   */
  public static PrngType getById(int id) {
    for (PrngType e : values()) {
      if (e.id == id) {
        return e;
      }
    }
    return null;
  }
}
