package net.gwerder.java.messagevortex.routing.operation;

import net.gwerder.java.messagevortex.asn1.IdentityBlock;
import net.gwerder.java.messagevortex.asn1.RemoveRedundancyOperation;

/**
 * Created by martin.gwerder on 19.04.2017.
 */
public class RemoveRedundancy extends AbstractOperation {

    RemoveRedundancyOperation operation;

    public RemoveRedundancy(IdentityBlock i, RemoveRedundancyOperation op) {
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
