package net.gwerder.java.messagevortex.transport;
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

    public X509Certificate getServerCert() {
        return serverCert;
    }

    public X509Certificate setServerCert( X509Certificate serverCert ) {
        X509Certificate ret=this.serverCert;
        this.serverCert=serverCert;
        return ret;
    }

    public X509Certificate getClientCert() {
        return clientCert;
    }

    public X509Certificate setClientCert( X509Certificate clientCert ) {
        X509Certificate ret=this.clientCert;
        this.clientCert=clientCert;
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
