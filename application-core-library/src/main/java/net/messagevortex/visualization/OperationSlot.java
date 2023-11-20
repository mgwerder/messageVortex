package net.messagevortex.visualization;

import net.messagevortex.asn1.Operation;

import java.util.TreeMap;

public class OperationSlot {
    enum OperationType {
        MERGE,
        SPLIT,
        ENCRYPT,
        DECRYPT,
        ADD_REDUNDANCY,
        REMOVE_REDUNDANCY,
        MAP,
    }

    private final OperationType type;
    private final Operation operation;
    private int horizontalSlot;
    private int verticalSlot;
    private TreeMap<Integer, GraphPayloadSlot> inputPayloads;
    private TreeMap<Integer, GraphPayloadSlot> outputPayloads;

    /**
     * <p>Creates a new Operation Slot for the graph.</p>
     *
     * @param horizontalSlot    The Horizontal slot for the Operation.
     * @param verticalSlot      The Vertical slot for the operation.
     * @param operation         The Graphoperation Object for this operation.
     * @param inputPayloads     A Treemap with the input Payloads.
     * @param outputPayloads    A Treemap with the output Payloads.
     */
    public OperationSlot(OperationType type, Operation operation, int horizontalSlot, int verticalSlot, TreeMap<Integer, GraphPayloadSlot> inputPayloads, TreeMap<Integer, GraphPayloadSlot> outputPayloads) {
        this.type = type;
        this.horizontalSlot = horizontalSlot;
        this.verticalSlot = verticalSlot;
        this.operation = operation;
        this.inputPayloads = inputPayloads;
        this.outputPayloads = outputPayloads;
    }

    /**
     * <p>Adds a new Payload slot to the Input Payloads</p>
     *
     * @param key   Integer with the key for the Treemap
     * @param value Payloadslot that should be added to the Input Payloads.
     */
    public void setInputPayloads(int key, GraphPayloadSlot value) {
        this.inputPayloads.put(key, value);
    }

    /**
     * <p>Adds a new Payload slot to the Output Payloads</p>
     *
     * @param key   Integer with the key for the Treemap
     * @param value Payloadslot that should be added to the Output Payloads.
     */
    public void setOutputPayloads(int key, GraphPayloadSlot value) {
        this.outputPayloads.put(key, value);
    }

    /**
     * <p>Returns the Operation object.</p>
     *
     * @return  The operation Object.
     */
    public Operation getOperation() {
        return this.operation;
    }

    /**
     * <p>Returns the Operation type of this operation.</p>
     *
     * @return  The Type of this operation.
     */
    public OperationType getType() {
        return this.type;
    }

    /**
     * <p>Returns the Horizontal Slot of the Operation.</p>
     *
     * @return Integer with the Horizontal Slot of the Operation.
     */
    public int getHorizontalSlot() {
        return this.horizontalSlot;
    }

    /**
     * <p>Returns the Vertical Slot of the Operation.</p>
     *
     * @return Integer with the Vertical Slot of the Operation.
     */
    public int getVerticalSlot() {
        return verticalSlot;
    }

    /**
     * <p>Returns the Input Payloads for the Operation in this slot.</p>
     *
     * @return A Treemap containing the Input Payloads for the Operation in this slot.
     */
    public TreeMap<Integer, GraphPayloadSlot> getInputPayloads() {
        return inputPayloads;
    }

    /**
     * <p>Returns the Output Payloads for the Operation in this slot.</p>
     *
     * @return A Treemap containing the Output Payloads for the Operation in this slot.
     */
    public TreeMap<Integer, GraphPayloadSlot> getOutputPayloads() {
        return outputPayloads;
    }
}
