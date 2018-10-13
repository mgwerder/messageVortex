package net.messagevortex.test.blender;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.IOException;
import java.util.List;
import java.util.Vector;
import java.util.logging.Level;
import net.messagevortex.MessageVortexLogger;
import net.messagevortex.asn1.AsymmetricKey;
import net.messagevortex.asn1.BlendingSpec;
import net.messagevortex.asn1.IdentityBlock;
import net.messagevortex.asn1.IdentityStore;
import net.messagevortex.asn1.InnerMessageBlock;
import net.messagevortex.asn1.PrefixBlock;
import net.messagevortex.asn1.RoutingBlock;
import net.messagevortex.asn1.VortexMessage;
import net.messagevortex.asn1.encryption.Algorithm;
import net.messagevortex.asn1.encryption.SecurityLevel;
import net.messagevortex.blending.BlenderReceiver;
import net.messagevortex.blending.DummyBlender;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/**
 * Created by martin.gwerder on 19.04.2017.
 */
@RunWith(JUnit4.class)
public class DummyBlenderTest implements BlenderReceiver {

    private static final java.util.logging.Logger LOGGER;

    static {
            LOGGER = MessageVortexLogger.getLogger((new Throwable()).getStackTrace()[0].getClassName());
            MessageVortexLogger.setGlobalLogLevel( Level.ALL);
    }

    private List<VortexMessage> msgs = new Vector<>();

    @Test
    public void dummyBlenderEndpointTest()  {
        LOGGER.log( Level.INFO, "Setting up dummy network" );
        DummyBlender[] dt = new DummyBlender[10];
        IdentityStore store = new IdentityStore();
        for( int i = 0; i < dt.length; i++ ) {
            LOGGER.log( Level.INFO, "  Setting up endpoint " + i );
            try {
                dt[i] = new DummyBlender( "martin@example.com" + i, this,store );
            }catch ( IOException ioe ) {
                fail( "failed to add martin@example.com"+i );
            }
        }

        // Test duplicate id generation for transport media
        try {
            new DummyBlender( "martin@example.com0", this, store );
            fail("duplicate addition of ID to DummyBlender unexpectedly succeeded");
        } catch(IOException ioe) {
            // this is expected behaviour
        }
        try {
            new DummyBlender( "martin@example.com0", this, store );
            fail("duplicate addition of ID to DummyTransportSender unexpectedly succeeded");
        } catch(IOException ioe) {
            // this is expected behaviour
        }
        try {
            VortexMessage v = new VortexMessage( new PrefixBlock(), new InnerMessageBlock( new PrefixBlock(), new IdentityBlock(), new RoutingBlock() ) );
            v.setDecryptionKey( new AsymmetricKey( Algorithm.RSA.getParameters( SecurityLevel.getDefault() ) ) );
            assertTrue( "Failed sending message to different endpoint", dt[0].blendMessage( new BlendingSpec("martin@example.com1"), v));
            assertFalse( "Failed sending message to unknown endpoint (unexpectedly succeeded)", dt[0].blendMessage( new BlendingSpec("martin@example.com-1"), v));
        } catch (Exception ioe) {
            LOGGER.log(Level.SEVERE, "Caught exception while creating message", ioe);
            fail("Endpointests failed as there was an error opening sample messages");
        }
    }


    @Override
    public boolean gotMessage(VortexMessage message) {
        synchronized( msgs ) {
            msgs.add( message );
        }
        return true;
    }

}
