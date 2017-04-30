package net.gwerder.java.messagevortex.routing.operation;

import net.gwerder.java.messagevortex.asn1.Identity;

public abstract class AbstractOperation implements Operation {

    Identity identity;

    public AbstractOperation(Identity i) {
        this.identity=i;
    }

    public abstract boolean canRun();

    public Identity getIdentity() {
        return identity;
    }

    public boolean isInUsagePeriod() {
        // TODO do something sensible here
        return true;
    }

}
