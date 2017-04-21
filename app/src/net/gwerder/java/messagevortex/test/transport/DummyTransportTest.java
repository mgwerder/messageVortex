package net.gwerder.java.messagevortex.test.transport;

import net.gwerder.java.messagevortex.MessageVortexLogger;
import net.gwerder.java.messagevortex.transport.DummyTransport;
import net.gwerder.java.messagevortex.transport.TransportListener;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Vector;
import java.util.logging.Level;

import static org.junit.Assert.fail;

/**
 * Created by martin.gwerder on 19.04.2017.
 */
@RunWith(JUnit4.class)
public class DummyTransportTest implements TransportListener {

    private List<InputStream> msgs=new Vector<>();
    private static final java.util.logging.Logger LOGGER;
    static {
            LOGGER = MessageVortexLogger.getLogger((new Throwable()).getStackTrace()[0].getClassName());
            MessageVortexLogger.setGlobalLogLevel( Level.ALL);
    }

    @Test
    public void dummyTransportEndpointTest()  {
        LOGGER.log( Level.INFO, "Setting up dummy network" );
        DummyTransport[] dt=new DummyTransport[10];
        for(int i=0;i<dt.length;i++) {
            LOGGER.log( Level.INFO, "  Setting up endpoint "+i );
            if(i==0) {
                try {
                    dt[i] = new DummyTransport("martin@example.com", this);
                }catch (IOException ioe) {
                    fail("failed to add martin@example.com");
                }
            } else {
                dt[i] = new DummyTransport(this);
            }
        }

        // Test duplicate id generation for transport media
        try {
            new DummyTransport("martin@example.com",this);
            fail("duplicate addition of ID to DummyTransport unexpectedly succeeded");
        } catch(IOException ioe) {
            // this is expected behaviour
        }

    }


    @Override
    public void gotMessage(InputStream is) {
        synchronized(msgs) {
            msgs.add(is);
        }
    }
}
