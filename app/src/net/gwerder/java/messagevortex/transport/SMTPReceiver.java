package net.gwerder.java.messagevortex.transport;

import javax.net.ssl.SSLContext;
import java.io.IOException;

/**
 * Created by martin.gwerder on 24.01.2018.
 */
public class SMTPReceiver extends LineReceiver {

    public SMTPReceiver(int port, SSLContext context, boolean encrypted, TransportReceiver receiver ) throws IOException {
        super(port,encrypted,new SMTPConnection(context,receiver));
    }

}
