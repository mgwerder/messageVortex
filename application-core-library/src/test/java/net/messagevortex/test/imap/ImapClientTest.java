package net.messagevortex.test.imap;

import static net.messagevortex.transport.SecurityRequirement.PLAIN;
import static net.messagevortex.transport.SecurityRequirement.UNTRUSTED_SSLTLS;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.List;
import java.util.Set;
import java.util.Vector;
import java.util.concurrent.TimeoutException;
import java.util.logging.Level;
import javax.net.SocketFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509KeyManager;
import net.messagevortex.ExtendedSecureRandom;
import net.messagevortex.MessageVortexLogger;
import net.messagevortex.transport.AllTrustManager;
import net.messagevortex.transport.CustomKeyManager;
import net.messagevortex.transport.SecurityContext;
import net.messagevortex.transport.SecurityRequirement;
import net.messagevortex.transport.imap.ImapClient;
import net.messagevortex.transport.imap.ImapCommand;
import net.messagevortex.transport.imap.ImapConnection;
import net.messagevortex.transport.imap.ImapServer;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/**
 * Tests for {@link ImapClient}.
 *
 * @author martin@gwerder.net (Martin GWERDER)
 */
@RunWith(JUnit4.class)
public class ImapClientTest {

  private static final java.util.logging.Logger LOGGER;

  static {
    ImapConnection.setDefaultTimeout(2000);
    ImapClient.setDefaultTimeout(2000);
    LOGGER = MessageVortexLogger.getLogger((new Throwable()).getStackTrace()[0].getClassName());
    MessageVortexLogger.setGlobalLogLevel(Level.ALL);
  }

  private ExtendedSecureRandom esr = new ExtendedSecureRandom();

  private static class DeadSocket implements Runnable {
    private boolean shutdown = false;
    private Thread runner = new Thread(this, "Dead socket (init)");

    private ServerSocket ss;
    private int counter;
    private List<Socket> deadSockets = new Vector<>();

    public DeadSocket(int port, int counter) {
      this.counter = counter;
      LOGGER.log(Level.INFO, "starting dead socket");
      try {
        ss = new ServerSocket(port, 20, InetAddress.getByName("localhost"));
      } catch (Exception e) {
      }
      runner.setName("DeadSocket (port " + ss.getLocalPort());
      runner.setDaemon(true);
      runner.start();
      LOGGER.log(Level.INFO, "started dead socket on port " + ss.getLocalPort());
    }

    public void shutdown() {
      // initiate shutdown of runner
      shutdown = true;

      // wakeup runner if necesary
      try {
        SocketFactory.getDefault().createSocket("localhost", ss.getLocalPort());
      } catch (Exception e) {
      }

      // Shutdown runner task
      while (runner.isAlive()) {
        try {
          runner.join();
        } catch (InterruptedException ie) {
        }
      }
    }

    public int getPort() {
      return ss.getLocalPort();
    }

    public void run() {
      while (!shutdown) {
        try {
          Socket s = ss.accept();
          LOGGER.log(Level.INFO, "got connection on dead socket port");
          deadSockets.add(s);
        } catch (Exception sorry) {
          assertTrue("Exception should not be rised", false);
        }
        counter--;
        if (counter == 0) {
          shutdown = true;
        }
      }
      LOGGER.log(Level.INFO, "dead socket shutdown on port " + ss.getLocalPort());
      try {
        ss.close();
      } catch (IOException ioe) {
        LOGGER.log(Level.SEVERE, "Unable to close down dead socket properly", ioe);
      }
      LOGGER.log(Level.INFO, "closing all dead connections");
      for (Socket s : deadSockets) {
        try {
          s.close();
        } catch (IOException ioe) {
          LOGGER.log(Level.SEVERE, "Unable to close down dead socket endpoint properly", ioe);
        }
      }
    }
  }

  @Test
  public void ImapClientEncryptedTest1() {
    try {
      LOGGER.log(Level.INFO, "************************************************************************");
      LOGGER.log(Level.INFO, "IMAP Client Encrypted Test");
      LOGGER.log(Level.INFO, "************************************************************************");
      Set<Thread> threadSet = ImapSSLTest.getThreadList();
      try {
        LOGGER.log(Level.INFO, "starting imap server");
        final SSLContext context = SSLContext.getInstance("TLS");
        String ks = "keystore.jks";
        InputStream stream = this.getClass().getClassLoader().getResourceAsStream(ks);
        assertTrue("Keystore check", (stream != null));
        context.init(new X509KeyManager[]{new CustomKeyManager(ks, "changeme", "mykey3")}, new TrustManager[]{new AllTrustManager()}, esr.getSecureRandom());
        ImapServer is = new ImapServer(new InetSocketAddress("0.0.0.0", 0), new SecurityContext(context, UNTRUSTED_SSLTLS));
        LOGGER.log(Level.INFO, "creating imap client");
        ImapClient ic = new ImapClient(new InetSocketAddress("localhost", is.getPort()), new SecurityContext(context, SecurityRequirement.UNTRUSTED_SSLTLS));
        ic.setTimeout(1000);
        LOGGER.log(Level.INFO, "connecting imap client to server");
        ic.connect();
        LOGGER.log(Level.INFO, "checking TLS status of connection");
        assertTrue("TLS is not as expected", ic.isTls());
        LOGGER.log(Level.INFO, "closing client");
        ic.shutdown();
        is.shutdown();
      } catch (IOException ioe) {
        ioe.printStackTrace();
        fail("IOException while handling client");
      }

      LOGGER.log(Level.INFO, "shutting down server");
      Set<Thread> tl = ImapSSLTest.verifyHangingThreads(threadSet);
      assertTrue("error searching for hanging threads", tl.size() == 0);
    } catch (Exception e) {
      e.printStackTrace();
      fail("Exception while creating server");
    }
  }

  @Test
  public void ImapClientTimeoutTest() {
    LOGGER.log(Level.INFO, "************************************************************************");
    LOGGER.log(Level.INFO, "IMAP Client Timeout Test");
    LOGGER.log(Level.INFO, "************************************************************************");
    Set<Thread> threadSet = ImapSSLTest.getThreadList();
    DeadSocket ds = new DeadSocket(0, -1);
    ImapClient ic = null;
    try {
      ic = new ImapClient(new InetSocketAddress("localhost", ds.getPort()), new SecurityContext(PLAIN));
      ic.connect();
    } catch (IOException ioe) {
      LOGGER.log(Level.SEVERE, "got unexpected IOException", ioe);
      fail("got unexpected IOException");
    }
    assertTrue("ImapClient is unexpectedly null", ic != null);
    assertTrue("TLS is not as expected", !ic.isTls());
    long start = System.currentTimeMillis();
    ImapCommandIWantATimeout ict = new ImapCommandIWantATimeout();
    ict.init();
    try {
      ic.setTimeout(2000);
      for (String s : ic.sendCommand("a0 IWantATimeout", 300)) System.out.println("Reply was: " + s);
      fail("No timeoutException was raised");
    } catch (TimeoutException te) {
      long el = (System.currentTimeMillis() - start);
      assertTrue("Did not wait until end of timeout was reached (just " + el + ")", el >= 300);
      assertFalse("Did wait too long", el > 2100);
    }
    ImapCommand.deregisterCommand("IWantATimeout");
    ict.shutdown();
    try {
      ic.shutdown();
    } catch (IOException ioe) {
      LOGGER.log(Level.SEVERE, "got unexpected IOException while shutting down connection", ioe);
      fail("got unexpected IOException");
    }
    ds.shutdown();
    assertTrue("error searching for hangig threads", ImapSSLTest.verifyHangingThreads(threadSet).size() == 0);
  }

}
