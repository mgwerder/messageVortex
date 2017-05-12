package net.gwerder.java.messagevortex.routing.operation;
// ************************************************************************************
// * Copyright (c) 2018 Martin Gwerder (martin@gwerder.net)
// *
// * Permission is hereby granted, free of charge, to any person obtaining a copy
// * of this software and associated documentation files (the "Software"), to deal
// * in the Software without restriction, including without limitation the rights
// * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
// * copies of the Software, and to permit persons to whom the Software is
// * furnished to do so, subject to the following conditions:
// *
// * The above copyright notice and this permission notice shall be included in all
// * copies or substantial portions of the Software.
// *
// * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
// * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
// * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
// * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
// * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
// * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
// * SOFTWARE.
// ************************************************************************************


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
        this(dataRows,total,mode,true,false);
    }

    /***
     * Creates a redundancy matrix based on vnadermonde matrices.
     *
     * @param dataRows the number of data rows
     * @param total    the number of total rows (redundancy + data rows)
     * @param mode     the math mode to be used
     * @param noInit  if set precached values are ignored
     */
    private RedundancyMatrix(int dataRows, int total, MathMode mode,boolean noCache,boolean noInit) {
        super(dataRows, total, mode);

        // get value from cache
        if(!matrixCacheDisabled) {
            Matrix m = matrixCache.get("rm" + dataRows + "/" + total + "/" + mode.toString());
            if (!noCache && m != null) {
                m = m.clone();
                this.matrix = m.matrix;
                return;
            }
        }

        if(!noInit) {
            for (int col = 1; col < getX(); col++) {
                // make x=y a unit field
                if (getField(col, col) != 1) {
                    int scalar = getField(col, col);
                    transformColumn(col, -1, scalar);
                    assert getField(col, col) == 1;
                }

                // nullify other columns in this row
                for (int col2 = 0; col2 < getX(); col2++) {
                    int scalar = getField(col2, col);
                    if (col != col2 && scalar != 0) {
                        transformColumn(col2, col, scalar);
                        assert getField(col2, col) == 0;
                    }
                }

            }
            matrixCache.put("rm" + dataRows + "/" + total + "/" + mode.toString(), clone());
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
        Matrix ret=red.getInverse();
        return ret;
    }

    @Override
    public RedundancyMatrix clone() {
        RedundancyMatrix ret=new RedundancyMatrix(dimension[0],dimension[1],mode,true,true);
        ret.matrix=Arrays.copyOf(this.matrix,this.matrix.length);
        assert ret.equals(this);
        return ret;
    }
}
