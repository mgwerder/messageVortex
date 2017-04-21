package net.gwerder.java.messagevortex.test.routing;

import net.gwerder.java.messagevortex.MessageVortexLogger;
import net.gwerder.java.messagevortex.routing.operation.Matrix;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.util.logging.Level;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * Created by martin.gwerder on 19.04.2017.
 */
@RunWith(JUnit4.class)
public class MatrixTest {

    private static final java.util.logging.Logger LOGGER;
    static {
            LOGGER = MessageVortexLogger.getLogger((new Throwable()).getStackTrace()[0].getClassName());
            MessageVortexLogger.setGlobalLogLevel( Level.ALL);
    }

    @Test
    public void basicMatrixTest()  {
        LOGGER.log( Level.INFO, "basic multiplication test (unit matrices)" );
        for(int i=1;i<=10;i++) {
            LOGGER.log( Level.INFO, "  Testing unit multiplication with size "+i );
            assertTrue("error multiplying unit matrices ("+i+")\n"+Matrix.unitMatrix(i).toString(), Matrix.unitMatrix(i).mul(Matrix.unitMatrix( i )).equals(Matrix.unitMatrix( i )) );
            for(int j=0;j<11;j++) {
                int size = (int) (Math.random() * 10) + 1;
                Matrix m = Matrix.randomMatrix( i, size );
                LOGGER.log( Level.INFO, "  Testing unit multiplication with random matrix size (" + i + "/" + size + "; run is "+j+")" );
                assertTrue( "error multiplying random matrices (" + i + "/" + size + ")\n" + m.toString() + "\n=\n" + m.mul( Matrix.unitMatrix( i ) ), m.mul( Matrix.unitMatrix( i ) ).equals( m ) );
            }
        }
    }

}
