package net.gwerder.java.messagevortex.transport;

import net.gwerder.java.messagevortex.MessageVortexLogger;
import javax.security.auth.callback.*;
import javax.security.sasl.AuthorizeCallback;
import javax.security.sasl.RealmCallback;
import javax.security.sasl.SaslException;
import java.io.IOException;
import java.util.logging.Level;

/**
 * Created by martin.gwerder on 17.04.2018.
 */

public class SaslServerCallbackHandler implements CallbackHandler {

  private static final java.util.logging.Logger LOGGER;

  static {
    LOGGER = MessageVortexLogger.getLogger((new Throwable()).getStackTrace()[0].getClassName());
  }

  private AuthenticationProxy proxy;

  private String authnid = null;
  private String password = null;

  public SaslServerCallbackHandler(AuthenticationProxy creds) {
    this.proxy = creds;
  }

  @Override
  public void handle(Callback[] cbs) throws IOException, UnsupportedCallbackException {
    for (Callback cb : cbs) {
      Credentials creds = proxy.getCredentials(authnid);
      if (cb instanceof AuthorizeCallback) {
        // Authorization may be checked here
        AuthorizeCallback ac = (AuthorizeCallback) cb;
        // we authorize all users
        ac.setAuthorizedID(authnid);
        ac.setAuthorized(true);
        //ac.setAuthorized( ac.getAuthenticationID()!=null && ac.getAuthenticationID().equals( ac.getAuthorizedID() ));
      } else if (cb instanceof NameCallback) {
        NameCallback nc = (NameCallback) cb;
        authnid = nc.getName() == null ? nc.getDefaultName() : nc.getName();
        LOGGER.log(Level.INFO, "Server sets authzid to " + authnid + " (" + nc.getName() + "/" + nc.getDefaultName() + ")");
        nc.setName(authnid);
        if (proxy.getCredentials(authnid) == null) {
          LOGGER.log(Level.WARNING, "Server did not find credentials for " + authnid);
        }
      } else if (cb instanceof PasswordCallback) {
        PasswordCallback pc = (PasswordCallback) cb;
        if (pc.getPassword() != null) {
          password = new String(pc.getPassword());
        }
        LOGGER.log(Level.INFO, "got password " + password + " (correct password is " + (creds == null ? null : creds.getPassword()) + ")");
        if (creds == null || (password != null && !creds.getPassword().equals(password))) {
          throw new SaslException("unknown user or bad password");
        } else {
          pc.setPassword(creds.getPassword().toCharArray());
        }
      } else if (cb instanceof RealmCallback) {
        RealmCallback pc = (RealmCallback) cb;
        // must match hostname or listed in prop com.sun.security.sasl.digest.realm
        if (creds != null) pc.setText(creds.getRealm());
      } else {
        LOGGER.log(Level.SEVERE, "Server - unknown callback " + cb);
      }
    }
  }

}
