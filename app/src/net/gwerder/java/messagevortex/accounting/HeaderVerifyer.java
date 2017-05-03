package net.gwerder.java.messagevortex.accounting;

import net.gwerder.java.messagevortex.asn1.IdentityBlock;

/**
 * Interface for an Accountant to verify the header for further processing.
 */
public interface HeaderVerifyer {

    /***
     * checks the given IdentityBlock for validity of processing.
     *
     * One of the following criteria must be met:
     * 1. The identity is known and the serial has not yet reached its replay limmit
     * 2. The identity is not known but has a RequestIdentityBlock
     * 3. The IdentityBlock is not known but has a request capability block
     *
     * @param header the header to be verified
     * @return the maximum nuber of bytes allowed for processing
     */
    int verfyHeaderForProcessing(IdentityBlock header);

}
