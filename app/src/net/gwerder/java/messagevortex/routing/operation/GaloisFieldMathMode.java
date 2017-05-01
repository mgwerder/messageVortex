package net.gwerder.java.messagevortex.routing.operation;
/***
 * Copyright (c) 2018 Martin Gwerder (martin@gwerder.net)
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 ***/


import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Offers galoise Math required for redundancy matrices.
 */
public class GaloisFieldMathMode implements MathMode {

    private final int GF_FIELD_SIZE;
    private final int[] GF_LOG;
    private final int[] GF_INVERSE_LOG;

    static final int[] PRIM_POLYNOM =new int[] {3,7,11,19,37,67,137,285,529,1033,2053,4179,8219,17475,32771,69643};
    static final Map<Integer,GaloisFieldMathMode> cachedMathMode=new ConcurrentHashMap<>();

    private GaloisFieldMathMode(int omega) {
        if(omega<1 ||omega>16) {
            throw new ArithmeticException( "illegal GF size "+omega+" (PRIM_POLYNOM unknown)" );
        }
        GF_FIELD_SIZE = (int)Math.pow(2,omega);
        GF_LOG=new int[GF_FIELD_SIZE];
        GF_INVERSE_LOG=new int[GF_FIELD_SIZE];
        int b=1;
        for(int log = 0; log< GF_FIELD_SIZE -1; log++) {
            GF_LOG[b% GF_FIELD_SIZE]=log;
            GF_INVERSE_LOG[log% GF_FIELD_SIZE]=b;
            b=lshift(b,1,(byte)33);
            if((b & GF_FIELD_SIZE)!=0) {
                b=b^PRIM_POLYNOM[omega-1];
            }
        }
        // initialize undefined values with 0
        GF_LOG[0]=-1;
        GF_INVERSE_LOG[GF_FIELD_SIZE -1]=-1;
    }

    public static GaloisFieldMathMode getGaloisFieldMathMode(int omega) {
        GaloisFieldMathMode ret=cachedMathMode.get(omega);
        if(ret==null) {
            ret=new GaloisFieldMathMode(omega);
            cachedMathMode.put(omega,ret);
        }
        return ret;
    }

    @Override
    public int mul(int c1, int c2) {
        if (c1 == 0 || c2 == 0) {
            return 0;
        }
        int sumLog = GF_LOG[c1] + GF_LOG[c2];
        if(sumLog>= GF_FIELD_SIZE -1) {
            sumLog-= GF_FIELD_SIZE -1;
        }
        return GF_INVERSE_LOG[sumLog];
    }

    public int div(int c1, int divisor) {
        if (c1 == 0) {
            return 0;
        }
        if (divisor == 0) {
            throw new ArithmeticException("Divisionby 0");
        }
        int diffLog = GF_LOG[c1] - GF_LOG[divisor];
        while(diffLog<0) {
            diffLog+= GF_FIELD_SIZE -1;
        }
        return GF_INVERSE_LOG[diffLog];
    }

    @Override
    public int add(int c1, int c2) {
        return c1^c2;
    }

    @Override
    public int sub(int c1, int c2) {
        return add(c1,c2);
    }

    public int[] getGFLog() { return GF_LOG; }

    public int[] getGFILog() { return GF_INVERSE_LOG; }

    public static int rshift(int value,int shift, byte length) {
        return lshift(value,-shift,length);
    }

    public static int lshift(int value,int shift, byte length) {
        long ret=value;
        if(shift==0) {
            return value;
        }
        int lshift=shift%length;
        if(lshift<0) {
            lshift+=length;
        }

        // do shift
        ret=ret << lshift;

        // move overflow to lower end
        long bitmask=((long)Math.pow(2,lshift)-1)<<length;
        long lowbits=(ret & bitmask)>>length;
        ret=ret | lowbits;

        // truncate result (inefficient but works)
        ret=ret & ((int)Math.pow(2,length)-1);
        return (int)ret;
    }
}
