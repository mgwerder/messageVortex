package net.messagevortex.router.operation;

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

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import net.messagevortex.asn1.IdentityBlock;

public class InternalPayloadSpaceStore {

  private Map<IdentityBlock, InternalPayloadSpace> internalPayloadMap = new HashMap<>();

  /***
   * <p>Sets a payload space into the payload space store.</p>
   *
   * @param identity the identity o the requested payload space
   * @param payload the payload space to be set
   * @return the previously set payload
   */
  public InternalPayloadSpace setInternalPayload(IdentityBlock identity,
                                                 InternalPayloadSpace payload) {
    InternalPayloadSpace ret = null;
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

  /***
   * <p>Gets a payload space from the payload space store.</p>
   *
   * @param identity the identity to be retrieved
   * @return the requested payload space
   */
  public InternalPayloadSpace getInternalPayload(IdentityBlock identity) {
    InternalPayloadSpace ret;
    synchronized (internalPayloadMap) {
      ret = internalPayloadMap.get(identity);
      if (ret == null) {
        ret = new InternalPayloadSpace(this, identity);
      }
    }
    return ret;
  }
}
