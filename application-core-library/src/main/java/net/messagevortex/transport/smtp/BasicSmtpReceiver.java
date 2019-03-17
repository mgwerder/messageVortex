package net.messagevortex.transport.smtp;

import net.messagevortex.AbstractDaemon;
import net.messagevortex.Config;
import org.subethamail.smtp.MessageContext;
import org.subethamail.smtp.MessageHandler;
import org.subethamail.smtp.MessageHandlerFactory;
import org.subethamail.smtp.RejectException;
import org.subethamail.smtp.server.SMTPServer;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.InetAddress;

public class BasicSmtpReceiver extends AbstractDaemon {

  private SMTPServer smtpServer;

  private class MyMessageHandlerFactory implements MessageHandlerFactory {

    public MessageHandler create(MessageContext ctx) {
      return new Handler(ctx);
    }

    class Handler implements MessageHandler {
      MessageContext ctx;

      public Handler(MessageContext ctx) {
        this.ctx = ctx;
      }

      public void from(String from) throws RejectException {
        System.out.println("FROM:"+from);
      }

      public void recipient(String recipient) throws RejectException {
        System.out.println("RECIPIENT:"+recipient);
      }

      public void data(InputStream data) throws IOException {
        System.out.println("MAIL DATA");
        System.out.println("= = = = = = = = = = = = = = = = = = = = = = = = = = = = = = =");
        System.out.println(this.convertStreamToString(data));
        System.out.println("= = = = = = = = = = = = = = = = = = = = = = = = = = = = = = =");
      }

      public void done() {
        System.out.println("Finished");
      }

      public String convertStreamToString(InputStream is) {
        BufferedReader reader = new BufferedReader(new InputStreamReader(is));
        StringBuilder sb = new StringBuilder();

        String line = null;
        try {
          while ((line = reader.readLine()) != null) {
            sb.append(line + "\n");
          }
        } catch (IOException e) {
          e.printStackTrace();
        }
        return sb.toString();
      }

    }
  }

  public BasicSmtpReceiver(String section) throws IOException {
    Config cfg = Config.getDefault();
    smtpServer = new SMTPServer(new MyMessageHandlerFactory());
    smtpServer.setPort(cfg.getNumericValue(section, "smtp_incoming_port"));
    smtpServer.setBindAddress(InetAddress.getByName(cfg.getStringValue(section, "smtp_incoming_address")));
    smtpServer.start();
  }

  @Override
  public void shutdownDaemon() {
    smtpServer.stop();
  }
}
