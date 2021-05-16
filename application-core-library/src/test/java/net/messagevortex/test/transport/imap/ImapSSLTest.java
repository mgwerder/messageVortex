package net.messagevortex.test.transport.imap;
// ************************************************************************************
// * Copyright (c) 2018 Martin Gwerder (martin@gwerder.net)
// *
// * Permission is hereby granted, free of charge, to any person obtaining a copy
// * of this software and associated documentation files (the "Software"), to deal
// * in the Software without restriction, including without limitation the rights
// * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
// * copies of the Software, and to permit persons to whom the Software is
// * furnished to do so, subject to the following conditions:
// *
// * The above copyright notice and this permission notice shall be included in all
// * copies or substantial portions of the Software.
// *
// * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
// * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
// * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
// * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
// * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
// * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
// * SOFTWARE.
// ************************************************************************************

import net.messagevortex.ExtendedSecureRandom;
import net.messagevortex.MessageVortex;
import net.messagevortex.MessageVortexLogger;
import net.messagevortex.test.GlobalJunitExtension;
import net.messagevortex.test.transport.SMTPTransportSenderTest;
import net.messagevortex.transport.AllTrustManager;
import net.messagevortex.transport.CustomKeyManager;
import net.messagevortex.transport.SecurityContext;
import net.messagevortex.transport.SecurityRequirement;
import net.messagevortex.transport.SocketDeblocker;
import net.messagevortex.transport.imap.ImapClient;
import net.messagevortex.transport.imap.ImapLine;
import net.messagevortex.transport.imap.ImapServer;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.parallel.ResourceAccessMode;
import org.junit.jupiter.api.parallel.ResourceLock;
import org.junit.jupiter.api.parallel.Resources;

import javax.net.ssl.*;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.security.KeyStore;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;


/**
 * Tests for {@link MessageVortex}.
 *
 * @author martin@gwerder.net (Martin GWERDER)
 */
@ExtendWith(GlobalJunitExtension.class)
public class ImapSSLTest {

  private static final java.util.logging.Logger LOGGER;
  private static final ExtendedSecureRandom esr = new ExtendedSecureRandom();

  static {
    LOGGER = MessageVortexLogger.getLogger((new Throwable()).getStackTrace()[0].getClassName());
  }

  @Test
  @ResourceLock(value = Resources.SYSTEM_PROPERTIES, mode = ResourceAccessMode.READ_WRITE)
  public void testInitalSSLClient() {
    try {
      LOGGER.log(Level.INFO, "************************************************************************");
      LOGGER.log(Level.INFO, "Testing SSL handshake by client");
      LOGGER.log(Level.INFO, "************************************************************************");

      String ks = "keystore.jks";
      InputStream stream = this.getClass().getClassLoader().getResourceAsStream(ks);
      Assertions.assertTrue((stream != null), "Keystore check");
      KeyStore keyStore = KeyStore.getInstance(KeyStore.getDefaultType());
      keyStore.load(stream, "changeme".toCharArray());
      stream.close();
      TrustManagerFactory tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
      tmf.init(keyStore);
      final SSLContext context = SSLContext.getInstance("TLS");
      KeyManagerFactory kmf = KeyManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
      kmf.init(keyStore, "changeme".toCharArray());
      context.init(kmf.getKeyManagers(), tmf.getTrustManagers(), esr.getSecureRandom());
      SSLContext.setDefault(context);

      Set<String> suppCiphers = new HashSet<>();
      String[] arr = ((SSLServerSocketFactory) SSLServerSocketFactory.getDefault()).getSupportedCipherSuites();
      LOGGER.log(Level.FINE, "Detecting supported cipher suites");
      Set<Thread> threadSet = getThreadList();
      for (int i = 0; i < arr.length; i++) {
        boolean supported = true;
        ServerSocket serverSocket = null;
        try {
          serverSocket = SSLServerSocketFactory.getDefault().createServerSocket(0);
          ((SSLServerSocket) serverSocket).setEnabledCipherSuites(new String[]{arr[i]});
          SocketDeblocker t = new SocketDeblocker(serverSocket.getLocalPort(), 30);
          t.start();
          SSLSocket s = (SSLSocket) serverSocket.accept();
          s.setSoTimeout(1000);
          s.close();
          serverSocket.close();
          serverSocket = null;
          t.shutdown();
          LOGGER.log(Level.INFO, "Cipher suite \"" + arr[i] + "\" seems to be supported");
        } catch (SSLException e) {
          LOGGER.log(Level.INFO, "Cipher suite \"" + arr[i] + "\" seems to be unsupported", e);
          supported = false;
          try {
            serverSocket.close();
          } catch (Exception e2) {
            LOGGER.log(Level.FINEST, "cleanup failed (never mind)", e2);
          }
          serverSocket = null;
        }
        if (supported) {
          suppCiphers.add(arr[i]);
        }
      }
      final ServerSocket ss = SSLServerSocketFactory.getDefault().createServerSocket(0);
      ((SSLServerSocket) (ss)).setEnabledCipherSuites(suppCiphers.toArray(new String[suppCiphers.size()]));
      (new Thread() {
        public void run() {
          try {
            SSLContext.setDefault(context);
            LOGGER.log(Level.INFO, "pseudoserver waiting for connect");
            Socket s = ss.accept();
            LOGGER.log(Level.INFO, "pseudoserver waiting for command");
            s.getInputStream().skip(9);
            LOGGER.log(Level.INFO, "pseudoserver sending reply");
            s.getOutputStream().write("a1 OK\r\n".getBytes(Charset.defaultCharset()));
            LOGGER.log(Level.INFO, "pseudoserver closing");
            s.close();
            ss.close();
          } catch (IOException ioe) {
            LOGGER.log(Level.WARNING, "Unexpected Exception", ioe);
            Assertions.fail("Exception risen in server (" + ioe + ") while communicating");
          }
        }
      }).start();

      ImapClient ic = new ImapClient(new InetSocketAddress("localhost", ss.getLocalPort()), new SecurityContext(context, SecurityRequirement.UNTRUSTED_SSLTLS));
      ic.setTimeout(1000);
      ic.connect();
      ic.sendCommand("a1 test");
      Assertions.assertTrue(ic.isTls(), "check client socket state");
      ic.shutdown();

      // Self test
      Assertions.assertTrue(verifyHangingThreads(threadSet).size() == 0, "error searching for hangig threads");
    } catch (Exception ioe) {
      LOGGER.log(Level.WARNING, "Unexpected Exception", ioe);
      Assertions.fail("Exception rised  in client(" + ioe + ") while communicating");
    } finally {
      MessageVortexLogger.flush();
    }
  }

  @Test
  @ResourceLock(value = Resources.SYSTEM_PROPERTIES, mode = ResourceAccessMode.READ_WRITE)
  public void testInitalSSLServer() {
    try {
      LOGGER.log(Level.INFO, "************************************************************************");
      LOGGER.log(Level.INFO, "Testing SSL handshake by server");
      LOGGER.log(Level.INFO, "************************************************************************");
      LOGGER.log(Level.INFO, "setting up server");
      Set<Thread> threadSet = getThreadList();

      final SSLContext context = SSLContext.getInstance("TLS");
      String ks = "keystore.jks";
      InputStream stream = this.getClass().getClassLoader().getResourceAsStream(ks);
      Assertions.assertTrue((stream != null), "Keystore check");
      context.init(new X509KeyManager[]{new CustomKeyManager(ks, "changeme", "mykey3")}, new TrustManager[]{new AllTrustManager()}, esr.getSecureRandom());
      SSLContext.setDefault(context);

      SecurityContext secContext = new SecurityContext(context, SecurityRequirement.UNTRUSTED_SSLTLS);
      ImapServer is = new ImapServer(new InetSocketAddress("0.0.0.0", 0), secContext);
      is.setTimeout(4000);
      LOGGER.log(Level.INFO, "setting up pseudo client");
      Socket s = SSLSocketFactory.getDefault().createSocket(InetAddress.getByName("localhost"), is.getPort());
      s.setSoTimeout(4000);
      LOGGER.log(Level.INFO, "sending command to  port " + is.getPort());
      s.getOutputStream().write("a1 capability\r\n".getBytes(StandardCharsets.UTF_8));
      s.getOutputStream().flush();
      LOGGER.log(Level.INFO, "sent... waiting for reply");
      StringBuilder sb = new StringBuilder();
      int start = 0;
      while (!sb.toString().endsWith("a1 OK" + SMTPTransportSenderTest.CRLF)) {
        byte[] b = new byte[1];
        int numread = s.getInputStream().read(b, 0, b.length);
        if (numread > 0) {
          start += numread;
          sb.append((char) (b[0]));
          LOGGER.log(Level.INFO, "got " + start + " bytes (" + sb + ")");
        }
      }
      LOGGER.log(Level.INFO, "got sequence \"" + sb + "\"");
      s.close();
      is.shutdown();
      Assertions.assertTrue(verifyHangingThreads(threadSet).size() == 0, "error searching for hangig threads");
    } catch (Exception ioe) {
      LOGGER.log(Level.WARNING, "Unexpected Exception", ioe);
      Assertions.fail("Exception rised  in client(" + ioe + ") while communicating");
    }
  }

  @Test
  @ResourceLock(value = Resources.SYSTEM_PROPERTIES, mode = ResourceAccessMode.READ_WRITE)
  public void testInitalSSLBoth() {
    try {
      LOGGER.log(Level.INFO, "************************************************************************");
      LOGGER.log(Level.INFO, "Testing initial SSL handshake for both components");
      LOGGER.log(Level.INFO, "************************************************************************");
      Set<Thread> threadSet = getThreadList();
      final SSLContext context = SSLContext.getInstance("TLS");
      String ks = "keystore.jks";
      InputStream stream = this.getClass().getClassLoader().getResourceAsStream(ks);
      Assertions.assertTrue((stream != null), "Keystore check");
      context.init(new X509KeyManager[]{new CustomKeyManager(ks, "changeme", "mykey3")}, new TrustManager[]{new AllTrustManager()}, esr.getSecureRandom());
      ImapServer is = new ImapServer(new InetSocketAddress("0.0.0.0", 0), new SecurityContext(context, SecurityRequirement.UNTRUSTED_SSLTLS));
      is.setTimeout(10000);
      ImapClient ic = new ImapClient(new InetSocketAddress("localhost", is.getPort()), new SecurityContext(context, SecurityRequirement.UNTRUSTED_SSLTLS));
      ic.setTimeout(10000);
      ImapClient.setDefaultTimeout(3000);
      LOGGER.log(Level.INFO, "IMAP<- C: <CONNECTING>");
      ic.connect();
      LOGGER.log(Level.INFO, "IMAP<- C: sending \"a1 capability\"");
      String[] s = ic.sendCommand("a1 capability");
      LOGGER.log(Level.INFO, "IMAP<- C: <GOT COMPLETE REPLY>");
      for (String v : s) {
        LOGGER.log(Level.INFO, "IMAP<- C: " + ImapLine.commandEncoder(v));
      }
      LOGGER.log(Level.INFO, "closing server");
      is.shutdown();
      LOGGER.log(Level.INFO, "closing client");
      ic.shutdown();
      LOGGER.log(Level.INFO, "done");
      Thread.sleep(300);
      Assertions.assertTrue(verifyHangingThreads(threadSet).size() == 0, "error searching for hangig threads");
    } catch (Exception ioe) {
      LOGGER.log(Level.WARNING, "Unexpected Exception", ioe);
      ioe.printStackTrace();
      Assertions.fail("Exception rised  in client(" + ioe + ") while communicating");
    }
  }

  public static Set<Thread> getThreadList() {
    Set<Thread> cThread = Thread.getAllStackTraces().keySet();
    ArrayList<Thread> al = new ArrayList<>();
    for (Thread t : cThread) {
      if (!t.isAlive() || t.isDaemon() || t.getState() == Thread.State.TERMINATED) {
        al.add(t);
      }
    }
    cThread.removeAll(al);
    return cThread;
  }

  public static Set<Thread> verifyHangingThreads(Set<Thread> pThread) {
    Set<Thread> cThread = Thread.getAllStackTraces().keySet();
    cThread.removeAll(pThread);
    ArrayList<Thread> al = new ArrayList<>();
    for (Thread t : cThread) {
      // protection from spurious non-daemon threads
      boolean gotMessageVortex = false;
      for (StackTraceElement ste : t.getStackTrace()) {
        if (ste.toString().contains(" at net.messagevortex")) {
          gotMessageVortex = true;
        }
      }
      if (gotMessageVortex && t.isAlive() && !t.isDaemon() && t.getState() != Thread.State.TERMINATED && !pThread.contains(t)) {
        // keep in set and issue logger message
        LOGGER.log(Level.SEVERE, "Error got new thread " + t.getName());
      } else {
        // add for removal from set
        al.add(t);
      }
    }
    cThread.removeAll(al);
    for (Thread t : cThread) {
      LOGGER.log(Level.WARNING, "" + t.getName() );
      for (StackTraceElement ste : t.getStackTrace()) {
        LOGGER.log(Level.WARNING, "  " + ste.toString() );
      }
    }
    return cThread;
  }

}
