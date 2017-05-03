package net.gwerder.java.messagevortex.routing.operation;

import net.gwerder.java.messagevortex.asn1.IdentityBlock;

public abstract class AbstractOperation implements Operation {

    IdentityBlock identity;

    public AbstractOperation(IdentityBlock i) {
        this.identity=i;
    }

    public abstract boolean canRun();

    public IdentityBlock getIdentity() {
        return identity;
    }

    public boolean isInUsagePeriod() {
        // TODO do something sensible here
        return true;
    }

}
