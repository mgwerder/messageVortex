package net.gwerder.java.mailvortex.asn1.encryption;

/**
 * Enumeration of all possible security levels.
 *
 * Created by martin.gwerder on 01.06.2016.
 */
public enum SecurityLevel {
    LOW,MEDIUM,HIGH,QUANTUM;

    public static SecurityLevel getDefault() {return LOW;}

}
