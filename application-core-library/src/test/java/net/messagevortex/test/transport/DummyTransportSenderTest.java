package net.messagevortex.test.transport;

import net.messagevortex.AbstractDaemon;
import net.messagevortex.MessageVortexLogger;
import net.messagevortex.test.GlobalJunitExtension;
import net.messagevortex.test.transport.imap.ImapSSLTest;
import net.messagevortex.transport.TransportReceiver;
import net.messagevortex.transport.dummy.DummyTransportTrx;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.parallel.*;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Set;
import java.util.Vector;
import java.util.logging.Level;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * Created by martin.gwerder on 19.04.2017.
 */
@ExtendWith(GlobalJunitExtension.class)
public class DummyTransportSenderTest extends AbstractDaemon implements TransportReceiver {

  private List<InputStream> msgs = new Vector<>();
  private static final java.util.logging.Logger LOGGER;

  static {
    LOGGER = MessageVortexLogger.getLogger((new Throwable()).getStackTrace()[0].getClassName());
  }

  @Test
  @ResourceLock(value = Resources.SYSTEM_PROPERTIES, mode = ResourceAccessMode.READ_WRITE)
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
          Assertions.fail("failed to add martin@example.com");
        }
      } else {
        try {
          dt[i] = new DummyTransportTrx(this);
        } catch(IOException ioe) {
          ioe.printStackTrace();
          Assertions.fail("unexpected IOException occurred when setting up endpoints");
        }
      }
    }

    // Test duplicate id generation for transport media
    try {
      new DummyTransportTrx("martin@example.com", this);
      Assertions.fail("duplicate addition of ID to DummyTransportSender unexpectedly succeeded");
    } catch (IOException ioe) {
      // this is expected behaviour
    }
    for (int i = 0; i < dt.length; i++) {
      dt[i].shutdownDaemon();
    }
    Assertions.assertTrue(ImapSSLTest.verifyHangingThreads(threadSet).size() == 0, "error searching for hangig threads");
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
