package net.gwerder.java.messagevortex;

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

import java.io.InputStream;
import net.gwerder.java.messagevortex.blending.Blender;
import net.gwerder.java.messagevortex.routing.operation.RoutingSender;
import net.gwerder.java.messagevortex.transport.TransportReceiver;

public class MessageVortexBlending implements TransportReceiver, RoutingSender {

  private TransportReceiver receiver = null;
  private RoutingSender sender = null;
  private Blender blender = null;


  public MessageVortexBlending(TransportReceiver blend, RoutingSender sender) {
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

  public RoutingSender setRoutingSender(RoutingSender sender) {
    RoutingSender ret = sender;
    this.sender = sender;
    return ret;
  }

  public RoutingSender getRoutingSender() {
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
