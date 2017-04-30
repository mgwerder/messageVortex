package net.gwerder.java.messagevortex.routing.operation;

import net.gwerder.java.messagevortex.asn1.Identity;

/**
 * Created by Martin on 30.04.2017.
 */
public interface Operation {

    Identity getIdentity();

    boolean canRun();

    boolean isInUsagePeriod();

    int[] getOutputID();

}
