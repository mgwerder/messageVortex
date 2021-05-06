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
import java.util.logging.Logger;

public class ImapCommandLogin extends ImapCommand {

  private static final Logger LOGGER;

  static {
    LOGGER = MessageVortexLogger.getLogger((new Throwable()).getStackTrace()[0].getClassName());
  }

  private String getAuthToken(ImapLine line) throws ImapException {
    String userid = line.getAString();
    if (userid == null) {
      throw new ImapException(line, "error parsing command (getting userid)");
    }
    return userid;
  }

  /***
   * <p>Process the login command.</p>
   *
   * @param line The Imap line representing a login command
   * @return array of lines representing the server reply
   * @throws ImapException For all parsing errors
   */
  public String[] processCommand(ImapLine line) throws ImapException {

    // get userid
    final String userid = getAuthToken(line);

    // skip space after command
    if (line.skipWhitespace(1) != 1) {
      throw new ImapException(line, "error parsing command (skipping to password)");
    }

    // get password
    final String password = getAuthToken(line);

    // skip space
    // WARNING this is "non-strict"
    line.skipWhitespace(-1);

    // skip line end
    if (!line.skipLineEnd()) {
      throw new ImapException(line, "error parsing command");
    }

    String[] reply = null;

    if (line.getConnection() == null) {
      LOGGER.log(Level.SEVERE, "no connection found while calling login");
      reply = new String[]{line.getTag() + " BAD server configuration error\r\n"};
    } else if (!line.getConnection().isTls()) {
      LOGGER.log(Level.SEVERE, "no TLS but logging in with username and password");
      reply = new String[]{line.getTag() + " BAD authentication with username and password "
              + "refused due current security strength\r\n"};
    } else if (line.getConnection().getAuth() == null) {
      LOGGER.log(Level.SEVERE, "no Authenticator found while calling login (1)");
      reply = new String[]{line.getTag() + " BAD server configuration error\r\n"};
    } else if (line.getConnection().getAuth().login(userid, password)) {
      line.getConnection().setImapState(ImapConnectionState.CONNECTION_AUTHENTICATED);
      reply = new String[]{line.getTag() + " OK LOGIN completed\r\n"};
    } else {
      reply = new String[]{line.getTag() + " NO bad username or password\r\n"};
    }
    return reply;
  }

  public String[] getCapabilities() {
    return getCapabilities(null);
  }

  @Override
  public String[] getCapabilities(ImapConnection ic) {
    if (ic != null && ic.isTls()) {
      return new String[]{"LOGIN"};
    } else {
      return new String[]{"LOGINDISABLED"};
    }
  }

  @Override
  public String[] getCommandIdentifier() {
    return new String[]{"LOGIN"};
  }

}
