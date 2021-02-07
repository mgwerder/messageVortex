package net.messagevortex.transport;

import java.io.IOException;
import java.io.InputStream;
import net.messagevortex.AbstractDaemon;

public class MailConnector extends AbstractDaemon implements Transport {

  public MailConnector(String section) {
    // FIXME dummy class
  }

  @Override
  public void sendMessage(String address, InputStream os) {
    // FIXME
  }

}
