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
 * @todo support multiple aliases
 ***/ 
public class CustomKeyManager implements X509KeyManager {
    private KeyStore keyStore;
    private String alias;
	char[] password;

    CustomKeyManager(String keyStoreFile, String password, String alias)
        throws IOException, GeneralSecurityException
	{
		this(keyStoreFile,password.toCharArray(),alias);
	}
	
    CustomKeyManager(String keyStoreFile, char[] password, String alias)
        throws IOException, GeneralSecurityException
    {
		this.password=password;
        this.alias = alias;
        keyStore = KeyStore.getInstance(KeyStore.getDefaultType());
        keyStore.load(new FileInputStream(keyStoreFile), password);
    }

    public PrivateKey getPrivateKey(String alias) 
	{
        try {
			return (PrivateKey) keyStore.getKey(alias, password);
		} catch (Exception e) {
			return null;
		}
    }

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

    public String chooseServerAlias(String keyType, Principal[] issuers,
                                    Socket socket) {
        return alias;
    }

    public String[] getClientAliases(String parm1, Principal[] parm2) {
        throw new UnsupportedOperationException("Method getClientAliases() not implemented. Server Socket only Manager");
    }

    public String chooseClientAlias(String keyTypes[], Principal[] issuers, Socket socket) {
        throw new UnsupportedOperationException("Method chooseClientAlias() not implemented. Server Socket only Manager");
    }

    public String[] getServerAliases(String parm1, Principal[] parm2) {
        return new String[] { alias };
    }

    public String chooseServerAlias(String parm1, Principal[] parm2) {
        return alias;
    }
}