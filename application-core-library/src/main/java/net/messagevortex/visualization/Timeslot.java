package net.messagevortex.visualization;

public class Timeslot {
    private int slotNumber;
    private final int startTime;
    private final int endTime;
    private final Routingblock rb;

    /**
     * <p>Creates a new Timeslot object with an unknown slot position</p>
     *
     * @param startTime     The Start time of the Timeslot.
     * @param endTime       The End time of the Timeslot.
     * @param rb            The Routing block associated with the Timeslot.
     */
    public Timeslot(int startTime, int endTime, Routingblock rb) {
        this.startTime = startTime;
        this.endTime = endTime;
        this.rb = rb;
    }

    /**
     * <p>Sets the slot for the timeslot</p>
     *
     * @param slot  Position of the slot in the graph.
     */
    public void setSlot(int slot) {
        slotNumber = slot;
    }

    /**
     * <p>Returns the start time of the timeslot</p>
     *
     * @return The start time as an integer.
     */
    public int getStartTime() {
        return startTime;
    }

    /**
     * <p>Returns the end time of the timeslot</p>
     *
     * @return The end time as an integer.
     */
    public int getEndTime() {
        return endTime;
    }

    /**
     * <p>Returns the Start and end time as a formatted string to draw in the graph</p>
     *
     * @return a string formatted with start and end time
     */
    public String getTimeString() {
        return startTime + " - " + endTime;
    }

    /**
     * <p>Returns the number of the time slot to draw the slot in the right position in the graph</p>
     *
     * @return an integer representing the slot number
     */
    public int getSlot() {
        return slotNumber;
    }

    /**
     * <p>Returns the Routingblock in this timeslot.</p>
     *
     * @return The Routingblock object that is associated with this timeslot.
     */
    public Routingblock getRb() {
        return rb;
    }
}
