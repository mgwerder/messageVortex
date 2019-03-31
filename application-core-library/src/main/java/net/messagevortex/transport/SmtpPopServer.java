package net.messagevortex.transport;

import net.messagevortex.AbstractDaemon;
import net.messagevortex.transport.pop3.TestPop3Handler;
import net.messagevortex.transport.smtp.TestSmtpHandler;

import java.io.IOException;
import java.io.InputStream;

public class SmtpPopServer extends AbstractDaemon implements Transport {

  Transport smtp;
  Transport pop;

  public SmtpPopServer(String section) throws IOException {
    smtp = new TestSmtpHandler(section);
    pop = new TestPop3Handler(section);
  }

  @Override
  public void sendMessage(String address, InputStream os) throws IOException {
    pop.sendMessage(address,os);
  }

  @Override
  public void shutdownDaemon() {
    smtp.shutdownDaemon();
    pop.shutdownDaemon();
  }
}
