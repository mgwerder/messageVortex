package net.messagevortex.visualization;

import org.bouncycastle.asn1.*;
import java.io.IOException;
import java.util.ArrayList;

public class Operations {
    private final ASN1TaggedObject operations;

    /**
     * <p>Creates a new Tagged Sequence of operations to be passed to a Routingcombo.</p>
     *
     * @param ops   An Arraylist with the data for all operations to be applied in the Routingcombo.
     */
    public Operations(ArrayList<PayloadOperation> ops) throws IOException {
        ASN1EncodableVector v = new ASN1EncodableVector();
        for(PayloadOperation op : ops) {
            OperationGenerator operationGenerator = new OperationGenerator();
            switch (op.opType) {
                case 150:
                    v.add(operationGenerator.createSplitOperation(op.id1, op.id2, op.sizeblock, op.newId));
                    break;
                case 160:
                    v.add(operationGenerator.createMergeOperation(op.id1, op.id2, op.newId));
                    break;
                case 300:
                    v.add(operationGenerator.createEncryptOperation(op.id1, op.key, op.newId));
                    break;
                case 310:
                    v.add(operationGenerator.createDecryptOperation(op.id1, op.key, op.newId));
                    break;
                case 400:
                    v.add(operationGenerator.createAddRedundancyOperation(op.id1, op.redundancy, op.dataStripes, op.keys, op.newId, op.gfSize));
                    break;
                case 410:
                    v.add(operationGenerator.createRemoveRedundancyOperation(op.id1, op.redundancy, op.dataStripes, op.keys, op.newId, op.gfSize));
                    break;
                case 1001:
                    v.add(operationGenerator.createMapBlockOperation(op.id1, op.newId));
                    break;
            }
        }

        ASN1Sequence opSeq = new DERSequence(v);
        operations = new DERTaggedObject(true, 132, opSeq);
    }

    /**
     * <p>Returns the Operations in this object</p>
     *
     * @return  Returns a Taggedobject containing a sequence of Operations
     */
    public ASN1TaggedObject getOperations() {
        return operations;
    }
}
