package net.messagevortex.blender;

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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.logging.Level;
import javax.activation.DataHandler;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.Session;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import javax.mail.util.ByteArrayDataSource;
import jdk.nashorn.internal.runtime.Version;
import net.messagevortex.Config;
import net.messagevortex.MessageVortex;
import net.messagevortex.MessageVortexLogger;
import net.messagevortex.asn1.BlendingSpec;
import net.messagevortex.asn1.IdentityStore;
import net.messagevortex.asn1.VortexMessage;
import net.messagevortex.asn1.encryption.DumpType;
import net.messagevortex.transport.Transport;
import net.messagevortex.transport.dummy.DummyTransportTrx;

public class DummyBlender extends Blender {

  private static final java.util.logging.Logger LOGGER;

  static {
    LOGGER = MessageVortexLogger.getLogger((new Throwable()).getStackTrace()[0].getClassName());
    MessageVortexLogger.setGlobalLogLevel(Level.ALL);
  }

  String identity;
  Transport transport;
  BlendingReceiver router;
  IdentityStore identityStore;

  /**
   * <p>A dummy blender implementation.</p>
   *
   * @param section    the config foile section to be used to configure
   * @throws IOException if anything fails :-D
   */
  public DummyBlender(String section) throws IOException {
    // This is a dummy constructor which breaks the implementation ->
    // FIXME add sensible identity store
    this(
            null,
            MessageVortex.getRouter(Config.getDefault().getStringValue(section, "router")),
            new IdentityStore()
    );
  }

  /***
   * <p>Creates a passthru blender which abstracts a local transport media.</p>
   *
   * @param identity        the identity (receiver/sender address)
   * @param router          the router layer to be used
   * @param identityStore   the identity store to be used (for decryption of headers)
   * @throws IOException    if anything fails :-D
   */
  public DummyBlender(String identity, BlendingReceiver router, IdentityStore identityStore)
          throws IOException {
    super(router, null);
    this.identity = identity;
    if (identity != null) {
      this.transport = new DummyTransportTrx(identity, this);
    } else {
      transport = null;
    }
    this.router = router;
    if (identityStore == null) {
      throw new NullPointerException("identitystore may not be null");
    }
    this.identityStore = identityStore;
  }

  @Override
  public String getBlendingAddress() {
    return this.identity;
  }

  @Override
  public boolean blendMessage(BlendingSpec target, VortexMessage msg) {
    // encode message in clear readable and send it
    try {
      Session session = Session.getDefaultInstance(new Properties(), null);
      MimeMessage mimeMsg = new MimeMessage(session);
      mimeMsg.setRecipient(Message.RecipientType.TO, new InternetAddress(target.getRecipientAddress()));
      mimeMsg.setSubject("VortexMessage");
      mimeMsg.setHeader("User-Agent:", "MessageVortex/"+ Version.fullVersion());
      Multipart content = new MimeMultipart();

      // create body
      MimeBodyPart bodyPart = new MimeBodyPart();
      bodyPart.setText("This is a VortexMessage");
      content.addBodyPart(bodyPart);

      //create attachment
      MimeBodyPart attachment = new MimeBodyPart();
      ByteArrayDataSource source = new ByteArrayDataSource(msg.toBytes(DumpType.PUBLIC_ONLY),"application/octet-stream");
      attachment.setDataHandler(new DataHandler(source));
      attachment.setFileName("messageVortex.raw");
      content.addBodyPart(attachment);

      mimeMsg.setContent(content);

      // send
      transport.sendMessage(target.getRecipientAddress(), mimeMsg.getRawInputStream());
      return true;
    } catch (AddressException ae) {
      LOGGER.log(Level.SEVERE, "Error when setting address", ae);
    } catch (MessagingException me) {
      LOGGER.log(Level.SEVERE, "Error when composing message", me);
    } catch (IOException ioe) {
      LOGGER.log(Level.SEVERE, "Unable to send to transport endpoint "
              + target.getRecipientAddress(), ioe);
    }
    return false;
  }

  @Override
  public boolean gotMessage(InputStream is) {
    try {
      ByteArrayOutputStream buffer = new ByteArrayOutputStream();
      int numBytesRead;
      byte[] data = new byte[16384];
      while ((numBytesRead = is.read(data, 0, data.length)) != -1) {
        buffer.write(data, 0, numBytesRead);
      }
      buffer.flush();
      return router.gotMessage(new VortexMessage(buffer.toByteArray(),
              identityStore.getHostIdentity()));
    } catch (IOException ioe) {
      return false;
    }
  }

}
