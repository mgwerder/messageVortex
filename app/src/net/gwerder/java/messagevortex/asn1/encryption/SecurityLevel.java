package net.gwerder.java.messagevortex.asn1.encryption;
// ************************************************************************************
// * Copyright (c) 2018 Martin Gwerder (martin@gwerder.net)
// *
// * for LICENSE see LICENSE.MD in the root of the repository
// ************************************************************************************

/**
 * Enumeration of all possible security levels.
 *
 * The security level classifies the algorithms strength in regards to the security.
 */
public enum SecurityLevel {
    LOW,
    MEDIUM,
    HIGH,
    QUANTUM;

    private static SecurityLevel defaultLevel = MEDIUM;

    /***
     * Retrieves the default security level to be used
     *
     * @return the default security level
     */
    public static SecurityLevel getDefault() {return defaultLevel;}

    /***
     * Sets the default security level to be used
     *
     * @param newLevel   the new default security level for all operations to be set
     * @return the previous security level
     */
    public static SecurityLevel setDefault( SecurityLevel newLevel ) {
        SecurityLevel ret = defaultLevel;
        defaultLevel = newLevel;
        return ret;
    }

    /**
     * Retrieves the next higher security level
     *
     * @return  the next higher security level
     */
    public SecurityLevel next() {
        if(this.ordinal()==values().length-1) {
            return null;
        }
        return values()[(this.ordinal() + 1) % values().length];
    }

    @Override
    public String toString() {
        return this.name() ;
    }

}
