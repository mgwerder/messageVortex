package net.messagevortex.accounting;

import net.messagevortex.AbstractDaemon;
import net.messagevortex.asn1.IdentityBlock;

/**
 * <p>A dummy accountant always agreeing to all transactions.</p>
 */
public class DummyAccountant extends AbstractDaemon implements Accountant {

  public DummyAccountant(String section) {
    // dummy accountant requires no configuration
  }

  /***
   * <p>Dummy Verifier always returning for all requests an unlimited quota.</p>
   *
   * @param header the header to be verified
   * @return Number of bytes allowed to be processed
   */
  @Override
  public int verifyHeaderForProcessing(IdentityBlock header) {
    // this verifier accepts all identities
    return Integer.MAX_VALUE;
  }

}
