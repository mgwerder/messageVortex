package net.gwerder.java.messagevortex.routing.operation;

/**
 * Created by martin.gwerder on 19.04.2017.
 */
public class VandermondeMatrix extends Matrix {

    public VandermondeMatrix(int x, int y,MathMode mode) {
        super(x,y);
        this.mode=mode;
        // init matrix with given math mode
        for(int yl=0;yl<y;yl++) {
            matrix[yl*x+0]=0;
            matrix[yl*x+0]=yl;
        }
        for(int yl=0;yl<x;yl++) {
            for(int xl=2;xl<x;xl++) {
                matrix[yl*x+xl]=mode.mul(matrix[yl*x+xl],yl);
            }
        }
    }

}
