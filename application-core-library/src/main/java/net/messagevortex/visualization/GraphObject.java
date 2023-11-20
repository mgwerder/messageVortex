package net.messagevortex.visualization;

import java.awt.*;

public class GraphObject {
    private int[] x = {0};
    private int[] y = {0};
    private int width = 0;
    private int height = 0;
    private String infoListContents;
    private GraphObjectType type;

    /**
     * <p>Creates a new GraphObject, with a width and height value.</p>
     *
     * @param x                 Integer with the X coordinate.
     * @param y                 Integer with the Y coordinate.
     * @param width             Value of type Integer with the width of the Object.
     * @param height            Value of type Integer with the height of the Object.
     * @param infoListContents  String with the contents to be displayed in the infolist.
     * @param type              Type of the Graphed Object.
     */
    public GraphObject(int x, int y, int width, int height, String infoListContents, GraphObjectType type) {
        this.x[0] = x;
        this.y[0] = y;
        this.width = width;
        this.height = height;
        this.infoListContents = infoListContents;
        this.type = type;
    }

    /**
     * <p>Creates a new GraphObject of type Rhombus, without a width or height</p>
     *
     * @param x                 Array of the X coordinates of the 4 corners.
     * @param y                 Array of the Y coordinates of the 4 corners.
     * @param infoListContents  String with the Contents to be displayed in the infolist.
     * @param type              Type of the Graphed Object. Must be of type Rhombus.
     */
    public GraphObject(int[] x, int[] y, String infoListContents, GraphObjectType type) {
        if(x.length != 4 || y.length != 4) {
            throw new IllegalArgumentException("Expected an Array with length of 4, " +
                    "but instead received an Array with length: X-Coordinates: "
                    + x.length + " | Y-Coordinates: " + y.length);
        }
        if(type != GraphObjectType.RHOMBUS) {
            throw new IllegalArgumentException("Expected type Rhombus, instead received: " + type);
        }

        this.x = x;
        this.y = y;
        this.infoListContents = infoListContents;
        this.type = type;
    }

    /**
     * <p>Returns if a given point is within the Object</p>
     *
     * @param p The Point to be compared to the object.
     * @return  Returns a boolean indicating if it is or isn't within the object.
     */
    public boolean comparePosition(Point p, double zoomFactor) {
        boolean comparisonResult = false;

        switch (type) {
            case RECTANGLE:
                comparisonResult = p.x >= (x[0] * zoomFactor) && p.y >= (y[0] * zoomFactor) && p.x <= (x[0] + width) * zoomFactor && p.y <= (y[0] + height) * zoomFactor;
                break;
            case RHOMBUS: {
                int[] xScaled = {(int) (x[0] * zoomFactor), (int) (x[1] * zoomFactor), (int) (x[2] * zoomFactor), (int) (x[3] * zoomFactor)};
                int[] yScaled = {(int) (y[0] * zoomFactor), (int) (y[1] * zoomFactor), (int) (y[2] * zoomFactor), (int) (y[3] * zoomFactor)};
                int[] xRelative = {Math.abs(xScaled[0] - xScaled[1]), 0, Math.abs(xScaled[2] - xScaled[1]), 0};
                int[] yRelative = {0, Math.abs(yScaled[1] - yScaled[0]), 0, Math.abs(yScaled[3] - yScaled[0])};

                Point pRelative = new Point(Math.abs(p.x - xScaled[1]), Math.abs(p.y - yScaled[0]));

                if (((double) pRelative.x / (double) xRelative[0]) + ((double) pRelative.y / (double) yRelative[1]) <= 1.0) {
                    comparisonResult = true;
                }
                break;
            }
            case CIRCLE: {
                int xRelative = (int) (x[0] * zoomFactor) + 50;
                int yRelative = (int) (y[0] * zoomFactor) + 50;

                Point pRelative = new Point(Math.abs(p.x - xRelative), Math.abs(p.y - yRelative));

                if(Math.sqrt(pRelative.x * pRelative.x + pRelative.y * pRelative.y) <= 50) {
                    comparisonResult = true;
                }
                break;
            }
            case HEXAGON: {
                int xRelative = (int) ((x[0] + 50) * zoomFactor);
                int yRelative = (int) ((y[0] + 50) * zoomFactor);

                Point pRelative = new Point(p.x - xRelative, p.y - yRelative);

                if(Math.sqrt(pRelative.x * pRelative.x + pRelative.y * pRelative.y) <= 50 * zoomFactor) {
                    comparisonResult = true;
                }
                break;
            }
        }

        return comparisonResult;
    }

    /**
     * <p>Returns the String containing the Information to be displayed in the infolist panel.</p>
     *
     * @return  A String with all information for the infolist panel.
     */
    public String getInfoListContents() {
        return infoListContents;
    }
}
