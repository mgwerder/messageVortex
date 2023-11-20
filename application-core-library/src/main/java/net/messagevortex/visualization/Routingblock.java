package net.messagevortex.visualization;

import net.messagevortex.asn1.RoutingCombo;

public class Routingblock {
    private int senderId = -1;
    private int recipientId;
    private final RoutingCombo rc;
    private String sender = "";

    /**
     * <p>Creates a new Routing block Object used to construct the graph.</p>
     *
     * @param senderId      The Node id of the sender.
     * @param recipientId   The Node id of the recipient.
     * @param rc            The RoutingCombo associated with this Routingblock.
     */
    public Routingblock(int senderId, int recipientId, RoutingCombo rc) {
        this.senderId = senderId;
        this.recipientId = recipientId;
        this.rc = rc;
    }

    /**
     * <p>Creates a Routingblock Object without Timing slots</p>
     *
     * @param routingCombo  The Routingcombo associated with this Routingblock
     * @param sender        The sender string
     */
    public Routingblock(RoutingCombo routingCombo, String sender) {
        this.rc = routingCombo;
        this.sender = sender;
    }

    /**
     * <p>Set the Node id of the sender.</p>
     *
     * @param senderId  The Node id of the sender.
     */
    public void setSenderId(int senderId) {
        this.senderId = senderId;
    }

    /**
     * <p>Set the Node id of the recipient.</p>
     *
     * @param recipientId   The Node if of the recipient.
     */
    public void setRecipientId(int recipientId) {
        this.recipientId = recipientId;
    }

    /**
     * <p>Returns the Node id of the sender</p>
     *
     * @return An Integer representing the id of the Sending Node.
     */
    public int getSenderId() {
        return senderId;
    }

    /**
     * <p>Returns the Node id of the recipient</p>
     *
     * @return An Integer representing the id of the Receiving Node.
     */
    public int getRecipientId() {
        return recipientId;
    }

    /**
     * <p>Returns the Routingcombo associated with this Routingblock.</p>
     *
     * @return  An object of type RoutingCombo, with the Routingcombo associated with this Routingblock.
     */
    public RoutingCombo getRoutingCombo() {
        return rc;
    }

    /**
     * <p>Returns the sender string of the Routingblock</p>
     *
     * @return  A String with the name of the Sender.
     */
    public String getSender() {
        return sender;
    }
}
