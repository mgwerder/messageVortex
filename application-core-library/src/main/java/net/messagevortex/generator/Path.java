package net.messagevortex.generator;

import java.util.TreeMap;
import java.util.concurrent.atomic.AtomicInteger;

public class Path extends GeneratorObject {
    private int numberOfHops = 0;
    private final TreeMap<Integer, Subpath> subpaths = new TreeMap<>();
    private Node previousNode;

    public Path() {
        this.subpaths.put(0, new Subpath(-1, new TreeMap<Integer, Hop>()));
    }

    public void addHop(Hop h, int subpath) {
        this.subpaths.get(subpath).addHop(h);
    }

    /**
     * <p>Inserts an Hop to a specified node in a specified Subpath.</p>
     *
     * @param node          The target Node for the hop.
     * @param subpathIndex  The Index of the subpath where the hop should be inserted.
     */
    public void insertHop(Node node, int subpathIndex) {
        int randomPosition = (int) (Math.random() * (subpaths.get(subpathIndex).getHops().size() - 2) + 1);

        while(subpaths.get(subpathIndex).getHops().get(randomPosition + 1).getStartNode() == node) {
            randomPosition = (int) (Math.random() * (subpaths.get(subpathIndex).getHops().size() - 2) + 1);
         }

        subpaths.get(subpathIndex).getHops().put(subpaths.get(subpathIndex).getHops().lastKey() + 1, new Hop(subpaths.get(subpathIndex).getHops().get(randomPosition - 1).getEndNode(), node, subpaths.get(subpathIndex).getHops().get(randomPosition - 1).getHopSequenceNumber() + 1));
        subpaths.get(subpathIndex).getHops().get(randomPosition + 1).setStartNode(node);
    }

    /**
     * <p>Inserts an subpath with a specified starting node.</p>
     *
     * @param path  Path to be inserted.
     * @param index Index of the starting index in the Main path.
     */
    public void insertSubpathWithStartNode(Path path, int index) {
        subpaths.put(subpaths.lastKey() + 1, new Subpath(index, path.subpaths.get(0).getHops()));
    }

    public void insertSubpath(Subpath subpath) {
        subpaths.put(subpaths.lastKey() + 1, subpath);
    }

    /**
     * <p>Get the Index of a Node in a path.</p>
     *
     * @param node  The Node which should be checked.
     * @return      The Index of the Hop
     */
    public int mainPathContainsStartnode(Node node) {
        AtomicInteger containsNode = new AtomicInteger(-1);

        subpaths.get(0).getHops().forEach((key, value) -> {
            if(value.getStartNode().ID == node.ID)
                containsNode.set(key);
        });

        return containsNode.get();
    }

    /**
     * <p>Returns a treemap with all subpaths of this path.</p>
     *
     * @return a treemap with all subpaths of this path, where the key 0 the mainpath is.
     */
    public TreeMap<Integer, Subpath> getSubpaths() {
        return this.subpaths;
    }
}
