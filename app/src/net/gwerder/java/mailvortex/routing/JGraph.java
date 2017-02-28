package net.gwerder.java.mailvortex.routing;

import net.gwerder.java.mailvortex.asn1.IdentityStore;
import net.gwerder.java.mailvortex.asn1.IdentityStoreBlock;
import org.bouncycastle.asn1.DEROutputStream;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Created by martin.gwerder on 13.06.2016.
 */
public class JGraph extends JPanel implements MouseListener  {

    private static final long serialVersionUID = 1213422324789789L;

    private int X_OFFSET     = 20;
    private int Y_OFFSET     = 10;
    private int ROUTE_BORDER = 10;
    private int BOX_HEIGHT   = 15;
    private int BOX_WIDTH    = 20;

    private int route   = 0;

    private GraphSet graph;

    public JGraph(GraphSet gs) {
        addMouseListener( this );
        this.graph=gs;
        update();
    }

    private void update() {
        GraphSet[] routes=graph.getRoutes();
        Dimension d=new Dimension(graph.getAnonymitySetSize()*(BOX_WIDTH+1)+2*X_OFFSET,2*Y_OFFSET+2*ROUTE_BORDER+routes.length*4);
        setMinimumSize( d );
    }

    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2=(Graphics2D)(g.create());
        Stroke s=g2.getStroke();
        Stroke s2=new BasicStroke(3);
        double ySpace=(0.0+getHeight()-2*Y_OFFSET-2*BOX_HEIGHT-ROUTE_BORDER)/(graph.size());
        double xSpace=(0.0+getWidth()-2*X_OFFSET-BOX_WIDTH)/(graph.getAnonymitySetSize()-1);

        // draw boxes
        for(int i=0;i<graph.getAnonymitySetSize();i++) {
            int x=(int)(X_OFFSET+i*xSpace);
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
            Graphics2D g3 = (Graphics2D) g.create();
            g3.setStroke(new BasicStroke(1, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL, 0, new float[]{9}, 0));
            g3.drawLine(x+BOX_WIDTH/2,Y_OFFSET+BOX_HEIGHT,x+BOX_WIDTH/2,getHeight()-Y_OFFSET-2*ROUTE_BORDER);
            g3.dispose();
        }

        // draw arrows
        GraphSet[] routes=graph.getRoutes();
        int lastY=0;
        System.out.println("## displaying route "+this.route+" ("+routes[this.route].size()+")");
        for(int i=0;i<graph.size();i++) {
            Edge gr=graph.get(i);
            int x1=(int)(X_OFFSET+BOX_WIDTH/2+graph.getAnonymityIndex( gr.getFrom() )*xSpace);
            int x2=(int)(X_OFFSET+BOX_WIDTH/2+graph.getAnonymityIndex( gr.getTo()   )*xSpace);
            int y=(int)(Y_OFFSET+2*BOX_HEIGHT+i*ySpace);

            if(routes[this.route].contains( gr )) {
                System.out.println("##   route "+this.route+" contains "+i+" ("+routes[this.route].size()+"/"+gr.getStartTime()+")");
                g2.setColor( Color.GREEN );
                g2.setStroke( s2 );
                if(lastY>0) g2.drawLine(x1,lastY,x1,y);
                lastY=y;
            }else {
                g2.setStroke( s );
                g2.setColor(Color.BLACK);
            }
            // draw arrow
            g2.drawLine(x1,y,x2,y);
            // draw arrowhead
            int xh=(int)((x2-x1)/Math.abs(x2-x1)*ySpace);
            g2.drawLine(x2,y,x2-xh,y-xh/4);
            g2.drawLine(x2,y,x2-xh,y+xh/4);
        }

        // draw route buttons
        xSpace=(getWidth()-2*X_OFFSET)/(routes.length*2-1);
        for(int i=0;i<routes.length;i++) {
            int x1=(int)((getWidth()-(routes.length*2-1)*xSpace)/2+i*xSpace*2);
            int y=getHeight()-Y_OFFSET-ROUTE_BORDER;
            if(this.route==i) {
                g.setColor(Color.BLUE);
                g.fillRect( x1,y,(int)xSpace,ROUTE_BORDER );
            }
            g.setColor(Color.BLACK);
            g.drawRect( x1,y,(int)xSpace,ROUTE_BORDER );
        }
    }

    public int setRoute(int r) {
        int s=graph.getRoutes().length;
        if(r<0 || s<=r) throw new NullPointerException("unable to find adressed route r (0<="+r+"<"+s+")");
        int old=this.route;
        this.route=r;
        if(old!=r) repaint();
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
        if(e!=null) {
            GraphSet[] routes = graph.getRoutes();
            double xSpace = (getWidth() - 2 * X_OFFSET) / (routes.length * 2 - 1);
            int offset = (int) (getWidth() - (routes.length * 2 - 1) * xSpace) / 2;
            int tmp = e.getX() - offset;
            int pos = (int) Math.min( Math.floor( (0.0 + tmp) / (xSpace * 2) ), routes.length );
            int y = getHeight() - Y_OFFSET - ROUTE_BORDER;
            tmp = tmp - (int) ((0.0 + pos) * 2 * xSpace);
            if (e.getY() <= y + ROUTE_BORDER && e.getY() >= y && tmp < xSpace) {
                setRoute( Math.min( routes.length, pos ) );
            }
        }
    }

    public static void main(String[] args) throws IOException{
        IdentityStore is=null;
        try {
            is = new IdentityStore( new File(System.getProperty( "java.io.tmpdir" ) + "/IdentityStoreExample1.der") );
        } catch( IOException ioe ){
            is = IdentityStore.getNewIdentityStoreDemo( false );
            DEROutputStream f = new DEROutputStream( new FileOutputStream( System.getProperty( "java.io.tmpdir" ) + "/IdentityStoreExample1.der" ) );
            f.writeObject( is.toASN1Object() );
            f.close();
        }
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
        JFrame f = new JFrame("Edge Demo");
        f.add(this);
        f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        f.setSize(250,250);
        f.setVisible(true);
    }

}
