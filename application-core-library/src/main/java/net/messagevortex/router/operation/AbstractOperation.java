package net.messagevortex.router.operation;

import net.messagevortex.asn1.IdentityBlock;
import net.messagevortex.asn1.UsagePeriod;

import java.io.Serializable;

public abstract class AbstractOperation implements Operation, Serializable {

  IdentityBlock identity;
  InternalPayloadSpace payload;
  UsagePeriod period = null;

  /***
   * <p>Puts a payload int the workspace.</p>
   *
   * @param payload the internal payload of an identity to be registered within its workspace
   */
  public void setInternalPayload(InternalPayloadSpace payload) {
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
