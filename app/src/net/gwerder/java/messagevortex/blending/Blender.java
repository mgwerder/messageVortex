package net.gwerder.java.messagevortex.blending;

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

import net.gwerder.java.messagevortex.accounting.HeaderVerifier;
import net.gwerder.java.messagevortex.asn1.BlendingSpec;
import net.gwerder.java.messagevortex.asn1.VortexMessage;
import net.gwerder.java.messagevortex.routing.IncomingMessageRouterListener;
import net.gwerder.java.messagevortex.transport.TransportReceiver;
import net.gwerder.java.messagevortex.transport.TransportSender;

/**
 * Interface specifying a blender.
 */
public abstract class Blender implements TransportReceiver {

  private IncomingMessageRouterListener listener = null;
  private HeaderVerifier verifyer = null;
  private BlenderReceiver blenderReceiver = null;
  private TransportSender sender = null;

  public Blender(BlenderReceiver receiver, HeaderVerifier verifier) {
    setBlenderReceiver(receiver);
    setVerifier(verifier);
  }

  /***
   * <p>Sets the listener for incoming messages to the routing listener.</p>
   *
   * @param listener the listening routing layer
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

  public final HeaderVerifier setVerifier(HeaderVerifier verifyer) {
    HeaderVerifier ret = this.verifyer;
    this.verifyer = verifyer;
    return ret;
  }

  /***
   * <p>Sets the listening routing layer.</p>
   *
   * <p>All future messages successfully extracted and authorized by the header verifyer are
   * passed to this object.</p>
   *
   * @param receiver The listening routing layer
   * @return The old/previous routing layer
   */
  public final BlenderReceiver setBlenderReceiver(BlenderReceiver receiver) {
    BlenderReceiver ret = blenderReceiver;
    this.blenderReceiver = receiver;
    return ret;
  }

  /***
   * <p>Sets the transport sending layer.</p>
   *
   * <p>All future messages successfully blended are passed to this object.</p>
   *
   * @param sender The listening transport layer
   * @return The old/previous routing layer
   */
  public final TransportSender setTransportSender(TransportSender sender) {
    TransportSender ret = sender;
    this.sender = sender;
    return ret;
  }

  /***
   * <p>Gets the currently set transport layer.</p>
   *
   * @return The old/previous routing layer
   */
  public final TransportSender getTransportSender() {
    return sender;
  }

  /***
   * <p>This method is called by the routing layer to blend a message.</p>
   *
   * @param message the message to be blended
   * @return true if blended successfully and sent by the transport layer
   */
  public abstract boolean blendMessage(BlendingSpec target, VortexMessage message);

  /***
   * <p>Returns the address supported for blending.</p>
   *
   * <p>The address is specified by &lt;transport&gt;&lt;address&gt;!&lt;publickey&gt;.</p>
   *
   * @return The vortex adress.
   */
  public abstract String getBlendingAddress();

}
