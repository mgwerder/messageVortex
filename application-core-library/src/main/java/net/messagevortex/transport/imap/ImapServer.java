package net.messagevortex.transport.imap;

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

import static net.messagevortex.transport.SecurityRequirement.SSLTLS;
import static net.messagevortex.transport.SecurityRequirement.UNTRUSTED_SSLTLS;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.logging.Level;
import java.util.logging.Logger;
import net.messagevortex.MessageVortexLogger;
import net.messagevortex.transport.AuthenticationProxy;
import net.messagevortex.transport.ListeningSocketChannel;
import net.messagevortex.transport.SecurityContext;
import net.messagevortex.transport.ServerConnection;
import net.messagevortex.transport.SocketListener;
import net.messagevortex.transport.StoppableThread;


public class ImapServer extends ListeningSocketChannel implements StoppableThread, SocketListener {

  private static final Logger LOGGER;
  private static int id = 1;
  private long gcLastRun = 0;
  private Set<ImapConnection> connSet = new ConcurrentSkipListSet<>();

  static {
    LOGGER = MessageVortexLogger.getLogger((new Throwable()).getStackTrace()[0].getClassName());
  }

  private AuthenticationProxy auth = null;
  private static long defaultTimeout = 10000;
  private long timeout = defaultTimeout;

  /***
   * <p>Creates an IMAP server listening the default port on all interfaces of the server.</p>
   *
   * @param secContext the security context for the server
   *
   * @throws IOException if socket binding fails
   */
  public ImapServer(SecurityContext secContext) throws IOException {
    super(new InetSocketAddress(InetAddress.getByAddress(new byte[]{0, 0, 0, 0}),
            (
                    secContext.getRequirement() == UNTRUSTED_SSLTLS
                    || secContext.getRequirement() == SSLTLS ? 993 : 143)
            ), null);

    setSocketListener(this);
    setSecurityContext(secContext);
    setProtocol("imap");
    setName("IMAPlisten-" + (id++));
  }

  /***
   * <p>Creates an IMAP server listening on the specified socket address.</p>
   *
   * @param addr the socket address to be used by the server
   * @param enc the security context for the server
   *
   * @throws IOException if socket binding fails
   */
  public ImapServer(InetSocketAddress addr, SecurityContext enc) throws IOException {
    super(addr, null);
    setSocketListener(this);
    setSecurityContext(enc);
    setProtocol("imap");
    setName("IMAPlisten-" + (id++));
  }

  /***
   * <p>Sets the authentication proxy for incoming connections.</p>
   *
   * @param ap the new proxy
   * @return the previously set proxy
   */
  public AuthenticationProxy setAuth(AuthenticationProxy ap) {
    AuthenticationProxy old = auth;
    auth = ap;
    return old;
  }

  @Override
  public void gotConnect(ServerConnection ac) {
    try {
      doGarbageCollection(false);
      LOGGER.log(Level.INFO, "got new connection");
      ImapConnection ic = new ImapConnection(ac, auth);
      ic.setTimeout(getTimeout());
      connSet.add(ic);
      LOGGER.log(Level.INFO, "inbound connection ready for use");
    } catch (IOException ioe) {
      LOGGER.log(Level.WARNING, "got exception while initial Handshake", ioe);
    }
  }

  private void doGarbageCollection(boolean force) {
    if (force || gcLastRun + 10000 < System.currentTimeMillis()) {
      LOGGER.log(Level.INFO, "Running garbage collector for connections");
      // running GC on set of connections
      Set<ImapConnection> tmp = new HashSet<>();
      for (ImapConnection ic : connSet) {
        if (ic.isShutdown()) {
          tmp.add(ic);
        }
      }
      connSet.removeAll(tmp);
      LOGGER.log(Level.INFO, "garbage collector removed " + tmp.size() + " connections ("
              + connSet.size() + " remaining)");
      gcLastRun = System.currentTimeMillis();
    }
  }

  @Override
  public boolean isShutdown() {
    doGarbageCollection(true);
    return super.isShutdown() && connSet.size() == 0;
  }

  @Override
  public void shutdown() {
    super.shutdown();
    doGarbageCollection(true);
    for (ImapConnection ic : connSet) {
      try {
        ic.shutdown();
      } catch (IOException ioe) {
        // safe to ignore as we shut dow all connects anyway
      }
    }
    // cleanup set
    doGarbageCollection(true);
  }

  /***
   * <p>Gets the timeout for new incoming connections.</p>
   *
   * @return the currently set timeout
   */
  public long getTimeout() {
    return timeout;
  }

  /***
   * <p>Sets the timeout for new incoming connections.</p>
   *
   * @param timeout the timeout in milli seconds
   * @return the previously set timeout
   */
  public long setTimeout(long timeout) {
    long ret = this.timeout;
    this.timeout = timeout;
    return ret;
  }
}


