package net.gwerder.java.messagevortex.transport;

import java.security.cert.X509Certificate;

import static net.gwerder.java.messagevortex.transport.SecurityRequirement.SSLTLS;

/**
 * Created by Martin on 23.01.2018.
 */
public class Credentials {

    String username=null;
    String password=null;

    SecurityRequirement requirement=SSLTLS;

    X509Certificate clientCert=null;
    X509Certificate serverCert=null;

    public Credentials( String username, String password ) {
        this.username=username;
        this.password=password;
    }

    public Credentials(String username, String password, SecurityRequirement requirement ) {
        this( username, password );
        this.requirement=requirement;
    }

    public String getUsername() {
        return username;
    }

    public String setUsername(String username) {
        String ret=this.username;
        this.username=username;
        return ret;
    }

    public String getPassword() {
        return password;
    }

    public String setPassword(String password) {
        String ret=this.password;
        this.password=password;
        return ret;
    }

    public SecurityRequirement setSecurityRequirement( SecurityRequirement req ) {
        SecurityRequirement ret=this.requirement;
        this.requirement=req;
        return ret;
    }

    public SecurityRequirement getSecurityRequirement() {
        return this.requirement;
    }

}
