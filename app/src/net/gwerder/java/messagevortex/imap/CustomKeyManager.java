package net.gwerder.java.messagevortex.imap;
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

import javax.net.ssl.X509KeyManager;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.Socket;
import java.security.GeneralSecurityException;
import java.security.KeyStore;
import java.security.Principal;
import java.security.PrivateKey;
import java.security.cert.X509Certificate;
import java.util.logging.Level;
import java.util.logging.Logger;

/***
 * Keymanager enables specification of key alias to be used.
 ***/
public class CustomKeyManager implements X509KeyManager {

    private static final Logger LOGGER;
    static {
        LOGGER = Logger.getLogger((new Throwable()).getStackTrace()[0].getClassName());
    }

    private KeyStore keyStore;
    private String alias;
    char[] password;

    /**
     * Convenience constructor.
     *
     * @param keyStoreFile      name of the JKS keystore file
     * @param password          password to open the kestore file 
     * @param alias              alias of the certificate to be used
     ***/
    public CustomKeyManager(String keyStoreFile, String password, String alias) throws GeneralSecurityException {
        this(keyStoreFile,password.toCharArray(),alias);
    }
    
    /**
     * Default constructor.
     *
     * @param keyStoreFile      name of the JKS keystore file
     * @param password          password to open the kestore file 
     * @param alias              alias of the certificate to be used
     ***/
    CustomKeyManager(String keyStoreFile, char[] password, String alias) throws GeneralSecurityException {
        this.password=password;
        this.alias = alias;
        keyStore = KeyStore.getInstance(KeyStore.getDefaultType());
        FileInputStream is;
        try{
          is=new FileInputStream(keyStoreFile);
          keyStore.load(is, password);
            is.close();
        } catch(IOException ioe) {
            throw new GeneralSecurityException("IOException while loading keystore", ioe);
        }

        if(getPrivateKey(alias)==null) {
            throw new GeneralSecurityException("requested alias not found in keystore");
        }
    }

    /**
     * Obtain private key from keystore
     *
     * @param alias              alias of the certificate to be used
     ***/
    public PrivateKey getPrivateKey(String alias) {
        try {
            return (PrivateKey) keyStore.getKey(alias, password);
        } catch (Exception e) {
            LOGGER.log(Level.WARNING,"unknown key requested \""+alias+"\"",e);
            return null;
        }
    }

    /**
     * Obtain certificate chain of a certificate from keystore
     *
     * @param alias              alias of the certificate to be used
     ***/
    public X509Certificate[] getCertificateChain(String alias) {
        try {
            java.security.cert.Certificate[] certs = keyStore.getCertificateChain(alias);
            if (certs == null || certs.length == 0)    {
                // was a null return val documentation is unclear
                return new X509Certificate[0]; 
            }
            
            // copy and typcast array
            X509Certificate[] x509 = new X509Certificate[certs.length];
            for (int i = 0; i < certs.length; i++) {
                x509[i] = (X509Certificate)certs[i];
            }
            return x509;
        } catch (Exception e) {
            LOGGER.log(Level.WARNING,"unknown key requested \""+alias+"\"",e);
            // was a null return val documentation is unclear
            return new X509Certificate[0]; 
        }          
    }

    /**
     * Alias choser always returning the desired alias.
     *
     * @param keyType        type of key to be looked for
     * @param issuers         issuers accepted
     * @param socket        socket requiring the certificate
     ***/
    public String chooseServerAlias(String keyType, Principal[] issuers, Socket socket) {
        return alias;
    }

    /**
     * Dummy methode returning UnsupportedOperationException (as this is a "server only" implementation)
     *
     * @param param1    dummy
     * @param param2    dummy
     ***/
    public String[] getClientAliases(String param1, Principal[] param2) {
        throw new UnsupportedOperationException("Method getClientAliases() not implemented. Server Socket only Manager");
    }

    /**
     * Dummy methode returning UnsupportedOperationException (as this is a "server only" implementation)
     *
     * @param param1    dummy
     * @param param2    dummy
     * @param param3    dummy
     ***/
    public String chooseClientAlias(String[] param1, Principal[] param2, Socket param3) {
        throw new UnsupportedOperationException("Method chooseClientAlias() not implemented. Server Socket only Manager");
    }

    /**
     * Dummy method always returning the preselected alias
     *
     * @param param1    dummy
     * @param param2    dummy
     ***/
    public String[] getServerAliases(String param1, Principal[] param2) {
        return new String[] { alias };
    }

    /**
     * Dummy method always returning the preselected alias
     *
     * @param param1    dummy
     * @param param2    dummy
     ***/
    public String chooseServerAlias(@SuppressWarnings("UnusedParameters") String param1, @SuppressWarnings("UnusedParameters") Principal[] param2) {
        return alias;
    }
}