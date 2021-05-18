package net.messagevortex.asn1;

/**
 * <p>Type of header requests</p>
 */
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

    /**
     * <p>obtain the ASN.1 ID of the header request.</p>
     *
     * @return the requested ID
     */
    public int getId() {
        return this.id;
    }

    /**
     * <p>Returns a class representing the respective header request.</p>
     *
     * @return the template class
     */
    public Class getTemplateClass() {
        return this.templateClass;
    }

    /**
     * <p>Obtain the respective header request type by using the template class.</p>
     *
     * @param c the template class to be identified
     * @return the respective constant
     */
    public static HeaderRequestType getByClass(Class c) {
        for (HeaderRequestType e : values()) {
            if (e.getTemplateClass() == c) {
                return e;
            }
        }
        return null;
    }
}
