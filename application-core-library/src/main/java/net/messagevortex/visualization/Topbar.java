package net.messagevortex.visualization;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;


public class Topbar extends JPanel {
    private final JSlider zoomSlider;

    /**
     * <p>Creates a new Topbar element.</p>
     */
    public Topbar() {
        this.zoomSlider = new JSlider(5, 50, 10);
        this.add(this.zoomSlider);
    }

    /**
     * <p>Sets the center panel for the Zoomslider to be able to change the zoom factor</p>
     *
     * @param centerPanel   The center Panel that is currently selected in the tree.
     */
    public void setCenterPanel(ZoomPanel centerPanel) {
        zoomSlider.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                double zoomValue = (double) zoomSlider.getValue() / 10;
                centerPanel.setZoomFactor(zoomValue);
            }
        });
    }

    /**
     * <p>Sets the position of the slider.</p>
     *
     * @param value A double value to be set on the slider. The value is multiplied by 10.
     */
    public void setZoomSlider(double value) {
        int sliderPosition = (int) (value * 10);

        if(sliderPosition > 50) sliderPosition = 50;
        else if(sliderPosition < 5) sliderPosition = 5;

        zoomSlider.setValue(sliderPosition);
    }

    /**
     * <p>Returns the current value of the zoom slider.</p>
     * @return  An int representing the current value of the slider
     */
    public int getZoomSliderValue() {
        return zoomSlider.getValue();
    }
}
