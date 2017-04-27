package net.gwerder.java.messagevortex.blending;

import net.gwerder.java.messagevortex.MessageVortexLogger;
import net.gwerder.java.messagevortex.asn1.BlendingSpec;
import net.gwerder.java.messagevortex.asn1.VortexMessage;
import net.gwerder.java.messagevortex.transport.DummyTransport;
import net.gwerder.java.messagevortex.transport.TransportListener;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.NoSuchAlgorithmException;
import java.text.ParseException;
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
            return transport.sendMessage(target.getBlendingEndpointAddress(), new ByteArrayInputStream(msg.toBytes()));
        } catch(IOException|NoSuchAlgorithmException|ParseException ioe) {
            LOGGER.log(Level.SEVERE,"Unable to send to transport endpoint "+target.toString());
            return false;
        }
    }

    @Override
    public void gotMessage(InputStream is) {
        router.gotMessage(is);
    }
}
