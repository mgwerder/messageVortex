package net.gwerder.java.messagevortex.routing.operation;

import net.gwerder.java.messagevortex.MessageVortexLogger;

import java.util.Arrays;
import java.util.logging.Level;

/**
 * Creates a redundancy matrix or a recovery matrix for the redundancy operations.
 */
public class RedundancyMatrix extends VandermondeMatrix {

    private static final java.util.logging.Logger LOGGER;
    static {
        LOGGER = MessageVortexLogger.getLogger((new Throwable()).getStackTrace()[0].getClassName());
        MessageVortexLogger.setGlobalLogLevel( Level.ALL);
    }

    /***
     * Creates a redundancy matrix based on vnadermonde matrices.
     *
     * @param dataRows the number of data rows
     * @param total    the number of total rows (redundancy + data rows)
     * @param mode     the math mode to be used
     */
    public RedundancyMatrix(int dataRows, int total, MathMode mode) {
        super(dataRows, total, mode);
        for(int col=1;col<getX();col++) {
            // make x=y a unit field
            if(getField(col,col)!=1) {
                int scalar=getField(col,col);
                transformColumn(col,-1,scalar);
                LOGGER.log(Level.FINEST,"## did unify ("+col+"/"+col+") ("+col+"/"+col+"/"+scalar+")\r\n"+toString());
                assert getField(col,col)==1;
            }

            // nullify other columns in this row
            for(int col2=0;col2<getX();col2++) {
                int scalar=getField(col2,col);
                if(col!=col2 && scalar!=0) {
                    transformColumn(col2,col,scalar);
                    LOGGER.log(Level.FINEST,"## nullified ("+col2+"/"+col+") with "+scalar+" "+(getField(col2,col)!=0?"FAILED ["+getField(col2,col)+"]":"")+"\r\n"+toString());
                    assert getField(col2,col)==0;
                }
            }

        }
    }

    /***
     * calculates a matrix to recover all data rows given the missing rows.
     *
     * @param missingRowIndex Index of the rows missing data
     * @return a square matrix rebuilding the data vector
     */
    public Matrix getRecoveryMatrix(int[] missingRowIndex) {
        RedundancyMatrix red=clone();
        Arrays.sort(missingRowIndex);
        for(int i=missingRowIndex.length-1;i>=0;i--) {
            red.removeRow(missingRowIndex[i]);
        }
        while (red.getX() < red.getY()) {
            red.removeRow(red.getY() - 1);
        }
        LOGGER.log(Level.FINEST, "  reduced redundancy matrix\r\n" + red.toString());
        Matrix ret=red.getInverse();
        LOGGER.log(Level.FINEST, "  inverse of reduced redundancy matrix\r\n" + ret.toString());
        return ret;
    }

    @Override
    public RedundancyMatrix clone() {
        return new RedundancyMatrix(dimension[0],dimension[1],mode);
    }
}
