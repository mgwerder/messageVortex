package net.gwerder.java.messagevortex;

import net.gwerder.java.messagevortex.transport.SMTPReceiver;
import net.gwerder.java.messagevortex.transport.TransportReceiver;

import java.io.IOException;

/**
 * Created by Martin on 30.01.2018.
 */
public class MessageVortexTransport {

    private SMTPReceiver      inSMTP;

    public MessageVortexTransport(TransportReceiver receiver) throws IOException {
        if( receiver == null ) {
            throw new NullPointerException( "TransportReceiver may not be null" );
        }

        Config cfg = Config.getDefault();
        assert cfg!=null;

        // setup receiver for mail relay
        inSMTP = new SMTPReceiver( cfg.getNumericValue("smtp_incomming_port"), null, cfg.getBooleanValue("smtp_incomming_ssl"), receiver );

        // setup receiver for IMAP requests

    }

    public TransportReceiver getTransportReceiver() {
        return this.inSMTP.getReceiver();
    }

    public TransportReceiver setTransportReceiver(TransportReceiver receiver) {
        return this.inSMTP.setReceiver( receiver );
    }

    public void shutdown() {
        inSMTP.shutdown();
    }

}
