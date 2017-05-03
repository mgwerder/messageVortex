package net.gwerder.java.messagevortex.accounting;

import net.gwerder.java.messagevortex.asn1.IdentityBlock;

/**
 * Created by Martin on 03.05.2017.
 */
public class DummyAccountant implements Accountant,HeaderVerifyer {
    @Override
    public int verfyHeaderForProcessing(IdentityBlock header) {
        // this verifier accepts all identities
        return Integer.MAX_VALUE;
    }
}
