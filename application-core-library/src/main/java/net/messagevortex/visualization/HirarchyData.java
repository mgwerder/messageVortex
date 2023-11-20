package net.messagevortex.visualization;

import net.messagevortex.asn1.RoutingCombo;
import net.messagevortex.asn1.encryption.DumpType;
import org.bouncycastle.asn1.ASN1Sequence;
import org.bouncycastle.asn1.ASN1TaggedObject;

import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import java.io.IOException;
import java.util.ArrayList;

public class HirarchyData {
    public DefaultTreeModel newTreeModel(ArrayList<RoutingCombo> routingComboList) throws IOException {
        DefaultMutableTreeNode rootNode = new DefaultMutableTreeNode("root");

        for (RoutingCombo rbElement : routingComboList) {
            DefaultMutableTreeNode node = new DefaultMutableTreeNode("node");

            HirarchyNode wsNode = new HirarchyNode(rbElement, HirarchyNode.NodeType.WORKSPACE);

            HirarchyNode rbNode = new HirarchyNode(rbElement, HirarchyNode.NodeType.ROUTINGBLOCK);
            wsNode.add(rbNode);
            node.add(wsNode);

            rootNode.add(node);
        }

        return new DefaultTreeModel(rootNode);
    }
}
