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

import net.messagevortex.asn1.IdentityBlock;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

public class InternalPayloadSpaceStore {

  private final Map<String, InternalPayloadSpace> internalPayloadMap = new HashMap<>();

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
      String key = new String(identity.getIdentityKey().getPublicKey(), StandardCharsets.UTF_8);
      ret = internalPayloadMap.get(key);
      if (payload != null) {
        internalPayloadMap.put(key, payload);
      } else {
        internalPayloadMap.remove(key);
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
      String key = new String(identity.getIdentityKey().getPublicKey(), StandardCharsets.UTF_8);
      ret = internalPayloadMap.get(key);
      if (ret == null) {
        ret = new InternalPayloadSpace(this, identity);
        internalPayloadMap.put(key, ret);
      }
    }
    return ret;
  }
}
