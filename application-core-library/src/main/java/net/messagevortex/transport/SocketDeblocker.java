package net.messagevortex.transport;

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

import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import java.net.ConnectException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class SocketDeblocker extends Thread {

  private static final Logger LOGGER;

  static {
    LOGGER = MessageVortexLogger.getLogger((new Throwable()).getStackTrace()[0].getClassName());
  }

  private final int timeout;
  private final int port;
  private volatile boolean shutdown = false;

  public SocketDeblocker(int port, int timeout) {
    this.port = port;
    this.timeout = timeout;
  }

  /***
   * <p>Shutdown the running threads and wait for termination.</p>
   */
  public void shutdown() {
    shutdown = true;
    while (this.isAlive()) {
      try {
        this.join();
      } catch (InterruptedException ie) {
        LOGGER.log(Level.FINEST, "Interrupted exception while shutting down deblocking socket", ie);
        // Preserve
        Thread.currentThread().interrupt();
      }
    }
  }

  /***
   * <p>Thread runner.</p>
   *
   * <p>Do not call this methode</p>
   * FIXME: move to private class
   */
  public void run() {
    int countdown = timeout / 10;
    while (!shutdown && countdown > 0) {
      countdown--;
      try {
        Thread.sleep(10);
      } catch (InterruptedException ie) {
        LOGGER.log(Level.FINEST, "Interrupted exception while running SocketDeblocker", ie);
        // Preserve
        Thread.currentThread().interrupt();
      }
    }

    if (!shutdown) {
      try {
        SSLSocket cs = (SSLSocket) SSLSocketFactory.getDefault().createSocket("localhost", port);
        cs.close();
      } catch (ConnectException e) {
        /* there was nothing to deblock */
        LOGGER.log(Level.FINEST, "Exception while running SocketDeblocker", e);
      } catch (Exception e) {
        LOGGER.log(Level.FINEST, "Exception while running SocketDeblocker", e);
      }
    }
  }

}
