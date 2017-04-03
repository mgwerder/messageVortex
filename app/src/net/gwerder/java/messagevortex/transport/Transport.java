package net.gwerder.java.messagevortex.transport;

import java.io.IOException;
import java.io.InputStream;

public interface Transport {

    public boolean sendMessage(String address, InputStream os) throws IOException;

}
