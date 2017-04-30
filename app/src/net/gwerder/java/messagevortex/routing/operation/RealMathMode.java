package net.gwerder.java.messagevortex.routing.operation;

/**
 * Created by martin.gwerder on 20.04.2017.
 */
public class RealMathMode implements MathMode {

    private static final RealMathMode real=new RealMathMode();

    private RealMathMode() {}

    public static RealMathMode getRealMathMode() {
        return real;
    }

    @Override
    public int mul(int c1, int c2) {
        return c1*c2;
    }

    @Override
    public int div(int c1, int c2) {
        return c1/c2;
    }

    @Override
    public int add(int c1, int c2) {
        return c1+c2;
    }

    @Override
    public int sub(int c1, int c2) {
        return c1-c2;
    }
}
