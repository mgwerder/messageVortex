package net.gwerder.java.messagevortex.transport;

import static net.gwerder.java.messagevortex.transport.SecurityRequirement.SSLTLS;
import static net.gwerder.java.messagevortex.transport.SecurityRequirement.UNTRUSTED_SSLTLS;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.logging.Level;
import java.util.logging.Logger;
import net.gwerder.java.messagevortex.MessageVortexLogger;

/**
 * Created by Martin on 10.03.2018.
 */
public class ListeningSocketChannel {

  static final Logger LOGGER;

  static {
    LOGGER = MessageVortexLogger.getLogger((new Throwable()).getStackTrace()[0].getClassName());
  }

  final InternalThread thread;

  private String protocol = "unknown";
  private SecurityContext context = null;


  private class InternalThread extends Thread {

    ServerSocketChannel serverSocketChannel = null;

    private SocketListener listener = null;

    private volatile boolean shutdown = false;

    public InternalThread(ServerSocketChannel channel) throws IOException {
      channel.configureBlocking(false);
      this.serverSocketChannel = channel;
    }

    public void shutdown() {
      shutdown = true;
    }

    public int getPort() {
      try {
        return ((InetSocketAddress) (serverSocketChannel.getLocalAddress())).getPort();
      } catch (IOException ioe) {
        return -1;
      }
    }

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
                      && (
                              getSecurityContext().getRequirement() == UNTRUSTED_SSLTLS
                              || getSecurityContext().getRequirement() == SSLTLS
                      )) {
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

  public String setProtocol(String protocol) {
    String ret = this.protocol;
    this.protocol = protocol;
    return ret;
  }

  public SecurityContext setSecurityContext(SecurityContext context) {
    SecurityContext ret = this.context;
    this.context = context;
    return ret;
  }

  public SecurityContext getSecurityContext() {
    return context;
  }

  public int getPort() {
    return thread.getPort();
  }

  public String getProtocol() {
    return this.protocol;
  }

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
