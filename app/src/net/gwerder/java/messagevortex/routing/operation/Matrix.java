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
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;

/**
 * Offers matrix calculations in different fields.
 */
public class Matrix {

    /** may be set to disable cache **/
    static boolean matrixCacheDisabled =false;

    private static final java.util.logging.Logger LOGGER;
    static {
        LOGGER = MessageVortexLogger.getLogger((new Throwable()).getStackTrace()[0].getClassName());
        MessageVortexLogger.setGlobalLogLevel( Level.ALL);
    }

    static Map<String,Matrix> matrixCache=new ConcurrentHashMap<>();


    int[] matrix;
    int[] dimension=new int[2];
    int modulo=Integer.MAX_VALUE;
    MathMode mode;

    public Matrix(Matrix m) {
        this(m.getX(),m.getY(),m.mode,m.matrix);
    }

    /***
     * creates a matrix (x,y) with the specified MathMode.
     *
     * @param x      the number of columns of the matrix
     * @param y      the number of rows of the matrix
     * @param mode   the math mode to be applied
     */
    public Matrix(int x,int y,MathMode mode) {
        dimension[0]=x;
        dimension[1]=y;
        matrix=new int[x*y];
        if(mode!=null) {
            this.mode=mode;
        }
    }

    /***
     * creates a two dimensional matrix (x,y) with the specified MathMode.
     *
     * @param x       the number of columns of the matrix
     * @param y       the number of rows of the matrix
     * @param mode    the math mode to be applied
     * @param content the content as one dimensional array (sequence of rows)
     */
    public Matrix(int x,int y,MathMode mode,int[] content) {
        this(x,y,mode);
        matrix=Arrays.copyOf(content,content.length);
    }

    /***
     * creates a two dimensional matrix (x,y) with the specified MathMode.
     *
     * @param x       the number of columns of the matrix
     * @param y       the number of rows of the matrix
     * @param mode    the math mode to be applied
     * @param content the content as one dimensional array (sequence of rows)
     */
    public Matrix(int x,int y,MathMode mode,byte[] content) {
        this(x,y,mode);
        matrix=new int[content.length];
        for(int i=0;i<content.length;i++) {
            matrix[i]=content[i] &0xFF;
        }
    }

    /***
     * creates a two dimensional matrix (x,y) with the specified MathMode.
     *
     * @param x       the number of columns of the matrix
     * @param y       the number of rows of the matrix
     * @param mode    the math mode to be applied
     * @param content the value to be set in all fields
     */
    public Matrix(int x,int y,MathMode mode,int content) {
        this(x,y,mode);
        for(int i=0;i<x*y;i++) {
            matrix[i]=content;
        }
    }

    /***
     * creates a two dimensional unit matrix (size,size) with the specified MathMode.
     *
     * @param size    the number of columns of the matrix
     * @param mode    the math mode to be applied
     */
    public static Matrix unitMatrix(int size,MathMode mode) {
        if(!matrixCacheDisabled) {
            Matrix m = matrixCache.get("um" + size + "/" + mode);
            if (m != null) {
                return new Matrix(m);
            }
        }
        Matrix ret=new Matrix(size,size,mode);
        for(int x=0;x<size;x++) {
            for(int y=0;y<size;y++) {
                if (x!=y) {
                    ret.matrix[y * size + x] = 0;
                } else {
                    ret.matrix[y * size + x] = 1;
                }
            }
        }
        matrixCache.put("um"+size+"/"+mode,new Matrix(ret));
        return ret;
    }

    /***
     * Get the number of columns.
     *
     * @return the nuber of columns as int value
     */
    public int getX() {
        return dimension[0];
    }

    /***
     * Set the number of rows.
     *
     * @return the nuber of rows as int value
     */
    public int getY() {
        return dimension[1];
    }

    /***
     * Removes the specified row from the matrix.
     *
     * @param index the index of the row to be removed (starting with 0)
     */
    public void removeRow(int index) {
        int[] newMatrix= Arrays.copyOf(matrix,getX()*(getY()-1));
        for(int i=(1+index)*getX();i<matrix.length;i++){
            newMatrix[i-getX()]=matrix[i];
        }
        dimension[1]--;
        matrix=newMatrix;
    }

    /***
     * Returns a matrix with the specified dimension initialised with random values.
     *
     * @param x       the number of columns of the matrix
     * @param y       the number of rows of the matrix
     * @param mode    the math mode to be applied
     * @return        the generated matrix
     */
    public static Matrix randomMatrix(int x,int y,MathMode mode) {
        Matrix ret=new Matrix(x,y,mode);
        for(int xl=0;xl<x;xl++) {
            for(int yl=0;yl<y;yl++) {
                ret.matrix[x * yl + xl] = (int)(Math.random()*Integer.MAX_VALUE);
            }
        }
        return ret;
    }

    /***
     * Multiplies the current matrix with the specified matrix.
     *
     * @param m                    the matrix to multitply with
     * @return                     the resulting matrix
     * @throws ArithmeticException if multiplication may not be caried out
     */
    public Matrix mul(Matrix m) {
        if(! this.mode.equals(m.mode)) {
            throw new ArithmeticException( "illegal matrix math mode" );
        }
        if(this.getX()!=m.getY()) {
            throw new ArithmeticException( "illegal matrix size" );
        }
        Matrix ret=new Matrix(m.getX(),getY(),mode);
        for(int x=0;x<m.dimension[0];x++) {
            for(int y=0;y<this.dimension[1];y++) {
                ret.matrix[y*ret.dimension[0]+x]=0;
                for (int i=0;i<m.dimension[1];i++) {
                    ret.matrix[y*ret.dimension[0]+x]=mode.add(ret.matrix[y*ret.dimension[0]+x],mode.mul(this.matrix[y*this.dimension[0]+i],m.matrix[i*m.dimension[0]+x]));
                }
                ret.matrix[y*ret.dimension[0]+x]=ret.matrix[y*ret.dimension[0]+x]%modulo;
            }
        }
        return ret;
    }

    @Override
    public boolean equals(Object o) {
        if( o.getClass() != this.getClass() ) {
            return false;
        }
        Matrix m=(Matrix)o;
        if(m.dimension.length!=dimension.length) {
            return false;
        }
        for(int i=0;dimension.length>i;i++) {
            if(dimension[i]!=m.dimension[i]) {
                return false;
            }
        }
        for(int i=0;m.matrix.length>i;i++) {
            if(matrix[i]!=m.matrix[i]) {
                return false;
            }
        }
        return true;
    }

    @Override
    public int hashCode() {
        return toString().hashCode();
    }

    public String toString() {
        StringBuilder sb=new StringBuilder();
        sb.append( "{" );
        for(int y=0;y<dimension[1];y++) {
            sb.append( "{" );
            for (int x = 0; x < dimension[0]; x++) {
                if (x > 0) {
                    sb.append( "," );
                }
                sb.append( matrix[y * dimension[0] + x] );
            }
            sb.append( "}" );
            if (y < dimension[1] - 1) {
                sb.append( ",\n" );
            }
        }
        sb.append( "}" );
        return sb.toString();
    }

    /***
     * Sets a modulo value of the matrix for all operations.
     *
     * @param i   the value to be used as modulo
     * @return    the previously set modulo
     */
    public int setModulo(int i) {
        int ret=modulo;
        if(i<1) {
            modulo=Integer.MAX_VALUE;
        }
        modulo=i;
        return ret;
    }

    /***
     * Get a row of the matrix as array.
     *
     * @param i  the index of the row to be extracted as array
     * @return   the row representation as array
     */
    public int[] getRow(int i) {
        if(i<0 || i>=dimension[1]) {
            throw new ArithmeticException("row index out of range 0<=i<"+dimension[0]);
        }
        int[] ret=new int[dimension[0]];
        for(int j=0;j<ret.length;j++) {
            ret[j]=getField(j,i);
        }
        return ret;
    }

    /***
     * Extracts the value of a specified matrix field.
     *
     * @param x  the column of the field
     * @param y  the row of the field
     * @return   the value of the field
     */
    public int getField(int x,int y) {
        if(x<0 || x>=dimension[0]) {
            throw new ArithmeticException("column index out of range 0<=col["+x+"]<"+dimension[0]);
        }
        if(y<0 || y>=dimension[1]) {
            throw new ArithmeticException("row index out of range 0<=row["+y+"]<"+dimension[1]);
        }
        return matrix[y*getX()+x];
    }

    /***
     * Sets the value of a specified matrix field.
     *
     * @param x     the column of the field
     * @param y     the row of the field
     * @param value the value to be set
     * @return      the previously set value of the field
     */
    public int setField(int x,int y,int value) {
        if(x<0 || x>=dimension[0]) {
            throw new ArithmeticException("column index out of range 0<=col["+x+"]<"+dimension[0]);
        }
        if(y<0 || y>=dimension[1]) {
            throw new ArithmeticException("row index out of range 0<=row["+y+"]<"+dimension[1]);
        }
        int old=getField(x,y);
        matrix[y*getX()+x]=value;
        return old;
    }

    /***
     * Calculates the inverse by aplying the Gauss-Jordan-algorithm.
     *
     * @return the inverse of the matrix
     * @throws ArithmeticException if matrix is not square in dimensions or the algorithm was unable to compute an inverse
     */
    public Matrix getInverse() {
        if(dimension[0]!=dimension[1]) {
            throw new ArithmeticException("matrix to inverse must have square dimensions (dimension is "+getX()+"/"+getY()+")");
        }
        Matrix red=new Matrix(this);
        Matrix ret = Matrix.unitMatrix(dimension[0], mode);
        for (int row = 0; row < getY(); row++) {
            // flip rows if required
            int scalar=red.getField(row, row);
            if(scalar==0) {
                // search next row!=0 at diagonal position
                int flipRow=row+1;
                while(flipRow<getY() && red.getField(row, flipRow)==0) {
                    flipRow++;
                }
                if(flipRow==getY()) {
                    throw new ArithmeticException("unable to inverse matrix (in flip row)");
                }

                // flip rows
                LOGGER.log(Level.FINEST, "  processing flip row with row=" + row + "; row2=" + flipRow );
                red.flipRow(row,flipRow);
                ret.flipRow(row,flipRow);
            }


            // make diagonal 1 and left 0 in red
            scalar = red.getField(row, row);
            if (scalar != 1) {
                red.divRow(row, scalar);
                ret.divRow(row, scalar);
            }

            for (int row2 = row + 1; row2 < dimension[1]; row2++) {
                scalar = red.getField(row, row2);
                if (scalar != 0) {
                    red.transformRow(row2, row, scalar,false);
                    ret.transformRow(row2, row, scalar,false);
                }
            }

        }
        for (int row = 1; row < dimension[0]; row++) {
            // make right 0 in red
            for (int row2 = row - 1; row2 >= 0; row2--) {
                int scalar = red.getField(row, row2);
                if (scalar != 0) {
                    red.transformRow(row2, row, scalar,false);
                    ret.transformRow(row2, row, scalar,false);
                }
            }
        }
        if (!Matrix.unitMatrix(red.dimension[0],mode).equals(red)) {
            throw new ArithmeticException("unable to calculate inverse");
        }
        return ret;
    }

    /***
     * Get the values of a row as byte arrays.
     *
     * @param row the index of the row to be used (starting with 0)
     * @return    the array containing the calues of the row
     */
    public byte[] getRowAsByteArray(int row) {
        byte[] ret=new byte[getX()];
        for(int i=0;i<ret.length;i++) {
            ret[i]=(byte)(getField(i,row));
        }
        return ret;
    }

    /***
     * Get the content of the matrix as byte array.
     *
     * @return the byte array representing the matrix values (row by row)
     */
    public byte[] getAsByteArray() {
        byte[] ret=new byte[getX()*getY()];
        for(int y=0;y<getY();y++) {
            for(int x=0;x<getX();x++) {
                ret[y * getX() + x] = (byte) (getField(x, y));
            }
        }
        return ret;
    }

    /***
     * Multiplies element by element the values of the second column by the specified scalar and subtracts the resulting value from the first element.
     *
     * @param col    the column to be recalculated/altered
     * @param col2   the column to be used for recalculation
     * @param scalar the scalar to be used for division/multiplication
     */
    public void transformColumn(int col,int col2, int scalar) {
        if(col<0 || col>getX()) {
            throw new ArithmeticException("first column is out of range");
        }
        if(col2>getX()) {
            throw new ArithmeticException("second column is out of range");
        }
        for(int row=0;row<getY();row++) {
            int value1 = getField(col, row);
            int newValue;
            if (col2 <0 )  {
                newValue=mode.div(value1,scalar);
            } else {
                int value2 = getField(col2, row);
                newValue = mode.sub(value1, mode.mul(value2, scalar));
            }
            setField(col,row,newValue);
        }
    }

    /***
     * Divides or multiplies element by element the values of the second row by the specified scalar and subtracts the resulting value from the first element.
     *
     * @param row    the row o be recalculated/altered
     * @param row2   the row to be used for recalculation
     * @param scalar the scalar to be used for division/multiplication
     * @param doDiv  flag to specify whether division (true) or multiplication (false) should be used
     */
    public void transformRow(int row,int row2, int scalar,boolean doDiv) {
        if(row<0 || row>getY()) {
            throw new ArithmeticException("first row is out of range");
        }
        if(row2<0 || row2>getX()) {
            throw new ArithmeticException("second row is out of range");
        }
        for(int col=0;col<getX();col++){
            int value1=getField(col,row);
            int value2=getField(col,row2);
            int tValue;
            if(doDiv) {
                tValue = mode.div(value2, scalar);
            } else {
                tValue = mode.mul(value2, scalar);
            }
            int newValue=mode.sub(value1,tValue);
            setField(col,row,newValue);
        }
    }

    /***
     * Divides all values of the specified row in the matrix by the scalar specified.
     *
     * @param row     the index of the row (starting with 0)
     * @param scalar  the scalar to be used as divisor
     */
    public void divRow(int row, int scalar) {
        if(row<0 || row>getY()) {
            throw new ArithmeticException("first row is out of range");
        }

        for(int col=0;col<getX();col++){
            setField(col,row,mode.div(getField(col,row),scalar));
        }
    }

    /***
     * Flips two rows of the current matrix.
     *
     * @param row1 index of the first row (starting with 0)
     * @param row2 index of the second row (starting with 0)
     */
    public void flipRow(int row1, int row2) {
        int tmp;
        for(int i=0;i<getX();i++) {
            tmp=getField(i,row1);
            setField(i,row1,getField(i,row2));
            setField(i,row2,tmp);
        }
    }

    /***
     * Enables or disables the matrix  cache.
     *
     * @param enable  set to true if matrix cache should be enabled
     * @return        the previously set value
     */
    public static boolean enableMatrixCache(boolean enable) {
        boolean old=!matrixCacheDisabled;
        matrixCacheDisabled = (!enable);
        return old;
    }

}
