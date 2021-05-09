package net.messagevortex.transport;

import net.messagevortex.MessageVortexLogger;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ListeningSocketChannel {

  static final Logger LOGGER;

  static {
    LOGGER = MessageVortexLogger.getLogger((new Throwable()).getStackTrace()[0].getClassName());
  }

  final InternalThread thread;

  /* Identifies protocol by name for logging */
  private String protocol = "unknown";
  /* The security context required to do encryption */
  private SecurityContext context = null;


  private class InternalThread extends Thread {

    ServerSocketChannel serverSocketChannel = null;

    private SocketListener listener = null;

    private volatile boolean shutdown = false;

    public InternalThread(ServerSocketChannel channel) throws IOException {
      // make sure that channel is non blocking if provided externally
      channel.configureBlocking(false);

      // store the socket channel for later use
      this.serverSocketChannel = channel;
    }

    /***
     * <p>Shuts the connection down.</p>
     */
    public void shutdown() {
      shutdown = true;
    }

    /***
     * <p>Gets the local port of the channel.</p>
     *
     * @return the local port number or -1 if not yet bound
     */
    public int getPort() {
      // Extract port of socket channel
      try {
        return ((InetSocketAddress) (serverSocketChannel.getLocalAddress())).getPort();
      } catch (IOException ioe) {
        //return -1 if no local address is available
        return -1;
      }
    }

    /***
     * <p>Sets a socket listener to handle incomming messages.</p>
     *
     * @param listener the socket listener to be used
     * @return the previously set socket listener
     */
    public SocketListener setSocketListener(SocketListener listener) {
      SocketListener ret = this.listener;
      this.listener = listener;
      return ret;
    }

    public SocketListener getSocketListener() {
      return this.listener;
    }

    @Override
    public void run() {
      try {
        while (!shutdown) {
          SocketChannel socketChannel = serverSocketChannel.accept();

          if (socketChannel != null) {
            if (listener != null) {
              LOGGER.log(Level.INFO, "calling SocketChannel listener");
              ServerConnection sc = new ServerConnection(socketChannel, getSecurityContext());
              if (getSecurityContext() != null && getSecurityContext().getRequirement() != null
                  && (getSecurityContext().getRequirement() == SecurityRequirement.UNTRUSTED_SSLTLS
                  || getSecurityContext().getRequirement() == SecurityRequirement.SSLTLS)) {
                sc.startTls();
              }
              listener.gotConnect(sc);
            } else {
              LOGGER.log(Level.SEVERE, "socketchannel listener is missing");
            }
          } else {
            try {
              Thread.sleep(10);
            } catch (InterruptedException ie) {
              // safe to ignore
            }
          }
        }
      } catch (IOException ioe) {
        LOGGER.log(Level.WARNING, "IOException while handling incomming connects", ioe);
      }
    }
  }

  /***
   * <p>Creates a listening socket channel.</p>
   *
   * @param address the socket to be bound
   * @param listener the listener to be used for incomming connections
   * @throws IOException if the address cannot be bound
   */
  public ListeningSocketChannel(InetSocketAddress address, SocketListener listener)
          throws IOException {
    super();
    ServerSocketChannel channel = ServerSocketChannel.open();
    channel.socket().bind(address);

    thread = new InternalThread(channel);
    thread.setSocketListener(listener);
    thread.start();
  }

  public SocketListener setSocketListener(SocketListener listener) {
    return thread.setSocketListener(listener);
  }

  public void setName(String name) {
    thread.setName(name);
  }

  public String getName() {
    return thread.getName();
  }

  public SocketListener getSocketListener() {
    return thread.getSocketListener();
  }

  /***
   * <p>Set the protocol identifier of the channel.</p>
   *
   * @param protocol the identifier string to be set
   * @return the previously set identifier
   */
  public String setProtocol(String protocol) {
    String ret = this.protocol;
    this.protocol = protocol;
    return ret;
  }

  /***
   * <p>Sets the security context of the socket channel.</p>
   *
   * @param context the context to be set
   * @return the previously set context
   */
  public SecurityContext setSecurityContext(SecurityContext context) {
    SecurityContext ret = this.context;
    this.context = context;
    return ret;
  }

  /***
   * <p>Gets the security context of the channel.</p>
   * @return the currently set security context
   */
  public SecurityContext getSecurityContext() {
    return context;
  }

  /***
   * <p>Gets the currently used local  port.</p>
   *
   * @return the local port number
   */
  public int getPort() {
    return thread.getPort();
  }

  public String getProtocol() {
    return this.protocol;
  }

  /***
   * <p>Shutdown the socket channel.</p>
   */
  public void shutdown() {
    thread.shutdown();
    while (thread.isAlive()) {
      try {
        thread.join();
      } catch (InterruptedException ie) {
        // safe to ignore
      }
    }
  }

  public boolean isShutdown() {
    return thread.isAlive();
  }

}
