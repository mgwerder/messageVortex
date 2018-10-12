package net.gwerder.java.messagevortex.transport;


import net.gwerder.java.messagevortex.MessageVortexLogger;

import java.io.IOException;
import java.nio.channels.SocketChannel;
import java.util.logging.Logger;

public class ServerConnection extends AbstractConnection {

  private static final Logger LOGGER;

  static {
    LOGGER = MessageVortexLogger.getLogger((new Throwable()).getStackTrace()[0].getClassName());
  }

  public ServerConnection(SocketChannel channel, SecurityContext context) throws IOException {
    super(channel, context, false);
  }

  public ServerConnection(AbstractConnection ac) throws IOException {
    super(ac);
  }

}
