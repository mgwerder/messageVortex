package net.messagevortex.accounting;

import net.messagevortex.asn1.IdentityBlock;

/**
 * <p>Interface for an Accountant to verify the header for further processing.</p>
 */
public interface HeaderVerifier {

  /***
   * <p>checks the given IdentityBlock for validity of processing.</p>
   *
   * <p>One of the following criteria must be met:</p>
   * <ul>
   *   <li>The identity is known and the serial has not yet reached its replay limit and is not
   *   replayed too early</li>
   *   <li>The identity is not known but has a RequestIdentityBlock</li>
   *   <li>The IdentityBlock is not known but has a request capability block</li>
   * </ul>
   *
   * @param header the header to be verified
   * @return the maximum nuber of bytes allowed for processing
   */
  int verifyHeaderForProcessing(IdentityBlock header);

}
