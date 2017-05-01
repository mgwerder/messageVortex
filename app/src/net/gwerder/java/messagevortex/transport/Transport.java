package net.gwerder.java.messagevortex.transport;

import java.io.IOException;
import java.io.InputStream;

public interface Transport {

    /***
     * sends a message on the transport layer.
     *
     * This method is called by the blending layer to send a message.
     *
     * @param address the string representation of the target address on the transport layer
     * @param os      the outputstream providing the message
     * @return        true if the message has been successfully sent
     * @throws IOException if transport layer was unable to satisfy the request
     */
    boolean sendMessage(String address, InputStream os) throws IOException;

}
