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

import net.messagevortex.MessageVortexLogger;

import java.util.logging.Level;


public class ImapCommandLogout extends ImapCommand {

  private static final java.util.logging.Logger LOGGER;

  static {
    LOGGER = MessageVortexLogger.getLogger((new Throwable()).getStackTrace()[0].getClassName());
  }

  @Override
  public String[] processCommand(ImapLine line) throws ImapException {
    // skip space
    // WARNING this is "non-strict"
    line.skipWhitespace(-1);

    // skip lineend
    if (!line.skipLineEnd()) {
      throw new ImapException(line, "error parsing command");
    }

    // set connection state to "not connected"
    if (line.getConnection() != null) {
      line.getConnection().setImapState(ImapConnectionState.CONNECTION_NOT_AUTHENTICATED);
    }
    LOGGER.log(Level.INFO, Thread.currentThread().getName() + " is now in state NOT_AUTHENTICATED");

    // Return standard message
    return new String[]{"* BYE IMAP4rev1 Server logged out\r\n", line.getTag() + " OK\r\n", null};
  }

  public String[] getCommandIdentifier() {
    return new String[]{"LOGOUT"};
  }

  public String[] getCapabilities(ImapConnection conn) {
    return new String[]{};
  }

}
