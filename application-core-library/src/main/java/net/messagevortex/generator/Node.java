package net.messagevortex.generator;

import java.util.TreeMap;

public class Node extends GeneratorObject implements Comparable<Node> {
    int ID;
    private final TreeMap<Integer, Hop> outgoingHops = new TreeMap<>();

    public Node(int ID) {
        this.ID = ID;
    }

    public String getID() {
        return String.valueOf(this.ID);
    }

    public TreeMap<Integer, Hop> getHops() {
        return this.outgoingHops;
    }

    @Override
    public int compareTo(Node o) {
        return o.ID - this.ID;
    }
}
