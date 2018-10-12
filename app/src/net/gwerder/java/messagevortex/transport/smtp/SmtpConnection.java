package net.gwerder.java.messagevortex.transport.smtp;

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

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.SocketTimeoutException;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;
import java.util.logging.Level;
import net.gwerder.java.messagevortex.Config;
import net.gwerder.java.messagevortex.MessageVortexLogger;
import net.gwerder.java.messagevortex.transport.ClientConnection;
import net.gwerder.java.messagevortex.transport.SecurityContext;
import net.gwerder.java.messagevortex.transport.TransportReceiver;
import org.bouncycastle.util.encoders.Base64;

/**
 * <p>Creates a connection to a SMTP Server Socket.</p>
 */
public class SmtpConnection extends ClientConnection {

  static final java.util.logging.Logger LOGGER;

  static {
    LOGGER = MessageVortexLogger.getLogger((new Throwable()).getStackTrace()[0].getClassName());
  }

  private static volatile int id = 1;

  TransportReceiver receiver = null;
  InternalConnectionHandler handler = new InternalConnectionHandler();

  private class InternalConnectionHandler extends Thread {
    public void run() {
      setName("SMTP" + id++);
      String command = null;
      try {
        LOGGER.log(Level.INFO, "got new SMTP incomming connect... sending server greeting");
        writeln("220 " + InetAddress.getLocalHost().getHostName() + " ESMTP MessageVortex receiver");
        String envelopeFrom = null;
        String envelopeTo = null;
        while (command == null || !"quit".equals(command.toLowerCase())) {
          LOGGER.log(Level.INFO, "Waiting for SMTP command to arrive");
          command = readln();
          LOGGER.log(Level.INFO, "got command '" + command + "'");
          if (command.toLowerCase().startsWith("helo ")) {
            write("250 Hi " + command.toLowerCase().substring(6) + " nice meeting you");
          } else if (command.toLowerCase().startsWith("ehlo ")) {
            write("250-Hi " + command.toLowerCase().substring(6) + " nice meeting you");
            write("250-ENHANCEDSTATUSCODES" + CRLF);
            write("250 AUTH login" + CRLF);
          } else if ("auth login".equals(command.toLowerCase())) {
            writeln("334 " + new String(Base64.encode("Username:".getBytes(StandardCharsets.UTF_8))));
            String username = new String(Base64.decode(readln()));
            Config.getDefault().getStringValue("smtp_incomming_username");
            write("334 " + new String(Base64.encode("Password:".getBytes(StandardCharsets.UTF_8))) + CRLF);
            String password = new String(Base64.decode(readln()));
            Config.getDefault().getStringValue("smtp_incomming_password");
          } else if (command.toLowerCase().startsWith("mail from")) {
            envelopeFrom = command.substring(10).trim();
            write("250 OK" + CRLF);
            // FIXME reject if not apropriate
          } else if (command.toLowerCase().startsWith("rcpt to")) {
            envelopeTo = command.substring(8).trim();
            write("250 OK" + CRLF);
            // FIXME reject if not apropriate
          } else if ("data".equals(command.toLowerCase())) {
            if (envelopeFrom != null && envelopeTo != null) {
              write("354 send the mail data, end with ." + CRLF);
              String l = null;
              StringBuilder sb = new StringBuilder();
              while (l == null || !".".equals(l)) {
                if (l != null) {
                  sb.append(l + CRLF);
                }
                l = readln();
              }
              if (getReceiver() != null) {
                LOGGER.log(Level.INFO, "Message passed to blender layer");
                getReceiver().gotMessage(new ByteArrayInputStream(sb.toString().getBytes()));
              } else {
                LOGGER.log(Level.WARNING, "blender layer unknown ... message discarded");
              }
              write("250 OK" + CRLF);
            } else {
              write("554 ERROR" + CRLF);
            }
          } else if ("rset".equals(command.toLowerCase().trim())) {
            envelopeFrom = null;
            envelopeTo = null;
            write("250 OK" + CRLF);
          } else if ("noop".equals(command.toLowerCase().trim())) {
            write("250 OK" + CRLF);
          } else if ("quit".equals(command.toLowerCase().trim())) {
            write("221 bye" + CRLF);
            command = "quit";
          } else {
            write("500 Syntax Error" + CRLF);
          }
        }
      } catch (SocketTimeoutException ste) {
        LOGGER.log(Level.WARNING, "Connection closed due to timeout", ste);
      } catch (IOException ioe) {
        if (!isShutdown()) {
          LOGGER.log(Level.WARNING, "error while communicating", ioe);
        }
      } finally {
        try {
          shutdown();
        } catch (IOException ioe) {
          LOGGER.log(Level.WARNING, "error while shutting down", ioe);
        }
      }
    }

  }

  public SmtpConnection(SocketChannel channel, SecurityContext secContext) throws IOException {
    super(channel, secContext);
    setProtocol("smtp");
    handler.start();
  }

  public TransportReceiver getReceiver() {
    return receiver;
  }

  public TransportReceiver setReceiver(TransportReceiver receiver) {
    TransportReceiver ret = this.receiver;
    this.receiver = receiver;
    return ret;
  }

  public void setName(String name) {
    handler.setName(name);
  }
}
