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

import net.messagevortex.MessageVortexLogger;

import javax.net.ssl.KeyManager;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManagerFactory;
import java.io.IOException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
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
  private final Set<String> supportedCiphers = new HashSet<>();

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
        context.init(
                new KeyManager[]{new CustomKeyManager("keystore.jks", "changeme", "mykey3")},
                trustManagerFactory.getTrustManagers(),
                new SecureRandom()); //new TrustManager[]{new AllTrustManager()}
      }
    } catch (Exception e) {
      LOGGER.log(Level.WARNING, "Exception while creating SecurityContext", e);
    }
  }

  private KeyStore getSelfsignedKeyStore() {
    //FIXME incomplete
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

      X500Name x500Name = new X500Name(commonName, organizationalUnit, organization,
                                       city, state, country);

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

  /***
   * <p>Gets the currently set SSL context.</p>
   * @return the currently set context
   */
  public SSLContext getContext() {
    if (context == null) {
      // init is broken init();
    }
    return context;
  }

  /***
   * <p>Sets the SSL context to be used.</p>
   *
   * @param context the SSL context to be set
   * @return the previously set context
   */
  public final SSLContext setContext(SSLContext context) {
    SSLContext ret = this.context;
    this.context = context;
    return ret;
  }

  public SecurityRequirement getRequirement() {
    return requirement;
  }

  /***
   * <p>Sets the necessities of the security context.</p>
   *
   * @param requirement the requrement to be achieved
   * @return the previously set requirement
   */
  public final SecurityRequirement setRequirement(SecurityRequirement requirement) {
    SecurityRequirement ret = this.requirement;
    this.requirement = requirement;
    return ret;
  }


  public boolean isCipherSupported(String name) {
    return supportedCiphers.contains(name);
  }

  /***
   * <p>Gets all the supported ciphers.</p>
   *
   * @return the requested set of strings
   */
  public Set<String> getSupportedCiphers() {
    Set<String> ret = new HashSet<>();
    ret.addAll(supportedCiphers);
    return ret;
  }

}
