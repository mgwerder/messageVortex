package net.gwerder.java.messagevortex.test.transport;

import net.gwerder.java.messagevortex.MessageVortexLogger;
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

import static org.junit.Assert.fail;

/**
 * Created by martin.gwerder on 19.04.2017.
 */
@RunWith(JUnit4.class)
public class DummyTransportSenderTest implements TransportReceiver {

    private List<InputStream> msgs=new Vector<>();
    private static final java.util.logging.Logger LOGGER;
    static {
            LOGGER = MessageVortexLogger.getLogger((new Throwable()).getStackTrace()[0].getClassName());
            MessageVortexLogger.setGlobalLogLevel( Level.ALL);
    }

    @Test
    public void dummyTransportEndpointTest()  {
        LOGGER.log( Level.INFO, "Setting up dummy network" );
        DummyTransportSender[] dt=new DummyTransportSender[10];
        for(int i=0;i<dt.length;i++) {
            LOGGER.log( Level.INFO, "  Setting up endpoint "+i );
            if(i==0) {
                try {
                    dt[i] = new DummyTransportSender("martin@example.com", this);
                }catch (IOException ioe) {
                    fail("failed to add martin@example.com");
                }
            } else {
                dt[i] = new DummyTransportSender(this);
            }
        }

        // Test duplicate id generation for transport media
        try {
            new DummyTransportSender("martin@example.com",this);
            fail("duplicate addition of ID to DummyTransportSender unexpectedly succeeded");
        } catch(IOException ioe) {
            // this is expected behaviour
        }

    }


    @Override
    public boolean gotMessage(InputStream is) {
        synchronized( msgs ) {
            msgs.add( is );
        }
        return true;
    }
}
