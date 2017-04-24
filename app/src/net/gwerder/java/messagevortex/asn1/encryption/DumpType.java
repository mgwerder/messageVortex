package net.gwerder.java.messagevortex.asn1.encryption;

/**
 * Enumeration representing the type of dump requested or parsed.
 */
public enum DumpType {
    /* Dump all information to the maximum possible */
    ALL,
    PUBLIC_ONLY,
    PUBLIC_COMMENTED,
    PRIVATE_ONLY,
    PRIVATE_COMMENTED;

    public boolean dumpPublicKey() {
        switch(this) {
            case ALL:
                return true;
            case PUBLIC_ONLY:
                return true;
            default:
                return false;
        }
    }

    public boolean dumpPrivateKey() {
        switch(this) {
            case ALL:
                return true;
            case PRIVATE_ONLY:
                return true;
            default:
                return false;
        }
    }
}
