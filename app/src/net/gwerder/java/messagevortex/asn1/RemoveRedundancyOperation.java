package net.gwerder.java.messagevortex.asn1;

import org.bouncycastle.asn1.ASN1Encodable;

import java.io.IOException;
import java.util.List;

/**
 * Represents a the Blending specification of the routing block.
 *
 */
public class RemoveRedundancyOperation extends AbstractRedundancyOperation {

    public RemoveRedundancyOperation(int inputId, int dataStripes, int redundancy, List<SymmetricKey> keys, int outputId, int gfSize) {
        super(inputId,dataStripes,redundancy,keys,outputId,gfSize);
    }

    public RemoveRedundancyOperation(ASN1Encodable to) throws IOException {
        super(to);
    }

}
