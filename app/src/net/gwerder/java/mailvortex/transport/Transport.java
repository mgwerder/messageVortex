package net.gwerder.java.mailvortex.transport;

import java.io.IOException;
import java.io.InputStream;

public interface Transport {

    public boolean sendMessage(String address, InputStream os) throws IOException;

}
