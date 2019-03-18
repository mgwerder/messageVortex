package net.messagevortex.transport.smtp;

import com.icegreen.greenmail.Managers;
import com.icegreen.greenmail.smtp.SmtpServer;
import com.icegreen.greenmail.store.FolderListener;
import com.icegreen.greenmail.store.Store;
import com.icegreen.greenmail.store.StoredMessage;
import com.icegreen.greenmail.util.ServerSetup;
import net.messagevortex.AbstractDaemon;
import net.messagevortex.Config;
import net.messagevortex.MessageVortex;
import net.messagevortex.transport.TransportReceiver;
import net.messagevortex.transport.TransportSender;

import javax.mail.Flags;
import javax.mail.MessagingException;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class BasicSmtpReceiver extends AbstractDaemon implements FolderListener {

  private static final Logger LOGGER;

  static {
    LOGGER = Logger.getLogger((new Throwable()).getStackTrace()[0].getClassName());
  }

  private SmtpServer smtpServer;
  private Managers managers = new Managers();
  private TransportReceiver blender=null;
  private String transportId;

  public BasicSmtpReceiver(String section) throws IOException {
    Config cfg = Config.getDefault();
    blender = MessageVortex.getBlender(cfg.getStringValue(section, "blender"));
    if (blender == null ) {
      throw new IOException("unable to fetch apropriate blender");
    }
    smtpServer = new SmtpServer(new ServerSetup(cfg.getNumericValue(section, "smtp_incoming_port"), cfg.getStringValue(section, "smtp_incoming_address"), ServerSetup.PROTOCOL_SMTP), managers);
    startDaemon();
    Store store = managers.getImapHostManager().getStore();
    transportId = cfg.getStringValue(section, "transport_id");
    store.getMailbox(transportId).addListener(this);
  }

  @Override
  public void startDaemon() {
    smtpServer.startService();

    // Wait up to 10 seconds in total (try 100 times)
    long start = System.currentTimeMillis();
    int ex = 0;
    while (ex >= 0 && ex < 100 && System.currentTimeMillis()-start <= 10000) {
      try {
        smtpServer.waitTillRunning(10000);
        ex = -1;
      } catch (InterruptedException ie) {
        ex++;
      }
    }
  }

  @Override
  public void shutdownDaemon() {
    smtpServer.stopService();
  }

  @Override
  public void expunged(int i) {}

  @Override
  public void added(int i) {
    Store store = managers.getImapHostManager().getStore();
    StoredMessage msg = store.getMailbox(transportId).getMessage(i);
    try {
      blender.gotMessage(msg.getMimeMessage().getInputStream());
    } catch(IOException| MessagingException ioe) {
      LOGGER.log(Level.WARNING, "unable to send messages to the blending layer", ioe);
    }
  }

  @Override
  public void flagsUpdated(int i, Flags flags, Long aLong) {}

  @Override
  public void mailboxDeleted() {}
}
