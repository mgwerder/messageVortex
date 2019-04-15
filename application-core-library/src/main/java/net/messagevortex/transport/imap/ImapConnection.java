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
import java.util.logging.Level;
import java.util.logging.Logger;
import net.messagevortex.MessageVortexLogger;
import net.messagevortex.transport.AbstractConnection;
import net.messagevortex.transport.AuthenticationProxy;
import net.messagevortex.transport.ServerConnection;
import net.messagevortex.transport.StoppableThread;

public class ImapConnection extends ServerConnection
                            implements Comparable<ImapConnection> {

  private static final Logger LOGGER;
  private static int id = 1;

  static {
    LOGGER = MessageVortexLogger.getLogger((new Throwable()).getStackTrace()[0].getClassName());
  }

  /* Status of the connection (according to RFC */
  private ImapConnectionState status = ImapConnectionState.CONNECTION_NOT_AUTHENTICATED;

  /* Authentication authority for this connection */
  private AuthenticationProxy authProxy = null;
  private ImapConnectionRunner runner = null;

  private class ImapConnectionRunner extends Thread implements StoppableThread {

    private volatile boolean shutdown = false;

    /***
     * <p>runner method for the connection handler.</p>
     */
    public void run() {
      try {
        while (!shutdown && !isShutdown()) {
          String line = readln();
          if (line == null) {
            // timeout reached
            shutdown = true;
            getSocketChannel().close();
            runner = null;
          } else {
            LOGGER.log(Level.INFO, "processing command \"" + ImapLine.commandEncoder(line) + "\"");

            String[] reply = processCommand(line + CRLF);
            if (reply != null) {
              for (String r : reply) {
                LOGGER.log(Level.INFO, "sending reply to client \"" + ImapLine.commandEncoder(r)
                        + "\"");
                if (r != null) {
                  write(r);
                }
              }
            }
            if (reply == null || reply[reply.length - 1] == null) {
              // process command requested connection close
              shutdown = true;
              //super.shutdown();
              getSocketChannel().close();
              runner = null;
            }
          }
        }
      } catch (IOException | ImapException ioe) {
        LOGGER.log(Level.WARNING, "got exception while waiting for lines (" + shutdown + ")", ioe);
      }
    }

    @Override
    public void shutdown() throws IOException {
      shutdown=true;
    }

    @Override
    public boolean isShutdown() {
      return false;
    }
  }

  /***
   * <p>Creates an imapConnection.</p>
   ***/
  public ImapConnection(AbstractConnection ac, AuthenticationProxy proxy) throws IOException {
    super(ac);
    setAuth(proxy);
    init();
  }

  /***
   * <p>Creates an imapConnection.</p>
   ***/
  private void init() throws IOException {
    runner = new ImapConnectionRunner();
    setId(Thread.currentThread().getName() + "-conn" + id);
    runner.start();
  }

  /***
   * <p>Setter for the authentication proxy handling incomming requests for authentication.</p>
   *
   * @param authProxy the proxy to be set
   * @return the previously set proxy
   */
  public AuthenticationProxy setAuth(AuthenticationProxy authProxy) {
    AuthenticationProxy oldProxyAuth = getAuth();
    this.authProxy = authProxy;
    if (authProxy != null) {
      this.authProxy.setImapConnection(this);
    }
    return oldProxyAuth;
  }

  /***
   * <p>Get the authentication proxy of the connection.</p>
   ***/
  public AuthenticationProxy getAuth() {
    return this.authProxy;
  }

  /***
   * <p>Sets the thread name of the connection handler.</p>
   *
   * @param id the thread name to be set
   */
  public void setId(String id) {
    if (runner != null) {
      runner.setName(id);
    }
  }

  /***
   * <p>Sets the current authentication state of the connection.</p>
   *
   * @param status the new connection state
   * @return the previously set connection state
   */
  public ImapConnectionState setImapState(ImapConnectionState status) {
    ImapConnectionState old = this.status;
    this.status = status;
    return old;
  }

  public ImapConnectionState getImapState() {
    return this.status;
  }

  @Override
  public int compareTo(ImapConnection i) {
    return Integer.compare(hashCode(), i.hashCode());
  }

  @Override
  public boolean equals(Object i) {
    return this == i;
  }

  @Override
  public int hashCode() {
    return super.hashCode() + 1;
  }

  private String[] processCommand(String command) throws ImapException {
    // Extract first word (command) and fetch respective command object
    ImapLine il = null;
    try {
      il = new ImapLine(this, command);
    } catch (ImapBlankLineException ie) {
      // just ignore blank lines
      LOGGER.log(Level.INFO, "got a blank line as command", ie);
      return new String[]{"* BAD empty line"};
    } catch (ImapException ie) {
      // If line violates the form <tag> <command> refuse processing
      LOGGER.log(Level.WARNING, "got invalid line", ie);
      return new String[]{"* BAD invalid line"};
    }

    LOGGER.log(Level.INFO, "got command \"" + il.getTag() + " " + il.getCommand() + "\".");
    ImapCommand c = ImapCommand.getCommand(il.getCommand());
    if (c == null) {
      throw new ImapException(il, "Command \"" + il.getCommand() + "\" is not implemented");
    }
    LOGGER.log(Level.FINEST, "found command in connection " + Thread.currentThread().getName()
            + ".");
    String[] s = c.processCommand(il);

    LOGGER.log(Level.INFO, "got command \"" + il.getTag() + " " + il.getCommand()
            + "\". Reply is \"" + ImapLine.commandEncoder(s == null ? "null" : s[s.length - 1])
            + "\" (" + (s != null ? s.length : "Null") + ").");
    return s;
  }

  /***
   * <p>Tear down connection handler thread.</p>
   * @throws IOException if shutdown failed
   */
  public void shutdown() throws IOException {
    super.shutdown();
    if (runner != null) {
      synchronized (runner) {
        while (runner != null && runner.isAlive()) {
          // runner.interrupt();
          try {
            runner.shutdown();
            runner.join();
          } catch (InterruptedException ie) {
            // ignore and reloop
          }
        }
        // LOGGER.log( Level.INFO, "shut down connection "+runner.getName()+" completed");
        runner = null;
      }
    }
  }

}

