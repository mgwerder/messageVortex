package net.gwerder.java.mailvortex.routing;

import net.gwerder.java.mailvortex.asn1.IdentityStore;
import net.gwerder.java.mailvortex.asn1.IdentityStoreBlock;

import java.awt.event.MouseListener;
import java.util.List;
import javax.swing.JPanel;
import javax.swing.JFrame;
import javax.swing.SwingUtilities;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.FontMetrics;
import java.awt.event.MouseMotionListener;
import java.io.IOException;

/**
 * Created by martin.gwerder on 13.06.2016.
 */
public class JGraph extends JPanel implements MouseMotionListener {

    private int X_OFFSET   = 20;
    private int Y_OFFSET   = 10;
    private int BOX_HEIGHT = 15;
    private int BOX_WIDTH  = 20;

    private int squareX = 50;
    private int squareY = 50;
    private int squareW = 20;
    private int squareH = 20;

    private GraphSet graph;

    public JGraph(GraphSet gs) {
        this.graph=gs;
    }

    private void moveSquare(int x, int y) {
        int OFFSET = 1;
        if ((squareX!=x) || (squareY!=y)) {
            repaint(squareX,squareY,squareW+OFFSET,squareH+OFFSET);
            squareX=x;
            squareY=y;
            repaint(squareX,squareY,squareW+OFFSET,squareH+OFFSET);
        }
    }


    public Dimension getPreferredSize() {
        return new Dimension(800,600);
    }

    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        int ySpace=(getHeight()-2*Y_OFFSET-2*BOX_HEIGHT)/(graph.size()-1);
        int xSpace=(getWidth()-2*X_OFFSET-BOX_WIDTH)/(graph.getAnonymitySetSize()-1);
        for(int i=0;i<graph.getAnonymitySetSize();i++) {
            int x=X_OFFSET+i*xSpace;
            if(graph.getAnonymity(i)==graph.getSource()) {
                g.setColor(Color.GREEN);
                g.fillRect(x,Y_OFFSET,BOX_WIDTH,BOX_HEIGHT);
            }
            if(graph.getAnonymity(i)==graph.getTarget()) {
                g.setColor(Color.RED);
                g.fillRect(x,Y_OFFSET,BOX_WIDTH,BOX_HEIGHT);
            }
            g.setColor(Color.BLACK);
            g.drawRect( x,Y_OFFSET,BOX_WIDTH,BOX_HEIGHT );
            // Center Text in box
            FontMetrics metrics = g.getFontMetrics(g.getFont());
            int hgt = metrics.getHeight();
            int adv = metrics.stringWidth(""+i);
            g.setColor(Color.BLACK);
            g.drawString(""+i,x+BOX_WIDTH/2-(adv)/2,Y_OFFSET+BOX_HEIGHT/2+(hgt+2)/2-2);
            // draw vertical lines
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setStroke(new BasicStroke(1, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL, 0, new float[]{9}, 0));
            g2.drawLine(x+BOX_WIDTH/2,Y_OFFSET+BOX_HEIGHT,x+BOX_WIDTH/2,getHeight()-Y_OFFSET);
            g2.dispose();
        }
        List<Graph> route=graph.getRoutes()[0];
        int lastY=0;
        for(int i=0;i<graph.size();i++) {
            Graph gr=graph.get(i);
            int x1=X_OFFSET+BOX_WIDTH/2+graph.getAnonymityIndex( gr.getFrom() )*xSpace;
            int x2=X_OFFSET+BOX_WIDTH/2+graph.getAnonymityIndex( gr.getTo() )*xSpace;
            int y=Y_OFFSET+2*BOX_HEIGHT+i*ySpace;

            if(route.contains( gr )) {
                g.setColor( Color.GREEN );
                if(lastY>0) g.drawLine(x1,lastY,x1,y);
                lastY=y;
            }else {
                g.setColor(Color.BLACK);
            }
            // draw arrow
            g.drawLine(x1,y,x2,y);
            // draw arrowhead
            int xh=(x2-x1)/Math.abs(x2-x1)*ySpace;
            g.drawLine(x2,y,x2-xh,y-xh/4);
            g.drawLine(x2,y,x2-xh,y+xh/4);
        }
    }

    public static void main(String[] args) throws IOException{
        IdentityStore is=IdentityStore.getIdentityStoreDemo();
        SimpleMessageFactory smf=new SimpleMessageFactory( "", 0,1, is.getAnonSet( 30 ).toArray( new IdentityStoreBlock[0] ),is);
        smf.build();
        System.out.println( "got "+smf.getGraph().getRoutes().length+" routes");
        final JGraph jg=new JGraph(smf.getGraph());
        SwingUtilities.invokeLater( new Runnable() {
            public void run() {
                jg.createAndShowGUI();
            }
        });
    }

    private void createAndShowGUI() {
        System.out.println("Created GUI on EDT? "+
                SwingUtilities.isEventDispatchThread());
        JFrame f = new JFrame("Graph Demo");
        f.add(this);
        f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        f.setSize(250,250);
        f.setVisible(true);
    }
    public void mouseMoved(MouseEvent e) {
        // saySomething("Mouse moved", e);
    }

    public void mouseDragged(MouseEvent e) {
        // saySomething("Mouse dragged", e);
    }
}