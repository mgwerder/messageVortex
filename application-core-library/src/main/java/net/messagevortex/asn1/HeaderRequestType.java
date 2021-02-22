package net.messagevortex.asn1;

public enum HeaderRequestType {
  IDENTITY(0, HeaderRequestIdentity.class),
  CAPABILITIES(1, HeaderRequestCapability.class),
  MESSAGE_QUOTA(2, HeaderRequestIncreaseMessageQuota.class),
  TRANSFER_QUOTA(3, HeaderRequestIncreaseTransferQuota.class),
  QUOTA_QUERY(4, HeaderRequestQueryQuota.class);

  final int id;
  final Class templateClass;

  HeaderRequestType(int id, Class templateClass) {
    this.id = id;
    this.templateClass = templateClass;
  }

  public int getId() {
    return this.id;
  }

  public Class getTemplateClass() {
    return this.templateClass;
  }

  public static HeaderRequestType getByClass(Class c) {
    for (HeaderRequestType e : values()) {
      if (e.getTemplateClass() == c) {
        return e;
      }
    }
    return null;
  }
}
