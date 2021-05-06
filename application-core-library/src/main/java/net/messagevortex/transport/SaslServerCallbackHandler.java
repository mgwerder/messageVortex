package net.messagevortex.transport;

// ************************************************************************************
// * Copyright (c) 2018 Martin Gwerder (martin@gwerder.net)
// *
// * Permission is hereby granted, free of charge, to any person obtaining a copy
// * of this software and associated documentation files (the "Software"), to deal
// * in the Software without restriction, including without limitation the rights
// * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
// * copies of the Software, and to permit persons to whom the Software is
// * furnished to do so, subject to the following conditions:
// *
// * The above copyright notice and this permission notice shall be included in all
// * copies or substantial portions of the Software.
// *
// * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
// * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
// * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
// * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
// * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
// * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
// * SOFTWARE.
// ************************************************************************************

import net.messagevortex.MessageVortexLogger;

import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.NameCallback;
import javax.security.auth.callback.PasswordCallback;
import javax.security.sasl.AuthorizeCallback;
import javax.security.sasl.RealmCallback;
import javax.security.sasl.SaslException;
import java.io.IOException;
import java.util.logging.Level;

public class SaslServerCallbackHandler implements CallbackHandler {

  private static final java.util.logging.Logger LOGGER;

  static {
    LOGGER = MessageVortexLogger.getLogger((new Throwable()).getStackTrace()[0].getClassName());
  }

  private final AuthenticationProxy proxy;

  private String authnid = null;
  private String password = null;

  public SaslServerCallbackHandler(AuthenticationProxy creds) {
    this.proxy = creds;
  }

  @Override
  public void handle(Callback[] cbs) throws IOException {
    for (Callback cb : cbs) {
      Credentials creds = proxy.getCredentials(authnid);
      if (cb instanceof AuthorizeCallback) {
        // Authorization may be checked here
        AuthorizeCallback ac = (AuthorizeCallback) cb;
        // we authorize all users
        ac.setAuthorizedID(authnid);
        ac.setAuthorized(true);
        //ac.setAuthorized( ac.getAuthenticationID()!=null
        //                  && ac.getAuthenticationID().equals( ac.getAuthorizedID() ));
      } else if (cb instanceof NameCallback) {
        NameCallback nc = (NameCallback) cb;
        authnid = nc.getName() == null ? nc.getDefaultName() : nc.getName();
        LOGGER.log(Level.INFO, "Server sets authzid to " + authnid + " (" + nc.getName()
                + "/" + nc.getDefaultName() + ")");
        nc.setName(authnid);
        if (proxy.getCredentials(authnid) == null) {
          LOGGER.log(Level.WARNING, "Server did not find credentials for " + authnid);
        }
      } else if (cb instanceof PasswordCallback) {
        PasswordCallback pc = (PasswordCallback) cb;
        if (pc.getPassword() != null) {
          password = new String(pc.getPassword());
        }
        LOGGER.log(Level.INFO, "got password " + password + " (correct password is "
                + (creds == null ? null : creds.getPassword()) + ')');
        if (creds == null || (password != null && !creds.getPassword().equals(password))) {
          throw new SaslException("unknown user or bad password");
        } else {
          pc.setPassword(creds.getPassword().toCharArray());
        }
      } else if (cb instanceof RealmCallback) {
        RealmCallback pc = (RealmCallback) cb;
        // must match hostname or listed in prop com.sun.security.sasl.digest.realm
        if (creds != null) {
          pc.setText(creds.getRealm());
        }
      } else {
        LOGGER.log(Level.SEVERE, "Server - unknown callback " + cb);
      }
    }
  }

}
