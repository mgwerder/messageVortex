package net.gwerder.java.messagevortex;

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
import net.gwerder.java.messagevortex.transport.SecurityContext;
import net.gwerder.java.messagevortex.transport.SecurityRequirement;
import net.gwerder.java.messagevortex.transport.TransportReceiver;
import net.gwerder.java.messagevortex.transport.smtp.SmtpReceiver;


public class MessageVortexTransport {

  private SmtpReceiver inSmtp;

  public MessageVortexTransport(TransportReceiver receiver) throws IOException {
    if (receiver == null) {
      throw new NullPointerException("TransportReceiver may not be null");
    }

    Config cfg = Config.getDefault();
    assert cfg != null;

    // setup receiver for mail relay
    inSmtp = new SmtpReceiver(new InetSocketAddress(cfg.getStringValue("smtp_incomming_address"),
            cfg.getNumericValue("smtp_incomming_port")),
            new SecurityContext(
                    SecurityRequirement.getByName(cfg.getStringValue("smtp_incomming_address"))
            ), receiver);

    // setup receiver for IMAP requests
    // FIXME
  }

  public TransportReceiver getTransportReceiver() {
    return this.inSmtp.getTransportReceiver();
  }

  public TransportReceiver setTransportReceiver(TransportReceiver receiver) {
    return this.inSmtp.setTransportReceiver(receiver);
  }

  public void shutdown() {
    inSmtp.shutdown();
  }

}
