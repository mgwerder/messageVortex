package net.gwerder.java.messagevortex.test.routing;

import net.gwerder.java.messagevortex.MessageVortexLogger;
import net.gwerder.java.messagevortex.routing.operation.GaloisFieldMathMode;
import net.gwerder.java.messagevortex.routing.operation.MathMode;
import net.gwerder.java.messagevortex.routing.operation.Matrix;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.util.Arrays;
import java.util.logging.Level;

import static org.junit.Assert.assertTrue;

/**
 * Created by martin.gwerder on 19.04.2017.
 */
@RunWith(JUnit4.class)
public class MathModeTest {

    private static final java.util.logging.Logger LOGGER;
    static {
            LOGGER = MessageVortexLogger.getLogger((new Throwable()).getStackTrace()[0].getClassName());
            MessageVortexLogger.setGlobalLogLevel( Level.ALL);
    }

    @Test
    public void basicMathModeTest()  {
        LOGGER.log( Level.INFO, "basic multiplication test (unit matrices)" );
    }

    @Test
    public void gfMathModeTest()  {
        LOGGER.log( Level.INFO, "testing using Blackbox" );
        GaloisFieldMathMode m=new GaloisFieldMathMode( 4 );
        // Testing gflog and gflogi for (omega=4)
        assertTrue("  Failed test for GFILOG for GF(2^4) ["+Arrays.toString( m.getGFILog() )+"]",Arrays.equals(m.getGFILog(),new int[]{1,2,4,8,3,6,12,11,5,10,7,14,15,13,9,0}));
        assertTrue("  Failed test for GFLOG for GF(2^4)",Arrays.equals(m.getGFLog(),new int[]{0,0,1,4,2,8,5,10,3,14,9,7,6,13,11,12}));

        // Testing example operations from http://web.eecs.utk.edu/~plank/plank/papers/CS-96-332.pdf with imnplementation
        assertTrue( "  Failed GF(2^4)-Test for 11+7",m.add(11,7)==12 );
        assertTrue( "  Failed GF(2^4)-Test for 3*7 ["+m.mul(3,7)+"]",m.mul(3,7)==9 );
        assertTrue( "  Failed GF(2^4)-Test for 13*10",m.mul(13,10)==11 );
        assertTrue( "  Failed GF(2^4)-Test for 13/10",m.div(13,10)==3 );
        assertTrue( "  Failed GF(2^4)-Test for 3/7",m.div(3,7)==10 );
    }

    @Test
    public void gfMathModeShiftTest()  {
        LOGGER.log( Level.INFO, "testing shift function" );
        // Test  rshift
        assertTrue( "  shift 2>>1 (length:4) ["+GaloisFieldMathMode.rshift(2,1, (byte)4)+"]", GaloisFieldMathMode.rshift(2,1, (byte)4)==1 );
        assertTrue( "  shift 2>>1 (length:8)", GaloisFieldMathMode.rshift(2,1, (byte)8)==1 );
        assertTrue( "  shift 2>>1 (length:16)", GaloisFieldMathMode.rshift(2,1, (byte)16)==1 );
        // Test  Lshift
        assertTrue( "  shift 1<<1 (length:4) ["+GaloisFieldMathMode.lshift(1,1, (byte)4)+"]", GaloisFieldMathMode.lshift(1,1, (byte)4)==2 );
        assertTrue( "  shift 1<<1 (length:8)", GaloisFieldMathMode.lshift(1,1, (byte)8)==2 );
        assertTrue( "  shift 1<<1 (length:16)", GaloisFieldMathMode.lshift(1,1, (byte)16)==2 );
        //Test overflow wrap
        assertTrue( "  shift 7<<3 (length:4)", GaloisFieldMathMode.lshift(7,3, (byte)4)==11 );
        assertTrue( "  shift 7<<7 (length:8)", GaloisFieldMathMode.lshift(7,7, (byte)8)==131 );
        assertTrue( "  shift 7<<15 (length:16)", GaloisFieldMathMode.lshift(7,15, (byte)16)==Math.pow(2,15)+3 );
        // Test underflow wrap
        assertTrue( "  shift 1>>1 (length:4)["+GaloisFieldMathMode.rshift(1,1, (byte)4)+"]", GaloisFieldMathMode.rshift(1,1, (byte)4)==8 );
        assertTrue( "  shift 1>>1 (length:8)", GaloisFieldMathMode.rshift(1,1, (byte)8)==128 );
        assertTrue( "  shift 1>>1 (length:16)", GaloisFieldMathMode.rshift(1,1, (byte)16)== 32768);
    }

}
