package net.gwerder.java.messagevortex.imap;
 
import javax.net.ssl.X509TrustManager;
import java.security.cert.X509Certificate;

public class AllTrustManager implements X509TrustManager {     
    
    public X509Certificate[] getAcceptedIssuers() { 
        return new X509Certificate[0];    
    }
    
    public void checkClientTrusted(X509Certificate[] certs, String authType) {
		// no certificate to be verified as we trust all certs
    }
    
    public void checkServerTrusted( X509Certificate[] certs, String authType) {
		// no certificate to be verified as we trust all certs
    }
    
} 
