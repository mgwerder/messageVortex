package net.gwerder.java.messagevortex.routing.operation;

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

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import net.gwerder.java.messagevortex.asn1.IdentityBlock;

public class InternalPayloadSpace {

  private Map<IdentityBlock, InternalPayload> internalPayloadMap = new ConcurrentHashMap<>();

  public InternalPayload setInternalPayload(IdentityBlock identity, InternalPayload payload) {
    InternalPayload ret = null;
    synchronized (internalPayloadMap) {
      ret = internalPayloadMap.get(identity);
      if (payload != null) {
        internalPayloadMap.put(identity, payload);
      } else {
        internalPayloadMap.remove(identity);
      }
    }
    return ret;
  }

  public InternalPayload getInternalPayload(IdentityBlock identity) {
    InternalPayload ret;
    synchronized (internalPayloadMap) {
      ret = internalPayloadMap.get(identity);
      if (ret == null) {
        ret = new InternalPayload(this, identity);
      }
    }
    return ret;
  }

}
