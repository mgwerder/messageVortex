package net.gwerder.java.messagevortex.routing.operation;

/**
 * Van der mode matrices.
 *
 * This class initializes a matrix with van der Monde values (F_{x,y}=y^x)
 */
public class VandermondeMatrix extends Matrix {

    public VandermondeMatrix(int x, int y,MathMode mode) {
        super(x,y,mode);
        // init matrix with given math mode
        for(int yl=0;yl<y;yl++) {
            setField(0,yl,1);
            if(x>1) {
                setField(1,yl,yl);
            }
        }
        if(x>1) {
            for(int yl=0;yl<y;yl++) {
                for(int xl=2;xl<x;xl++) {
                    setField(xl,yl,mode.mul(getField(xl-1,yl),yl));
                }
            }
        }
    }

}
