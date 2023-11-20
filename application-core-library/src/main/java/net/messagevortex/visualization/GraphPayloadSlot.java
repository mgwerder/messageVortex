package net.messagevortex.visualization;

import java.awt.*;

public class GraphPayloadSlot {
    private final int payloadSlotId;
    private int horizontalSpacing;
    private boolean output = false;
    private boolean input = false;

    /**
     * <p>Creates a new Payload slot Object with the Payload slot ID.</p>
     *
     * @param payloadSlotId An Integer with the Payload Slot ID.
     */
    public GraphPayloadSlot(int payloadSlotId) {
        this.payloadSlotId = payloadSlotId;
    }

    /**
     * <p>Sets the horizontal spacing of the Payloadslots in the Graph as an integer indicating it's position.</p>
     *
     * @param horizontalSpacing An integer with the horizontal spacing of the slot in the Graph.
     */
    public void setHorizontalSpacing (int horizontalSpacing) {
        this.horizontalSpacing = horizontalSpacing;
    }

    /**
     * <p>Sets if the Payload slot is the output of a previous operation in the Graph.</p>
     *
     * @param output A boolean indicating if the operation is the output of a previous operation.
     */
    public void setIsOutput(boolean output) {
        this.output = output;
    }

    /**
     * <p>Sets if the Payload slot is the input to a later operation.</p>
     *
     * @param input A boolean indicating if the operation is the input to a later operation.
     */
    public void setIsInput(boolean input) {
        this.input = input;
    }

    /**
     * <p>Returns the horizontal spacing of the Payloadslots in the Graph.</p>
     *
     * @return An integer with the horizontal spacing of the slot in the Graph.
     */
    public int getHorizontalSpacing() {
        return this.horizontalSpacing;
    }

    /**
     * <p>Returns if the Payload is the output of another Operation.</p>
     *
     * @return a boolean if the Payload is the output of another Operation.
     */
    public boolean isOutput() {
        return this.output;
    }

    /**
     * <p>Returns if the Payload is the input to a later operation.</p>
     *
     * @return A boolean indicating if the Payload slot is the input to a later operation.
     */
    public boolean isInput() {
        return this.input;
    }

    /**
     * <p>Returns the Payload slot ID.</p>
     *
     * @return an Integer with the Payload slot ID.
     */
    public int getSlotId() {
        return this.payloadSlotId;
    }

    /**
     *<p>Draws a Payloadslot in the Graph.</p>
     *
     * @param g2d       The Graphics2D Object that is used to draw the Graph.
     * @param fm        A Fontmetrics Object.
     * @param width     The width of the Horizontal slot.
     * @param startX    The X-Coordinate at which the Horizontal slot starts.
     * @param size      The Number of total Payloadslots to be drawn in a single Horizontal slot.
     */
    public void drawSlot(Graphics2D g2d, FontMetrics fm, int width, int startX, int size, int horizontalSlot) {
    }

    /**
     *<p>Draws a Payloadslot in the Graph.</p>
     *
     * @param g2d       The Graphics2D Object that is used to draw the Graph.
     * @param fm        A Fontmetrics Object.
     * @param width     The width of the Horizontal slot.
     * @param startX    The X-Coordinate at which the Horizontal slot starts.
     * @param size      The Number of total Payloadslots to be drawn in a single Horizontal slot.
     * @param y         Y-Coordinate of the Slot in the graph.
     */
    public void drawSlot(Graphics2D g2d, FontMetrics fm, int width, int startX, int size, int y, int horizontalSlot) {
    }
}
