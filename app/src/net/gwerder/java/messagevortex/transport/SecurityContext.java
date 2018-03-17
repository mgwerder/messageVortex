package net.gwerder.java.messagevortex.transport;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by Martin on 11.03.2018.
 */
public class SecurityContext {

    private SecurityRequirement requirement = SecurityRequirement.STARTTLS;
    private SSLContext context;
    private Set<String> supportedCiphers = new HashSet<>();

    public SecurityContext() {
        try {
            context = SSLContext.getInstance("TLS");
            context.init(null, new TrustManager[]{new AllTrustManager()}, new SecureRandom());
        } catch( NoSuchAlgorithmException|KeyManagementException e ) {
            // should never happen
        }
    }

    public SecurityContext( SecurityRequirement requirement ) {
        this();
        this.requirement = requirement;
    }

    public SSLContext getContext() {
        return context;
    }

    public SSLContext setContext( SSLContext context ) {
        SSLContext ret = this.context;
        this.context = context;
        return ret;
    }

    public SecurityRequirement getRequirement() {
        return requirement;
    }

    public SecurityRequirement setRequirement( SecurityRequirement requirement ) {
        SecurityRequirement ret = this.requirement;
        this.requirement = requirement;
        return ret;
    }

    public boolean isCipherSupported( String name ) {
        return supportedCiphers.contains( name );
    }

    public Set<String> getSupportedCiphers() {
        Set<String> ret = new HashSet<>();
        ret.addAll( supportedCiphers );
        return ret;
    }

}
