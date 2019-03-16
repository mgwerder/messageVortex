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
import net.messagevortex.asn1.UsagePeriod;

public abstract class AbstractOperation implements Operation {

  IdentityBlock identity;
  InternalPayload payload;
  UsagePeriod period = null;

  /***
   * <p>Puts a payload int the workspace.</p>
   *
   * @param payload the internal payload of an identity to be registered within its workspace
   */
  public void setInternalPayload(InternalPayload payload) {
    if (payload == null) {
      this.identity = null;
      this.payload = null;
    } else {
      this.identity = payload.getIdentity();
      this.payload = payload;
    }
  }

  public abstract boolean canRun();

  public IdentityBlock getIdentity() {
    return identity;
  }

  public UsagePeriod getUsagePeriod() {
    return period;
  }

  /***
   * <p>Sets the usage period of the respective operation.</p>
   *
   * <p>After expiry the accounting layer will remove this operation</p>
   *
   * @param period the usage period in which the operation is valid
   * @return the previously set usage period
   */
  public UsagePeriod setUsagePeriod(UsagePeriod period) {
    UsagePeriod ret = this.period;
    this.period = period;
    return ret;
  }

  /***
   * <p>Checks if the operation is within its usage period.</p>
   *
   * <p>This method is called by the accounting layer when searching for expired operations.</p>
   *
   * @return true if the operation did not expire yet.
   */
  public boolean isInUsagePeriod() {
    if (identity == null || identity.getUsagePeriod() == null) {
      return true;
    }
    return identity.getUsagePeriod().inUsagePeriod();
  }

  public abstract int[] execute(int[] id);

}
