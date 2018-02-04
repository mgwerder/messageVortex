package net.gwerder.java.messagevortex.blending;

import net.gwerder.java.messagevortex.accounting.HeaderVerifier;
import net.gwerder.java.messagevortex.asn1.BlendingSpec;
import net.gwerder.java.messagevortex.asn1.VortexMessage;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import java.io.InputStream;

/**
 * Created by Martin on 04.02.2018.
 */
public class SMTPBlender extends Blender {

    HeaderVerifier hVerifier = null;

    public SMTPBlender(BlenderReceiver receiver, HeaderVerifier verifier) {
        super(receiver, verifier);
    }

    @Override
    /***
     * receives an SMTP message from the transport layer and extracts the message.
     *
     * @return true if message has been accepted as vortex message
     */
    public boolean gotMessage(InputStream is) {

        return false;
    }

    @Override
    public boolean blendMessage(BlendingSpec target, VortexMessage message) {
        throw new NotImplementedException(); // FIXME
    }

    @Override
    public String getBlendingAddress() {
        return null;
    }
}
