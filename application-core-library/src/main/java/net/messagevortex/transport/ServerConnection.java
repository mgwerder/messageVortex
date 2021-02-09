package net.messagevortex.transport;

import java.nio.channels.SocketChannel;

public class ServerConnection extends AbstractConnection {

  public ServerConnection(SocketChannel channel, SecurityContext context) {
    super(channel, context, false);
  }

  public ServerConnection(AbstractConnection ac) {
    super(ac);
  }

}
