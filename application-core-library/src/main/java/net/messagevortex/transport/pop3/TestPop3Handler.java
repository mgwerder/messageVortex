package net.messagevortex.transport.pop3;

import com.icegreen.greenmail.user.GreenMailUser;
import com.icegreen.greenmail.util.GreenMail;
import com.icegreen.greenmail.util.ServerSetup;

import java.io.IOException;
import java.io.InputStream;
import java.util.logging.Logger;
import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;

import net.messagevortex.Config;
import net.messagevortex.MessageVortex;
import net.messagevortex.transport.Transport;
import net.messagevortex.transport.TransportReceiver;

/*
 * FIXME: This implementation uses a Greenmail POP3 server.
 * FIXME: It holds all data in memory and gets thus slower and slower.
 *
 * FIXME: still broken code
 */

public class TestPop3Handler implements Transport {

  private static final Logger LOGGER;

  static {
    LOGGER = Logger.getLogger((new Throwable()).getStackTrace()[0].getClassName());
  }

  private GreenMail server;
  private String section;
  private TransportReceiver blender;
  private GreenMailUser outUser;


  /**
   * <p>Constructor starting a POP3 server from the named config section.</p>
   *
   * @param section the configuration section to be used
   * @throws IOException if server fails to start
   */
  public TestPop3Handler(String section) throws IOException {
    this.section = section;
    Config cfg = Config.getDefault();
    blender = MessageVortex.getBlender(cfg.getStringValue(this.section, "blender"));
    if (blender == null) {
      throw new IOException("unable to fetch apropriate blender");
    }
    server = new GreenMail(new ServerSetup[]{new ServerSetup(
            cfg.getNumericValue(section, "pop3_outgoing_port"),
            cfg.getStringValue(section, "pop3_outgoing_address"),
            ServerSetup.PROTOCOL_POP3)
    });
    outUser = server.setUser(
            cfg.getStringValue(section, "pop3_outgoing_user"),
            cfg.getStringValue(section, "pop3_outgoing_password"));
    startDaemon();
  }


  @Override
  public void sendMessage(String address, InputStream os) throws IOException {
    Config cfg = Config.getDefault();
    try {
      MimeMessage msg = new MimeMessage(null, os);
      outUser.deliver(msg);
    } catch (MessagingException me) {
      throw new IOException("exception while creating MimeMessage", me);
    }
  }

  @Override
  public void startDaemon() {
    server.start();
  }

  @Override
  public void stopDaemon() {
    server.stop();
  }

  @Override
  public void shutdownDaemon() {
    stopDaemon();
  }


}
