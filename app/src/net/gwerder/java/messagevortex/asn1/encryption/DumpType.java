package net.gwerder.java.messagevortex.asn1.encryption;

/**
 * Enumeration representing the type of dump requested or parsed.
 */
public enum DumpType {
    /* Dump all information to the maximum possible */
    ALL,
    /* Dump all information to the maximum possible in unecrypted default manor */
    ALL_UNENCRYPTED,
    /* dump only public information */
    PUBLIC_ONLY,
    /* dump public information; private information is dumped as comment */
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
            case ALL_UNENCRYPTED:
                return true;
            default:
                return false;
        }
    }
}
