package net.messagevortex.generator;

import net.messagevortex.asn1.Operation;

import java.util.TreeMap;

public class Hop extends GeneratorObject {
    private Node startNode;
    private final Node endNode;
    private final int hopSequenceNumber;
    private long startTime;
    private long endTime;
    private final TreeMap<Integer, Operation> operations = new TreeMap<>();

    /**
     * <p>Creates a new Hop.</p>
     *
     * @param startNode         The Start Node of the Hop.
     * @param endNode           The End Node of the Hop.
     * @param hopSequenceNumber The Sequence Number of the Hop.
     */
    public Hop(Node startNode, Node endNode, int hopSequenceNumber) {
        this.startNode = startNode;
        this.endNode = endNode;
        this.hopSequenceNumber = hopSequenceNumber;
    }

    /**
     * <p>Sets the Start Node of the Hop.</p>
     *
     * @param n The new Start Node.
     */
    public void setStartNode(Node n) {
        this.startNode = n;
    }

    /**
     * <p>Sets the Start Time of the Hop.</p>
     *
     * @param startTime The Start Time.
     */
    public void setStartTime(long startTime) {
        this.startTime = startTime;
    }

    /**
     * <p>Sets the End Time of the Hop.</p>
     *
     * @param endTime The End Time.
     */
    public void setEndTime(long endTime) {
        this.endTime = endTime;
    }

    /**
     * <p>Adds an Operation to the Hop.</p>
     *
     * @param op The operation to be added.
     */
    public void addOperation(Operation op) {
        if(!operations.isEmpty())
            operations.put(operations.lastKey() + 1, op);
        else
            operations.put(0, op);
    }

    /**
     * <p>Get the Starting Node of the Hop.</p>
     *
     * @return The Starting Node.
     */
    public Node getStartNode() {
        return startNode;
    }

    /**
     * <p>Get the End Node of the Hop.</p>
     *
     * @return The End Node.
     */
    public Node getEndNode() {
        return endNode;
    }

    /**
     * <p>Get the Sequence Number of the Hop.</p>
     *
     * @return The Sequence Number.
     */
    public int getHopSequenceNumber() {
        return hopSequenceNumber;
    }

    /**
     * <p>Get the Start Time of the Hop.</p>
     *
     * @return The Start Time.
     */
    public long getStartTime() {
        return startTime;
    }

    /**
     * <p>Get the End Time of the Hop.</p>
     *
     * @return The End Time.
     */
    public long getEndTime() {
        return endTime;
    }

    /**
     * <p>Get all operations applied to the Hop.</p>
     *
     * @return A TreeMap with the Operations.
     */
    public TreeMap<Integer, Operation> getOperations() {
        return this.operations;
    }
}
