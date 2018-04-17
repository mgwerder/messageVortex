package net.gwerder.java.messagevortex.transport;

import net.gwerder.java.messagevortex.MessageVortexLogger;
import net.gwerder.java.messagevortex.transport.imap.AuthenticationDummyProxy;

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

    private AuthenticationDummyProxy proxy;

    private String authzid = null;

    public SaslServerCallbackHandler( AuthenticationDummyProxy creds ) {
        this.proxy = creds;
    }

    @Override
    public void handle(Callback[] cbs) throws IOException, UnsupportedCallbackException {
        for (Callback cb : cbs) {
            if (cb instanceof AuthorizeCallback) {
                // Authorization may be checked here
                AuthorizeCallback ac = (AuthorizeCallback)cb;
                // we authorize all users
                ac.setAuthorized(true);
            } else if (cb instanceof NameCallback) {
                NameCallback nc = (NameCallback)cb;
                authzid = nc.getName()==null?nc.getDefaultName():nc.getName();
                LOGGER.log( Level.INFO, "Server sets authzid to "+authzid+" ("+nc.getName()+")");
                nc.setName(authzid);
                if( proxy.getCredentials(authzid)==null) {
                    LOGGER.log(Level.WARNING, "Server did not find credentials for " + authzid);
                }
            } else if (cb instanceof PasswordCallback) {
                PasswordCallback pc = (PasswordCallback)cb;
                pc.setPassword( proxy.getCredentials( authzid ).getPassword().toCharArray() );
            } else if (cb instanceof RealmCallback) {
                RealmCallback pc = (RealmCallback)cb;
                // must match hostname or listed in prop com.sun.security.sasl.digest.realm
                pc.setText( proxy.getCredentials( authzid ).getRealm() );
            } else {
                LOGGER.log(Level.SEVERE, "Server - unknown callback "+cb );
            }
            System.out.flush();
        }
    }

}
