package net.messagevortex.test.transport;

import net.messagevortex.AbstractDaemon;
import net.messagevortex.MessageVortexLogger;
import net.messagevortex.transport.SecurityContext;
import net.messagevortex.transport.SecurityRequirement;
import net.messagevortex.transport.smtp.SmtpReceiver;
import net.messagevortex.transport.smtp.SmtpSender;
import net.messagevortex.transport.TransportReceiver;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Vector;
import java.util.logging.Level;

import static org.junit.Assert.assertTrue;

/**
 * <p>Teastclass for sending and receiving SMTP based messages.</p>
 *
 * Created by Martin on 26.01.2018.
 */
@RunWith(JUnit4.class)
public class SMTPTransportSenderTest extends AbstractDaemon implements TransportReceiver {

  public static final String CRLF = "\r\n";

  private List<InputStream> msgs = new Vector<>();
  private static final java.util.logging.Logger LOGGER;

  static {
    LOGGER = MessageVortexLogger.getLogger((new Throwable()).getStackTrace()[0].getClassName());
    MessageVortexLogger.setGlobalLogLevel(Level.ALL);
  }

  @Test
  public void basicSMTPTest() throws IOException {
    LOGGER.log(Level.INFO, "Setup receiver");
    SmtpReceiver receiver = new SmtpReceiver(new InetSocketAddress("localhost", 0), new SecurityContext(SecurityRequirement.PLAIN), this);

    LOGGER.log(Level.INFO, "Setup sender");
    SmtpSender send = new SmtpSender("SMTPSender_of_MessageVortex@gwerder.net", "localhost", receiver.getPort(), null, null);

    LOGGER.log(Level.INFO, "Sending message");
    try {
      SimpleDateFormat dt1 = new SimpleDateFormat("EEE, d MMM yyyy HH:mm:ss Z");
      String txt = "Date:" + dt1.format(new Date()) + CRLF + "From: SMTPSender_of_MessageVortex@gwerder.net" + CRLF + "To: Martin@gwerder.net" + CRLF + "Subject: Testmail" + CRLF + CRLF + "Testmail" + CRLF;
      send.sendMessage("martin@gwerder.net", new ByteArrayInputStream(txt.getBytes(StandardCharsets.UTF_8.name())));
    } catch (IOException ioe) {
      LOGGER.log(Level.SEVERE, "got unexpected exception while sending message", ioe);
    }
    receiver.shutdown();
    assertTrue("Message not arrived (yet?)", msgs.size() == 1);
  }


  @Override
  public boolean gotMessage(InputStream is) {
    synchronized (msgs) {
      msgs.add(is);
    }
    return true;
  }

  public void shutdown() {
  }

}
