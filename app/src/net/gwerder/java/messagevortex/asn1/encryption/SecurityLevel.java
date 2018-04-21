package net.gwerder.java.messagevortex.asn1.encryption;
// ************************************************************************************
// * Copyright (c) 2018 Martin Gwerder (martin@gwerder.net)
// *
// * for LICENSE see LICENSE.MD in the root of the repository
// ************************************************************************************

/**
 * Enumeration of all possible security levels.
 */
public enum SecurityLevel {
    LOW,
    MEDIUM,
    HIGH,
    QUANTUM;

    public static SecurityLevel getDefault() {return LOW;}

    public SecurityLevel next() {
        if(this.ordinal()==values().length-1) {
            return null;
        }
        return values()[(this.ordinal() + 1) % values().length];
    }

    @Override
    public String toString() {
        return ""+this.name() ;
    }

}
