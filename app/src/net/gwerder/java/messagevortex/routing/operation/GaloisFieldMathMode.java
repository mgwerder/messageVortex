package net.gwerder.java.messagevortex.routing.operation;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by martin.gwerder on 20.04.2017.
 */
public class GaloisFieldMathMode implements MathMode {

    int MODAR_W;
    int MODAR_NW;
    int MODAR_NWM1;
    int[] gflog;
    int[] gfilog;

    final static Map<Integer,Integer> PRIM_POLY=new ConcurrentHashMap<>(  );
    static{
        PRIM_POLY.put(2,7);
        PRIM_POLY.put(4,19);
        PRIM_POLY.put(8,285);
        PRIM_POLY.put(16,69643);
    }

    public GaloisFieldMathMode(int omega) {
        if(!PRIM_POLY.containsKey( omega )) throw new ArithmeticException( "illegal GF size (PRIM_POLY unknown)" );
        MODAR_W=omega;
        MODAR_NW = MODAR_W*MODAR_W;
        MODAR_NWM1 = MODAR_NW-1;
        gflog=new int[MODAR_NW];
        gfilog=new int[MODAR_NW];
        int b=1;
        for(int log=0;log<MODAR_NWM1;log++) {
            gflog[b]=log;
            gfilog[log]=b;
            b=2*b;
            if((b & MODAR_NW)!=0) b=b^PRIM_POLY.get(omega);
        }
        // initialize undefined values with 0
        gflog[0]=0;
        gfilog[MODAR_NWM1]=0;
    }


    @Override
    public int mul(int c1, int c2) {
        int sumLog;
        if (c1 == 0 || c2 == 0) return 0;
        sumLog = (gflog[c1] + gflog[c2]);
        if(sumLog>=MODAR_NWM1) sumLog-=MODAR_NWM1;
        return gfilog[sumLog];
    }

    public int div(int c1, int divisor) {
        int diffLog;
        if (c1 == 0) return 0;
        if (divisor == 0) return -1; /* Can’t divide by 0 */
        diffLog = (gflog[c1] - gflog[divisor])%MODAR_NWM1;
        while(diffLog<0) diffLog+=MODAR_NWM1;
        return gfilog[diffLog];
    }

    @Override
    public int add(int c1, int c2) {
        return c1^c2;
    }

    public int[] getGFLog() { return gflog; }
    public int[] getGFILog() { return gfilog; }

    public static int rshift(int value,int shift, byte length) {
        return lshift(value,-shift,length);
    }

    public static int lshift(int value,int shift, byte length) {
        long ret=value;
        if(shift==0) return value;
        shift=shift%length;
        if(shift<0) shift+=length;
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
