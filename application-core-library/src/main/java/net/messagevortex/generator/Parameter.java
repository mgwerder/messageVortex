package net.messagevortex.generator;

public class Parameter {
    static int redundantNodes;
    static int maxLengthPath;
    static int totalMaxLength;
    static int minDiagnosePaths;
    static int pathCount;

    /**
     * <p>Assigns the Parameters for a Routingblock generator.</p>
     *
     * @param redundantNodes Minimum Number of redundant Nodes for a Routing graph.
     */
    public static void assignParameter(int redundantNodes) {
        Parameter.redundantNodes = redundantNodes;
        totalMaxLength = AnonymitySet.nodes.size();
        pathCount = Math.round(redundantNodes * ((float) AnonymitySet.nodes.size() / 12));
        maxLengthPath = totalMaxLength / pathCount + 1;
        minDiagnosePaths = (int) (Math.random() * (pathCount + 3));
    }
}
