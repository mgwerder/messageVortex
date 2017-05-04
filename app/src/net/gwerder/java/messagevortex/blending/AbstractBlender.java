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

import net.gwerder.java.messagevortex.accounting.HeaderVerifyer;
import net.gwerder.java.messagevortex.asn1.BlendingSpec;
import net.gwerder.java.messagevortex.asn1.VortexMessage;
import net.gwerder.java.messagevortex.routing.IncomingMessageRouterListener;
import net.gwerder.java.messagevortex.transport.TransportListener;

/**
 * Abstract blender class unifying required interfaces and offers basic functionality.
 */
public abstract class AbstractBlender implements Blender,TransportListener {

    IncomingMessageRouterListener listener=null;
    HeaderVerifyer verifyer=null;

    /***
     * Sets the listener for incomming messages to the routing listener.
     * @param listener the listening routing layer
     * @return the previously set listener
     */
    public IncomingMessageRouterListener setIncomingMessageListener(IncomingMessageRouterListener listener) {
        IncomingMessageRouterListener old=this.listener;
        this.listener=listener;
        return old;
    }

    /***
     * Sets the header verifier for the blending layer.
     *
     * @param verifyer the verifier to be set
     * @return the previously set header verifier
     */
    public HeaderVerifyer setHeaderVerifyer(HeaderVerifyer verifyer) {
        HeaderVerifyer old=this.verifyer;
        this.verifyer=verifyer;
        return old;

    }

    public abstract boolean blendMessage(BlendingSpec target,VortexMessage message);

}
