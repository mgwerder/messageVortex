package net.messagevortex.visualization;

import java.awt.*;
import java.util.TreeMap;

public class GraphRoutings {
    private TreeMap<Integer, Point> routeCoordinates = new TreeMap<>();
    private int routeStroke = 1;
    private Color color = Color.BLACK;
    private int slotID = -1;

    /**
     * <p>Creates a new Graphroute object based on the Coordinates provided. At least 2 points need to be provided to be a valid route.</p>
     *
     * @param routeCoordinates A Treemap of Points, with the coordinates of the Route in the Graph.
     */
    public GraphRoutings(TreeMap<Integer, Point> routeCoordinates, int slotID) {
        if(routeCoordinates.size() < 2) {
            throw new IllegalArgumentException("A route requires at least 2 Points to be a valid route.");
        }
        else {
            this.routeCoordinates = routeCoordinates;
            this.slotID = slotID;
        }
    }

    /**
     * <p>Draws the route in the graph.</p>
     *
     * @param g2d The Graphics2D Instance used to draw the Graph.
     */
    public void drawRoute(Graphics2D g2d) {
        g2d.setStroke(new BasicStroke(routeStroke));
        g2d.setColor(color);

        for (int i = 1; i < routeCoordinates.size(); i++) {
            g2d.drawLine(routeCoordinates.get(i - 1).x, routeCoordinates.get(i - 1).y, routeCoordinates.get(i).x, routeCoordinates.get(i).y);
        }

        this.routeStroke = 1;
        this.color = Color.BLACK;
        g2d.setColor(Color.BLACK);
        g2d.setStroke(new BasicStroke(1));
    }

    public int getSlotID() {
        return this.slotID;
    }

    /**
     * <p>Checks if the mouse hovers over the route drawn in the graph.</p>
     *
     * @param p             The Point at which the mouse is.
     * @param zoomFactor    The current zoomfactor of the Graph.
     * @return              A boolean indicating if the Mouse is hovering over the route.
     */
    public boolean checkMouseOver(Point p, double zoomFactor) {
        int i = 1;
        while(i < routeCoordinates.size()) {
            int y1 = (int) (routeCoordinates.get(i).y * zoomFactor);
            int y2 = (int) (routeCoordinates.get(i-1).y * zoomFactor);
            int x1 = (int) (routeCoordinates.get(i).x * zoomFactor);
            int x2 = (int) (routeCoordinates.get(i-1).x * zoomFactor);
            double tolerance = 1E-6;

            if(x1 == x2 || routeCoordinates.get(i).x == routeCoordinates.get(i-1).x) {
                if(p.x == routeCoordinates.get(i).x * zoomFactor && p.y <= Math.max(y1,y2) && p.y >= Math.min(y1,y2)) return true;
            }
            else {
                if(p.x == x1) {
                    if(p.y == y1) return true;
                } else {
                    double differenceIncline = Math.abs((p.y - y1) / (x2 - x1) - (y2 - y1) / (p.x - x1));
                    if (differenceIncline < tolerance) {
                        if (p.x <= Math.max(x1, x2) && p.x >= Math.min(x1, x2) && p.y <= Math.max(y1, y2) && p.y >= Math.min(y1, y2))
                            return true;
                    }
                }
            }
            i++;
        }

        return false;
    }

    /**
     * <p>Highlights the route in the graph.</p>
     *
     * @param c         The color to highlight the Route with.
     * @param stroke    The stroke thickness to highligh the route with.
     */
    public void highlightRoute(Color c, int stroke) {
        this.color = c;
        this.routeStroke = stroke;
    }
}
