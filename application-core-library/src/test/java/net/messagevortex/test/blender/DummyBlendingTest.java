package net.messagevortex.test.blender;

import net.messagevortex.MessageVortexLogger;
import net.messagevortex.asn1.AsymmetricKey;
import net.messagevortex.asn1.BlendingSpec;
import net.messagevortex.asn1.IdentityBlock;
import net.messagevortex.asn1.IdentityStore;
import net.messagevortex.asn1.InnerMessageBlock;
import net.messagevortex.asn1.PrefixBlock;
import net.messagevortex.asn1.RoutingCombo;
import net.messagevortex.asn1.VortexMessage;
import net.messagevortex.asn1.encryption.Algorithm;
import net.messagevortex.asn1.encryption.SecurityLevel;
import net.messagevortex.blender.BlendingReceiver;
import net.messagevortex.blender.DummyBlender;
import net.messagevortex.test.GlobalJunitExtension;
import net.messagevortex.test.transport.imap.ImapSSLTest;
import net.messagevortex.transport.dummy.DummyTransportTrx;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.parallel.ResourceAccessMode;
import org.junit.jupiter.api.parallel.ResourceLock;
import org.junit.jupiter.api.parallel.Resources;

import java.io.IOException;
import java.net.InetAddress;
import java.util.List;
import java.util.Set;
import java.util.Vector;
import java.util.logging.Level;


/**
 * Created by martin.gwerder on 19.04.2017.
 */
@ExtendWith(GlobalJunitExtension.class)
public class DummyBlendingTest implements BlendingReceiver {
  
  private static final java.util.logging.Logger LOGGER;
  
  static {
    LOGGER = MessageVortexLogger.getLogger((new Throwable()).getStackTrace()[0].getClassName());
  }
  
  private List<VortexMessage> msgs = new Vector<>();
  
  @Test
  @ResourceLock(value = Resources.SYSTEM_PROPERTIES, mode = ResourceAccessMode.READ_WRITE)
  public void dummyBlenderEndpointTest() {
    Set<Thread> threadSet = ImapSSLTest.getThreadList();
    LOGGER.log(Level.INFO, "Setting up dummy network");
    DummyBlender[] dt = new DummyBlender[10];
    IdentityStore store = new IdentityStore();
    for (int i = 0; i < dt.length; i++) {
      LOGGER.log(Level.INFO, "  Setting up endpoint " + i);
      String hostname = null;
      try {
        hostname = InetAddress.getLocalHost().getHostName();
        dt[i] = new DummyBlender("martin@example.com" + i + hostname, this, store);
      } catch (IOException ioe) {
        try {
          Assertions.fail("failed to add martin@example.com" + i + InetAddress.getLocalHost().getHostName());
        } catch(IOException ioe2) {
          Assertions.fail("IOException when fetching hostnames");
        }
      }
    }
    // Test duplicate id generation for transport media
    DummyBlender b = null;
    try {
      b = new DummyBlender("martin@example.com0" + InetAddress.getLocalHost().getHostName(), this, store);
      Assertions.fail("duplicate addition of ID to DummyBlender unexpectedly succeeded");
    } catch (IOException ioe) {
      // this is expected behaviour
    } finally {
      if (b != null) {
        b.shutdownDaemon();
      }
    }
    try {
      b = new DummyBlender("martin@example.com0" + InetAddress.getLocalHost().getHostName(), this, store);
      Assertions.fail("duplicate addition of ID to DummyTransportSender unexpectedly succeeded");
    } catch (IOException ioe) {
      // this is expected behaviour
    } finally {
      if (b != null) {
        b.shutdownDaemon();
      }
    }
    try {
      VortexMessage v = new VortexMessage(new PrefixBlock(), new InnerMessageBlock(new PrefixBlock(), new IdentityBlock(), new RoutingCombo()));
      v.setDecryptionKey(new AsymmetricKey(Algorithm.RSA.getParameters(SecurityLevel.getDefault())));
      Assertions.assertTrue(dt[0].blendMessage(new BlendingSpec("martin@example.com1"+ InetAddress.getLocalHost().getHostName()), v), "Failed sending message to different endpoint");
      Assertions.assertFalse(dt[0].blendMessage(new BlendingSpec("martin@example.com-1"+ InetAddress.getLocalHost().getHostName()), v), "Failed sending message to unknown endpoint (unexpectedly succeeded)");
    } catch (Exception ioe) {
      LOGGER.log(Level.SEVERE, "Caught exception while creating message", ioe);
      Assertions.fail("Endpointests failed as there was an error opening sample messages");
    }
    for (int i = 0; i < dt.length; i++) {
      dt[i].shutdownDaemon();
    }
    Assertions.assertTrue(ImapSSLTest.verifyHangingThreads(threadSet).size() == 0, "error searching for hangig threads");
    DummyTransportTrx.clearDummyEndpoints();
  }
  
  
  @Override
  public boolean gotMessage(VortexMessage message) {
    synchronized (msgs) {
      msgs.add(message);
    }
    return true;
  }
  
}
