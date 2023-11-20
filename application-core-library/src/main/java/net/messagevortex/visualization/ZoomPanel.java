package net.messagevortex.visualization;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.util.TreeMap;

public class ZoomPanel extends JPanel implements MouseWheelListener {
    protected double zoomFactor = 1.0;
    protected Topbar topbar;
    protected static TreeMap<Integer, GraphObject> graphedObjects = new TreeMap<>();

    public void setZoomFactor(double zoomFactor) {
        this.zoomFactor = zoomFactor;
        if(this.zoomFactor > 5) this.zoomFactor = 5;
        if(this.zoomFactor < 0.5) this.zoomFactor = 0.5;

        this.revalidate();
        this.repaint();
    }

    /**
     * <p>Handles a mouse click on the JPanel.</p>
     *
     * @param p    the Point on the JPanel that was clicked
     */
    public void mouseClick(Point p, InfoList infoList) {
        graphedObjects.forEach((key, value) -> {
            if(value.comparePosition(p, zoomFactor)) {
                String info = value.getInfoListContents();
                infoList.updateList(info);
            }
        });
    }

    public void mouseMoved(MouseEvent e) {
        return;
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
    }

    @Override
    public Dimension getPreferredSize() {
        return new Dimension();
    }

    @Override
    public void mouseWheelMoved(MouseWheelEvent e) {
        if(e.isControlDown()) {
            setZoomFactor(zoomFactor + e.getWheelRotation() * 0.1);
            topbar.setZoomSlider(zoomFactor);
        }
        else {
            getParent().dispatchEvent(e);
        }
    }
}
