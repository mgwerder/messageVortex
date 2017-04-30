package net.gwerder.java.messagevortex.asn1;

import org.bouncycastle.asn1.ASN1Encodable;

import java.util.List;

/**
 * Represents a the Blending specification of the routing block.
 *
 */
public class AddRedundancyOperation extends AbstractRedundancyOperation {

    public AddRedundancyOperation(int inputId, int dataStripes, int redundancy, List<SymmetricKey> keys, int outputId, int gfSize) {
        super(inputId,dataStripes,redundancy,keys,outputId,gfSize);
    }

    public AddRedundancyOperation(ASN1Encodable to) {
        super(to);
    }

}
