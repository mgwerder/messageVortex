package net.messagevortex.visualization;

public class Node {
    private int id;
    private final String name;

    /**
     * <p>Creates a new Node with information</p>
     *
     * @param name  The name to give the node.
     * @param id    A unique ID for the Node used to draw the routes later.
     */
    public Node(String name, int id) {
        this.name = name;
        this.id = id;
    }

    /**
     * <p>Sets the id of this node</p>
     *
     * @param id An integer with the ID that should be set of this node.
     */
    public void setId(int id) {
        this.id = id;
    }

    /**
     * <p>Returns the ID of a node used to identify it when drawing the graph</p>
     *
     * @return An integer representing the node
     */
    public int getId() {
        return id;
    }

    /**
     * <p>Returns the name of the node</p>
     *
     * @return a string with the name of the node
     */
    public String getName() {
        return name;
    }
}
