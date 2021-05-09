package net.messagevortex.transport;

import net.messagevortex.AbstractDaemon;
import net.messagevortex.MessageVortexConfig;
import net.messagevortex.MessageVortexLogger;
import net.messagevortex.transport.pop3.TestPop3Handler;
import net.messagevortex.transport.smtp.TestSmtpHandler;

import java.io.IOException;
import java.io.InputStream;
import java.util.logging.Level;
import java.util.logging.Logger;

public class MailServer extends AbstractDaemon implements Transport {

  private static final Logger LOGGER;

  static {
    LOGGER = MessageVortexLogger.getLogger((new Throwable()).getStackTrace()[0].getClassName());
  }

  Transport smtp = null;
  Transport pop = null;

  /***
   * <p>Creates a mail server according to the parameters specified.</p>
   *
   * @param section the name of the config section
   * @throws IOException if creation of the mail server fails
   */
  public MailServer(String section) throws IOException {
    if ("-1".equals(MessageVortexConfig.getDefault().getStringValue(section,
                    "smtp_incoming_address"))
            || -1 == MessageVortexConfig.getDefault().getNumericValue(section,
                    "smtp_incoming_port")) {
      LOGGER.log(Level.INFO, "skipped creation of smtp server enpoint");
    } else {
      smtp = new TestSmtpHandler(section);
    }
    if ("-1".equals(MessageVortexConfig.getDefault().getStringValue(section,
                    "pop3_incoming_address"))
            || -1 == MessageVortexConfig.getDefault().getNumericValue(section,
                    "pop3_incoming_port")) {
      LOGGER.log(Level.INFO, "skipped creation of pop3 server enpoint");
    } else {
      pop = new TestPop3Handler(section);
    }
  }

  @Override
  public void sendMessage(String address, InputStream os) throws IOException {
    pop.sendMessage(address, os);
  }

  @Override
  public void shutdownDaemon() {
    if (smtp != null) {
      smtp.shutdownDaemon();
    }
    if (pop != null) {
      pop.shutdownDaemon();
    }
  }
}
