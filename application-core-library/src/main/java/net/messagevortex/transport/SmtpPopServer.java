package net.messagevortex.transport;

import net.messagevortex.AbstractDaemon;

import java.io.IOException;
import java.io.InputStream;

public class SmtpPopServer extends AbstractDaemon implements Transport {

  public SmtpPopServer(String section) {
    // FIXME this is a dummy
  }

  @Override
  public void sendMessage(String address, InputStream os) throws IOException {

  }
}
