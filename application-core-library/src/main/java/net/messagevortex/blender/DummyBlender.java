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

import java.io.IOException;
import java.io.InputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.logging.Level;
import javax.activation.DataHandler;
import javax.mail.BodyPart;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.Part;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import javax.mail.util.ByteArrayDataSource;
import net.messagevortex.Config;
import net.messagevortex.MessageVortex;
import net.messagevortex.MessageVortexLogger;
import net.messagevortex.Version;
import net.messagevortex.asn1.BlendingSpec;
import net.messagevortex.asn1.IdentityStore;
import net.messagevortex.asn1.VortexMessage;
import net.messagevortex.asn1.encryption.DumpType;
import net.messagevortex.transport.Transport;
import net.messagevortex.transport.dummy.DummyTransportTrx;

/***
 * This Dummy blender supports only plain blending without an offset
 */
public class DummyBlender extends Blender {

  private static final java.util.logging.Logger LOGGER;

  static {
    LOGGER = MessageVortexLogger.getLogger((new Throwable()).getStackTrace()[0].getClassName());
    MessageVortexLogger.setGlobalLogLevel(Level.ALL);
  }

  private String identity;
  private Transport transport;
  private BlendingReceiver router;
  private IdentityStore identityStore;

  /**
   * <p>A dummy blender implementation.</p>
   *
   * @param section the config foile section to be used to configure
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
      //Session session = Session.getDefaultInstance(new Properties(), null);
      Session session = Session.getInstance(new Properties(),
          new javax.mail.Authenticator() {
            protected PasswordAuthentication getPasswordAuthentication() {
              return new PasswordAuthentication("username", "password");
            }
          }
      );
      final MimeMessage mimeMsg = new MimeMessage(session);
      mimeMsg.setFrom(new InternetAddress("test@test.com"));
      mimeMsg.setRecipient(Message.RecipientType.TO,
              new InternetAddress(target.getRecipientAddress()));
      mimeMsg.setSubject("VortexMessage");
      mimeMsg.setHeader("User-Agent:",
              "MessageVortex/" + Version.getStringVersion());
      MimeMultipart content = new MimeMultipart("mixed");

      // body
      MimeBodyPart body = new MimeBodyPart();
      body.setText("This is a VortexMessage");
      content.addBodyPart(body);

      //create attachment
      MimeBodyPart attachment = new MimeBodyPart();
      ByteArrayDataSource source = new ByteArrayDataSource(msg.toBytes(DumpType.PUBLIC_ONLY),
              "application/octet-stream");
      attachment.setDataHandler(new DataHandler(source));
      attachment.setFileName("messageVortex.raw");
      content.addBodyPart(attachment);

      mimeMsg.setContent(content);
      if (transport != null) {
        final PipedOutputStream os = new PipedOutputStream();
        // FIXME catch error values
        Thread t = new Thread() {
          public void run() {
            try {
              mimeMsg.writeTo(os);
              os.close();
            } catch (IOException | MessagingException ioe) {
              LOGGER.log(Level.WARNING, "Error while sending message", ioe);
            }
          }
        };
        t.setName("MessageInDummyBlenderTransportThread");
        t.start();
        PipedInputStream inp = new PipedInputStream(os);

        // send
        transport.sendMessage(target.getRecipientAddress(), inp);
        return true;
      } else {
        return false;
      }
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
  public boolean gotMessage(final InputStream is) {
    try {
      Session session = Session.getInstance(new Properties(),
          new javax.mail.Authenticator() {
            protected PasswordAuthentication getPasswordAuthentication() {
              return new PasswordAuthentication("username", "password");
            }
          }
      );
      MimeMessage msg = new MimeMessage(session, is);
      is.close();
      VortexMessage vmsg = null;
      List<InputStream> isl = getAttachments(msg);
      if (isl == null) {
        isl = new ArrayList<>();
      }
      for (InputStream attaStream : isl) {
        try {
          vmsg = new VortexMessage(attaStream, identityStore.getHostIdentity());
          LOGGER.log(Level.INFO, "Found attachment WITH VortexMessage contained");
        } catch (IOException io) {
          // This exception will occur if no vortex message is contained
          LOGGER.log(Level.INFO, "Found attachment with no VortexMessage contained", io);
        }
        attaStream.close();
      }
      return router.gotMessage(vmsg);
    } catch (IOException | MessagingException ioe) {
      LOGGER.log(Level.WARNING, "Exception while getting and parsing message", ioe);
      return false;
    }
  }

  private List<InputStream> getAttachments(Message message) throws MessagingException, IOException {
    Object content = message.getContent();
    if (content instanceof String) {
      return null;
    }

    if (content instanceof Multipart) {
      Multipart multipart = (Multipart) content;
      List<InputStream> result = new ArrayList<InputStream>();

      for (int i = 0; i < multipart.getCount(); i++) {
        result.addAll(getAttachments(multipart.getBodyPart(i)));
      }
      return result;

    }
    return null;
  }

  private List<InputStream> getAttachments(BodyPart part) throws IOException, MessagingException {
    List<InputStream> result = new ArrayList<InputStream>();
    Object content = part.getContent();
    if (content instanceof InputStream || content instanceof String) {
      if (Part.ATTACHMENT.equalsIgnoreCase(part.getDisposition())
              || ! "".equals(part.getFileName())) {
        result.add(part.getInputStream());
        return result;
      } else {
        return new ArrayList<InputStream>();
      }
    }

    if (content instanceof Multipart) {
      Multipart multipart = (Multipart) content;
      for (int i = 0; i < multipart.getCount(); i++) {
        BodyPart bodyPart = multipart.getBodyPart(i);
        result.addAll(getAttachments(bodyPart));
      }
    }
    return result;
  }

}
