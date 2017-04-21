package net.gwerder.java.messagevortex.routing.operation;

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
    public RemoveRedundancy(int startid,int n, int m) {
        // first m-n blocks are inputs and following m blocks are outblocks
        super(startid,m+(m-n));

        initMatrix(n,m);
    }

    private void initMatrix(int n, int m) {
        matrix=new VandermondeMatrix(m,m-n,new GaloisFieldMathMode( m>256?16:8) );
    }


}
