package net.messagevortex.generator;

import java.util.TreeMap;
import java.util.concurrent.atomic.AtomicInteger;

public class testIterationResult {
    private final TreeMap<Node, Integer> OutgoingAbsolute;
    private final TreeMap<Node, Integer> IncomingAbsolute;

    public testIterationResult(TreeMap<Node, Integer> OutgoingAbsolute, TreeMap<Node, Integer> IncomingAbsolute) {
        this.OutgoingAbsolute = OutgoingAbsolute;
        this.IncomingAbsolute = IncomingAbsolute;
    }

    public TreeMap<Node, Integer> getAbsoluteOutgoing() {
        return OutgoingAbsolute;
    }

    public TreeMap<Node, Integer> getAbsoluteIncoming() {
        return IncomingAbsolute;
    }
}
