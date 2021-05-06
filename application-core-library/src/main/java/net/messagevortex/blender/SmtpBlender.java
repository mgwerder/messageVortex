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

import net.messagevortex.accounting.HeaderVerifier;
import net.messagevortex.asn1.BlendingSpec;
import net.messagevortex.asn1.VortexMessage;

import java.io.InputStream;

public class SmtpBlender extends Blender {

  public SmtpBlender(BlendingReceiver receiver, HeaderVerifier verifier) {
    super(receiver, verifier);
  }

  /***
   * <p>Receives an SMTP message from the transport layer and extracts the message.</p>
   *
   * @return true if message has been accepted as vortex message
   */
  @Override
  public boolean gotMessage(InputStream is) {

    return false;
  }

  @Override
  public boolean blendMessage(BlendingSpec target, VortexMessage message) {
    // FIXME blending incomplete
    byte[] msg = blendMessageToBytes(target, message);
    return true;
  }

  @Override
  public String getBlendingAddress() {
    return null;
  }

  @Override
  public byte[] blendMessageToBytes(BlendingSpec target, VortexMessage msg) {
    return new byte[0];
  }

  @Override
  public VortexMessage unblendMessage(byte[] blendedMessage) {
    return null;
  }

  public void shutdown() {}

}
