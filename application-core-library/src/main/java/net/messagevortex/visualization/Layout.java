package net.messagevortex.visualization;

import net.messagevortex.asn1.RoutingCombo;
import java.awt.*;
import java.io.IOException;
import java.util.ArrayList;
import javax.swing.*;
import javax.swing.tree.DefaultTreeModel;

public class Layout {
    private static void createAndShowGUI(DefaultTreeModel treeModel) throws IOException {
        JFrame f = new JFrame();
        f.setLayout(new BorderLayout());

        // Initialize Infolist
        InfoList infoList = new InfoList();

        // Create Window splits
        final JSplitPane splitPaneLeft = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        final JSplitPane splitPaneRight = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);

        JScrollPane scrollPane = new JScrollPane();
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);

        // Create Topbar
        Topbar topBar = new Topbar();

        // Create Hirarchy tree
        HirarchyTree tree = new HirarchyTree(treeModel, infoList, scrollPane, topBar);
        JScrollPane scrollableTree = new JScrollPane(tree);
        scrollableTree.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
        scrollableTree.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);

        // Make infolist scrollable
        JScrollPane scrollableList = new JScrollPane(infoList);
        scrollableList.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
        scrollableList.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);

        // Add Components to Splitpanes
        splitPaneLeft.setLeftComponent(scrollableTree);
        splitPaneLeft.setRightComponent(scrollPane);
        splitPaneRight.setLeftComponent(splitPaneLeft);
        splitPaneRight.setRightComponent(scrollableList);

        // Add Components to Frame
        f.getContentPane().add(topBar, BorderLayout.NORTH);
        f.getContentPane().add(splitPaneRight, BorderLayout.CENTER);
        splitPaneRight.setDividerLocation(0.85);
        f.setSize(1920, 1080);
        f.setVisible(true);
    }

    public static void main(String[] args) throws IOException {
        GenerateRB genRB = new GenerateRB();
        ArrayList<RoutingCombo> rbList = new ArrayList<>();

        rbList.add(genRB.generateRoutingBlock());

        HirarchyData data = new HirarchyData();
        DefaultTreeModel treeModel = data.newTreeModel(rbList);

        javax.swing.SwingUtilities.invokeLater(() -> {
            try {
                createAndShowGUI(treeModel);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
    }
}
