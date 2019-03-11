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
import java.util.logging.Level;
import java.util.logging.Logger;
import net.messagevortex.accounting.DummyAccountant;

public class MessageVortex {

  private static final Logger LOGGER;

  static {
    LOGGER = Logger.getLogger((new Throwable()).getStackTrace()[0].getClassName());
  }

  private static MessageVortexTransport transport = null;
  private static MessageVortexBlending blending = null;
  private static MessageVortexRouting routing = null;
  private static MessageVortexAccounting accounting = null;

  private MessageVortex() {
    super();
  }

  public static int help() {
    System.err.println("MessageVortex V" + Version.getBuild());
    System.err.println("===============================================================");
    System.err.println("usage: messageVortex -conf <configfile>");
    return 100;
  }

  public static int main(String[] args) {
    LOGGER.log(Level.INFO, "MessageVortex V" + Version.getBuild());
    if (args != null && args.length > 0 && "--help".equals(args[0])) {
      // output help here
      return help();
    }

    // create config store
    try {
      MessageVortexConfig.getDefault();
    } catch (IOException ioe) {
      LOGGER.log(Level.SEVERE, "Unable to parse config file", ioe);
    }

    try {
      accounting = new MessageVortexAccounting(null);
      routing = new MessageVortexRouting(accounting.getAccountant(), null);
      blending = new MessageVortexBlending(null, routing.getRoutingSender());
      transport = new MessageVortexTransport(null, blending);

      blending.setTransportReceiver(transport.getTransportReceiver());
      routing.setRoutingSender(blending.getRoutingSender());

      // FIXME Use sensible accountant
      accounting.setAccountant(new DummyAccountant());
    } catch (IOException ioe) {
      LOGGER.log(Level.SEVERE, "Exception while setting up transport infrastructure", ioe);
    }

    if (transport != null) {
      transport.shutdown();
    }
    return 0;
  }
}
