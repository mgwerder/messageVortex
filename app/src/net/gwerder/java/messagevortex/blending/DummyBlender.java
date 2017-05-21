package net.gwerder.java.messagevortex.blending;
/***
 * Copyright (c) 2018 Martin Gwerder (martin@gwerder.net)
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 ***/

import net.gwerder.java.messagevortex.MessageVortexLogger;
import net.gwerder.java.messagevortex.asn1.BlendingSpec;
import net.gwerder.java.messagevortex.asn1.VortexMessage;
import net.gwerder.java.messagevortex.asn1.encryption.DumpType;
import net.gwerder.java.messagevortex.transport.DummyTransport;
import net.gwerder.java.messagevortex.transport.TransportListener;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.logging.Level;

public class DummyBlender extends AbstractBlender implements TransportListener {

    private static final java.util.logging.Logger LOGGER;
    static {
        LOGGER = MessageVortexLogger.getLogger((new Throwable()).getStackTrace()[0].getClassName());
        MessageVortexLogger.setGlobalLogLevel( Level.ALL);
    }

    String identity;
    DummyTransport transport;
    BlenderListener router;

    public DummyBlender(String identity, BlenderListener router) throws IOException {
        this.identity=identity;
        this.transport=new DummyTransport(identity,this);
        this.router=router;
    }

    @Override
    public String getBlendingAddress() {
        return this.identity;
    }

    public boolean blendMessage(BlendingSpec target, VortexMessage msg) {
        // FIXME encode message in clear readable and send it
        try {
            return transport.sendMessage(target.getRecipientAddress(), new ByteArrayInputStream(msg.toBytes(DumpType.PUBLIC_ONLY)));
        } catch(IOException ioe) {
            LOGGER.log(Level.SEVERE,"Unable to send to transport endpoint "+target.toString(),ioe);
            return false;
        }
    }

    @Override
    public void gotMessage(InputStream is) {
        router.gotMessage(is);
    }
}
