package net.gwerder.java.messagevortex.transport;

import java.nio.charset.StandardCharsets;
import java.security.Provider;
import java.util.Map;

import javax.security.auth.callback.*;
import javax.security.sasl.AuthorizeCallback;
import javax.security.sasl.Sasl;
import javax.security.sasl.SaslException;
import javax.security.sasl.SaslServer;
import javax.security.sasl.SaslServerFactory;


public class SaslPlainServer implements SaslServer {

    // Register security provider
    @SuppressWarnings("serial")
    public static class SecurityProvider extends Provider {
        public SecurityProvider() {
            super("SaslPlainServer", 1.0,"SASL PLAIN Authentication Server");
            put("SaslServerFactory.PLAIN", SaslPlainServerFactory.class.getName());
        }
    }

    // Factory class
    public static class SaslPlainServerFactory implements SaslServerFactory {
        @Override
        public SaslServer createSaslServer(String mech, String protocol, String serverName, Map<String, ?> props, CallbackHandler cbh) throws SaslException {
            return "PLAIN".equals( mech ) ? new SaslPlainServer( cbh ) : null;
        }

        @Override
        public String[] getMechanismNames(Map<String, ?> props) {
            if (props == null || "false".equals(props.get(Sasl.POLICY_NOPLAINTEXT).toString().toLowerCase())) {
                return new String[]{"PLAIN"};
            } else {
                return new String[0];
            }
        }
    }

    private CallbackHandler cbh;
    private boolean completed;
    private String authz;

    private SaslPlainServer( CallbackHandler callback ) {
        this.cbh = callback;
    }

    @Override
    public String getMechanismName() {
        return "PLAIN";
    }

    @Override
    public byte[] evaluateResponse(byte[] response) throws SaslException {
        if( response != null && response.length==0 ) {
            return new byte[0];
        }
        if (completed) {
            throw new IllegalStateException( "PLAIN already completed" );
        }
        if (response == null) {
            throw new IllegalArgumentException("Received null response");
        }
        try {
            String[] chunks = new String(response, StandardCharsets.UTF_8 ).split("\0", 3);

            if ( chunks.length != 3 ) {
                throw new IllegalArgumentException( "error parsing response (got "+chunks.length+" chunks out of "+response.length+")" );
            }
            if ( chunks[0]==null || chunks[0].isEmpty() ) {
                chunks[0] = chunks[1];
            }

            NameCallback nc = new NameCallback("SASL PLAIN");
            nc.setName(chunks[1]);
            PasswordCallback pc = new PasswordCallback("SASL PLAIN", false);
            pc.setPassword(chunks[2].toCharArray());
            AuthorizeCallback ac = new AuthorizeCallback(chunks[1], chunks[0]);
            cbh.handle(new Callback[] { nc, pc, ac });
            if (ac.isAuthorized()) {
                authz = ac.getAuthorizedID();
            }
        } catch (Exception e) {
            throw new SaslException("PLAIN auth failed: " + e.getMessage(), e );
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
            throw new IllegalStateException( "PLAIN authentication not yet completed");
        }
        return authz;
    }

    @Override
    public Object getNegotiatedProperty(String propName) {
        if (!completed) {
            throw new IllegalStateException( "PLAIN authentication not yet completed");
        }
        return "javax.security.sasl.qop".equals(propName) ? "auth" : null;
    }

    @Override
    public byte[] wrap(byte[] outgoing, int offset, int len) throws SaslException {
        throw new IllegalStateException( "PLAIN supports no integrity or privacy");
    }

    @Override
    public byte[] unwrap(byte[] incoming, int offset, int len) throws SaslException {
        throw new IllegalStateException( "PLAIN supports no integrity or privacy" );
    }

    @Override
    public void dispose() throws SaslException {
        cbh = null;
        authz = null;
    }
}