package net.messagevortex.visualization;

import net.messagevortex.asn1.Block;
import net.messagevortex.asn1.RoutingCombo;

import javax.swing.tree.DefaultMutableTreeNode;

public class HirarchyNode extends DefaultMutableTreeNode {
    enum NodeType {
        ROUTINGBLOCK,
        WORKSPACE,
        NODE,
    }

    private final Block data;
    private final NodeType type;

    public HirarchyNode(Block data, NodeType type) {
        this.data = data;
        this.type = type;
    }

    public Block getData() {
        return this.data;
    }

    public NodeType getType() {
        return this.type;
    }
}
