package net.messagevortex.transport.smtp;

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

import net.messagevortex.MessageVortexLogger;
import net.messagevortex.transport.ListeningSocketChannel;
import net.messagevortex.transport.SecurityContext;
import net.messagevortex.transport.ServerConnection;
import net.messagevortex.transport.SocketListener;
import net.messagevortex.transport.TransportReceiver;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.logging.Level;
import java.util.logging.Logger;

public class SmtpReceiver implements SocketListener {

  private static final Logger LOGGER;

  static {
    LOGGER = MessageVortexLogger.getLogger((new Throwable()).getStackTrace()[0].getClassName());
  }

  private static volatile int gid = 1;
  private volatile int id = 1;

  private final ListeningSocketChannel listener;
  private TransportReceiver receiver = null;
  private SecurityContext context = null;

  /***
   * <p>creates a local SMTP server.</p>
   *
   * @param address the adress to be bound
   * @param secContext the security context to be used
   * @param lreceiver the blending layer to be used
   * @throws IOException if interfaces cannot be bound
   */
  public SmtpReceiver(InetSocketAddress address, SecurityContext secContext,
                      TransportReceiver lreceiver) throws IOException {
    setTransportReceiver(lreceiver);
    listener = new ListeningSocketChannel(address, this);
    listener.setName("SMTPlist" + (gid++));
    this.context = secContext;
  }

  @Override
  public void gotConnect(ServerConnection ac) {
    LOGGER.log(Level.INFO, "called gotConnection()");
    try {
      SmtpConnection s = new SmtpConnection(ac.getSocketChannel(), context,null);
      s.setName(listener.getName() + "-" + (id++));
      s.setReceiver(this.receiver);
    } catch (IOException ioe) {
      LOGGER.log(Level.WARNING, "Exception while creating SmtpConnection object", ioe);
    }
  }

  public TransportReceiver getTransportReceiver() {
    return receiver;
  }

  /***
   * <p>Sets the belnding layer to be used.</p>
   *
   * @param lreceiver the blending layer to be set
   * @return the previously set blending layer
   */
  public final TransportReceiver setTransportReceiver(TransportReceiver lreceiver) {
    TransportReceiver ret = this.receiver;
    this.receiver = lreceiver;
    return ret;
  }

  public void shutdown() {
    listener.shutdown();
  }

  public int getPort() {
    return listener.getPort();
  }
}
