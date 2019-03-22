package net.messagevortex.transport.smtp;

import com.icegreen.greenmail.util.GreenMail;
import com.icegreen.greenmail.util.GreenMailUtil;
import com.icegreen.greenmail.util.ServerSetup;
import net.messagevortex.AbstractDaemon;
import net.messagevortex.Config;
import net.messagevortex.MessageVortex;
import net.messagevortex.transport.Transport;
import net.messagevortex.transport.TransportReceiver;

import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.internet.MimeMessage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * FIXME: This implementation uses a Greenmail SMTP server. It holds all in memory and gets thus slower and slower.
 *
 * NOT FOR PRODUCTION USE
 */
public class TestSmtpHandler extends AbstractDaemon implements Transport, Runnable {

  private static final Logger LOGGER;

  static {
    LOGGER = Logger.getLogger((new Throwable()).getStackTrace()[0].getClassName());
  }

  private GreenMail server;
  private String section;
  private Thread mailHandler = new Thread (this);
  private TransportReceiver blender;
  private Boolean isRunning = false;


  public TestSmtpHandler(String section) throws IOException {
    Config cfg = Config.getDefault();
    blender = MessageVortex.getBlender(cfg.getStringValue(section, "blender"));
    if (blender == null ) {
      throw new IOException("unable to fetch apropriate blender");
    }
    ServerSetup setup = new ServerSetup(
            cfg.getNumericValue(section, "smtp_incoming_port"),
            cfg.getStringValue(section, "smtp_incoming_address"),
            ServerSetup.PROTOCOL_SMTP
    );
    setup.setServerStartupTimeout(50000);

    server = new GreenMail(new ServerSetup[] {setup});
    this.section = section;
    startDaemon();
  }

  public void run() {
    int count = 1;
    while (isRunning) {

      // wait for a mail but wake up at least every second to shut down if required
      boolean gotMail = server.waitForIncomingEmail(1000, count);

      // process mail if required
      if (gotMail) {

        // fetch mail and process
        LOGGER.log(Level.INFO, "got smtp mail in transport handler ["+section+"]");
        String msg = GreenMailUtil.getBody(server.getReceivedMessages()[count-1]);
        blender.gotMessage(new ByteArrayInputStream(msg.getBytes(StandardCharsets.UTF_8)));

        // wait for one more mail
        count++;
      }
    }
  }

  @Override
  public void startDaemon() {
    synchronized(isRunning) {
      isRunning = true;
      server.start();
      mailHandler.start();
    }
  }

  @Override
  public void shutdownDaemon() {
    synchronized(isRunning) {
      isRunning = false;
      server.stop();
      while (mailHandler.isAlive()) {
        try {
          Thread.sleep(100);
        } catch(InterruptedException ie) {
          // we do not care about this
        }
      }
    }
  }

  @Override
  public void sendMessage(String address, InputStream os) throws IOException {
    MimeMessage msg;
    try {
      final String username = Config.getDefault().getStringValue(section, "smtp_outgoing_user");
      final String password = Config.getDefault().getStringValue(section, "smtp_outgoing_password");

      Properties props = new Properties();
      props.put("mail.smtp.auth", username!=null && ! "".equals(username) ? "true" : "false");
      props.put("mail.smtp.starttls.enable", Config.getDefault().getBooleanValue(section, "smtp_outgoing_starttls") ? "true" : "false" );
      props.put("mail.smtp.host", Config.getDefault().getStringValue(section, "smtp_outgoing_address"));
      props.put("mail.smtp.port", ""+Config.getDefault().getNumericValue(section, "smtp_outgoing_port"));

      Session session = Session.getInstance(props,
              new javax.mail.Authenticator() {
                protected PasswordAuthentication getPasswordAuthentication() {
                  return new PasswordAuthentication(username, password);
                }
              });
      msg = new MimeMessage(session, os);
      javax.mail.Transport.send(msg);
    } catch(MessagingException me) {
      throw new IOException("exception while creating MimeMessage", me);
    }
  }
}
