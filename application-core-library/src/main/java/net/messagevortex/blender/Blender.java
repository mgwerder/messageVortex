package net.messagevortex.blender;

import net.messagevortex.AbstractDaemon;
import net.messagevortex.accounting.HeaderVerifier;
import net.messagevortex.asn1.BlendingSpec;
import net.messagevortex.asn1.VortexMessage;
import net.messagevortex.router.IncomingMessageRouterListener;
import net.messagevortex.transport.TransportReceiver;
import net.messagevortex.transport.TransportSender;

import java.io.IOException;

/**
 * Interface specifying a blender.
 */
public abstract class Blender extends AbstractDaemon implements TransportReceiver,BlendingSender {

  private IncomingMessageRouterListener listener = null;
  private HeaderVerifier headerVerifier = null;
  private BlendingReceiver blendingReceiver = null;
  private TransportSender transportSender = null;

  public Blender(BlendingReceiver receiver, HeaderVerifier verifier) {
    setBlenderReceiver(receiver);
    setVerifier(verifier);
  }

  /***
   * <p>Sets the listener for incoming messages to the router listener.</p>
   *
   * @param listener the listening router layer
   * @return the previously set listener
   */
  public IncomingMessageRouterListener setIncomingMessageListener(
          IncomingMessageRouterListener listener) {
    IncomingMessageRouterListener old = this.listener;
    this.listener = listener;
    return old;
  }

  public final HeaderVerifier getVerifier() {
    return headerVerifier;
  }

  /***
   * <p>Sets the header verifier of the accounting layer.</p>
   *
   * @param verifier the header verifier which is called upon incomming messages
   * @return the previously set verifier
   */
  public final HeaderVerifier setVerifier(HeaderVerifier verifier) {
    HeaderVerifier ret = getVerifier();
    this.headerVerifier = verifier;
    return ret;
  }

  /***
   * <p>Sets the listening router layer.</p>
   *
   * <p>All future messages successfully extracted and authorized by the header verifyer are
   * passed to this object.</p>
   *
   * @param receiver The listening router layer
   * @return The old/previous router layer
   */
  public final BlendingReceiver setBlenderReceiver(BlendingReceiver receiver) {
    BlendingReceiver ret = blendingReceiver;
    this.blendingReceiver = receiver;
    return ret;
  }

  /***
   * <p>Sets the transport sending layer.</p>
   *
   * <p>All future messages successfully blended are passed to this object.</p>
   *
   * @param sender The listening transport layer
   * @return The old/previous router layer
   */
  public final TransportSender setTransportSender(TransportSender sender) {
    TransportSender ret = this.transportSender;
    this.transportSender = sender;
    return ret;
  }

  /***
   * <p>Gets the currently set transport layer.</p>
   *
   * @return The old/previous router layer
   */
  public final TransportSender getTransportSender() {
    return transportSender;
  }

  /***
   * <p>This method is called by the router layer to blend a message.</p>
   *
   * @param message the message to be blended
   * @return true if blended successfully and sent by the transport layer
   */
  public abstract boolean blendMessage(BlendingSpec target, VortexMessage message)
          throws IOException;

  /***
   * <p>Returns the address supported for blender.</p>
   *
   * <p>The address is specified by &lt;transport&gt;&lt;address&gt;!&lt;publickey&gt;.</p>
   *
   * @return The vortex adress.
   */
  public abstract String getBlendingAddress();

  /**
   * <p>Blends a VortexMessage into the apropriate text.</p>
   * @param target  the blending spec for the recipient
   * @param msg  the message to be blended
   * @return the blended message
   */
  public abstract byte[] blendMessageToBytes(BlendingSpec target, VortexMessage msg);

  /**
   * <p>Extracts a vortexMessage from a blended message.</p>
   * @param blendedMessage the blended message
   * @return the VortexMessage
   */
  public abstract VortexMessage unblendMessage(byte[] blendedMessage);

}
