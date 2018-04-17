package net.gwerder.java.messagevortex.transport;

import net.gwerder.java.messagevortex.MessageVortexLogger;

import javax.security.auth.callback.*;
import javax.security.sasl.RealmCallback;
import java.io.IOException;
import java.util.logging.Level;

/**
 * Created by martin.gwerder on 17.04.2018.
 */
public class SaslClientCallbackHandler implements CallbackHandler {

    private static final java.util.logging.Logger LOGGER;
    static {
        LOGGER = MessageVortexLogger.getLogger((new Throwable()).getStackTrace()[0].getClassName());
    }

    Credentials credentials;

    public SaslClientCallbackHandler( Credentials creds ) {
        this.credentials = creds;
    }

    @Override
    public void handle(Callback[] cbs) throws IOException, UnsupportedCallbackException {
        for (Callback cb : cbs) {
            if (cb instanceof NameCallback) {
                NameCallback nc = (NameCallback)cb;
                nc.setName("username");
            } else if (cb instanceof PasswordCallback) {
                PasswordCallback pc = (PasswordCallback)cb;
                pc.setPassword("password".toCharArray());
            } else if (cb instanceof RealmCallback) {
                RealmCallback pc = (RealmCallback)cb;
                pc.setText("theRealm");
            } else {
                LOGGER.log(Level.SEVERE, "Server - unknown callback "+cb );
            }
            System.out.flush();
        }
    }

}
