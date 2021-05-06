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
import net.messagevortex.transport.RandomString;
import net.messagevortex.transport.SaslMechanisms;
import net.messagevortex.transport.SaslPlainServer;
import net.messagevortex.transport.SaslServerCallbackHandler;
import org.bouncycastle.util.encoders.Base64;

import javax.security.auth.callback.CallbackHandler;
import javax.security.sasl.Sasl;
import javax.security.sasl.SaslException;
import javax.security.sasl.SaslServer;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.Security;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeoutException;
import java.util.logging.Level;
import java.util.logging.Logger;

/***
 * <p>Provides the the Authenticate command to the IMAP server.</p>
 *
 * @author Martin Gwerder
 * @version 1.0
 * @since 2014-12-09
 */
public class ImapCommandAuthenticate extends ImapCommand {

  private static final Logger LOGGER;

  static {
    LOGGER = MessageVortexLogger.getLogger((new Throwable()).getStackTrace()[0].getClassName());
  }

  /***
   * <p>Initializer called by the static constructor of ImapCommand.</p>
   */
  public void init() {
    ImapCommandFactory.registerCommand(this);
  }

  public static String getChallenge(int length) {
    return RandomString.nextString(length);
  }

  /***
   * <p>process authentication command.</p>
   *
   * @param  line           The context of the line triggered
   * @throws ImapException  when problem processing the command
   */
  public String[] processCommand(ImapLine line) throws ImapException {
    // register plain server provider
    Security.addProvider(new SaslPlainServer.SecurityProvider());

    // get mech
    String mech = line.getATag();
    LOGGER.log(Level.INFO, "authenticate has read mec " + mech);

    // skip space
    // WARNING this is "non-strict"
    line.skipWhitespace(-1);

    String context = line.getATag();
    LOGGER.log(Level.INFO, "authenticate has read context information (PLAIN only) \"" + context
            + "\"");

    // skip line end
    if (!line.skipLineEnd()) {
      throw new ImapException(line, "error parsing command");
    }
    LOGGER.log(Level.INFO, "has parsed last character of line");

    if (line.getConnection() == null) {
      LOGGER.log(Level.SEVERE, "no connection found while calling login");
      return new String[]{line.getTag() + " BAD server configuration error\r\n"};
    } else if (line.getConnection().getAuth() == null) {
      LOGGER.log(Level.SEVERE, "no Authenticator or connection found while calling login (2)");
      return new String[]{line.getTag() + " BAD server configuration error\r\n"};
    }
    // create sasl server
    CallbackHandler serverHandler = new SaslServerCallbackHandler(line.getConnection().getAuth());

    SaslServer ss = null;
    try {
      Map<String, String> props = new HashMap<>();
      if (!line.getConnection().isTls()) {
        props.put(Sasl.POLICY_NOPLAINTEXT, "true");
      }
      // FIXME add possibility to add realm
      props.put("com.sun.security.sasl.digest.realm", "theRealm");
      ss = Sasl.createSaslServer(mech, "IMAP", "FQHN", props, serverHandler);
    } catch (SaslException e) {
      LOGGER.log(Level.WARNING, "unsuported sasl mech " + mech + " requested by client (2)", e);
      return new String[]{line.getTag() + " BAD server configuration error\r\n"};
    } catch (Exception e) {
      LOGGER.log(Level.WARNING, "got unexpected exception", e);
      throw e;
    }
    if (ss == null) {
      LOGGER.log(Level.WARNING, "unsuported sasl mech " + mech + " requested by client (1)");
      return new String[]{line.getTag() + " BAD server configuration error\r\n"};
    }
    // send challenge
    LOGGER.log(Level.INFO, "preparing challenge");
    byte[] saslChallenge = null;
    byte[] saslReply = null;
    try {
      if (context != null && SaslMechanisms.PLAIN.toString().equals(mech)) {
        saslReply = Base64.decode(context);
      } else {
        saslChallenge = ss.evaluateResponse(new byte[0]);
        LOGGER.log(Level.INFO, "sending challenge");
        if (saslChallenge.length > 0) {
          LOGGER.log(Level.INFO, "sending challenge (" + saslChallenge.length + " bytes; "
                  + new String(Base64.encode(saslChallenge), StandardCharsets.UTF_8) + ")");
          line.getConnection().writeln("+ "
                  + new String(Base64.encode(saslChallenge), StandardCharsets.UTF_8));
        } else {
          LOGGER.log(Level.INFO, "sending empty challenge");
          line.getConnection().writeln("+ ");
        }
        LOGGER.log(Level.INFO, "getting reply");
        try {
          String reply = line.getConnection().readln(300000);
          LOGGER.log(Level.INFO, "got reply (" + reply + ")");
          saslReply = Base64.decode(reply);
        } catch (TimeoutException te) {
          throw new IOException("Tmeout while wating for sasl challenge reply", te);
        }
      }

      // verify reply
      LOGGER.log(Level.INFO, "evaluating reply");
      ss.evaluateResponse(saslReply);
      LOGGER.log(Level.INFO, "reply evaluated");

      if (ss.isComplete()) {
        LOGGER.log(Level.INFO, "Sucessfully authenticated");
        line.getConnection().setImapState(ImapConnectionState.CONNECTION_AUTHENTICATED);
        return new String[]{line.getTag() + " OK LOGIN completed\r\n"};
      } else {
        LOGGER.log(Level.INFO, "Bad username or password");
        return new String[]{line.getTag() + " BAD login failed\r\n"};
      }
    } catch (IOException e) {
      LOGGER.log(Level.WARNING, "failed processing sasl reply");
      return new String[]{line.getTag() + " BAD login failed\r\n"};
    }
  }

  /***
   * <p>Returns the Identifier (IMAP command) which are processed by this class.</p>
   *
   * @return A list of identifiers
   */
  public String[] getCommandIdentifier() {
    return new String[]{"AUTHENTICATE"};
  }

  private String getAuthToken(ImapLine line) throws ImapException {
    String userid = line.getAString();
    if (userid == null) {
      throw new ImapException(line, "error parsing command (getting userid)");
    }
    return userid;
  }

  @Override
  public String[] getCapabilities(ImapConnection ic) {
    if (ic == null || ic.getImapState() == ImapConnectionState.CONNECTION_NOT_AUTHENTICATED) {
      if (ic != null && ic.isTls()) {
        return new String[]{"AUTH=CRAM-MD5", "AUTH=PLAIN"};
      } else {
        return new String[]{"AUTH=CRAM-MD5"};
      }
    } else {
      return new String[0];
    }
  }

}
