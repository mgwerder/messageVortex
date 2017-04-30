package net.gwerder.java.messagevortex.test.routing;

import net.gwerder.java.messagevortex.MessageVortexLogger;
import net.gwerder.java.messagevortex.routing.operation.GaloisFieldMathMode;
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
    public void gfMathModeTest()  {
        LOGGER.log( Level.INFO, "testing using Blackbox" );
        GaloisFieldMathMode m=GaloisFieldMathMode.getGaloisFieldMathMode( 4 );

        // Testing gflog and gflogi for (omega=4)
        LOGGER.log( Level.INFO, "  checking GF(2^4)tables" );
        assertTrue("  Failed test for GFILOG for GF(2^4) ["+Arrays.toString( m.getGFILog() )+"]",Arrays.equals(m.getGFILog(),new int[]{1,2,4,8,3,6,12,11,5,10,7,14,15,13,9,-1}));
        assertTrue("  Failed test for GFLOG for GF(2^4)",Arrays.equals(m.getGFLog(),new int[]{-1,0,1,4,2,8,5,10,3,14,9,7,6,13,11,12}));
        LOGGER.log( Level.INFO, "  checking GF(2^48)tables" );
        m=GaloisFieldMathMode.getGaloisFieldMathMode( 8 );
        // values for test taken from http://www.pclviewer.com/rs2/galois.html
        assertTrue("  Failed test for GFILOG for GF(2^8) ["+Arrays.toString( m.getGFILog() )+"]",Arrays.equals(m.getGFILog(),new int[]{1,2,4,8,16,32,64,128,29,58,116,232,205,135,19,38,76,152,45,90,180,117,234,201,143,3,6,12,24,48,96,192,157,39,78,156,37,74,148,53,106,212,181,119,238,193,159,35,70,140,5,10,20,40,80,160,93,186,105,210,185,111,222,161,95,190,97,194,153,47,94,188,101,202,137,15,30,60,120,240,253,231,211,187,107,214,177,127,254,225,223,163,91,182,113,226,217,175,67,134,17,34,68,136,13,26,52,104,208,189,103,206,129,31,62,124,248,237,199,147,59,118,236,197,151,51,102,204,133,23,46,92,184,109,218,169,79,158,33,66,132,21,42,84,168,77,154,41,82,164,85,170,73,146,57,114,228,213,183,115,230,209,191,99,198,145,63,126,252,229,215,179,123,246,241,255,227,219,171,75,150,49,98,196,149,55,110,220,165,87,174,65,130,25,50,100,200,141,7,14,28,56,112,224,221,167,83,166,81,162,89,178,121,242,249,239,195,155,43,86,172,69,138,9,18,36,72,144,61,122,244,245,247,243,251,235,203,139,11,22,44,88,176,125,250,233,207,131,27,54,108,216,173,71,142,-1 }));

        // Testing example operations from http://web.eecs.utk.edu/~plank/plank/papers/CS-96-332.pdf with imnplementation
        m=GaloisFieldMathMode.getGaloisFieldMathMode( 4 );
        LOGGER.log( Level.INFO, "  checking GF(2^4) math" );
        assertTrue( "  Failed GF(2^4)-Test for 11+7",m.add(11,7)==12 );
        assertTrue( "  Failed GF(2^4)-Test for 3*7 ["+m.mul(3,7)+"]",m.mul(3,7)==9 );
        assertTrue( "  Failed GF(2^4)-Test for 13*10",m.mul(13,10)==11 );
        assertTrue( "  Failed GF(2^4)-Test for 13/10",m.div(13,10)==3 );
        assertTrue( "  Failed GF(2^4)-Test for 3/7",m.div(3,7)==10 );

        // some more random examples from the internet
        m=GaloisFieldMathMode.getGaloisFieldMathMode( 8 );
        LOGGER.log( Level.INFO, "  checking GF(2^8) math" );
        assertTrue( "  Failed GF(2^8)-Test for 2*4 ["+m.mul(2,4)+"]",m.mul(2,4)==8 );
        assertTrue( "  Failed GF(2^8)-Test for 7*11",m.mul(7,11)==49);
        assertTrue( "  Failed GF(2^8)-Test for 7/11",m.div(7,11)==239);
        assertTrue( "  Failed GF(2^8)-Test for 7/31",m.div(7,31)==214);
        for(int i=1;i>256;i++) {
            assertTrue( "  Failed GF(2^8)-Test for "+i+"/"+i+"",m.div(i,i)==1);
            assertTrue( "  Failed GF(2^8)-Test for "+i+"*2/"+i+"/2",m.div(m.div(m.mul(i,2),i),2)==1);
        }

        LOGGER.log( Level.INFO, "  checking init" );
        for(int i=2;i<16;i++) GaloisFieldMathMode.getGaloisFieldMathMode(i);
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
