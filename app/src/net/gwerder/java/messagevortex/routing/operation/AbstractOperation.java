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

import net.gwerder.java.messagevortex.asn1.IdentityBlock;
import net.gwerder.java.messagevortex.asn1.UsagePeriod;

public abstract class AbstractOperation implements Operation {

  IdentityBlock identity;
  InternalPayload payload;
  UsagePeriod period = null;

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

  public UsagePeriod setUsagePeriod(UsagePeriod period) {
    UsagePeriod ret = this.period;
    this.period = period;
    return ret;
  }

  public boolean isInUsagePeriod() {
    if (identity == null || identity.getUsagePeriod() == null) {
      return true;
    }
    return identity.getUsagePeriod().inUsagePeriod();
  }

  public abstract int[] execute(int[] id);

}
