package net.gwerder.java.messagevortex.test.blender;

import net.gwerder.java.messagevortex.MessageVortexLogger;
import net.gwerder.java.messagevortex.asn1.*;
import net.gwerder.java.messagevortex.asn1.encryption.Algorithm;
import net.gwerder.java.messagevortex.asn1.encryption.SecurityLevel;
import net.gwerder.java.messagevortex.blending.BlenderListener;
import net.gwerder.java.messagevortex.blending.DummyBlender;
import net.gwerder.java.messagevortex.transport.DummyTransportSender;
import net.gwerder.java.messagevortex.transport.TransportReceiver;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Vector;
import java.util.logging.Level;

import static org.junit.Assert.*;

/**
 * Created by martin.gwerder on 19.04.2017.
 */
@RunWith(JUnit4.class)
public class DummyBlenderTest implements BlenderListener,TransportReceiver {

    private static final java.util.logging.Logger LOGGER;

    static {
            LOGGER = MessageVortexLogger.getLogger((new Throwable()).getStackTrace()[0].getClassName());
            MessageVortexLogger.setGlobalLogLevel( Level.ALL);
    }

    private List<InputStream> msgs = new Vector<>();

    @Test
    public void dummyBlenderEndpointTest()  {
        LOGGER.log( Level.INFO, "Setting up dummy network" );
        DummyBlender[] dt=new DummyBlender[10];
        for(int i=0;i<dt.length;i++) {
            LOGGER.log( Level.INFO, "  Setting up endpoint "+i );
            try {
                dt[i] = new DummyBlender("martin@example.com"+i, this);
            }catch (IOException ioe) {
                fail("failed to add martin@example.com");
            }
        }

        // Test duplicate id generation for transport media
        try {
            new DummyBlender("martin@example.com0",this);
            fail("duplicate addition of ID to DummyBlender unexpectedly succeeded");
        } catch(IOException ioe) {
            // this is expected behaviour
        }
        try {
            new DummyTransportSender("martin@example.com0",this);
            fail("duplicate addition of ID to DummyTransportSender unexpectedly succeeded");
        } catch(IOException ioe) {
            // this is expected behaviour
        }
        try {
            VortexMessage v=new VortexMessage(new PrefixBlock(), new InnerMessageBlock(new PrefixBlock(),new IdentityBlock(),new RoutingBlock()));
            v.setDecryptionKey(new AsymmetricKey(Algorithm.RSA.getParameters(SecurityLevel.LOW)));
            assertTrue("Failed sending message to different endpoint", dt[0].blendMessage(new BlendingSpec("martin@example.com1"), v));
            assertFalse("Failed sending message to unknown endpoint (unexpectedly succeeded)", dt[0].blendMessage(new BlendingSpec("martin@example.com-1"), v));
        } catch (Exception ioe) {
            LOGGER.log(Level.SEVERE, "Caught exception while creating message", ioe);
            fail("Endpointests failed as there was an error opening sample messages");
        }

    }


    @Override
    public void gotMessage(InputStream is) {
        synchronized(msgs) {
            msgs.add(is);
        }
    }
}
