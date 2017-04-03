package net.gwerder.java.messagevortex.asn1.encryption;

/**
 * Enumeration of all possible security levels.
 *
 * Created by martin.gwerder on 01.06.2016.
 */
public enum SecurityLevel {
    LOW,
    MEDIUM,
    HIGH,
    QUANTUM;

    public static SecurityLevel getDefault() {return LOW;}

    public SecurityLevel next() {
        return values()[(this.ordinal() + 1) % values().length];
    }

}
