package net.gwerder.java.messagevortex.transport;

import net.gwerder.java.messagevortex.MessageVortexLogger;

import javax.security.auth.callback.*;
import javax.security.sasl.AuthorizeCallback;
import javax.security.sasl.RealmCallback;
import java.io.IOException;
import java.util.logging.Level;

/**
 * Created by martin.gwerder on 17.04.2018.
 */

public class SaslServerCallbackHandler implements CallbackHandler{

    private static final java.util.logging.Logger LOGGER;
    static {
        LOGGER = MessageVortexLogger.getLogger((new Throwable()).getStackTrace()[0].getClassName());
    }

    AuthenticationProxy proxy;

    public SaslServerCallbackHandler( AuthenticationProxy creds ) {
        this.proxy = creds;
    }

    @Override
    public void handle(Callback[] cbs) throws IOException, UnsupportedCallbackException {
        for (Callback cb : cbs) {
            if (cb instanceof AuthorizeCallback) {
                // Authorization may be checked here
                AuthorizeCallback ac = (AuthorizeCallback)cb;
                ac.setAuthorized(true);
            } else if (cb instanceof NameCallback) {
                NameCallback nc = (NameCallback)cb;
                nc.setName("username");
            } else if (cb instanceof PasswordCallback) {
                PasswordCallback pc = (PasswordCallback)cb;
                pc.setPassword("password".toCharArray());
            } else if (cb instanceof RealmCallback) {
                RealmCallback pc = (RealmCallback)cb;
                // must match hostname or listed in prop com.sun.security.sasl.digest.realm
                pc.setText("theRealm");
            } else {
                LOGGER.log(Level.SEVERE, "Server - unknown callback "+cb );
            }
            System.out.flush();
        }
    }

}
