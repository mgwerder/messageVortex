package net.messagevortex.transport;

import net.messagevortex.AbstractDaemon;
import net.messagevortex.Config;
import net.messagevortex.MessageVortexConfig;
import net.messagevortex.MessageVortexLogger;
import net.messagevortex.transport.smtp.TestSmtpHandler;

import java.io.IOException;
import java.io.InputStream;
import java.util.logging.Level;
import java.util.logging.Logger;

// TODO implement imap listener
public class SmtpImapServer extends AbstractDaemon implements Transport {

    private static final Logger LOGGER;

    static {
        LOGGER = MessageVortexLogger.getLogger((new Throwable()).getStackTrace()[0].getClassName());
    }

    private Transport smtp = null;

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
            LOGGER.log(Level.INFO, "skipped creation of smtp server endpoint");
        } else {
            smtp = new TestSmtpHandler(section);
        }
    }

    @Override
    public void sendMessage(String address, InputStream is) {
        try {
            smtp.sendMessage(address, is);
        } catch (IOException mex) {
            LOGGER.log(Level.SEVERE, "unable to send outgoing smtp message", mex);
        }
    }

    @Override
    public void shutdownDaemon() {
        if (smtp != null) {
            smtp.shutdownDaemon();
        }
    }


}
