package net.messagevortex.visualization;

import net.messagevortex.asn1.RoutingCombo;
import net.messagevortex.asn1.encryption.DumpType;

import javax.swing.*;

public class InfoList extends JTextArea {
    public void updateList(String data) {
        setText(data);
    }

    @Override
    public void setEditable(boolean b) {
        super.setEditable(false);
    }
}
