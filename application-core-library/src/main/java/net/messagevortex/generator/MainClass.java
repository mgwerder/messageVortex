package net.messagevortex.generator;

import java.io.IOException;
import java.util.TreeMap;

public class MainClass {
    final static int minLength = 3;
    final static TreeMap<Integer, Node> allNodes = new TreeMap<>();
    final static int knownNodes = 25;
    static Node receiver = null;
    static Node sender = null;

    public static void main(String [] args) throws IOException {
        initiateNodes(knownNodes);
        sender = MainClass.allNodes.get(5);
        receiver = MainClass.allNodes.get(1);

        Tests tests = new Tests();
        tests.executeTests(1);
    }

    private static void initiateNodes(int setSize) {
        for(int i = 0; i < setSize; i++) {
            allNodes.put(i, new Node(i));
        }
    }

    public static Node getRandomNode(TreeMap<Integer, Node> nodes) {
        return (Node) nodes.values().toArray()[(int) (Math.random() * (nodes.size() - 1))];
    }
}