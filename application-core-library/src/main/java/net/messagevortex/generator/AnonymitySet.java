package net.messagevortex.generator;

import java.util.TreeMap;

public class AnonymitySet {
    static TreeMap<Integer, Node> nodes;
    static Node peer;

    /**
     * <p>Creates an AnonymitySet for a Peer.</p>
     *
     * @param peer The Node which is the Peer for the AnonymitySet.
     */
    public static void createAnonymitySet(Node peer) {
        nodes = MainClass.allNodes;
        AnonymitySet.peer = peer;
    }

    /**
     * <p>Get the Nodes of the AnonymitySet.</p>
     *
     * @return A TreeMap with the Nodes in the AnonymitySet.
     */
    public static TreeMap<Integer, Node> getNodes() {
        return AnonymitySet.nodes;
    }
}
