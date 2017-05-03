package net.gwerder.java.messagevortex.routing.operation;

import net.gwerder.java.messagevortex.asn1.IdentityBlock;

/**
 * Created by Martin on 30.04.2017.
 */
public interface Operation {

    IdentityBlock getIdentity();

    boolean canRun();

    boolean isInUsagePeriod();

    int[] getOutputID();

}
