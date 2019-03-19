package net.messagevortex.transport;

import net.messagevortex.AbstractDaemon;
import net.messagevortex.transport.smtp.TestSmtpHandler;

import java.io.IOException;
import java.io.InputStream;

public class SmtpImapServer extends AbstractDaemon implements Transport {

  Transport smtp;

  public SmtpImapServer(String section) throws IOException {
    smtp = new TestSmtpHandler(section);
  }

  @Override
  public void sendMessage(String address, InputStream os) throws IOException {

  }
}
