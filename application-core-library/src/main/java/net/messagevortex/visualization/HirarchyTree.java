package net.messagevortex.visualization;

import net.messagevortex.asn1.RoutingCombo;
import net.messagevortex.asn1.encryption.DumpType;
import org.bouncycastle.asn1.ASN1Sequence;
import org.bouncycastle.asn1.ASN1TaggedObject;

import javax.swing.*;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.io.IOException;
import java.util.TreeMap;

public class HirarchyTree extends JTree {
    InfoList infoList;
    private final JScrollPane centerPanel;
    private final Topbar topbar;

    /**
     * <p>Creates a new Hirachy Tree with the required information.</p>
     *
     * @param treeModel     The treemodel to be displayed in the tree.
     * @param infoList      The Infolist component for the information to be displayed in.
     * @param centerPanel   The JScrollpane in which the main graphics of the application are shown in.
     * @param topbar        The topbar of the Frame.
     */
    public HirarchyTree(DefaultTreeModel treeModel, InfoList infoList, JScrollPane centerPanel, Topbar topbar) {
        this.infoList = infoList;
        this.centerPanel = centerPanel;
        this.topbar = topbar;

        setModel(treeModel);

        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                try {
                    nodeSelected(e);
                } catch (IOException ex) {
                    throw new RuntimeException(ex);
                }
            }
        });
    }

    private void nodeSelected(MouseEvent e) throws IOException {
        TreePath tp = getPathForLocation(e.getX(), e.getY());
        if(tp != null) {
            Object lastTreeNode = tp.getLastPathComponent();
            if(lastTreeNode instanceof HirarchyNode) {
                ZoomPanel panel;
                if(((HirarchyNode) lastTreeNode).getType() == HirarchyNode.NodeType.ROUTINGBLOCK) {
                    String data = ((HirarchyNode) lastTreeNode).getData().dumpValueNotation("", DumpType.INTERNAL);

                    infoList.updateList(data);

                    // Create Graph
                    panel = new RBGraph((RoutingCombo) ((HirarchyNode) lastTreeNode).getData(), topbar);
                    panel.addMouseListener(new MouseAdapter() {
                        @Override
                        public void mouseClicked(MouseEvent e) {
                            panel.mouseClick(e.getPoint(), infoList);
                        }
                    });
                }
                else if(((HirarchyNode) lastTreeNode).getType() == HirarchyNode.NodeType.WORKSPACE) {
                    TreeMap<Integer, ASN1TaggedObject> operations = new TreeMap<>();
                    int childCount = ((HirarchyNode) lastTreeNode).getChildCount();
                    for(int i = 0; i < childCount; i++) {
                        ASN1Sequence rb = ((ASN1Sequence) ((HirarchyNode) ((HirarchyNode) lastTreeNode).getChildAt(i)).getData().toAsn1Object(DumpType.INTERNAL));
                        operations.put(i, (ASN1TaggedObject) rb.getObjectAt(7));
                    }

                    panel = new WorkspaceGraph(operations, topbar);
                    panel.addMouseListener(new MouseAdapter() {
                        @Override
                        public void mouseClicked(MouseEvent e) {
                            panel.mouseClick(e.getPoint(), infoList);
                        }
                    });
                    panel.addMouseMotionListener(new MouseMotionAdapter() {
                        @Override
                        public void mouseMoved(MouseEvent e) {
                            panel.mouseMoved(e);
                        }
                    });
                }
                else {
                    panel = new ZoomPanel();
                }

                topbar.setCenterPanel(panel);
                centerPanel.getViewport().add(panel);
                centerPanel.revalidate();
                centerPanel.repaint();
            }
        }
    }
}
