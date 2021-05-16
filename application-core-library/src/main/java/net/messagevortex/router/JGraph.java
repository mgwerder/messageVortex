package net.messagevortex.router;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.LinkedHashMap;
import java.util.Map;

public class JGraph extends JPanel implements MouseListener {

    private static final long serialVersionUID = 1213422324789789L;

    private static final int X_OFFSET = 20;
    private static final int Y_OFFSET = 10;
    private static final int ROUTE_BORDER = 10;
    private static final int BOX_HEIGHT = 15;
    private static final int BOX_WIDTH = 20;

    private int route = 0;

    private final GraphSet graph;

    private final TooltipContainer ttContainer = new TooltipContainer();

    /***
     * <p>Creates a graph with the specified set.</p>
     *
     * @param gs the set to be used
     */
    public JGraph(GraphSet gs) {
        addMouseListener(this);
        ToolTipManager.sharedInstance().registerComponent(this);
        this.graph = gs;
        update();
        ToolTipManager.sharedInstance().setInitialDelay(1);
        ToolTipManager.sharedInstance().setLightWeightPopupEnabled(true);
    }

    private void update() {
        GraphSet[] routes = graph.getRoutes();
        Dimension d = new Dimension(graph.getAnonymitySetSize() * (BOX_WIDTH + 1) + 2 * X_OFFSET,
                2 * Y_OFFSET + 2 * ROUTE_BORDER + routes.length * 4);
        setMinimumSize(d);
    }

    private void drawTopLabels(Graphics g) {
        double horizontalSpace = (0.0 + getWidth() - 2 * X_OFFSET - BOX_WIDTH)
                / (graph.getAnonymitySetSize() - 1);
        for (int i = 0; i < graph.getAnonymitySetSize(); i++) {
            int x = (int) (X_OFFSET + i * horizontalSpace);

            // draw top boxes
            if (graph.getAnonymity(i) == graph.getSource()) {
                g.setColor(Color.GREEN);
                g.fillRect(x, Y_OFFSET, BOX_WIDTH, BOX_HEIGHT);
                ttContainer.addTooltip(
                        new TooltipExtentBox(new Rectangle2D.Float(x, Y_OFFSET, BOX_WIDTH, BOX_HEIGHT)),
                        "Sender");
            } else if (graph.getAnonymity(i) == graph.getTarget()) {
                g.setColor(Color.RED);
                g.fillRect(x, Y_OFFSET, BOX_WIDTH, BOX_HEIGHT);
                ttContainer.addTooltip(
                        new TooltipExtentBox(new Rectangle2D.Float(x, Y_OFFSET, BOX_WIDTH, BOX_HEIGHT)),
                        "Recipient");
            } else {
                g.setColor(Color.BLACK);
                g.drawRect(x, Y_OFFSET, BOX_WIDTH, BOX_HEIGHT);
                ttContainer.addTooltip(
                        new TooltipExtentBox(new Rectangle2D.Float(x, Y_OFFSET, BOX_WIDTH, BOX_HEIGHT)),
                        "Anonymity node");
            }

            // Center Text in box
            FontMetrics metrics = g.getFontMetrics(g.getFont());
            int hgt = metrics.getHeight();
            int adv = metrics.stringWidth("" + i);
            g.setColor(Color.BLACK);
            g.drawString("" + i, x + BOX_WIDTH / 2 - (adv) / 2, Y_OFFSET + BOX_HEIGHT / 2 + (hgt + 2)
                    / 2 - 2);

            // draw vertical lines
            Graphics2D g3 = (Graphics2D) g.create();
            g3.setStroke(new BasicStroke(1, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL, 0,
                    new float[]{9}, 0));
            g3.drawLine(x + BOX_WIDTH / 2, Y_OFFSET + BOX_HEIGHT, x + BOX_WIDTH / 2, getHeight()
                    - Y_OFFSET - 2 * ROUTE_BORDER);
            g3.dispose();
        }
    }

    private void drawArrows(Graphics g) {
        GraphSet[] routes = graph.getRoutes();

        double horizontalSpace = (0.0 + getWidth() - 2 * X_OFFSET - BOX_WIDTH)
                / (graph.getAnonymitySetSize() - 1);
        double verticalSpace = (0.0 + getHeight() - 2 * Y_OFFSET - 2 * BOX_HEIGHT - ROUTE_BORDER)
                / (graph.size());
        Graphics2D g2 = (Graphics2D) (g.create());
        Stroke s = g2.getStroke();
        Stroke s2 = new BasicStroke(3);

        int lastY = 0;
        if (this.route >= 0) {
            System.out.println("## displaying route " + this.route + " (" + routes[this.route].size()
                    + ")");
        } else {
            System.out.println("## no route set");
        }
        for (int i = 0; i < graph.size(); i++) {
            Edge gr = graph.get(i);
            int x1 = (int) (X_OFFSET + (double) BOX_WIDTH / 2 + graph.getAnonymityIndex(gr.getFrom())
                    * horizontalSpace);
            int x2 = (int) (X_OFFSET + (double) BOX_WIDTH / 2 + graph.getAnonymityIndex(gr.getTo())
                    * horizontalSpace);
            int y = (int) (Y_OFFSET + 2 * BOX_HEIGHT + i * verticalSpace);

            if (this.route >= 0 && routes[this.route].contains(gr)) {
                System.out.println("##   route " + this.route + " contains " + i + " ("
                        + (this.route < 0 ? "none" : routes[this.route].size()) + '/' + gr.getStartTime()
                        + ')');
                g2.setColor(Color.GREEN);
                g2.setStroke(s2);
                if (lastY > 0) {
                    g2.drawLine(x1, lastY, x1, y);
                }
                lastY = y;
            } else {
                g2.setStroke(s);
                g2.setColor(Color.BLACK);
            }
            // draw arrow
            g2.drawLine(x1, y, x2, y);
            // draw arrowhead
            int xh = (int) ((double) (x2 - x1) / Math.abs(x2 - x1) * verticalSpace);
            g2.drawLine(x2, y, x2 - xh, y - xh / 4);
            g2.drawLine(x2, y, x2 - xh, y + xh / 4);
            g2.dispose();
        }
    }

    private void drawRouteButtons(Graphics g) {
        GraphSet[] routes = graph.getRoutes();

        double horizontalSpace = (double) (getWidth() - 2 * X_OFFSET) / (routes.length * 2 - 1);
        for (int i = 0; i < routes.length; i++) {
            int x1 = (int) ((getWidth() - (routes.length * 2 - 1) * horizontalSpace)
                    / 2 + i * horizontalSpace * 2);
            int y = getHeight() - Y_OFFSET - ROUTE_BORDER;
            if (this.route == i) {
                g.setColor(Color.BLUE);
                g.fillRect(x1, y, (int) horizontalSpace, ROUTE_BORDER);
            }
            g.setColor(Color.BLACK);
            g.drawRect(x1, y, (int) horizontalSpace, ROUTE_BORDER);
        }
    }

    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        drawTopLabels(g);
        // draw arrows
        drawArrows(g);

        // draw route buttons
        drawRouteButtons(g);
    }

    /**
     * This method is called automatically when the mouse is over the component.
     * Based on the location of the event, we detect if we are over one of
     * the circles. If so, we display some information relative to that circle
     * If the mouse is not over any circle we return the tooltip of the
     * component.
     */
    @Override
    public String getToolTipText(MouseEvent event) {
        String t = ttContainer.getTooltipText(new Point(event.getX(), event.getY()));
        if (t == null) {
            t = super.getToolTipText(event);
        }
        return t;
    }

    /***
     * <p>Sets the highlighted route.</p>
     *
     * <p>The selected route is highlighted in the graph.</p>
     *
     * @param r the route to be highlighted
     * @return the previously set route
     */
    public int setRoute(int r) {
        int s = graph.getRoutes().length;
        if (r < 0 || s <= r) {
            r = -1;
        }
        int old = this.route;
        this.route = r;
        if (old != r) {
            repaint();
        }
        return old;
    }

    @Override
    public void mousePressed(MouseEvent e) {
        // dummy no event management
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        // dummy no event management
    }

    @Override
    public void mouseEntered(MouseEvent e) {
        // dummy no event management
    }

    @Override
    public void mouseExited(MouseEvent e) {
        // dummy no event management
    }

    @Override
    public void mouseClicked(MouseEvent e) {
        if (e != null) {
            GraphSet[] routes = graph.getRoutes();
            double horizontalSpace = (double) (getWidth() - 2 * X_OFFSET) / (routes.length * 2 - 1);
            int offset = (int) (getWidth() - (routes.length * 2 - 1) * horizontalSpace) / 2;
            int tmp = e.getX() - offset;
            int pos = (int) Math.min(Math.floor((0.0 + tmp) / (horizontalSpace * 2)), routes.length);
            int y = getHeight() - Y_OFFSET - ROUTE_BORDER;
            tmp = tmp - (int) ((0.0 + pos) * 2 * horizontalSpace);
            if (e.getY() <= y + ROUTE_BORDER && e.getY() >= y && tmp < horizontalSpace) {
                int oldPos = setRoute(Math.min(routes.length, pos));
                if (oldPos == pos) {
                    setRoute(-1);
                }
            }
        }
    }

    /**
     * <p>Shows UI interface with graph with specified sizes.</p>
     *
     * @param x width of window
     * @param y height of window
     */
    public void createAndShowUserInterface(int x, int y) {
        System.out.println("Created GUI on event dispatching thread? "
                + SwingUtilities.isEventDispatchThread());
        JFrame f = new JFrame("Edge Demo");
        f.add(this);
        f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        f.setSize(x, y);
        f.pack();
        f.setVisible(true);
    }

    public void createAndShowUserInterface() {
        createAndShowUserInterface(250, 100);
    }

    /***
     * <p>gets an image of the current graph.</p>
     *
     * @param width the width of the screenshot in pixels
     * @param height the height of the screenshot in pixels
     * @return the image
     */
    public BufferedImage getScreenShot(int width, int height) {
        BufferedImage image = new BufferedImage(
                width,
                height,
                BufferedImage.TYPE_INT_RGB
        );
        setSize(width, height);
        paintComponent(image.getGraphics());
        return image;
    }

    /***
     * <p>Writes a screenshot of the current graph into a jpeg file.</p>
     *
     * @param filename name of the file to be written
     * @param width the width of the screenshot in pixels
     * @param height the height of the screenshot in pixels
     *
     * @throws IOException when writing file
     */
    public void saveScreenshot(String filename, int width, int height) throws IOException {
        BufferedImage image = getScreenShot(width, height);
        try (OutputStream os = Files.newOutputStream(Paths.get(filename))) {
            ImageIO.write(image, "jpg", os);
        }
    }

}
