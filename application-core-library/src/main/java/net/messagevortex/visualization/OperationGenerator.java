package net.messagevortex.visualization;

import net.messagevortex.asn1.SymmetricKey;
import net.messagevortex.asn1.encryption.DumpType;
import org.bouncycastle.asn1.*;

import java.io.IOException;
import java.util.List;

public class OperationGenerator {
    /**
     * <p>Creates a new Split Payload GraphOperation.</p>
     *
     * @param id1           ID of payload slot to apply split operation.
     * @param id2           ?
     * @param sizeBlock     A Sizeblock indicating the split size.
     * @param newId         ID of the target payload slot.
     * @return              Returns a tagged object with a Split Payload GraphOperation.
     */
    public ASN1TaggedObject createSplitOperation(int id1, int id2, ASN1Encodable sizeBlock, int newId) {
        ASN1Sequence splitOperation = new DERSequence(new ASN1Encodable[]{new ASN1Integer(id1), new ASN1Integer(id2), sizeBlock, new ASN1Integer(newId)});

        return new DERTaggedObject(true, 150, splitOperation);
    }

    /**
     * <p>Creates a new Merge Payload GraphOperation.</p>
     *
     * @param id1       ID of the first payload slot.
     * @param id2       ID of the second payload slot.
     * @param newId     ID of the target payload slot.
     * @return          Returns a tagged object with the Merge Payload GraphOperation.
     */
    public ASN1TaggedObject createMergeOperation(int id1, int id2, int newId) {
        ASN1Sequence mergeOperation = new DERSequence(new ASN1Encodable[]{new ASN1Integer(id1), new ASN1Integer(id2), new ASN1Integer(newId)});

        return new DERTaggedObject(true, 160, mergeOperation);
    }

    /**
     * <p>Creates a new Encrypt Payload GraphOperation</p>
     *
     * @param originalId    Unencrypted Payload slot.
     * @param key           Symmetrical Key that should be used for the encryption.
     * @param newId         Targeted Payload slot for the encrypted data.
     * @return              Returns a tagged object with the Encrypt Payload GraphOperation.
     */
    public ASN1TaggedObject createEncryptOperation(int originalId, SymmetricKey key, int newId) throws IOException {
        ASN1Sequence encryptOperation = new DERSequence(new ASN1Encodable[]{new ASN1Integer(originalId), key.toAsn1Object(DumpType.INTERNAL), new ASN1Integer(newId)});

        return new DERTaggedObject(true, 300, encryptOperation);
    }

    /**
     * <p>Creates a new Decrypt Payload GraphOperation</p>
     *
     * @param originalId    Encrypted Payload slot.
     * @param key           Symmetrical Key that should be used for the decryption.
     * @param newId         Targeted Payload slot for the Decrypted data.
     * @return              Returns a tagged object with the Decrypt Payload GraphOperation.
     */
    public ASN1TaggedObject createDecryptOperation(int originalId, SymmetricKey key, int newId) throws IOException {
        ASN1Sequence decryptOperation = new DERSequence(new ASN1Encodable[]{new ASN1Integer(originalId), key.toAsn1Object(DumpType.INTERNAL), new ASN1Integer(newId)});

        return new DERTaggedObject(true, 310, decryptOperation);
    }

    /**
     * <p>Creates a new Add Redundancy Payload GraphOperation</p>
     *
     * @param inputId               ID of the input payload slot.
     * @param dataStripes           data stripes
     * @param redundancyStripes     redundancy stripes
     * @param keys                  List of Symmetrical Keys.
     * @param outputId              ID of the output payload slot.
     * @param gfSize                Size of the galois field.
     * @return                      Returns a tagged object with the add redundancy payload operation.
     */
    public ASN1TaggedObject createAddRedundancyOperation(int inputId, int dataStripes, int redundancyStripes, List<SymmetricKey> keys, int outputId, int gfSize) throws IOException {
        ASN1EncodableVector keysList = new ASN1EncodableVector();
        for(SymmetricKey k : keys) {
            keysList.add(k.toAsn1Object(DumpType.INTERNAL));
        }

        ASN1TaggedObject keysTagged = new DERTaggedObject(true, 16003, new DERSequence(keysList));
        ASN1TaggedObject inputIdTagged = new DERTaggedObject(true, 16000, new ASN1Integer(inputId));
        ASN1TaggedObject dataStripesTagged = new DERTaggedObject(true, 16001, new ASN1Integer(dataStripes));
        ASN1TaggedObject redundancyStripesTagged = new DERTaggedObject(true, 16002, new ASN1Integer(redundancyStripes));
        ASN1TaggedObject outputIdTagged = new DERTaggedObject(true, 16004, new ASN1Integer(outputId));
        ASN1TaggedObject gfSizeTagged = new DERTaggedObject(true, 16005, new ASN1Integer(gfSize));

        ASN1Sequence addRedundancyOperation = new DERSequence(new ASN1Encodable[]{inputIdTagged, dataStripesTagged, redundancyStripesTagged, keysTagged, outputIdTagged, gfSizeTagged});

        return new DERTaggedObject(true, 400, addRedundancyOperation);
    }

    /**
     * <p>Creates a new Remove Redundancy Payload GraphOperation</p>
     *
     * @param inputId               ID of the input payload slot.
     * @param dataStripes           data stripes
     * @param redundancyStripes     redundancy stripes
     * @param keys                  List of Symmetrical Keys.
     * @param outputId              ID of the output payload slot.
     * @param gfSize                Size of the galois field.
     * @return                      Returns a tagged object with the remove redundancy payload operation.
     */
    public ASN1TaggedObject createRemoveRedundancyOperation(int inputId, int dataStripes, int redundancyStripes, List<SymmetricKey> keys, int outputId, int gfSize) throws IOException {
        ASN1EncodableVector keysList = new ASN1EncodableVector();
        for(SymmetricKey k : keys) {
            keysList.add(k.toAsn1Object(DumpType.INTERNAL));
        }

        ASN1TaggedObject keysTagged = new DERTaggedObject(true, 16003, new DERSequence(keysList));
        ASN1TaggedObject inputIdTagged = new DERTaggedObject(true, 16000, new ASN1Integer(inputId));
        ASN1TaggedObject dataStripesTagged = new DERTaggedObject(true, 16001, new ASN1Integer(dataStripes));
        ASN1TaggedObject redundancyStripesTagged = new DERTaggedObject(true, 16002, new ASN1Integer(redundancyStripes));
        ASN1TaggedObject outputIdTagged = new DERTaggedObject(true, 16004, new ASN1Integer(outputId));
        ASN1TaggedObject gfSizeTagged = new DERTaggedObject(true, 16005, new ASN1Integer(gfSize));

        ASN1Sequence removeRedundancyOperation = new DERSequence(new ASN1Encodable[]{inputIdTagged, dataStripesTagged, redundancyStripesTagged, keysTagged, outputIdTagged, gfSizeTagged});
        return new DERTaggedObject(true, 410, removeRedundancyOperation);
    }

    /**
     * <p>Creates a new Map Payload slot GraphOperation.</p>
     *
     * @param originalId    ID of the original Payload slot.
     * @param newId         ID of the new payload slot.
     * @return              Returns the tagged object with the Map Payload Slot GraphOperation.
     */
    public ASN1TaggedObject createMapBlockOperation(int originalId, int newId) {
        ASN1Sequence mapOperation = new DERSequence(new ASN1Encodable[]{new ASN1Integer(originalId), new ASN1Integer(newId)});

        return new DERTaggedObject(true, 1001, mapOperation);
    }

    /**
     * <p>Creates s new Sizeblock used for the Split Payload GraphOperation</p>
     *
     * @param from  ?
     * @param to    ?
     * @param tag   The tag indicating the size option (Relative | Absolute)
     * @return      Returns a Sequence with the Sizeblock
     */
    public ASN1Sequence createSizeBlock(int from, int to, int tag) {
        ASN1Sequence seq = new DERSequence(new ASN1Encodable[]{new ASN1Integer(from), new ASN1Integer(to)});
        ASN1TaggedObject taggedSizeblock = new DERTaggedObject(true, tag, seq);

        return new DERSequence(new ASN1Encodable[]{taggedSizeblock});
    }
}
