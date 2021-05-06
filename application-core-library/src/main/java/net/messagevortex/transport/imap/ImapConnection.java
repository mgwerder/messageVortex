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
import net.messagevortex.transport.AbstractConnection;
import net.messagevortex.transport.AuthenticationProxy;
import net.messagevortex.transport.ServerConnection;
import net.messagevortex.transport.StoppableThread;

import java.io.IOException;
import java.util.concurrent.TimeoutException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ImapConnection extends ServerConnection
    implements Comparable<ImapConnection> {

  private static final Logger LOGGER;
  private static final int id = 1;

  static {
    LOGGER = MessageVortexLogger.getLogger((new Throwable()).getStackTrace()[0].getClassName());
  }

  /* Status of the connection (according to RFC */
  private ImapConnectionState status = ImapConnectionState.CONNECTION_NOT_AUTHENTICATED;

  /* Authentication authority for this connection */
  private AuthenticationProxy authProxy = null;
  private volatile ImapConnectionRunner imapConnectionRunner = null;

  private class ImapConnectionRunner extends Thread implements StoppableThread {

    private volatile boolean shutdownImapRunner = false;

    /***
     * <p>runner method for the connection handler.</p>
     */
    public void run() {
      try {
        while (!shutdownImapRunner && imapConnectionRunner != null) {
          try {
            String line = readln();
            if (line == null) {
              // timeout reached
              shutdownImapRunner = true;
              getSocketChannel().close();
              imapConnectionRunner = null;
            } else {
              LOGGER.log(Level.INFO, "processing command \"" + ImapLine.commandEncoder(line)
                  + "\"");

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
                shutdownImapRunner = true;
                //super.shutdown();
                getSocketChannel().close();
                imapConnectionRunner = null;
              }
            }
          } catch (TimeoutException te) {
            LOGGER.log(Level.WARNING, "got timeout exception when reading", te);
          }
        }
        LOGGER.log(Level.INFO, "left main loop (shutting down)");
      } catch (IOException | ImapException ioe) {
        LOGGER.log(Level.WARNING, "got exception while waiting for lines ("
            + shutdownImapRunner + ")", ioe);
      }
      imapConnectionRunner = null;
      shutdownImapRunner = true;
      LOGGER.log(Level.INFO, "shutdown of runner completed");
    }

    @Override
    public void shutdown() throws IOException {
      if (!shutdownImapRunner) {
        throw new IOException("No Imap runner running");
      }
      shutdownImapRunner = true;
    }

    @Override
    public boolean isShutdown() {
      return (!this.isAlive()) && shutdownImapRunner;
    }

    public void waitForShutdown() {
      while ((!isShutdown()) && Thread.currentThread() != this) {
        try {
          this.join(100);
        } catch (InterruptedException ie) {
          // safely ignore it
        }
      }
    }
  }

  /***
   * <p>Creates an ImapConnection.</p>
   *
   * @param ac the connection to be wrapped
   * @param proxy the authentication proxy to be used for login
   */
  public ImapConnection(AbstractConnection ac, AuthenticationProxy proxy) {
    super(ac);
    setAuth(proxy);
    init();
  }

  /***
   * <p>Creates an imapConnection.</p>
   ***/
  private void init() {
    imapConnectionRunner = new ImapConnectionRunner();
    setId(Thread.currentThread().getName() + "-conn" + id);
    imapConnectionRunner.start();
  }

  /***
   * <p>Setter for the authentication proxy handling incomming requests for authentication.</p>
   *
   * @param authProxy the proxy to be set
   * @return the previously set proxy
   */
  public final AuthenticationProxy setAuth(AuthenticationProxy authProxy) {
    AuthenticationProxy oldProxyAuth = getAuth();
    this.authProxy = authProxy;
    if (authProxy != null) {
      this.authProxy.setImapConnection(this);
    }
    return oldProxyAuth;
  }

  /***
   * <p>Get the authentication proxy of the connection.</p>
   *
   * @return the currently set proxy
   */
  public AuthenticationProxy getAuth() {
    return this.authProxy;
  }

  /***
   * <p>Sets the thread name of the connection handler.</p>
   *
   * @param id the thread name to be set
   */
  public void setId(String id) {
    if (imapConnectionRunner != null) {
      imapConnectionRunner.setName(id);
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
      return new String[] {"* BAD empty line"};
    } catch (ImapException ie) {
      // If line violates the form <tag> <command> refuse processing
      LOGGER.log(Level.WARNING, "got invalid line", ie);
      return new String[] {"* BAD invalid line"};
    }

    LOGGER.log(Level.INFO, "got command \"" + il.getTag() + " " + il.getCommand() + "\".");
    ImapCommand c = ImapCommandFactory.getCommand(il.getCommand());
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
   */
  public void shutdown() {
    boolean conRunner = false;
    if (imapConnectionRunner != null) {
      conRunner = true;
      String rname = imapConnectionRunner.getName();
      ImapConnectionRunner icr = imapConnectionRunner;
      imapConnectionRunner = null;
      LOGGER.log(Level.INFO, "shut down for connection " + rname + " runner called");
      try {
        icr.shutdown();
      } catch (IOException ex) {
        // ignore error due to non running connectors
      }
      LOGGER.log(Level.INFO, "shut down of abstract connection of " + rname + " called");
      try {
        super.shutdown();
      } catch (IOException ex) {
        // ignore error due to non running connectors
      }
      LOGGER.log(Level.INFO, "waiting for shutdown of " + rname + " runner");
      icr.waitForShutdown();
      LOGGER.log(Level.INFO, "shut down connection " + rname + " completed");
    }
  }

}

