package net.gwerder.java.mailvortex.asn1.encryption;

/**
 * Created by martin.gwerder on 23.06.2016.
 */
public enum DumpType {
    ALL,
    PUBLIC_ONLY,
    PUBLIC_COMMENTED,
    PRIVATE_ONLY,
    PRIVATE_COMMENTED;

    public boolean dumpPublicKey() {
        if("ALL".equals(name())) {
            return true;
        }
        if("PUBLIC_ONLY".equals(name())) {
            return true;
        }
        return false;
    }

    public boolean dumpPrivateKey() {
        if("ALL".equals(name())) {
            return true;
        }
        if("PRIVATE_ONLY".equals(name())) {
            return true;
        }
        return false;
    }
}