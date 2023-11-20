package net.messagevortex.visualization;

public class RoutingLayerRoute {
    private final int originSlot;
    private final int targetSlot;
    private final int verticalSlot;
    private int inputNumber;
    private int routingLayerSlot = -1;

    /**
     * <p>Creates a new Routing Layer Routing for a operation.</p>
     *
     * @param originSlot        The Horizontal Origin slot for the routing.
     * @param targetSlot        The Horizontal Target slot for the routing.
     * @param verticalSlot      The Vertical slot for the route.
     * @param routingLayerSlot  The Vertical Routing layer slot for the route.
     *
     */
    public RoutingLayerRoute(int originSlot, int targetSlot, int verticalSlot, int routingLayerSlot, int inputNumber) {
        this.originSlot = originSlot;
        this.targetSlot = targetSlot;
        this.verticalSlot = verticalSlot;
        this.routingLayerSlot = routingLayerSlot;
        this.inputNumber = inputNumber;
    }

    /**
     * <p>Creates a new Routing Layer Route for an output which is not the input for another Operation.</p>
     *
     * @param originSlot    The Horizontal Origin slot for the routing.
     * @param targetSlot    The Horizontal Target slot for the routing.
     * @param verticalSlot  The Vertical slot for the route.
     */
    public RoutingLayerRoute(int originSlot, int targetSlot, int verticalSlot) {
        this.originSlot = originSlot;
        this.targetSlot = targetSlot;
        this.verticalSlot = verticalSlot;
    }

    /**
     * <p>Returns the Origin Slot fo the route.</p>
     *
     * @return an int with the horizontal Origin Slot.
     */
    public int getOriginSlot() {
        return this.originSlot;
    }

    /**
     * <p>Returns the Target Slot fo the route</p>
     *
     * @return an int with the horizontal Target Slot.
     */
    public int getTargetSlot() {
        return this.targetSlot;
    }

    /**
     * <p>Returns the Vertical Slot fo the route.</p>
     *
     * @return an int with the Vertical Slot.
     */
    public int getVerticalSlot() {
        return this.verticalSlot;
    }

    /**
     * <p>Returns the position of the route as the input into the next Operation.</p>
     *
     * @return an int with the position of the route as the input to the next Operation.
     */
    public int getInputNumber() {
        return this.inputNumber;
    }

    /**
     * <p>Returns the vertical Slot within the routing layer for the route.</p>
     *
     * @return an int with the Vertical Slot within the routing layer.
     */
    public int getRoutingLayerSlot() {
        return this.routingLayerSlot;
    }
}
