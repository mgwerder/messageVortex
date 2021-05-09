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
import javax.net.ssl.SSLEngine;
import javax.net.ssl.X509ExtendedKeyManager;
import javax.net.ssl.X509KeyManager;
import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;
import java.security.GeneralSecurityException;
import java.security.KeyStore;
import java.security.Principal;
import java.security.PrivateKey;
import java.security.cert.X509Certificate;
import java.util.logging.Level;
import java.util.logging.Logger;

/***
 * <p>Keymanager enables specification of key alias to be used.</p>
 ***/
public class CustomKeyManager extends X509ExtendedKeyManager implements KeyManager, X509KeyManager {

  private static final Logger LOGGER;

  static {
    LOGGER = MessageVortexLogger.getLogger((new Throwable()).getStackTrace()[0].getClassName());
  }

  private final KeyStore keyStore;
  private final String alias;
  char[] password;

  /**
   * <p>Convenience constructor.</p>
   *
   * @param keyStoreFile name of the JKS keystore file
   * @param password     password to open the kestore file
   * @param alias        alias of the certificate to be used
   * @throws GeneralSecurityException if keystore generation fails
   */
  public CustomKeyManager(String keyStoreFile, String password, String alias)
          throws GeneralSecurityException {
    this(keyStoreFile, password.toCharArray(), alias);
  }

  /**
   * <p>Default constructor.</p>
   *
   * @param keyStoreFile name of the JKS keystore file
   * @param password     password to open the kestore file
   * @param alias        alias of the certificate to be used
   * @throws GeneralSecurityException if alias is not in keystore
   */
  CustomKeyManager(String keyStoreFile, char[] password, String alias)
          throws GeneralSecurityException {
    this.password = password;
    this.alias = alias;
    // KeyStore.getDefaultType()
    keyStore = KeyStore.getInstance("JKS");
    try {
      try (InputStream is = this.getClass().getClassLoader().getResourceAsStream(keyStoreFile)) {
        keyStore.load(is, password);
      }
    } catch (IOException ioe) {
      throw new GeneralSecurityException("IOException while loading keystore \"" + keyStoreFile
              + "\" with password " + new String(password), ioe);
    }

    if (getPrivateKey(alias) == null) {
      throw new GeneralSecurityException("requested alias not found in keystore");
    }
  }

  /**
   * <p>Obtain private key from keystore.</p>
   *
   * @param alias alias of the certificate to be used
   */
  public final PrivateKey getPrivateKey(String alias) {
    try {
      LOGGER.log(Level.INFO, "key for \"" + alias + "\" requested ",
              new Object[]{keyStore.getKey(alias, password)});
      return (PrivateKey) keyStore.getKey(alias, password);
    } catch (Exception e) {
      LOGGER.log(Level.WARNING, "unknown key requested \"" + alias + "\"", e);
      return null;
    }
  }

  /**
   * <p>Obtain certificate chain of a certificate from keystore.</p>
   *
   * @param alias alias of the certificate to be used
   */
  public X509Certificate[] getCertificateChain(String alias) {
    try {
      java.security.cert.Certificate[] certs = keyStore.getCertificateChain(alias);
      LOGGER.log(Level.INFO, "key chain for \"" + alias + "\" requested ", new Object[]{certs});
      if (certs == null || certs.length == 0) {
        // was a null return val documentation is unclear
        return new X509Certificate[0];
      }

      // copy and typcast array
      X509Certificate[] x509 = new X509Certificate[certs.length];
      for (int i = 0; i < certs.length; i++) {
        x509[i] = (X509Certificate) certs[i];
      }
      return x509;
    } catch (Exception e) {
      LOGGER.log(Level.WARNING, "unknown key requested \"" + alias + "\"", e);
      // was a null return val documentation is unclear
      return new X509Certificate[0];
    }
  }

  /**
   * <p>Dummy method always returning the preselected alias.</p>
   *
   * @param param1 dummy
   * @param param2 dummy
   * @return always return a list of one with the preselected alias
   */
  public String[] getClientAliases(@SuppressWarnings("UnusedParameters") String param1,
                                   @SuppressWarnings("UnusedParameters") Principal[] param2) {
    LOGGER.log(Level.INFO, "client alias list for  \"" + alias + "\" requested ");
    return new String[]{alias};
  }

  public String chooseEngineClientAlias(String[] keyType, Principal[] issuers, SSLEngine engine) {
    return chooseClientAlias(null, null, null);
  }

  public String chooseEngineServerAlias(String keyType, Principal[] issuers, SSLEngine engine) {
    return chooseServerAlias(null, null, null);
  }

  /***
   * <p>Dummy method always returning the preselected alias.</p>
   *
   * @param param1 dummy
   * @param param2 dummy
   * @param param3 dummy
   */
  public String chooseClientAlias(String[] param1, Principal[] param2, Socket param3) {
    LOGGER.log(Level.INFO, "client alias for  \"" + alias + "\" requested ");
    return alias;
  }

  /**
   * <p>Dummy method always returning the preselected alias.</p>
   *
   * @param param1 dummy
   * @param param2 dummy
   */
  public String[] getServerAliases(String param1, Principal[] param2) {
    LOGGER.log(Level.INFO, "server alias list for  \"" + alias + "\" requested ");
    return new String[]{alias};
  }

  /**
   * <p>Alias choser always returning the desired alias.</p>
   *
   * @param keyType type of key to be looked for
   * @param issuers issuers accepted
   * @param socket  socket requiring the certificate
   */
  public String chooseServerAlias(String keyType, Principal[] issuers, Socket socket) {
    LOGGER.log(Level.INFO, "server alias (1) for \"" + alias + "\" requested ");
    return alias;
  }

  /**
   * <p>Dummy method always returning the preselected alias.</p>
   *
   * @param param1 dummy
   * @param param2 dummy
   * @return always returns the preset alias
   */
  public String chooseServerAlias(@SuppressWarnings("UnusedParameters") String param1,
                                  @SuppressWarnings("UnusedParameters") Principal[] param2) {
    LOGGER.log(Level.INFO, "server alias (2) for \"" + alias + "\" requested ");
    return alias;
  }
}
