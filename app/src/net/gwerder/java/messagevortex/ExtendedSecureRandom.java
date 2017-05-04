package net.gwerder.java.messagevortex;
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
