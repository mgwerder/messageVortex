package net.messagevortex;

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

import java.io.IOException;
import java.net.InetSocketAddress;
import net.messagevortex.transport.SecurityContext;
import net.messagevortex.transport.SecurityRequirement;
import net.messagevortex.transport.TransportReceiver;
import net.messagevortex.transport.TransportSender;
import net.messagevortex.transport.smtp.SmtpReceiver;

// FIXME this class is not yet functional
public class MessageVortexTransport {

  private TransportReceiver receiver = null;
  private TransportSender   sender   = null;

  /***
   * <p>Creates a message vortex transport layer for local reception.</p>
   *
   * @param receiver      the receiver to be used
   * @throws IOException  if anything fails in setting up the local handler
   */
  public MessageVortexTransport(String section, TransportReceiver receiver) throws IOException {
    if (receiver == null) {
      throw new NullPointerException("TransportReceiver may not be null");
    }

    Config cfg = Config.getDefault();

    String receiverClassName = cfg.getStringValue(section, "transport_endpoint_receiver");
    String senderClassName = cfg.getStringValue(section, "transport_endpoint_Sender");

    // setup sender and receiver
    // FIXME
  }

  public TransportReceiver getTransportReceiver() {
    return this.receiver;
  }

  public TransportReceiver setTransportReceiver(TransportReceiver receiver) {
    return this.receiver=receiver;
  }

  public TransportSender getTransportSender() {
    return this.sender;
  }

  public TransportSender setTransportSender(TransportSender sender) {
    return this.sender=sender;
  }

  public void shutdown() {
    receiver.shutdown();
    sender.shutdown();
  }

}
