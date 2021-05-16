package net.messagevortex.blender;

import net.messagevortex.Config;
import net.messagevortex.MessageVortexLogger;
import net.messagevortex.MessageVortexRepository;
import net.messagevortex.Version;
import net.messagevortex.accounting.Accountant;
import net.messagevortex.asn1.BlendingSpec;
import net.messagevortex.asn1.IdentityStore;
import net.messagevortex.asn1.IdentityStoreBlock;
import net.messagevortex.asn1.InnerMessageBlock;
import net.messagevortex.asn1.PrefixBlock;
import net.messagevortex.asn1.RoutingCombo;
import net.messagevortex.asn1.VortexMessage;
import net.messagevortex.asn1.encryption.DumpType;
import net.messagevortex.blender.recipes.BlenderRecipe;
import net.messagevortex.transport.Transport;
import net.messagevortex.transport.dummy.DummyTransportTrx;

import javax.activation.DataHandler;
import javax.mail.Address;
import javax.mail.Authenticator;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import javax.mail.util.ByteArrayDataSource;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.util.Properties;
import java.util.Set;
import java.util.logging.Level;

public class InitialRecipesBlender extends Blender {
  private static final java.util.logging.Logger LOGGER;

  static {
    LOGGER = MessageVortexLogger.getLogger((new Throwable()).getStackTrace()[0].getClassName());
    //MessageVortexLogger.setGlobalLogLevel(Level.ALL);
  }

  private final String identity;
  private final Transport transport;
  private final BlendingReceiver router;
  private IdentityStore identityStore;
  private int anonSetSize = 5;

  /**
   * <p>An initial blender implementation based on anonymity recipes.</p>
   *
   * @param section the config foile section to be used to configure
   * @throws IOException if anything fails :-D
   */
  public InitialRecipesBlender(String section) throws IOException {
    this(
        Config.getDefault().isDefaultValue(section, "node_identity") ? null :
            Config.getDefault().getStringValue(section, "node_identity"),
        MessageVortexRepository.getRouter("",
            Config.getDefault().getSectionValue(section, "router")),
        MessageVortexRepository.getIdentityStore("",
            Config.getDefault().getSectionValue(section, "identity_store")),
        MessageVortexRepository.getAccountant("",
            Config.getDefault().getSectionValue(section, "accountant")
        )

    );
  }

  /***
   * <p>Creates a passthru blender which abstracts a local transport media.</p>
   *
   * @param identity        the identity (receiver/sender address)
   * @param router          the router layer to be used
   * @param identityStore   the identity store to be used (for decryption of headers)
   * @param acc             the accountant to be used
   *
   * @throws IOException    if anything fails :-D
   */
  public InitialRecipesBlender(String identity, BlendingReceiver router,
                               IdentityStore identityStore, Accountant acc)
      throws IOException {
    super(router, acc);
    this.identityStore = identityStore;
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

  /**
   * <p>Sets the size of the anonymity set.</p>
   *
   * @param newSize the new size of the set to be used
   * @return the previously set size
   */
  public int setAnonymitySetSize(int newSize) {
    int oldsetsize = anonSetSize;
    anonSetSize = newSize;
    return oldsetsize;
  }

  @Override
  public String getBlendingAddress() {
    return this.identity;
  }

  @Override
  public byte[] blendMessageToBytes(BlendingSpec target, VortexMessage msg) {
    return new byte[0];
  }

  @Override
  public VortexMessage unblendMessage(byte[] blendedMessage) {
    return null;
  }

  @Override
  public boolean blendMessage(BlendingSpec target, VortexMessage msg) {
    // encode message in clear readable and send it
    try {
      //Session session = Session.getDefaultInstance(new Properties(), null);
      Authenticator a = new javax.mail.Authenticator() {
        protected PasswordAuthentication getPasswordAuthentication() {
          return new PasswordAuthentication("username", "password");
        }
      };
      Session session = Session.getInstance(new Properties(), a);
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
      final PipedOutputStream os = new PipedOutputStream();
      // FIXME catch error values
      new Thread() {
        public void run() {
          try {
            mimeMsg.writeTo(os);
            os.close();
          } catch (IOException | MessagingException ioe) {
            LOGGER.log(Level.WARNING, "Error while sending message", ioe);
          }
        }
      }.start();
      PipedInputStream inp = new PipedInputStream(os);

      // send
      transport.sendMessage(target.getRecipientAddress(), inp);
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
  public boolean gotMessage(final InputStream is) {
    try {
      Authenticator a = new javax.mail.Authenticator() {
        protected PasswordAuthentication getPasswordAuthentication() {
          return new PasswordAuthentication("username", "password");
        }
      };
      Session session = Session.getInstance(new Properties(), a);
      int i = 0;

      MimeMessage msg = new MimeMessage(session, is);

      // Convert Inputstream to byte array
      ByteArrayOutputStream os = new ByteArrayOutputStream();
      msg.writeTo(os);
      os.close();
      byte[] barr = os.toByteArray();

      // extract sender address
      Address[] from = msg.getFrom();

      // extract final recipient address
      Address[] to = msg.getAllRecipients();
      LOGGER.log(Level.INFO, "Got a message to blend from " + from[0] + " to " + to[0]);

      // get identity store
      IdentityStore istore = this.identityStore;

      // get anonymity set
      Set<IdentityStoreBlock> anonSet = istore.getAnonSet(anonSetSize);
      if (anonSet == null) {
        LOGGER.log(Level.WARNING, "unable to get anonymity set for message");
        return false;
      }

      // get receipes
      BlenderRecipe recipe = BlenderRecipe.getRecipe(null, anonSet);
      if (recipe == null) {
        LOGGER.log(Level.WARNING, "unable to get recipe for message");
        return false;
      }

      // apply receipes
      LOGGER.log(Level.INFO, "blending messages");
      for (Address receiverAddress : to) {
        LOGGER.log(Level.INFO, "blending message for \"" + receiverAddress + "\"");
        IdentityStoreBlock fromAddr = istore.getIdentity(from[0].toString());

        IdentityStoreBlock toAddr = istore.getIdentity(receiverAddress.toString());
        RoutingCombo rb = recipe.applyRecipe(anonSet, fromAddr, toAddr);

        if (rb == null) {
          LOGGER.log(Level.WARNING, "Unable to route message to " + receiverAddress);
        }

        PrefixBlock pb = new PrefixBlock();
        InnerMessageBlock im = new InnerMessageBlock();
        im.setRouting(rb);
        im.setPayload(0, barr);

        // send to workspace
        VortexMessage vmsg = new VortexMessage(pb, im);
        if (router.gotMessage(vmsg)) {
          i++;
        }
      }
      LOGGER.log(Level.INFO, "done blending");
      return i == to.length;
    } catch (IOException | MessagingException ioe) {
      LOGGER.log(Level.WARNING, "Exception while getting and parsing message", ioe);
      return false;
    }
  }

}
