package net.gwerder.java.messagevortex.transport;

import net.gwerder.java.messagevortex.MessageVortexLogger;

import javax.net.ssl.KeyManager;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManagerFactory;
import java.io.IOException;
import java.security.*;
import java.security.cert.CertificateException;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created by Martin on 11.03.2018.
 */
public class SecurityContext {

  private static final Logger LOGGER;

  static {
    LOGGER = MessageVortexLogger.getLogger((new Throwable()).getStackTrace()[0].getClassName());
  }


  private SecurityRequirement requirement = SecurityRequirement.STARTTLS;
  private SSLContext context = null;
  private Set<String> supportedCiphers = new HashSet<>();

  public SecurityContext() {
  }

  public SecurityContext(SecurityRequirement requirement) {
    setRequirement(requirement);
  }

  public SecurityContext(SSLContext context) {
    setRequirement(null);
    setContext(context);
  }

  public SecurityContext(SSLContext context, SecurityRequirement req) {
    setRequirement(req);
    setContext(context);
  }

  private void init() {
    try {
      if (context == null) {
        // FIXME broken part ... replace with selfsigned host cert
        context = SSLContext.getInstance("TLS");
        KeyManagerFactory keyManager = KeyManagerFactory.getInstance("SunX509");
        KeyStore keyStore = getSelfsignedKeyStore();
        keyManager.init(keyStore, "changeme".toCharArray());
        TrustManagerFactory trustManagerFactory = TrustManagerFactory.getInstance("SunX509");
        trustManagerFactory.init(keyStore);
        context.init(new KeyManager[]{new CustomKeyManager("keystore.jks", "changeme", "mykey3")}, trustManagerFactory.getTrustManagers(), new SecureRandom()); //new TrustManager[]{new AllTrustManager()}
      }
    } catch (Exception e) {
      LOGGER.log(Level.WARNING, "Exception while creating SecurityContext", e);
    }
  }

  private KeyStore getSelfsignedKeyStore() {
    KeyStore keyStore = null;
    try {
      String commonName = "MessageVortex";
      String organizationalUnit = "MessageVortex";
      String organization = "none";
      String city = "none";
      String state = "none";
      String country = "none";
      int keysize = 2048;

      String alias = "selfsigned";
      char[] keyPass = "changeme".toCharArray();

      int validity = 356 * 10;

      keyStore = KeyStore.getInstance("JKS");
      keyStore.load(null, null);

            /*CertAndKeyGen keypair = new CertAndKeyGen("RSA", "SHA256WithRSA", null);

            X500Name x500Name = new X500Name(commonName, organizationalUnit, organization, city, state, country);

            keypair.generate(keysize);
            PrivateKey privKey = keypair.getPrivateKey();

            X509Certificate[] chain = new X509Certificate[] {
                    keypair.getSelfCertificate( x500Name, new Date(), (long) validity * 24 * 60 * 60 )
            };

            keyStore.setKeyEntry(alias, privKey, keyPass, chain);

            */ //FIXME undocumented API ... solve properly
    } catch (KeyStoreException | NoSuchAlgorithmException | IOException | CertificateException e) {
      LOGGER.log(Level.WARNING, "Exception while creating keystore", e);
    }
    return keyStore;
  }

  public SSLContext getContext() {
    if (context == null) {
      // init is broken init();
    }
    return context;
  }

  public SSLContext setContext(SSLContext context) {
    SSLContext ret = this.context;
    this.context = context;
    return ret;
  }

  public SecurityRequirement getRequirement() {
    return requirement;
  }

  public SecurityRequirement setRequirement(SecurityRequirement requirement) {
    SecurityRequirement ret = this.requirement;
    this.requirement = requirement;
    return ret;
  }

  public boolean isCipherSupported(String name) {
    return supportedCiphers.contains(name);
  }

  public Set<String> getSupportedCiphers() {
    Set<String> ret = new HashSet<>();
    ret.addAll(supportedCiphers);
    return ret;
  }

}
