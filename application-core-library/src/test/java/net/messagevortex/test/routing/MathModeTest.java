package net.messagevortex.test.routing;

import net.messagevortex.MessageVortexLogger;
import net.messagevortex.router.operation.BitShifter;
import net.messagevortex.router.operation.GaloisFieldMathMode;
import net.messagevortex.router.operation.MathMode;
import net.messagevortex.router.operation.RealMathMode;
import net.messagevortex.test.GlobalJunitExtension;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.util.Arrays;
import java.util.logging.Level;

@ExtendWith(GlobalJunitExtension.class)
public class MathModeTest {

    private static final java.util.logging.Logger LOGGER;
    static {
            LOGGER = MessageVortexLogger.getLogger((new Throwable()).getStackTrace()[0].getClassName());
    }

    @Test
    public void gfMathModeLogTablesTest()  {
        LOGGER.log( Level.INFO, "testing tables by static build" );
        GaloisFieldMathMode m=GaloisFieldMathMode.getGaloisFieldMathMode( 4 );

        // Testing gflog and gflogi for (omega=4)
        LOGGER.log( Level.INFO, "  checking GF(2^4)tables" );
        Assertions.assertTrue(Arrays.equals(m.getGfIlog(),new int[]{1,2,4,8,3,6,12,11,5,10,7,14,15,13,9,-1}), "  Failed test for GFILOG for GF(2^4) ["+ Arrays.toString( m.getGfIlog() )+"]");
        Assertions.assertTrue(Arrays.equals(m.getGfLog(),new int[]{-1,0,1,4,2,8,5,10,3,14,9,7,6,13,11,12}), "  Failed test for GFLOG for GF(2^4)");
        LOGGER.log( Level.INFO, "  checking GF(2^48)tables" );

        LOGGER.log( Level.INFO, "  checking GF(2^8)tables" );
        m=GaloisFieldMathMode.getGaloisFieldMathMode( 8 );
        // values for test taken from http://www.pclviewer.com/rs2/galois.html
        Assertions.assertTrue(Arrays.equals(m.getGfIlog(),new int[]{1,2,4,8,16,32,64,128,29,58,116,232,205,135,19,38,76,152,45,90,180,117,234,201,143,3,6,12,24,48,96,192,157,39,78,156,37,74,148,53,106,212,181,119,238,193,159,35,70,140,5,10,20,40,80,160,93,186,105,210,185,111,222,161,95,190,97,194,153,47,94,188,101,202,137,15,30,60,120,240,253,231,211,187,107,214,177,127,254,225,223,163,91,182,113,226,217,175,67,134,17,34,68,136,13,26,52,104,208,189,103,206,129,31,62,124,248,237,199,147,59,118,236,197,151,51,102,204,133,23,46,92,184,109,218,169,79,158,33,66,132,21,42,84,168,77,154,41,82,164,85,170,73,146,57,114,228,213,183,115,230,209,191,99,198,145,63,126,252,229,215,179,123,246,241,255,227,219,171,75,150,49,98,196,149,55,110,220,165,87,174,65,130,25,50,100,200,141,7,14,28,56,112,224,221,167,83,166,81,162,89,178,121,242,249,239,195,155,43,86,172,69,138,9,18,36,72,144,61,122,244,245,247,243,251,235,203,139,11,22,44,88,176,125,250,233,207,131,27,54,108,216,173,71,142,-1 }), "  Failed test for GFILOG for GF(2^8) ["+Arrays.toString( m.getGfIlog() )+"]");
    }

    @Test
    public void gfMathModeExceptionTest() {
        LOGGER.log(Level.INFO, "testing using Blackbox");
        LOGGER.log(Level.INFO, "  testing illgal GF sizes");
        try {
            GaloisFieldMathMode.getGaloisFieldMathMode(1);
            Assertions.fail("GF 1 did not raise exception");
        } catch (ArithmeticException e) {
            // this is expected
        }

        try {
            GaloisFieldMathMode.getGaloisFieldMathMode(17);
            Assertions.fail("GF 17 did not raise exception");
        } catch (IllegalArgumentException e) {
            // this is expected
        }

        GaloisFieldMathMode m = GaloisFieldMathMode.getGaloisFieldMathMode(4);
        try {
            m.div(13, 0);
            Assertions.fail("division by zero did not raise exception");
        } catch(ArithmeticException e) {
            // this is expected
        }

    }

    @Test
    public void realMathModeTest() {
        // Testing example operations from http://web.eecs.utk.edu/~plank/plank/papers/CS-96-332.pdf with imnplementation
        MathMode m = RealMathMode.getRealMathMode();
        LOGGER.log(Level.INFO, " checking real math");
        Assertions.assertTrue(m.add(11, 7) == 18, "  Failed Real-Test for 11+7");
        Assertions.assertTrue(m.sub(11, 7) == 4, "  Failed Real-Test for 11-7");
        Assertions.assertTrue(m.mul(3, 7) == 21, "  Failed Real-Test for 3*7");
        Assertions.assertTrue(m.mul(13, 10) == 130, "  Failed Real-Test for 13*10");
        Assertions.assertTrue(m.div(13, 10) == 1, "  Failed Real-Test for 13/10");
        Assertions.assertTrue(m.div(3, 7) == 0, "  Failed Real-Test for 3/7");
        Assertions.assertTrue(m.div(0, 7) == 0, "  Failed Real-Test for 0/7");
        Assertions.assertTrue(m.div(0, 13) == 0, "  Failed Real-Test for 0/13");
    }


     @Test
     public void gfMathModeTest() {
        // Testing example operations from http://web.eecs.utk.edu/~plank/plank/papers/CS-96-332.pdf with imnplementation
        GaloisFieldMathMode m = GaloisFieldMathMode.getGaloisFieldMathMode(4);
        LOGGER.log(Level.INFO, "  checking GF(2^4) math");
        Assertions.assertTrue(m.add(11, 7) == 12, "  Failed GF(2^4)-Test for 11+7");
        Assertions.assertTrue(m.sub(11, 7) == 12, "  Failed GF(2^4)-Test for 11-7");
        Assertions.assertTrue(m.mul(3, 7) == 9, "  Failed GF(2^4)-Test for 3*7 [" + m.mul(3, 7) + "]");
        Assertions.assertTrue(m.mul(13, 10) == 11, "  Failed GF(2^4)-Test for 13*10");
        Assertions.assertTrue(m.div(13, 10) == 3, "  Failed GF(2^4)-Test for 13/10");
        Assertions.assertTrue(m.div(3, 7) == 10, "  Failed GF(2^4)-Test for 3/7");
        Assertions.assertTrue(m.div(0, 7) == 0, "  Failed GF(2^4)-Test for 0/7");
        Assertions.assertTrue(m.div(0, 13) == 0, "  Failed GF(2^4)-Test for 0/13");

        // some more random examples from the internet
        m = GaloisFieldMathMode.getGaloisFieldMathMode(8);
        LOGGER.log(Level.INFO, "  checking GF(2^8) math");
        Assertions.assertTrue(m.mul(2, 4) == 8, "  Failed GF(2^8)-Test for 2*4 [" + m.mul(2, 4) + "]");
        Assertions.assertTrue(m.mul(7, 11) == 49, "  Failed GF(2^8)-Test for 7*11");
        Assertions.assertTrue(m.div(7, 11) == 239, "  Failed GF(2^8)-Test for 7/11");
        Assertions.assertTrue(m.div(7, 31) == 214, "  Failed GF(2^8)-Test for 7/31");
        Assertions.assertTrue(m.div(0, 7) == 0, "  Failed GF(2^8)-Test for 0/7");
        Assertions.assertTrue(m.div(0, 13) == 0, "  Failed GF(2^8)-Test for 0/13");
        try {
            m.div(13, 0);
            Assertions.fail("division by zero did not raise exception");
        } catch (ArithmeticException e) {
            // this is expected
        }

         for(int i=1;i<256;i++) {
             Assertions.assertTrue(m.div(i, i) == 1, "  Failed GF(2^8)-Test for division by itself for i");
         }

         m = GaloisFieldMathMode.getGaloisFieldMathMode(16);
         LOGGER.log(Level.INFO, "  checking GF(2^16) math");
         for(int i=1;i<65536;i++) {
             Assertions.assertTrue(m.div(i, i) == 1, "  Failed GF(2^16)-Test for division by itself for i");
         }
    }

    @Test
    public void gfDivisopnByItself() {
        LOGGER.log(Level.INFO, "checking division by itself");
        //testing division by itself (should alway return 1
        for (int galois = 2; galois <= 16; galois++) {
            GaloisFieldMathMode m = GaloisFieldMathMode.getGaloisFieldMathMode(galois);
            for (int i = 1; i > Math.pow(2, galois); i++) {
                Assertions.assertTrue(m.div(i, i) == 1, "  Failed GF(2^" + galois + ")-Test for " + i + "/" + i + "");
                Assertions.assertTrue(m.div(m.div(m.mul(i, 2), i), 2) == 1, "  Failed GF(2^" + galois + ")-Test for " + i + "*2/" + i + "/2");
            }
        }
    }

    @Test
    public void gfCheckAllValidInits() {
        LOGGER.log( Level.INFO, "  checking init" );
        for(int i=2;i<16;i++) GaloisFieldMathMode.getGaloisFieldMathMode(i);
    }

    @Test
    public void gfMathModeShiftTest()  {
        LOGGER.log( Level.INFO, "testing shift function" );
        // Test  rshift
        Assertions.assertTrue(BitShifter.rshift(2,1, (byte)4)==1, "  shift 2>>1 (length:4) ["+ BitShifter.rshift(2,1, (byte)4)+"]");
        Assertions.assertTrue(BitShifter.rshift(2,1, (byte)8)==1, "  shift 2>>1 (length:8)");
        Assertions.assertTrue(BitShifter.rshift(2,1, (byte)16)==1, "  shift 2>>1 (length:16)");
        // Test  Lshift
        Assertions.assertTrue(BitShifter.lshift(1,1, (byte)4)==2, "  shift 1<<1 (length:4) ["+BitShifter.lshift(1,1, (byte)4)+"]");
        Assertions.assertTrue(BitShifter.lshift(1,1, (byte)8)==2, "  shift 1<<1 (length:8)");
        Assertions.assertTrue(BitShifter.lshift(1,1, (byte)16)==2, "  shift 1<<1 (length:16)");
        //Test overflow wrap
        Assertions.assertTrue(BitShifter.lshift(7,3, (byte)4)==11, "  shift 7<<3 (length:4)");
        Assertions.assertTrue(BitShifter.lshift(7,7, (byte)8)==131, "  shift 7<<7 (length:8)");
        Assertions.assertTrue(BitShifter.lshift(7,15, (byte)16)==Math.pow(2,15)+3, "  shift 7<<15 (length:16)");
        // Test underflow wrap
        Assertions.assertTrue(BitShifter.rshift(1,1, (byte)4)==8, "  shift 1>>1 (length:4)["+BitShifter.rshift(1,1, (byte)4)+"]");
        Assertions.assertTrue(BitShifter.rshift(1,1, (byte)8)==128, "  shift 1>>1 (length:8)");
        Assertions.assertTrue(BitShifter.rshift(1,1, (byte)16)== 32768, "  shift 1>>1 (length:16)");
    }

}
