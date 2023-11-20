package net.messagevortex.visualization;

import net.messagevortex.asn1.SymmetricKey;
import org.bouncycastle.asn1.ASN1Encodable;
import java.util.List;

public class PayloadOperation {
    public int opType;
    public int id1;
    public int id2;
    public int newId;
    public ASN1Encodable sizeblock;
    public int dataStripes;
    public int redundancy;
    public List<SymmetricKey> keys;
    public int gfSize;
    public SymmetricKey key;

    /**
     * <p>Creates a new data object for a Split Payload GraphOperation</p>
     *
     * @param id1           ID of payload slot to apply split operation.
     * @param id2           ?
     * @param sizeBlock     A Sizeblock indicating the split size. (?)
     * @param newId         ID of the target payload slot.
     */
    public PayloadOperation(int id1, int id2, ASN1Encodable sizeBlock, int newId) {
        opType = 150;
        this.id1 = id1;
        this.id2 = id2;
        this.sizeblock = sizeBlock;
        this.newId = newId;
    }

    /**
     * <p>Creates a data object for a new Merge Payload GraphOperation</p>
     *
     * @param id1       ID of the first payload slot.
     * @param id2       ID of the second payload slot.
     * @param newId     ID of the target payload slot.
     */
    public PayloadOperation(int id1, int id2, int newId) {
        opType = 160;
        this.id1 = id1;
        this.id2 = id2;
        this.newId = newId;
    }

    /**
     * <pCreates a new data object for a new Encrypt/Decrypt Payload GraphOperation></p>
     *
     * @param type          the type of the GraphOperation (300 | 310)
     * @param id1           the ID of the source block in the workspace
     * @param newId         the ID of the target block in the workspace
     * @param key           the key to be used for decryption
     */
    public PayloadOperation(int type, int id1, int newId, SymmetricKey key) {
        if(type != 300 && type != 310) {
            throw new RuntimeException("Received unexpected type.");
        }
        this.opType = type;
        this.id1 = id1;
        this.newId = newId;
        this.key = key;
    }

    /**
     * <p>Creates a new data object for a new Remove/Add Redundancy GraphOperation</p>
     *
     * @param type          Type of the GraphOperation (400 | 410).
     * @param id1           first ID of the input workspace
     * @param dataStripes   number of data stripes contained in operation
     * @param redundancy    number of redundancy stripes
     * @param keys          keys for the resulting stripes (number should be dataStripes+redundancy)
     * @param newId         first output ID
     * @param gfSize        Size of the Galois Field in bits
     */
    public PayloadOperation(int type, int id1, int dataStripes, int redundancy, List<SymmetricKey> keys, int newId, int gfSize) {
        if(type != 410 && type != 400) {
            throw new RuntimeException("Received unexpected type.");
        }
        this.opType = type;
        this.id1 = id1;
        this.dataStripes = dataStripes;
        this.redundancy = redundancy;
        this.keys = keys;
        this.newId = newId;
        this.gfSize = gfSize;
    }

    /**
     * <p>Remaps a Payload slot to a different payload slot.</p>
     *
     * @param id1       The ID of the initial Payload Slot.
     * @param newId     The ID of the new payload slot.
     */
    public PayloadOperation(int id1, int newId) {
        this.opType = 1001;
        this.id1 = id1;
        this.newId = newId;
    }
}
