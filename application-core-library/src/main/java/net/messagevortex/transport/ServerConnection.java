package net.messagevortex.transport;

import java.nio.channels.SocketChannel;

/**
 * <p>A generic listening server connection.</p>
 */
public class ServerConnection extends AbstractConnection {

  /**
   * <p>Creates a server connection based on a server channel.</p>
   *
   * @param channel the channel to be used
   * @param context the security context to be applied to the connection
   */
  public ServerConnection(SocketChannel channel, SecurityContext context) {
    super(channel, context, false);
  }

  /**
   * <p>Creates a connection based on a template connection.</p>
   *
   * @param ac the template connection
   */
  public ServerConnection(AbstractConnection ac) {
    super(ac);
  }

}
