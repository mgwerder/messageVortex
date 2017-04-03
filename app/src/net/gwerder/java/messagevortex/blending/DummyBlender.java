package net.gwerder.java.messagevortex.blending;

import net.gwerder.java.messagevortex.asn1.Message;

import java.io.InputStream;

public class DummyBlender extends AbstractBlender {
    String identity;

    public DummyBlender(String identity) {
        // FIXME validate identity
        this.identity=identity;
    }

    @Override
    public String getBlendingAddress() {
        return this.identity;
    }

    public boolean blendMessage(Message msg) {
        // FIXME URGENT encode message in clear readable and send it
        return true;
    }

    @Override
    public void gotMessage(InputStream is) {
        //FIXME URGENT implementation still missing ... do decoding and pass to IncomingMessageRouterListener
    }
}
