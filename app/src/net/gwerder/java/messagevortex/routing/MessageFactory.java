package net.gwerder.java.messagevortex.routing;

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

import net.gwerder.java.messagevortex.ExtendedSecureRandom;
import net.gwerder.java.messagevortex.asn1.IdentityStore;
import net.gwerder.java.messagevortex.asn1.IdentityStoreBlock;
import net.gwerder.java.messagevortex.asn1.VortexMessage;

/**
 * Factory class to build full message (anonymizing structure)
 * <p>
 * Created by martin.gwerder on 06.06.2016.
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

  public static MessageFactory buildMessage(String msg, int source, int target, IdentityStoreBlock[] anonGroupMembers, IdentityStore is) {

    MessageFactory fullmsg = new SimpleMessageFactory(msg, source, target, anonGroupMembers, is);

    // selecting hotspot
    fullmsg.hotspot = anonGroupMembers[ExtendedSecureRandom.nextInt(anonGroupMembers.length)];

    fullmsg.build();

    return fullmsg;
  }

  public IdentityStore setIdentityStore(IdentityStore is) {
    IdentityStore ret = this.identityStore;
    this.identityStore = is;
    return ret;
  }

  public VortexMessage getMessage() {
    if (this.fullmsg == null) {
      build();
    }
    return this.fullmsg;
  }

  public abstract void build();

  public abstract GraphSet getGraph();

}
