package net.messagevortex.generator;

import java.util.TreeMap;

public class Subpath {
    private final int startSequenceNumber;
    private final TreeMap<Integer, Hop> hops;

    public Subpath(int startSequenceNumber, TreeMap<Integer, Hop> hops) {
        this.startSequenceNumber = startSequenceNumber;
        this.hops = hops;
    }

    public void addHop(Hop h) {
        this.hops.put(getLastKey(hops) + 1, h);
    }

    public TreeMap<Integer, Hop> getHops() {
        return this.hops;
    }

    public int getStartSequenceNumber() {
        return this.startSequenceNumber;
    }

    private Integer getLastKey(TreeMap<Integer, ? extends GeneratorObject> tm) {
        if(!tm.isEmpty())
            return tm.lastKey();
        return -1;
    }
}