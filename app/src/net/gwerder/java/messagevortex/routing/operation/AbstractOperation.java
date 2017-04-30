package net.gwerder.java.messagevortex.routing.operation;

import net.gwerder.java.messagevortex.asn1.Identity;

/**
 * Created by martin.gwerder on 19.04.2017.
 */
public abstract class AbstractOperation {

    Identity identity;

    public AbstractOperation(Identity i) {
        this.identity=i;
    }

    abstract public boolean canRun();

}
