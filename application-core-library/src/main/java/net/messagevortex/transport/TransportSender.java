package net.messagevortex.transport;

import net.messagevortex.RunningDaemon;

import java.io.IOException;
import java.io.InputStream;

public interface TransportSender extends RunningDaemon {

  /***
   * <p>sends a message on the transport layer.</p>
   *
   * <p>This method is called by the blender layer to send a message.</p>
   *
   * @param address the string representation of the target address on the transport layer
   * @param os      the outputstream providing the message
   * @throws IOException if transport layer was unable to satisfy the request
   */
  void sendMessage(String address, InputStream os) throws IOException;

}
