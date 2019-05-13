package net.messagevortex.router;

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

import net.messagevortex.ExtendedSecureRandom;
import net.messagevortex.asn1.IdentityStore;
import net.messagevortex.asn1.IdentityStoreBlock;
import net.messagevortex.asn1.RoutingBlock;
import net.messagevortex.asn1.VortexMessage;

/**
 * <p>Factory class to build full message (anonymizing structure).</p>
 */
public abstract class MessageFactory {

  protected VortexMessage fullmsg = null;

  protected String msg = "";
  protected IdentityStoreBlock source = null;
  protected IdentityStoreBlock target = null;
  protected IdentityStoreBlock hotspot = null;
  protected IdentityStore identityStore = null;

  protected MessageFactory() {

  }

  /***
   * <p>Build a message with the specified parameters.</p>
   *
   * @param msg the message to be embedded
   * @param source the indes of the source identity
   * @param target the index of the target identity
   * @param anonGroupMembers a set of all available targets in the group set
   * @param is the identity store to be used
   * @return the built message wrapped in a message factory
   *
   * @FIXME augment to plugable MessageFactory implementation
   */
  public static MessageFactory buildMessage(String msg, int source, int target,
                                            IdentityStoreBlock[] anonGroupMembers,
                                            IdentityStore is) {

    MessageFactory fullmsg = new SimpleMessageFactory(msg, source, target, anonGroupMembers, is);

    // selecting hotspot
    fullmsg.hotspot = anonGroupMembers[ExtendedSecureRandom.nextInt(anonGroupMembers.length)];

    fullmsg.build();

    return fullmsg;
  }

  /***
   * <p>Sets the identity store to be used for creation of the message.</p>
   *
   * @param is the identity store to be set
   * @return the previously set identity store
   */
  public IdentityStore setIdentityStore(IdentityStore is) {
    IdentityStore ret = this.identityStore;
    this.identityStore = is;
    return ret;
  }

  /***
   * <p>Gets the current message as a VortexMessage.</p>
   *
   * @return the requested message
   */
  public VortexMessage getMessage() {
    if (this.fullmsg == null) {
      build();
    }
    return this.fullmsg;
  }

  public abstract RoutingBlock build();

  public abstract GraphSet getGraph();

}
