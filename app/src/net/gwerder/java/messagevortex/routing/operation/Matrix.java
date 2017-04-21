package net.gwerder.java.messagevortex.routing.operation;

/**
 * Created by martin.gwerder on 19.04.2017.
 */
public class Matrix {

    protected int[] matrix;
    protected int[] dimension=new int[2];
    protected int modulo=Integer.MAX_VALUE;
    protected MathMode mode=new RealMathMode();

    public Matrix(int x,int y) {
      dimension[0]=x;
      dimension[1]=y;
      matrix=new int[x*y];
    }

    public Matrix(int x,int y,int[] content) {
        this(x,y);
        for(int i=0;i<x*y;i++) {
            matrix[i]=content[i];
        }
    }

    public Matrix(int x,int y,int content) {
        this(x,y);
        for(int i=0;i<x*y;i++) {
            matrix[i]=content;
        }
    }

    public static Matrix unitMatrix(int size) {
        Matrix ret=new Matrix(size,size);
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

    public static Matrix randomMatrix(int x,int y) {
        Matrix ret=new Matrix(x,y);
        for(int xl=0;xl<x;xl++) {
            for(int yl=0;yl<y;yl++) {
                ret.matrix[x * yl + xl] = (int)(Math.random()*Integer.MAX_VALUE);
            }
        }
        return ret;
    }

    public Matrix mul(Matrix m) {
        if(this.dimension[0]!=m.dimension[1]) throw new ArithmeticException( "illegal matrix size" );
        Matrix ret=new Matrix(m.dimension[0],this.dimension[1]);
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

}
