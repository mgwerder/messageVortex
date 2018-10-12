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

import static java.lang.System.exit;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.text.ParseException;
import java.util.logging.Level;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.gwerder.java.messagevortex.MessageVortexLogger;
import net.gwerder.java.messagevortex.MessageVortexStatus;
import net.gwerder.java.messagevortex.transport.AuthenticationProxy;
import net.gwerder.java.messagevortex.transport.Credentials;
import net.gwerder.java.messagevortex.transport.SecurityContext;
import net.gwerder.java.messagevortex.transport.SecurityRequirement;
import sun.misc.Signal;
import sun.misc.SignalHandler;

public class ImapPassthruServer implements SignalHandler {

  private static final java.util.logging.Logger LOGGER;

  static {
    LOGGER = MessageVortexLogger.getLogger((new Throwable()).getStackTrace()[0].getClassName());
  }

  private static final String imapUrl = "(?<protocol>imap[s]?)://"
          + "(?:(?<username>[\\p{Alnum}\\-\\.]+):(?<password>[\\p{ASCII}&&[^@]]+)@)?"
          + "(?<server>[\\p{Alnum}\\.\\-]+)(?::(?<port>[\\digit]{1,5}))?";

  private ImapServer localServer;
  private ImapClient remoteServer;

  public ImapPassthruServer(InetSocketAddress listeningAddress, SecurityContext context,
                            Credentials listeningCredentials, InetSocketAddress forwardingServer,
                            Credentials forwardingCredentials) throws IOException {
    localServer = new ImapServer(listeningAddress, context);
    AuthenticationProxy authProxy = new AuthenticationProxy();
    authProxy.addCredentials(listeningCredentials);
    localServer.setAuth(authProxy);
    remoteServer = new ImapClient(forwardingServer, context);
    Signal.handle(new Signal("INT"), this);
  }

  public void handle(Signal sig) {

    if ("INT".equals(sig.getName())) {
      LOGGER.log(Level.INFO, "Received SIGINT signal. Will teardown.");

      try {
        shutdown();
      } catch (IOException ioe) {
        LOGGER.log(Level.WARNING, "caught exception while shutting down", ioe);
      }

      // Force exit anyway
      System.exit(0);
    } else {
      LOGGER.log(Level.WARNING, "Received unthandöed signal SIG" + sig.getName() + ". IGNORING");
    }
  }

  public void shutdown() throws IOException {
    remoteServer.shutdown();
    localServer.shutdown();
  }

  public static void main(String[] args) throws Exception {
    if (args.length != 2) {
      System.err.println("usage: java -jar "
          + "messageVortex.jar net.gwerder.java.messagevortex.transport.imap.ImapPassthruServer "
          + "imap(s)://<accepted_username>:<accepted_password>@<local_interface>:<port> "
          + "imap(s)://<fetch_username>:<fetch_password>@<sever>:<port>");
      exit(100);
    }

    MessageVortexStatus.displayMessage(null, "IMAP passthru Sserver starting as standalone");

    InetSocketAddress listener = getSocketAdressFromUrl(args[0]);
    Credentials creds = new Credentials(getUsernameFromUrl(args[0]), getPasswordFromUrl(args[0]));
    ImapPassthruServer s = new ImapPassthruServer(listener,
            new SecurityContext(SecurityRequirement.STARTTLS), creds,
            getSocketAdressFromUrl(args[1]), null);
    MessageVortexStatus.displayMessage(null, "Passthru Server started as standalone");
  }

  public static String getUsernameFromUrl(String url) throws ParseException {
    if (url == null) {
      throw new NullPointerException("Address may not be null");
    }
    Pattern p = Pattern.compile(imapUrl);
    Matcher m = p.matcher(url);
    if (!m.matches()) {
      throw new ParseException("Unable to parse imap URL \"" + url + "\"", -1);
    }
    return m.group("username");
  }

  public static String getPasswordFromUrl(String url) throws ParseException {
    if (url == null) {
      throw new NullPointerException("Address may not be null");
    }
    Pattern p = Pattern.compile(imapUrl);
    Matcher m = p.matcher(url);
    if (!m.matches()) {
      throw new ParseException("Unable to parse imap URL \"" + url + "\"", -1);
    }
    return m.group("password");
  }

  public static String getProtocolFromUrl(String url) throws ParseException {
    if (url == null) {
      throw new NullPointerException("Address may not be null");
    }
    Pattern p = Pattern.compile(imapUrl);
    Matcher m = p.matcher(url);
    if (!m.matches()) {
      throw new ParseException("Unable to parse imap URL \"" + url + "\"", -1);
    }
    return m.group("protocol");
  }

  public static int getPortFromUrl(String url) throws ParseException {
    if (url == null) {
      throw new NullPointerException("Address may not be null");
    }
    Pattern p = Pattern.compile(imapUrl);
    Matcher m = p.matcher(url);
    if (!m.matches()) {
      throw new ParseException("Unable to parse imap URL \"" + url + "\"", -1);
    }
    return m.group("port") == null ? -1 : Integer.parseInt(m.group("port"));
  }

  public static InetSocketAddress getSocketAdressFromUrl(String url) throws ParseException {
    if (url == null) {
      throw new NullPointerException("Address may not be null");
    }
    Pattern p = Pattern.compile(imapUrl);
    Matcher m = p.matcher(url);
    if (!m.matches()) {
      throw new ParseException("Unable to parse imap URL \"" + url + "\"", -1);
    }
    String host = m.group("server");
    int port;
    if (m.group("port") != null) {
      port = Integer.parseInt(m.group("port"));
    } else {
      port = "imaps".equals(m.group("protocol")) ? 993 : 143;
    }
    return new InetSocketAddress(host, port);
  }
}
