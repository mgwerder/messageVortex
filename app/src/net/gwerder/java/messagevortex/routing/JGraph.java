package net.gwerder.java.messagevortex.routing;
// ************************************************************************************
// * Copyright (c) 2018 Martin Gwerder (martin@gwerder.net)
// *
// * Permission is hereby granted, free of charge, to any person obtaining a copy
// * of this software and associated documentation files (the "Software"), to deal
// * in the Software without restriction, including without limitation the rights
// * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
// * copies of the Software, and to permit persons to whom the Software is
// * furnished to do so, subject to the following conditions:
// *
// * The above copyright notice and this permission notice shall be included in all
// * copies or substantial portions of the Software.
// *
// * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
// * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
// * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
// * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
// * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
// * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
// * SOFTWARE.
// ************************************************************************************

import net.gwerder.java.messagevortex.asn1.IdentityStore;
import net.gwerder.java.messagevortex.asn1.IdentityStoreBlock;
import net.gwerder.java.messagevortex.asn1.encryption.DumpType;
import org.bouncycastle.asn1.DEROutputStream;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.geom.Ellipse2D;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by martin.gwerder on 13.06.2016.
 */
public class JGraph extends JPanel implements MouseListener {

  private static final long serialVersionUID = 1213422324789789L;

  private static final int X_OFFSET = 20;
  private static final int Y_OFFSET = 10;
  private static final int ROUTE_BORDER = 10;
  private static final int BOX_HEIGHT = 15;
  private static final int BOX_WIDTH = 20;

  private Map<Shape, String> tooltipps = new HashMap<>();

  private int route = 0;

  private GraphSet graph;

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
    Dimension d = new Dimension(graph.getAnonymitySetSize() * (BOX_WIDTH + 1) + 2 * X_OFFSET, 2 * Y_OFFSET + 2 * ROUTE_BORDER + routes.length * 4);
    setMinimumSize(d);
  }

  private void drawTopLabels(Graphics g) {
    double xSpace = (0.0 + getWidth() - 2 * X_OFFSET - BOX_WIDTH) / (graph.getAnonymitySetSize() - 1);
    for (int i = 0; i < graph.getAnonymitySetSize(); i++) {
      int x = (int) (X_OFFSET + i * xSpace);

      // draw top boxes
      if (graph.getAnonymity(i) == graph.getSource()) {
        g.setColor(Color.GREEN);
        g.fillRect(x, Y_OFFSET, BOX_WIDTH, BOX_HEIGHT);
      }
      if (graph.getAnonymity(i) == graph.getTarget()) {
        g.setColor(Color.RED);
        g.fillRect(x, Y_OFFSET, BOX_WIDTH, BOX_HEIGHT);
      }
      g.setColor(Color.BLACK);
      g.drawRect(x, Y_OFFSET, BOX_WIDTH, BOX_HEIGHT);

      // Center Text in box
      FontMetrics metrics = g.getFontMetrics(g.getFont());
      int hgt = metrics.getHeight();
      int adv = metrics.stringWidth("" + i);
      g.setColor(Color.BLACK);
      g.drawString("" + i, x + BOX_WIDTH / 2 - (adv) / 2, Y_OFFSET + BOX_HEIGHT / 2 + (hgt + 2) / 2 - 2);

      // draw vertical lines
      Graphics2D g3 = (Graphics2D) g.create();
      g3.setStroke(new BasicStroke(1, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL, 0, new float[]{9}, 0));
      g3.drawLine(x + BOX_WIDTH / 2, Y_OFFSET + BOX_HEIGHT, x + BOX_WIDTH / 2, getHeight() - Y_OFFSET - 2 * ROUTE_BORDER);
      g3.dispose();
    }
  }

  private void drawArrows(Graphics g) {
    GraphSet[] routes = graph.getRoutes();

    double xSpace = (0.0 + getWidth() - 2 * X_OFFSET - BOX_WIDTH) / (graph.getAnonymitySetSize() - 1);
    double ySpace = (0.0 + getHeight() - 2 * Y_OFFSET - 2 * BOX_HEIGHT - ROUTE_BORDER) / (graph.size());
    Graphics2D g2 = (Graphics2D) (g.create());
    Stroke s = g2.getStroke();
    Stroke s2 = new BasicStroke(3);

    int lastY = 0;
    System.out.println("## displaying route " + this.route + " (" + routes[this.route].size() + ")");
    for (int i = 0; i < graph.size(); i++) {
      Edge gr = graph.get(i);
      int x1 = (int) (X_OFFSET + (double) BOX_WIDTH / 2 + graph.getAnonymityIndex(gr.getFrom()) * xSpace);
      int x2 = (int) (X_OFFSET + (double) BOX_WIDTH / 2 + graph.getAnonymityIndex(gr.getTo()) * xSpace);
      int y = (int) (Y_OFFSET + 2 * BOX_HEIGHT + i * ySpace);

      if (routes[this.route].contains(gr)) {
        System.out.println("##   route " + this.route + " contains " + i + " (" + routes[this.route].size() + "/" + gr.getStartTime() + ")");
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
      int xh = (int) ((double) (x2 - x1) / Math.abs(x2 - x1) * ySpace);
      g2.drawLine(x2, y, x2 - xh, y - xh / 4);
      g2.drawLine(x2, y, x2 - xh, y + xh / 4);
    }
  }

  private void drawRouteButtons(Graphics g) {
    GraphSet[] routes = graph.getRoutes();

    double xSpace = (0.0 + getWidth() - 2 * X_OFFSET - BOX_WIDTH) / (graph.getAnonymitySetSize() - 1);

    xSpace = (double) (getWidth() - 2 * X_OFFSET) / (routes.length * 2 - 1);
    for (int i = 0; i < routes.length; i++) {
      int x1 = (int) ((getWidth() - (routes.length * 2 - 1) * xSpace) / 2 + i * xSpace * 2);
      int y = getHeight() - Y_OFFSET - ROUTE_BORDER;
      if (this.route == i) {
        g.setColor(Color.BLUE);
        g.fillRect(x1, y, (int) xSpace, ROUTE_BORDER);
      }
      g.setColor(Color.BLACK);
      g.drawRect(x1, y, (int) xSpace, ROUTE_BORDER);
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
    Point p = new Point(event.getX(), event.getY());
    String t = tooltipForCircle(p, new Ellipse2D.Double(0, 0, 20, 20));
    if (t != null) {
      return t;
    }
    return "No location effective tooltip (x=" + event.getX() + "/y=" + event.getY() + "" + super.getToolTipText(event);
  }

  private String tooltipForCircle(Point p, Ellipse2D circle) {
    // Test to check if the point  is inside circle
    if (circle.contains(p)) {
      // p is inside the circle, we return some information
      // relative to that circle.
      return "Circle: (" + circle.getX() + " " + circle.getY() + ")";
    }
    return null;
  }

  public int setRoute(int r) {
    int s = graph.getRoutes().length;
    if (r < 0 || s <= r) {
      throw new NullPointerException("unable to find adressed route r (0<=" + r + "<" + s + ")");
    }
    int old = this.route;
    this.route = r;
    if (old != r) {
      repaint();
    }
    return old;
  }

  public void mousePressed(MouseEvent e) {
    // dummy no event management
  }

  public void mouseReleased(MouseEvent e) {
    // dummy no event management
  }

  public void mouseEntered(MouseEvent e) {
    // dummy no event management
  }

  public void mouseExited(MouseEvent e) {
    // dummy no event management
  }

  public void mouseClicked(MouseEvent e) {
    if (e != null) {
      GraphSet[] routes = graph.getRoutes();
      double xSpace = (double) (getWidth() - 2 * X_OFFSET) / (routes.length * 2 - 1);
      int offset = (int) (getWidth() - (routes.length * 2 - 1) * xSpace) / 2;
      int tmp = e.getX() - offset;
      int pos = (int) Math.min(Math.floor((0.0 + tmp) / (xSpace * 2)), routes.length);
      int y = getHeight() - Y_OFFSET - ROUTE_BORDER;
      tmp = tmp - (int) ((0.0 + pos) * 2 * xSpace);
      if (e.getY() <= y + ROUTE_BORDER && e.getY() >= y && tmp < xSpace) {
        setRoute(Math.min(routes.length, pos));
      }
    }
  }

  public static void main(String[] args) throws IOException {
    IdentityStore is = null;
    try {
      is = new IdentityStore(new File(System.getProperty("java.io.tmpdir") + "/IdentityStoreExample1.der"));
    } catch (IOException ioe) {
      is = IdentityStore.getNewIdentityStoreDemo(false);
      DEROutputStream f = new DEROutputStream(new FileOutputStream(System.getProperty("java.io.tmpdir") + "/IdentityStoreExample1.der"));
      f.writeObject(is.toAsn1Object(DumpType.ALL_UNENCRYPTED));
      f.close();
    }
    SimpleMessageFactory smf = new SimpleMessageFactory("", 0, 1, is.getAnonSet(7).toArray(new IdentityStoreBlock[0]), is);
    smf.build();
    System.out.println("got " + smf.getGraph().getRoutes().length + " routes");
    final JGraph jg = new JGraph(smf.getGraph());
    SwingUtilities.invokeLater(new Runnable() {
      public void run() {
        jg.createAndShowGUI();
      }
    });
  }

  private void createAndShowGUI() {
    System.out.println("Created GUI on event dispatching thread? " + SwingUtilities.isEventDispatchThread());
    JFrame f = new JFrame("Edge Demo");
    f.add(this);
    f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    f.setSize(250, 250);
    f.setVisible(true);
  }

}
