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

import static net.gwerder.java.messagevortex.transport.imap.ImapConnectionState.CONNECTION_NOT_AUTHENTICATED;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import net.gwerder.java.messagevortex.MessageVortexLogger;
import net.gwerder.java.messagevortex.transport.AbstractConnection;
import net.gwerder.java.messagevortex.transport.AuthenticationProxy;
import net.gwerder.java.messagevortex.transport.ServerConnection;
import net.gwerder.java.messagevortex.transport.StoppableThread;

public class ImapConnection extends ServerConnection
                            implements Comparable<ImapConnection>, StoppableThread, Runnable {

  private static final Logger LOGGER;
  private static int id = 1;
  private volatile boolean shutdown = false;

  static {
    LOGGER = MessageVortexLogger.getLogger((new Throwable()).getStackTrace()[0].getClassName());
  }

  /* Status of the connection (according to RFC */
  private ImapConnectionState status = CONNECTION_NOT_AUTHENTICATED;

  /* Authentication authority for this connection */
  private AuthenticationProxy authProxy = null;
  private Thread runner = null;

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
    runner = new Thread(this);
    setId(Thread.currentThread().getName() + "-conn" + id);
    runner.start();
  }

  public AuthenticationProxy setAuth(AuthenticationProxy authProxy) {
    AuthenticationProxy oldProxyAuth = getAuth();
    this.authProxy = authProxy;
    if (authProxy != null) {
      this.authProxy.setImapConnection(this);
    }
    return oldProxyAuth;
  }

  /***
   * <p>Get the authenticator of the connection.</p>
   ***/
  public AuthenticationProxy getAuth() {
    return this.authProxy;
  }

  public void setId(String id) {
    if (runner != null) {
      runner.setName(id);
    }
  }

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

  public void shutdown() throws IOException {
    shutdown = true;
    super.shutdown();
    if (runner != null) {
      synchronized (runner) {
        while (runner != null && runner.isAlive()) {
          // runner.interrupt();
          try {
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

