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

    final int MODAR_W;
    final int MODAR_NW;
    final int[] gflog;
    final int[] gfilog;

    final static int[] PRIM_POLY=new int[] {3,7,11,19,37,67,137,285,529,1033,2053,4179,8219,17475,32771,69643};
    final static Map<Integer,GaloisFieldMathMode> cachedMathMode=new ConcurrentHashMap<>();

    private GaloisFieldMathMode(int omega) {
        if(omega<1 ||omega>16) {
            throw new ArithmeticException( "illegal GF size "+omega+" (PRIM_POLY unknown)" );
        }
        MODAR_W=omega;
        MODAR_NW = (int)Math.pow(2,omega);
        gflog=new int[MODAR_NW];
        gfilog=new int[MODAR_NW];
        int b=1;
        for(int log=0;log<MODAR_NW-1;log++) {
            gflog[b%MODAR_NW]=log;
            gfilog[log%MODAR_NW]=b;
            b=lshift(b,1,(byte)33);
            if((b & MODAR_NW)!=0) {
                b=b^PRIM_POLY[omega-1];
            }
        }
        // initialize undefined values with 0
        gflog[0]=-1;
        gfilog[MODAR_NW-1]=-1;
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
        };
        int sumLog = (gflog[c1] + gflog[c2]);
        if(sumLog>=MODAR_NW-1) {
            sumLog-=MODAR_NW-1;
        }
        return gfilog[sumLog];
    }

    public int div(int c1, int divisor) {
        if (c1 == 0) {
            return 0;
        }
        if (divisor == 0) {
            throw new ArithmeticException("Divisionby 0");
        }
        int diffLog = (gflog[c1] - gflog[divisor]);
        while(diffLog<0) {
            diffLog+=MODAR_NW-1;
        }
        return gfilog[diffLog];
    }

    @Override
    public int add(int c1, int c2) {
        return c1^c2;
    }

    @Override
    public int sub(int c1, int c2) {
        return add(c1,c2);
    }

    public int[] getGFLog() { return gflog; }

    public int[] getGFILog() { return gfilog; }

    public static int rshift(int value,int shift, byte length) {
        return lshift(value,-shift,length);
    }

    public static int lshift(int value,int shift, byte length) {
        long ret=value;
        if(shift==0) {
            return value;
        }
        shift=shift%length;
        if(shift<0) {
            shift+=length;
        }

        // do shift
        ret=ret << shift;

        // move overflow to lower end
        long bitmask=((long)Math.pow(2,shift)-1)<<length;
        long lowbits=(ret & bitmask)>>length;
        ret=ret | lowbits;

        // truncate result (inefficient but works)
        ret=(long)(ret & ((int)Math.pow(2,length)-1));
        return (int)ret;
    }
}
