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

import net.messagevortex.AbstractDaemon;
import net.messagevortex.accounting.HeaderVerifier;
import net.messagevortex.asn1.BlendingSpec;
import net.messagevortex.asn1.VortexMessage;
import net.messagevortex.router.IncomingMessageRouterListener;
import net.messagevortex.transport.TransportReceiver;
import net.messagevortex.transport.TransportSender;

/**
 * Interface specifying a blender.
 */
public abstract class Blender extends AbstractDaemon implements TransportReceiver {

  private IncomingMessageRouterListener listener = null;
  private HeaderVerifier verifyer = null;
  private BlendingReceiver blendingReceiver = null;
  private TransportSender sender = null;

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
    return verifyer;
  }

  /***
   * <p>Sets the header verifier of the accounting layer.</p>
   *
   * @param verifier the header verifier which is called upon incomming messages
   * @return the previously set verifier
   */
  public final HeaderVerifier setVerifier(HeaderVerifier verifier) {
    HeaderVerifier ret = this.verifyer;
    this.verifyer = verifier;
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
    TransportSender ret = sender;
    this.sender = sender;
    return ret;
  }

  /***
   * <p>Gets the currently set transport layer.</p>
   *
   * @return The old/previous router layer
   */
  public final TransportSender getTransportSender() {
    return sender;
  }

  /***
   * <p>This method is called by the router layer to blend a message.</p>
   *
   * @param message the message to be blended
   * @return true if blended successfully and sent by the transport layer
   */
  public abstract boolean blendMessage(BlendingSpec target, VortexMessage message);

  /***
   * <p>Returns the address supported for blender.</p>
   *
   * <p>The address is specified by &lt;transport&gt;&lt;address&gt;!&lt;publickey&gt;.</p>
   *
   * @return The vortex adress.
   */
  public abstract String getBlendingAddress();

}
