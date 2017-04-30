package net.gwerder.java.messagevortex.routing.operation;

import net.gwerder.java.messagevortex.MessageVortexLogger;

import java.util.Arrays;
import java.util.logging.Level;

/**
 * Created by martin.gwerder on 19.04.2017.
 */
public class Matrix {

    private static final java.util.logging.Logger LOGGER;
    static {
        LOGGER = MessageVortexLogger.getLogger((new Throwable()).getStackTrace()[0].getClassName());
        MessageVortexLogger.setGlobalLogLevel( Level.ALL);
    }


    int[] matrix;
    int[] dimension=new int[2];
    int modulo=Integer.MAX_VALUE;
    MathMode mode;

    public Matrix(int x,int y,MathMode mode) {
        dimension[0]=x;
        dimension[1]=y;
        matrix=new int[x*y];
        if(mode!=null) {
            this.mode=mode;
        }
    }

    public Matrix(int x,int y,MathMode mode,int[] content) {
        this(x,y,mode);
        matrix=Arrays.copyOf(content,content.length);
    }

    public Matrix(int x,int y,MathMode mode,int content) {
        this(x,y,mode);
        for(int i=0;i<x*y;i++) {
            matrix[i]=content;
        }
    }

    public static Matrix unitMatrix(int size,MathMode mode) {
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
        return ret;
    }

    public int getX() {
        return dimension[0];
    }

    public int getY() {
        return dimension[1];
    }

    public void removeRow(int index) {
        int[] newMatrix= Arrays.copyOf(matrix,getX()*(getY()-1));
        for(int i=(1+index)*getX();i<matrix.length;i++){
            newMatrix[i-getX()]=matrix[i];
        }
        dimension[1]--;
        matrix=newMatrix;
    }

    public static Matrix randomMatrix(int x,int y,MathMode mode) {
        Matrix ret=new Matrix(x,y,mode);
        for(int xl=0;xl<x;xl++) {
            for(int yl=0;yl<y;yl++) {
                ret.matrix[x * yl + xl] = (int)(Math.random()*Integer.MAX_VALUE);
            }
        }
        return ret;
    }

    public Matrix mul(Matrix m) {
        if(! this.mode.equals(m.mode)) throw new ArithmeticException( "illegal matrix math mode" );
        if(this.dimension[0]!=m.dimension[1]) throw new ArithmeticException( "illegal matrix size" );
        Matrix ret=new Matrix(m.dimension[0],this.dimension[1],mode);
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
        if(! (o instanceof Matrix) ) return false;
        Matrix m=(Matrix)o;
        if(m.dimension.length!=dimension.length) return false;
        for(int i=0;dimension.length>i;i++) if(dimension[i]!=m.dimension[i]) return false;
        for(int i=0;m.matrix.length>i;i++) if(matrix[i]!=m.matrix[i]) return false;
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
                if (x > 0) sb.append( "," );
                sb.append( matrix[y * dimension[0] + x] );
            }
            sb.append( "}" );
            if (y < dimension[1] - 1) sb.append( ",\n" );
        }
        sb.append( "}" );
        return sb.toString();
    }

    public int setModulo(int i) {
        int ret=modulo;
        if(i<1) modulo=Integer.MAX_VALUE;
        modulo=i;
        return ret;
    }

    public int[] getRow(int i) {
        if(i<0 || i>=dimension[1]) throw new ArithmeticException("row index out of range 0<=i<"+dimension[0]);
        int[] ret=new int[dimension[0]];
        for(int j=0;j<ret.length;j++) {
            ret[j]=getField(j,i);
        }
        return ret;
    }

    public int getField(int x,int y) {
        if(x<0 || x>=dimension[0]) throw new ArithmeticException("column index out of range 0<=i<"+dimension[0]);
        if(y<0 || y>=dimension[1]) throw new ArithmeticException("row index out of range 0<=i<"+dimension[0]);
        return matrix[y*getX()+x];
    }

    public int setField(int x,int y,int value) {
        if(x<0 || x>=dimension[0]) throw new ArithmeticException("column index out of range 0<=i<"+dimension[0]);
        if(y<0 || y>=dimension[1]) throw new ArithmeticException("row index out of range 0<=i<"+dimension[0]);
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
        if(dimension[0]!=dimension[1]) throw new ArithmeticException("matrix to inverse must have square dimensions");
        Matrix red=clone();
        Matrix ret = Matrix.unitMatrix(dimension[0], mode);
        for (int row = 0; row < dimension[1]; row++) {
            // make diagonal 1 and left 0 in red
            int scalar = red.getField(row, row);
            if (scalar != 1) {
                red.transformRow(row, row, scalar);
                ret.transformRow(row, row, scalar);
            }
            LOGGER.log(Level.FINEST, "  step diagonal \r\n" + red.toString() + "\r\n" + ret.toString());

            for (int row2 = row + 1; row2 < dimension[1]; row2++) {
                scalar = red.getField(row, row2);
                if (scalar != 0) {
                    red.transformRow(row2, row, scalar);
                    ret.transformRow(row2, row, scalar);
                }
                LOGGER.log(Level.FINEST, "    row=" + row + "; row2=" + row2 + "; scalar=" + scalar);
                LOGGER.log(Level.FINEST, "  step left \r\n" + red.toString() + "\r\n" + ret.toString());
            }

        }
        for (int row = 1; row < dimension[0]; row++) {
            // make right 0 in red
            for (int row2 = row - 1; row2 >= 0; row2--) {
                int scalar = red.getField(row, row2);
                if (scalar != 0) {
                    red.transformRow(row2, row, scalar);
                    ret.transformRow(row2, row, scalar);
                }
                LOGGER.log(Level.FINEST, "    row=" + row + "; row2=" + row2 + "; scalar=" + scalar);
                LOGGER.log(Level.FINEST, "  step diagonal right\r\n" + red.toString() + "\r\n" + ret.toString());
            }
        }
        if (!Matrix.unitMatrix(red.dimension[0],mode).equals(red)) throw new ArithmeticException("unable to calculate inverse");
        return ret;
    }


    @Override
    public Matrix clone() {
        Matrix ret=new Matrix(getX(),getY(),mode,matrix);
        return ret;
    }

    void transformColumn(int col,int col2, int scalar) {
        if(col<0 || col>getX()) throw new ArithmeticException("first column is out of range");
        if(col2>getX()) throw new ArithmeticException("second column is out of range");
        for(int row=0;row<getY();row++) {
            int value1 = getField(col, row);
            int value2;
            int newValue;
            if (col2 <0 )  {
                value2=-1;
                newValue=mode.div(value1,scalar);
            } else {
                value2 = getField(col2, row);
                newValue = mode.sub(value1, mode.mul(value2, scalar));
            }
            LOGGER.log(Level.FINEST, "  doing transformColumn("+col+","+col2+","+scalar+") for row "+row+" -> col1="+value1+"; col2="+value2+"; newValue="+newValue);
            setField(col,row,newValue);
        }
    }

    void transformRow(int row,int row2, int scalar) {
        if(row<0 || row>getY()) throw new ArithmeticException("first row is out of range");
        if(row2<0 || row2>getX()) throw new ArithmeticException("second row is out of range");
        for(int col=0;col<getX();col++){
            int value1=getField(col,row);
            int value2=getField(col,row2);
            int newValue=mode.sub(value1,mode.div(value2,scalar));
            setField(col,row,newValue);
        }
    }



}
