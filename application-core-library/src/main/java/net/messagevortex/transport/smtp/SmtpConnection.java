package net.messagevortex.transport.smtp;

import net.messagevortex.Config;
import net.messagevortex.MessageVortexLogger;
import net.messagevortex.transport.ClientConnection;
import net.messagevortex.transport.Credentials;
import net.messagevortex.transport.SecurityContext;
import net.messagevortex.transport.TransportReceiver;
import org.bouncycastle.util.encoders.Base64;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketTimeoutException;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.TimeoutException;
import java.util.logging.Level;

/**
 * <p>Creates a connection to a SMTP Server Socket.</p>
 */
public class SmtpConnection extends ClientConnection {

  static final java.util.logging.Logger LOGGER;

  static {
    LOGGER = MessageVortexLogger.getLogger((new Throwable()).getStackTrace()[0].getClassName());
  }

  private static volatile int id = 1;

  private String cfgSection;

  TransportReceiver receiver = null;
  InternalConnectionHandler handler = new InternalConnectionHandler();
  Credentials creds;

  public SmtpConnection(InetSocketAddress socketAddress, SecurityContext context, Credentials creds)
      throws IOException {
    super(socketAddress, context);
    init(creds);
  }

  public SmtpConnection(SocketChannel channel, SecurityContext secContext, Credentials creds)
      throws IOException {
    super(channel, secContext);
    init(creds);
  }

  private void init(Credentials creds) {
    setProtocol("smtp");
    this.creds = creds;
    handler.start();
  }

  private class InternalConnectionHandler extends Thread {
    public void run() {
      // set a default name for the connection handler
      setName("SMTP" + id++);

      String command = null;
      try {
        LOGGER.log(Level.INFO, "got new SMTP incomming connect... sending server greeting");
        // write SMTP banner of server
        writeln("220 " + InetAddress.getLocalHost().getHostName()
            + " ESMTP MessageVortex receiver");

        // smtp state machine
        String envelopeFrom = null;
        String envelopeTo = null;
        while (!"quit".equalsIgnoreCase(command)) {
          LOGGER.log(Level.INFO, "Waiting for SMTP command to arrive");

          // wait for incomming command
          command = "";
          try {
            command = readln();
            LOGGER.log(Level.INFO, "got command '" + command + "'");
            // check for helo command
            if (command.toLowerCase().startsWith("helo ")) {
              write("250 Hi " + command.toLowerCase().substring(6) + " nice meeting you");

              // check for ehlo command
            } else if (command.toLowerCase().startsWith("ehlo ")) {
              write("250-Hi " + command.toLowerCase().substring(6) + " nice meeting you");
              write("250-ENHANCEDSTATUSCODES" + CRLF);
              write("250 AUTH login" + CRLF);
              // check for login
            } else if ("auth login".equalsIgnoreCase(command)) {
              writeln("334 " + new String(
                  Base64.encode("Username:".getBytes(StandardCharsets.UTF_8)),
                  StandardCharsets.UTF_8
              ));
              String username = new String(Base64.decode(readln()), StandardCharsets.UTF_8);
              Config.getDefault().getStringValue(cfgSection, "smtp_incomming_user");
              write("334 " + new String(
                  Base64.encode("Password:".getBytes(StandardCharsets.UTF_8)),
                  StandardCharsets.UTF_8) + CRLF
              );
              String password = new String(Base64.decode(readln()), StandardCharsets.UTF_8);
              Config.getDefault().getStringValue(cfgSection, "smtp_incomming_password");

              // check for sender string
            } else if (command.toLowerCase().startsWith("mail from")) {
              envelopeFrom = command.substring(10).trim();
              write("250 OK" + CRLF);
              // FIXME reject if not apropriate
              // check for receiver string
            } else if (command.toLowerCase().startsWith("rcpt to")) {
              envelopeTo = command.substring(8).trim();
              write("250 OK" + CRLF);
              // FIXME reject if not apropriate
              // check for message body
            } else if ("data".equalsIgnoreCase(command)) {
              if (envelopeFrom != null && envelopeTo != null) {
                write("354 send the mail data, end with CRLF.CRLF" + CRLF);

                // get body until terminated with a line with a single dot
                String l = null;
                StringBuilder sb = new StringBuilder();
                while (!".".equals(l)) {
                  if (l != null) {
                    sb.append(l).append(CRLF);
                  }
                  l = readln();
                }

                // send message to blending layer
                if (getReceiver() != null) {
                  LOGGER.log(Level.INFO, "Message passed to blender layer");
                  getReceiver().gotMessage(new ByteArrayInputStream(
                      sb.toString().getBytes(StandardCharsets.UTF_8))
                  );
                } else {
                  LOGGER.log(Level.WARNING, "blender layer unknown ... message discarded");
                }
                write("250 OK" + CRLF);
              } else {
                write("554 ERROR" + CRLF);
              }

              // check for state rset
            } else if ("rset".equals(command.toLowerCase().trim())) {
              envelopeFrom = null;
              envelopeTo = null;
              write("250 OK" + CRLF);

              // ignore NOP command
            } else if ("noop".equals(command.toLowerCase().trim())) {
              write("250 OK" + CRLF);

              // check for client terminating the connection
            } else if ("quit".equals(command.toLowerCase().trim())) {
              write("221 bye" + CRLF);
              command = "quit";
            } else {

              // on unknown command throw error message
              write("500 Syntax Error" + CRLF);
            }
          } catch (TimeoutException te) {
            LOGGER.log(Level.INFO, "got Timeout while wating for command");
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

          // on all cases of connection termination tear down connection handler
          shutdown();
        } catch (IOException ioe) {
          LOGGER.log(Level.WARNING, "error while shutting down", ioe);
        }
      }
    }

  }

  /***
   * <p>Gets the currently set transport receiver.</p>
   *
   * @return the currently set transport receiver
   */
  public TransportReceiver getReceiver() {
    return receiver;
  }

  /***
   * <p>Sets the transport receiver.</p>
   *
   * @param receiver the transport receiver to be set
   * @return the previously set transport receiver
   */
  public TransportReceiver setReceiver(TransportReceiver receiver) {
    TransportReceiver ret = this.receiver;
    this.receiver = receiver;
    return ret;
  }

  /***
   * <p>Sets the thread name of the connection handler.</p>
   *
   * @param name the name to be set
   */
  public void setName(String name) {
    // set the name of the connection handler
    handler.setName(name);
  }
}
