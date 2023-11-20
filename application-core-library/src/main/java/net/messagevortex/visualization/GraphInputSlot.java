package net.messagevortex.visualization;

import java.awt.*;

public class GraphInputSlot extends GraphPayloadSlot {
    /**
     * <p>Creates a new Payload slot Object with the Payload slot ID.</p>
     *
     * @param payloadSlotId An Integer with the Payload Slot ID.
     */
    public GraphInputSlot(int payloadSlotId) {
        super(payloadSlotId);
    }

    @Override
    public void drawSlot(Graphics2D g2d, FontMetrics fm, int width, int startX, int size, int horizontalSlot) {
        if(!isOutput()) {
            int xStart = (width - (size * 160 + 40)) / 2;
            int x = xStart + 50 + (startX + (horizontalSlot * 160));
            int y = 100;
            g2d.fillOval(x, y, 100, 100);

            WorkspaceGraph.graphedObjects.put(WorkspaceGraph.objectGraphIterator, new GraphObject(x, y, 100, 100, String.valueOf(getSlotId()), GraphObjectType.CIRCLE));
            WorkspaceGraph.objectGraphIterator++;

            int textWidth = fm.stringWidth(String.valueOf(getSlotId()));
            int textHeight = fm.getHeight();
            g2d.setColor(Color.BLACK);
            g2d.drawString(String.valueOf(getSlotId()), x + (100 - textWidth) / 2, y + (100 + textHeight) / 2);
            g2d.setColor(new Color(69, 93, 133));
        }
    }
}
