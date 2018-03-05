package net.gwerder.java.messagevortex;
// ************************************************************************************
// * Copyright (c) 2018 Martin Gwerder (martin@gwerder.net)
// *
// * Permission is hereby granted, free of charge, to any person obtaining a copy
// * of this software and associated documentation files (the "Software"), to deal
// * in the Software without restriction, including without limitation the rights
// * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
// * copies of the Software, and to permit persons to whom the Software is
// * furnished to do so, subject to the following conditions:
// *
// * The above copyright notice and this permission notice shall be included in all
// * copies or substantial portions of the Software.
// *
// * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
// * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
// * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
// * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
// * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
// * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
// * SOFTWARE.
// ************************************************************************************

import net.gwerder.java.messagevortex.transport.SecurityRequirement;
import net.gwerder.java.messagevortex.transport.smtp.SMTPReceiver;
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
        inSMTP = new SMTPReceiver( cfg.getNumericValue("smtp_incomming_port"), null, SecurityRequirement.getByName( cfg.getStringValue("smtp_incomming_ssl") ), receiver );

        // setup receiver for IMAP requests
        // FIXME
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
