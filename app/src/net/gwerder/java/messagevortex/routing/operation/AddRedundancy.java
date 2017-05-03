package net.gwerder.java.messagevortex.routing.operation;

import net.gwerder.java.messagevortex.asn1.AbstractRedundancyOperation;
import net.gwerder.java.messagevortex.asn1.IdentityBlock;

public class AddRedundancy extends AbstractOperation {

    AbstractRedundancyOperation operation;

    public AddRedundancy(IdentityBlock i, AbstractRedundancyOperation op) {
        super(i);
        this.operation=op;
    }

    @Override
    public boolean canRun() {
        return false;
    }

    @Override
    public int[] getOutputID() {
        int[] ret=new int[operation.getDataStripes()+operation.getRedundancy()];
        for(int i=0;i<ret.length;i++) {
            ret[i]=operation.getOutputId()+i;
        }
        return ret;
    }

}
