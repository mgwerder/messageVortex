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

import java.io.IOException;
import java.net.InetSocketAddress;
import java.text.ParseException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.messagevortex.MessageVortexLogger;
import net.messagevortex.transport.AuthenticationProxy;
import net.messagevortex.transport.Credentials;
import net.messagevortex.transport.SecurityContext;

public class ImapPassthruServer {

  private static final java.util.logging.Logger LOGGER;

  static {
    LOGGER = MessageVortexLogger.getLogger((new Throwable()).getStackTrace()[0].getClassName());
  }

  private static final String imapUrl = "(?<protocol>imap[s]?)://"
          + "(?:(?<username>[\\p{Alnum}\\-\\.]+):(?<password>[\\p{ASCII}&&[^@]]+)@)?"
          + "(?<server>[\\p{Alnum}\\.\\-]+)(?::(?<port>[\\digit]{1,5}))?";

  private ImapServer localServer;
  private ImapClient remoteServer;

  /***
   * <p>Create an IMAP passthru proxy server.</p>
   * @param listeningAddress listening address for the incomming proxy port
   * @param context the security context for the proxy sever
   * @param listeningCredentials credentials for the listening proxy
   * @param forwardingServer IMAP address of the proxied server
   * @param forwardingCredentials credentials for the proxied IMAP server
   * @throws IOException if start of proxy fails
   */
  public ImapPassthruServer(InetSocketAddress listeningAddress, SecurityContext context,
                            Credentials listeningCredentials, InetSocketAddress forwardingServer,
                            Credentials forwardingCredentials) throws IOException {
    localServer = new ImapServer(listeningAddress, context);
    AuthenticationProxy authProxy = new AuthenticationProxy();
    authProxy.addCredentials(listeningCredentials);
    localServer.setAuth(authProxy);
    remoteServer = new ImapClient(forwardingServer, context);
  }

  public void shutdown() throws IOException {
    localServer.shutdown();
    remoteServer.shutdown();
  }

  /***
   * <p>Retrieves the username from an IMAPUrl string.</p>
   *
   * @param url the URL to extract the port
   * @return the username or null if none
   *
   * @throws ParseException if URL does not follow specification
   * @throws NullPointerException if url is null
   */
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

  /***
   * <p>Retrieves the password from an IMAPUrl string.</p>
   *
   * @param url the URL to extract the port
   * @return the encoded password or null if none
   *
   * @throws ParseException if URL does not follow specification
   * @throws NullPointerException if url is null
   */
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


  /***
   * <p>retrieves the protocol string from an IMAPUrl string.</p>
   *
   * @param url the URL to extract the port
   * @return the protocol string
   *
   * @throws ParseException if URL does not follow specification
   * @throws NullPointerException if url is null
   */
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

  /***
   * <p>retrieves the port number from an IMAPUrl string.</p>
   *
   * @param url the URL to extract the port
   * @return the port number
   *
   * @throws ParseException if URL does not follow specification
   * @throws NullPointerException if url is null
   */
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

  /***
   * <p>Convert an imap URL to a scket address with apropriate port.</p>
   * @param url the URL to be cconvertde
   * @return An equivalent socket address
   *
   * @throws ParseException if the pattern doeas not match a regular imap url
   * @throws NullPointerException if the URL is null
   */
  public static InetSocketAddress getSocketAddressFromUrl(String url) throws ParseException {
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
