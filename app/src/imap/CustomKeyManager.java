package net.gwerder.java.mailvortex.imap;
 
import java.security.cert.X509Certificate;
import javax.net.ssl.X509KeyManager;
import java.security.KeyStore;
import java.io.IOException;
import javax.net.ssl.SSLContext;
import java.security.PrivateKey;
import javax.net.ssl.X509KeyManager;
import java.security.GeneralSecurityException;
import java.security.Principal;
import java.io.FileInputStream;
import java.io.InputStream;
import java.net.Socket;

/***
 * Keymanager enables specification of key alias to be used.
 *
 * @to.do support multiple aliases
 ***/ 
public class CustomKeyManager implements X509KeyManager {
    private KeyStore keyStore;
    private String alias;
	char[] password;

	/**
     * Convenience constructor.
     *
     * @param keyStoreFile      name of the JKS keystore file
     * @param password      	password to open the kestore file 
     * @param alias      		alias of the certificate to be used
     ***/
    CustomKeyManager(String keyStoreFile, String password, String alias) throws IOException, GeneralSecurityException {
		this(keyStoreFile,password.toCharArray(),alias);
	}
	
	/**
     * Default constructor.
     *
     * @param keyStoreFile      name of the JKS keystore file
     * @param password      	password to open the kestore file 
     * @param alias      		alias of the certificate to be used
     ***/
    CustomKeyManager(String keyStoreFile, char[] password, String alias) throws IOException, GeneralSecurityException {
		this.password=password;
        this.alias = alias;
        keyStore = KeyStore.getInstance(KeyStore.getDefaultType());
        keyStore.load(new FileInputStream(keyStoreFile), password);
    }

	/**
     * Obtain private key from keystore
     *
     * @param alias      		alias of the certificate to be used
     ***/
    public PrivateKey getPrivateKey(String alias) {
        try {
			return (PrivateKey) keyStore.getKey(alias, password);
		} catch (Exception e) {
			return null;
		}
    }

	/**
     * Obtain certificate chain of a certificate from keystore
     *
     * @param alias      		alias of the certificate to be used
     ***/
    public X509Certificate[] getCertificateChain(String alias) {
        try {
            java.security.cert.Certificate[] certs = keyStore.getCertificateChain(alias);
            if (certs == null || certs.length == 0)	return null;
			// copy and typcast array
            X509Certificate[] x509 = new X509Certificate[certs.length];
            for (int i = 0; i < certs.length; i++) x509[i] = (X509Certificate)certs[i];
			return x509;
		} catch (Exception e) {
			return null;
		}          
    }

	/**
     * Alias choser always returning the desired alias.
     *
     * @param keyType		type of key to be looked for
     * @param issuers     	issuers accepted
     * @param socket    	socket requiring the certificate
     ***/
    public String chooseServerAlias(String keyType, Principal[] issuers, Socket socket) {
        return alias;
    }

	/**
     * Dummy methode returning UnsupportedOperationException (as this is a "server only" implementation)
     *
     * @param param1	dummy
     * @param param2	dummy
     ***/
    public String[] getClientAliases(String param1, Principal[] param2) {
        throw new UnsupportedOperationException("Method getClientAliases() not implemented. Server Socket only Manager");
    }

	/**
     * Dummy methode returning UnsupportedOperationException (as this is a "server only" implementation)
     *
     * @param param1	dummy
     * @param param2	dummy
     * @param param3	dummy
     ***/
    public String chooseClientAlias(String[] param1, Principal[] param2, Socket param3) {
        throw new UnsupportedOperationException("Method chooseClientAlias() not implemented. Server Socket only Manager");
    }

	/**
     * Dummy method always returning the preselected alias
     *
     * @param param1	dummy
     * @param param2	dummy
     ***/
    public String[] getServerAliases(String param1, Principal[] param2) {
        return new String[] { alias };
    }

	/**
     * Dummy method always returning the preselected alias
     *
     * @param param1	dummy
     * @param param2	dummy
     ***/
    public String chooseServerAlias(String param1, Principal[] param2) {
        return alias;
    }
}