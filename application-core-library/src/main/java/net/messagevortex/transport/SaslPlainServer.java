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

import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.NameCallback;
import javax.security.auth.callback.PasswordCallback;
import javax.security.sasl.AuthorizeCallback;
import javax.security.sasl.Sasl;
import javax.security.sasl.SaslException;
import javax.security.sasl.SaslServer;
import javax.security.sasl.SaslServerFactory;
import java.nio.charset.StandardCharsets;
import java.security.Provider;
import java.util.Map;


public class SaslPlainServer implements SaslServer {

  // Register security provider
  @SuppressWarnings("serial")
  public static class SecurityProvider extends Provider {
    public SecurityProvider() {
      super("SaslPlainServer", 1.0, "SASL PLAIN Authentication Server");
      put("SaslServerFactory.PLAIN", SaslPlainServerFactory.class.getName());
    }
  }

  // Factory class
  public static class SaslPlainServerFactory implements SaslServerFactory {
    @Override
    public SaslServer createSaslServer(String mech, String protocol, String serverName,
                                       Map<String, ?> props, CallbackHandler cbh) {
      return "PLAIN".equals(mech) ? new SaslPlainServer(cbh) : null;
    }

    @Override
    public String[] getMechanismNames(Map<String, ?> props) {
      if (props == null
          || "false".equalsIgnoreCase(props.get(Sasl.POLICY_NOPLAINTEXT).toString())) {
        return new String[] {"PLAIN"};
      } else {
        return new String[0];
      }
    }
  }

  private CallbackHandler cbh;
  private boolean completed;
  private String authz;

  private SaslPlainServer(CallbackHandler callback) {
    this.cbh = callback;
  }

  @Override
  public String getMechanismName() {
    return "PLAIN";
  }

  @Override
  public byte[] evaluateResponse(byte[] response) throws SaslException {
    if (response != null && response.length == 0) {
      return new byte[0];
    }
    if (completed) {
      throw new IllegalStateException("PLAIN already completed");
    }
    if (response == null) {
      throw new IllegalArgumentException("Received null response");
    }
    try {
      String[] chunks = new String(response, StandardCharsets.UTF_8).split("\0", 3);

      if (chunks.length != 3) {
        throw new IllegalArgumentException("error parsing response (got " + chunks.length
            + " chunks out of " + response.length + ")");
      }
      if (chunks[0] == null || chunks[0].isEmpty()) {
        chunks[0] = chunks[1];
      }

      NameCallback nc = new NameCallback("SASL PLAIN");
      nc.setName(chunks[1]);
      PasswordCallback pc = new PasswordCallback("SASL PLAIN", false);
      pc.setPassword(chunks[2].toCharArray());
      AuthorizeCallback ac = new AuthorizeCallback(chunks[1], chunks[0]);
      cbh.handle(new Callback[] {nc, pc, ac});
      if (ac.isAuthorized()) {
        authz = ac.getAuthorizedID();
      }
    } catch (Exception e) {
      throw new SaslException("PLAIN auth failed: " + e.getMessage(), e);
    } finally {
      completed = true;
    }
    return null;
  }

  @Override
  public boolean isComplete() {
    return completed;
  }

  @Override
  public String getAuthorizationID() {
    if (!completed) {
      throw new IllegalStateException("PLAIN authentication not yet completed");
    }
    return authz;
  }

  @Override
  public Object getNegotiatedProperty(String propName) {
    if (!completed) {
      throw new IllegalStateException("PLAIN authentication not yet completed");
    }
    return "javax.security.sasl.qop".equals(propName) ? "auth" : null;
  }

  @Override
  public byte[] wrap(byte[] outgoing, int offset, int len) {
    throw new IllegalStateException("PLAIN supports no integrity or privacy");
  }

  @Override
  public byte[] unwrap(byte[] incoming, int offset, int len) {
    throw new IllegalStateException("PLAIN supports no integrity or privacy");
  }

  @Override
  public void dispose() {
    cbh = null;
    authz = null;
  }
}
