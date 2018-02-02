package net.gwerder.java.messagevortex;

import net.gwerder.java.messagevortex.transport.SMTPReceiver;
import net.gwerder.java.messagevortex.transport.TransportReceiver;

import java.io.IOException;

/**
 * Created by Martin on 30.01.2018.
 */
public class MessageVortexTransport {

    private TransportReceiver receiver;
    private SMTPReceiver      inSMTP;
    private Config cfg;

    public MessageVortexTransport(TransportReceiver receiver) throws IOException {
        if( receiver == null ) {
            throw new NullPointerException( "TransportReceiver may not be null" );
        }
        this.receiver = receiver;

        cfg = Config.getDefault();

        // setup receiver for mail relay
        inSMTP = new SMTPReceiver( cfg.getNumericValue("smtp_incomming_port"), null, cfg.getBooleanValue("smtp_incomming_ssl"), receiver );

        // setup receiver for IMAP requests

    }

    public void shutdown() {
        inSMTP.shutdown();
    }

}
