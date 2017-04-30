package net.gwerder.java.messagevortex.routing.operation;

import net.gwerder.java.messagevortex.asn1.Identity;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

/**
 * Created by martin.gwerder on 19.04.2017.
 */
public class RemoveRedundancy extends AbstractOperation {

    Matrix matrix=null;

    /***
     * Creates an add operation with startid as the first id and an matrice to recover a maximum of n out of m blocks
     * @param startid
     * @param n
     * @param m
     ***/
    public RemoveRedundancy(Identity i, int startid, int n, int m) {
        // first m-n blocks are inputs and following m blocks are outblocks
        super(i);

        initMatrix(n,m);
    }

    private void initMatrix(int n, int m) {
        matrix=new VandermondeMatrix(m,m-n,GaloisFieldMathMode.getGaloisFieldMathMode( m>256?16:8) );
    }


    @Override
    public boolean canRun() {
        // FIXME
        throw new NotImplementedException();
    }

}
