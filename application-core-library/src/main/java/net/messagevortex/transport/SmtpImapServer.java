package net.messagevortex.transport;

import net.messagevortex.AbstractDaemon;
import net.messagevortex.MessageVortexConfig;
import net.messagevortex.MessageVortexLogger;
import net.messagevortex.transport.smtp.TestSmtpHandler;

import java.io.IOException;
import java.io.InputStream;
import java.util.logging.Level;
import java.util.logging.Logger;

public class SmtpImapServer extends AbstractDaemon implements Transport {

  private static final Logger LOGGER;

  static {
    LOGGER = MessageVortexLogger.getLogger((new Throwable()).getStackTrace()[0].getClassName());
  }

  Transport smtp = null;

  /***
   * <p>Creates a comboo of local SMTP and IMAP server as listener for a client.</p>
   *
   * @param section the configuration section to be used
   * @throws IOException if unable to bind interfaces
   */
  public SmtpImapServer(String section) throws IOException {
    if ("-1".equals(MessageVortexConfig.getDefault().getStringValue(section,
                      "smtp_incoming_address"))
            || -1 == MessageVortexConfig.getDefault().getNumericValue(section,
                      "smtp_incoming_port")) {
      LOGGER.log(Level.INFO, "skipped creation of smtp server enpoint");
    } else {
      smtp = new TestSmtpHandler(section);
    }
  }

  @Override
  public void sendMessage(String address, InputStream os) {
    // TODO empty
  }

  @Override
  public void shutdownDaemon() {
    if (smtp != null) {
      smtp.shutdownDaemon();
    }
  }
}
