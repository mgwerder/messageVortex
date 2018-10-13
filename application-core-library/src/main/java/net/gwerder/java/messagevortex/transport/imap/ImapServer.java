package net.gwerder.java.messagevortex.transport.imap;

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

import static net.gwerder.java.messagevortex.transport.SecurityRequirement.SSLTLS;
import static net.gwerder.java.messagevortex.transport.SecurityRequirement.UNTRUSTED_SSLTLS;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.logging.Level;
import java.util.logging.Logger;
import net.gwerder.java.messagevortex.MessageVortexLogger;
import net.gwerder.java.messagevortex.transport.AuthenticationProxy;
import net.gwerder.java.messagevortex.transport.ListeningSocketChannel;
import net.gwerder.java.messagevortex.transport.SecurityContext;
import net.gwerder.java.messagevortex.transport.ServerConnection;
import net.gwerder.java.messagevortex.transport.SocketListener;
import net.gwerder.java.messagevortex.transport.StoppableThread;


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

  public ImapServer(InetSocketAddress addr, SecurityContext enc) throws IOException {
    super(addr, null);
    setSocketListener(this);
    setSecurityContext(enc);
    setProtocol("imap");
    setName("IMAPlisten-" + (id++));
  }

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

  public long getTimeout() {
    return timeout;
  }

  public long setTimeout(long timeout) {
    long ret = this.timeout;
    this.timeout = timeout;
    return ret;
  }
}


