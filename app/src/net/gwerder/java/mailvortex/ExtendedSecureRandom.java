package net.gwerder.java.mailvortex;

import java.security.SecureRandom;

/**
 * Created by martin.gwerder on 20.06.2016.
 */
public class ExtendedSecureRandom {

    private static final SecureRandom sr=new SecureRandom();

    public int nextInt(int bound) { return sr.nextInt( bound );}
    public void nextBytes(byte[] bound) { sr.nextBytes( bound );}

    public double nextDouble() { return sr.nextDouble();}

    public SecureRandom getSecureRandom() { return sr; }

    public double nextGauss() {
        //calculate value
        double d=Math.sqrt(-2*Math.log(nextDouble()))*Math.cos(2*Math.PI*nextDouble());

        // convert to boundaries
        d+=Math.E;
        d=d/(2*Math.E);

        // get rid of rounding problems
        d=Math.min(1,d);
        d=Math.max(0,d);

        return d;
    }

}
