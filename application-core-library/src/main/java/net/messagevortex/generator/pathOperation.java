package net.messagevortex.generator;

import net.messagevortex.asn1.SymmetricKey;

import java.util.List;

public class pathOperation {
    public enum Operations {
        ADD_REDUNDANCY,
        REMOVE_REDUNDANCY,
        ENCRYPT,
        DECRYPT
    }

    private final int pathIndex;
    private final int subpathIndex;
    private final int hopIndex;
    private final int sourceID;
    private final int targetID;
    private final Operations operationType;
    private int dataStripes;
    private int redundancies;
    private final List<SymmetricKey> keys;
    private int gfSize;

    /**
     * <p>Creates a new pathOperation object for a Encrypt or Decrypt operation.</p>
     *
     * @param pathIndex     Index of the path.
     * @param subpathIndex  Index of the subpath.
     * @param hopIndex      Index of the hop.
     * @param sourceID      ID of the source Payloadslot.
     * @param targetID      ID of the target Payloadslot.
     * @param operationType An Enum with the Type of Operation.
     * @param keys          List of Symmetric keys for the operation.
     */
    public pathOperation(int pathIndex, int subpathIndex, int hopIndex, int sourceID, int targetID, Operations operationType, List<SymmetricKey> keys) {
        this.pathIndex = pathIndex;
        this.subpathIndex = subpathIndex;
        this.hopIndex = hopIndex;
        this.sourceID = sourceID;
        this.targetID = targetID;
        this.operationType = operationType;
        this.keys = keys;
    }

    /**
     * <p>Creates a new pathOperation object for an Add Redundancy or Remove Redundancy Operation.</p>
     *
     * @param pathIndex     Index of the path.
     * @param subpathIndex  Index of the subpath.
     * @param hopIndex      Index of the hop.
     * @param sourceID      ID of the source Payloadslot.
     * @param targetID      ID of the source Payloadslot.
     * @param operationType An Enum with the Type of Operation.
     * @param keys          List of Symmetric keys for the operation.
     * @param dataStripes   Number of dataStripes to be created.
     * @param redundancies  Number of redundant dataStripes to be created.
     * @param gfSize        Size of the galois field.
     */
    public pathOperation(int pathIndex, int subpathIndex, int hopIndex, int sourceID, int targetID, Operations operationType, List<SymmetricKey> keys, int dataStripes, int redundancies, int gfSize) {
        this.pathIndex = pathIndex;
        this.subpathIndex = subpathIndex;
        this.hopIndex = hopIndex;
        this.sourceID = sourceID;
        this.targetID = targetID;
        this.operationType = operationType;
        this.keys = keys;
        this.dataStripes = dataStripes;
        this.redundancies = redundancies;
        this.gfSize = gfSize;
    }

    /**
     * <p>Get the ID of the source Payloadslot.</p>
     *
     * @return The ID of the source Payloadslot.
     */
    public int getSourceID() {
        return sourceID;
    }

    /**
     * <p>Get the ID of the target Payloadslot.</p>
     *
     * @return The ID of the target Payloadslot.
     */
    public int getTargetID() {
        return targetID;
    }

    /**
     * <p>Get the Operation Type.</p>
     *
     * @return The Operation Type.
     */
    public Operations getOperationType() {
        return operationType;
    }

    /**
     * <p>Get the Symmetric Keys.</p>
     *
     * @return A List with the Symmetric Keys used in this Operation.
     */
    public List<SymmetricKey> getKeys() {
        return keys;
    }

    /**
     * <p>Get the number of DataStripes for a Redundancy operation.</p>
     *
     * @return The number of DataStripes for a Redundancy operation.
     */
    public int getDataStripes() {
        return dataStripes;
    }

    /**
     * <p>Get the number of RedundancyStripes for a Redundancy operation.</p>
     *
     * @return The number of RedundancyStripes for a Redundancy operation.
     */
    public int getRedundancies() {
        return redundancies;
    }

    /**
     * <p>Get the size of the Galois Field for a Redundancy operation.</p>
     *
     * @return The size of the Galois Field for a Redundancy operation.
     */
    public int getGfSize() {
        return gfSize;
    }
}
