package net.messagevortex;

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

import net.messagevortex.blender.Blender;
import net.messagevortex.blender.BlendingSender;
import net.messagevortex.transport.TransportReceiver;

import java.io.InputStream;

public class MessageVortexBlending extends AbstractDaemon implements TransportReceiver, BlendingSender {

  private TransportReceiver receiver = null;
  private BlendingSender sender = null;
  private Blender blender = null;


  public MessageVortexBlending(TransportReceiver blend, BlendingSender sender) {
    receiver = blend;
    this.sender = sender;
  }

  public TransportReceiver setTransportReceiver(TransportReceiver receiver) {
    TransportReceiver ret = receiver;
    this.receiver = receiver;
    return ret;
  }

  public TransportReceiver getTransportReceiver() {
    return receiver;
  }

  public BlendingSender setBlendingSender(BlendingSender sender) {
    BlendingSender ret = sender;
    this.sender = sender;
    return ret;
  }

  public BlendingSender getRoutingSender() {
    return sender;
  }

  public Blender setBlender(Blender blender) {
    Blender ret = blender;
    this.blender = blender;
    return ret;
  }

  public Blender getBlender() {
    return blender;
  }

  @Override
  public boolean gotMessage(InputStream is) {
    // extra
    return receiver.gotMessage(is);
  }

  @Override
  public boolean sendMessage(String target, MessageVortex msg) {
    return sender.sendMessage(target, msg);
  }

}
