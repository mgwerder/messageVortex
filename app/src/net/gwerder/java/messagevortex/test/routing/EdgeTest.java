package net.gwerder.java.messagevortex.test.routing;

import net.gwerder.java.messagevortex.MessageVortexLogger;
import net.gwerder.java.messagevortex.asn1.IdentityStoreBlock;
import net.gwerder.java.messagevortex.routing.Edge;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.io.IOException;
import java.util.logging.Level;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * Created by martin.gwerder on 19.04.2017.
 */
@RunWith(JUnit4.class)
public class EdgeTest {

    private static final java.util.logging.Logger LOGGER;
    static {
            LOGGER = MessageVortexLogger.getLogger((new Throwable()).getStackTrace()[0].getClassName());
            MessageVortexLogger.setGlobalLogLevel( Level.ALL);
    }

    @Test
    public void edgeEqualityTest()  {
        LOGGER.log( Level.INFO, "Testing edge equality" );
        // Test  rshift
        LOGGER.log( Level.INFO, "  Creating Edges" );

        IdentityStoreBlock[] isb=null;
        try {
            isb=new IdentityStoreBlock[]{IdentityStoreBlock.getIdentityStoreBlockDemo( IdentityStoreBlock.IdentityType.NODE_IDENTITY, false ),IdentityStoreBlock.getIdentityStoreBlockDemo( IdentityStoreBlock.IdentityType.NODE_IDENTITY, false ),IdentityStoreBlock.getIdentityStoreBlockDemo( IdentityStoreBlock.IdentityType.NODE_IDENTITY, false )};
        } catch(IOException ioe) {
            fail("exception getting demo blocks from identity store");
        }
        assertFalse("IdentityStorBlock precondition (0)",isb[0]==null);
        assertFalse("IdentityStorBlock precondition (1)",isb[1]==null);
        assertFalse("IdentityStorBlock precondition (2)",isb[2]==null);
        Edge[] e = new Edge[] { new Edge(isb[0],isb[1],1,2),new Edge(isb[0],isb[1],1,2),new Edge(isb[0],isb[1],1,3),new Edge(isb[0],isb[1],2,2),new Edge(isb[0],isb[2],1,2),new Edge(isb[2],isb[1],1,2),new Edge(isb[1],isb[0],1,2) };
        assertFalse( "equal to null failed", e[0].equals(null));
        assertTrue( "equal to self", e[0].equals(e[0]));
        assertTrue( "equal to aequivalet object", e[0].equals(e[1]));
        assertFalse( "equal to different object", e[0].equals(e[2]));
        assertFalse( "equal to different object", e[0].equals(e[3]));
        assertFalse( "equal to different object", e[0].equals(e[4]));
        assertFalse( "equal to different object", e[0].equals(e[5]));
    }

}
