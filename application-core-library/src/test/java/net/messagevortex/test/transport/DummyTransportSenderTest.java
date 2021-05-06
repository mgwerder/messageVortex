package net.messagevortex.test.transport;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Set;
import java.util.Vector;
import java.util.logging.Level;
import net.messagevortex.AbstractDaemon;
import net.messagevortex.MessageVortexLogger;
import net.messagevortex.test.imap.ImapSSLTest;
import net.messagevortex.transport.TransportReceiver;
import net.messagevortex.transport.dummy.DummyTransportTrx;

import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/**
 * Created by martin.gwerder on 19.04.2017.
 */
public class DummyTransportSenderTest extends AbstractDaemon implements TransportReceiver {

  private List<InputStream> msgs = new Vector<>();
  private static final java.util.logging.Logger LOGGER;

  static {
    LOGGER = MessageVortexLogger.getLogger((new Throwable()).getStackTrace()[0].getClassName());
    MessageVortexLogger.setGlobalLogLevel(Level.ALL);
  }

  @Test
  public void dummyTransportEndpointTest() {
    Set<Thread> threadSet = ImapSSLTest.getThreadList();
    LOGGER.log(Level.INFO, "Setting up dummy network");
    DummyTransportTrx[] dt = new DummyTransportTrx[10];
    for (int i = 0; i < dt.length; i++) {
      LOGGER.log(Level.INFO, "  Setting up endpoint " + i);
      if (i == 0) {
        try {
          dt[i] = new DummyTransportTrx("martin@example.com", this);
        } catch (IOException ioe) {
          ioe.printStackTrace();
          fail("failed to add martin@example.com");
        }
      } else {
        try {
          dt[i] = new DummyTransportTrx(this);
        } catch(IOException ioe) {
          ioe.printStackTrace();
          fail( "unexpected IOException occurred when setting up endpoints");
        }
      }
    }

    // Test duplicate id generation for transport media
    try {
      new DummyTransportTrx("martin@example.com", this);
      fail("duplicate addition of ID to DummyTransportSender unexpectedly succeeded");
    } catch (IOException ioe) {
      // this is expected behaviour
    }
    for (int i = 0; i < dt.length; i++) {
      dt[i].shutdownDaemon();
    }
    assertTrue("error searching for hangig threads", ImapSSLTest.verifyHangingThreads(threadSet).size() == 0);
    DummyTransportTrx.clearDummyEndpoints();
  }


  @Override
  public boolean gotMessage(InputStream is) {
    synchronized (msgs) {
      msgs.add(is);
    }
    return true;
  }

  public void shutdown() {
  }

}
