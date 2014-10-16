package net.gwerder.java.mailvortex.imap;
 
import java.util.logging.Logger;
import java.util.logging.Level;

import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.Callback;
import java.io.IOException;
import javax.security.auth.callback.UnsupportedCallbackException;
 
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