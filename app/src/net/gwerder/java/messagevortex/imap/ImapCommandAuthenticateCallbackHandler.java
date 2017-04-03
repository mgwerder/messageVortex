package net.gwerder.java.messagevortex.imap;

import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.UnsupportedCallbackException;
import java.io.IOException;
import java.util.logging.Logger;
 
public class ImapCommandAuthenticateCallbackHandler implements CallbackHandler {

    private static final Logger LOGGER;
    static {
        LOGGER = Logger.getLogger((new Throwable()).getStackTrace()[0].getClassName());
    }
    
    public void handle(Callback[] callbacks) throws IOException, UnsupportedCallbackException {
        
    }
    
    // AuthorizeCallback
    // NameCallback
    // PasswordCallback
    // RealmCallback
    // RealmChoiceCallback

}