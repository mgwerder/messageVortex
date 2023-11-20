package net.messagevortex.visualization;

import net.messagevortex.asn1.RoutingCombo;
import net.messagevortex.asn1.encryption.DumpType;
import org.bouncycastle.asn1.*;

import java.awt.*;
import java.io.IOException;
import java.util.ArrayList;
import java.util.TreeMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

//ToDo Split up DrawComponent Method to reduce Complexity of Method
public class RBGraph extends ZoomPanel {
    private final TreeMap<Integer, Node> nodes = new TreeMap<>();
    private final TreeMap<String, Timeslot> timeslots;
    private final TreeMap<Integer, Routingblock> routingblocks = new TreeMap<>();
    private final int spacingNodes = 180;
    private final int spacingTimeSlots = 60;
    private static int mapIterator = 0;
    private static int graphedObjectsIterator = 0;

    /**
     * <p>Creates a new Routing Block Graph from the initial Routing Block</p>
     *
     * @param initialRB    The initial Routing Block that was selected
     */
    public RBGraph(RoutingCombo initialRB, Topbar topbar) throws IOException {
        getRoutingCombos(initialRB);
        mapIterator = 0;
        getNodes();
        mapIterator = 0;
        timeslots = getTimeslots();
        addMouseWheelListener(this);
        this.topbar = topbar;
        if((double) this.topbar.getZoomSliderValue() / 10 != zoomFactor) {
            setZoomFactor((double) this.topbar.getZoomSliderValue() / 10);
        }
    }

    // get all "children" Routingcombos of 1st Routingcombo
    private void getRoutingCombos(RoutingCombo initialCombo) throws IOException {
        ASN1TaggedObject seqTagged = ASN1TaggedObject.getInstance(ASN1Sequence.getInstance(initialCombo.toAsn1Object(DumpType.INTERNAL)).getObjectAt(4));

        String sender = ASN1Sequence.getInstance(ASN1Sequence.getInstance(ASN1Sequence.getInstance(initialCombo.toAsn1Object(DumpType.INTERNAL)).getObjectAt(0)).getObjectAt(0)).getObjectAt(1).toString();

        ASN1Sequence seq = ASN1Sequence.getInstance(seqTagged.getBaseObject());

        for(int i = 0; i < seq.size(); i++) {
            RoutingCombo rc =  new RoutingCombo(ASN1Sequence.getInstance(seq.getObjectAt(i)));

            if (getRecipient(rc).isEmpty()) {
                break;
            }

            Routingblock rb = new Routingblock(rc, sender);

            routingblocks.put(mapIterator, rb);
            mapIterator++;

            getRoutingCombos(rc);
        }
    }

    // get all nodes in the Routingcombos and add them to a list
    private void getNodes() throws IOException {
        AtomicInteger nodeId = new AtomicInteger();

        routingblocks.forEach((rbKey, rbValue) -> {
            Node node = new Node(rbValue.getSender(), nodeId.get());

            AtomicBoolean newNode = new AtomicBoolean(true);

            nodes.forEach((nodeKey, nodeValue) -> {
                if(nodeValue.getName().equals(node.getName())) {
                    newNode.set(false);
                }
            });

            if(newNode.get()) {
                nodes.put(mapIterator, node);
                mapIterator++;
                nodeId.getAndIncrement();
            }
        });

        routingblocks.forEach((rbKey, rbValue) -> {
            nodes.forEach((nodeKey, nodeValue) -> {
                if(nodeValue.getName().equals(rbValue.getSender())) {
                    rbValue.setSenderId(nodeValue.getId());
                }
                else {
                    try {
                        if(nodeValue.getName().equals(getRecipient(rbValue.getRoutingCombo()))) {
                            rbValue.setRecipientId(nodeValue.getId());
                        }
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }
            });
        });
    }

    private TreeMap<String, Timeslot> getTimeslots() throws IOException {
        TreeMap<String, Timeslot> slots = new TreeMap<>();

        routingblocks.forEach((key, value) -> {
            int startTime = 0;
            try {
                startTime = Integer.parseInt(ASN1Sequence.getInstance(value.getRoutingCombo().toAsn1Object(DumpType.INTERNAL)).getObjectAt(1).toString());
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            int endTime = 0;
            try {
                endTime = Integer.parseInt(ASN1Sequence.getInstance(value.getRoutingCombo().toAsn1Object(DumpType.INTERNAL)).getObjectAt(2).toString());
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

            slots.put(String.valueOf(startTime) + String.valueOf(endTime) + String.valueOf(mapIterator), new Timeslot(startTime, endTime, value));
        });

        AtomicInteger i = new AtomicInteger();
        slots.forEach((key, value) -> {
            value.setSlot(i.get());
            i.getAndIncrement();
        });

        return slots;
    }

    private String getRecipient(RoutingCombo c) throws IOException {
        ASN1Sequence seq = ASN1Sequence.getInstance(c.toAsn1Object(DumpType.INTERNAL));
        ASN1Sequence seq2 = ASN1Sequence.getInstance(seq.getObjectAt(0));
        ASN1Sequence seq3 = ASN1Sequence.getInstance(seq2.getObjectAt(0));
        return seq3.getObjectAt(1).toString();
    }

    // Create Polygon to draw the arrowheads
    private Polygon drawArrowHead(Point sender, Point recipient) {
        Polygon arrowHead = new Polygon();
        if(sender.x > recipient.x) {
            arrowHead.addPoint(recipient.x - 5, recipient.y);
        }
        else {
            arrowHead.addPoint(recipient.x + 5, recipient.y);
        }

        arrowHead.addPoint(recipient.x, recipient.y - 5);
        arrowHead.addPoint(recipient.x, recipient.y + 5);

        return arrowHead;
    }

    // Create Single Route Rhombus
    private Polygon drawRhombus(int sender, Timeslot s) {
        Polygon rhombus = new Polygon();

        rhombus.addPoint(225 + (spacingNodes * sender), 250 + (spacingTimeSlots * s.getSlot()));
        rhombus.addPoint(250 + (spacingNodes * sender), 275 + (spacingTimeSlots * s.getSlot()));
        rhombus.addPoint(275 + (spacingNodes * sender), 250 + (spacingTimeSlots * s.getSlot()));
        rhombus.addPoint(250 + (spacingNodes * sender), 225 + (spacingTimeSlots * s.getSlot()));

        return rhombus;
    }

    private void drawRhombuses(TreeMap<String, Timeslot> timeslots, Graphics2D g2d) {
        timeslots.forEach((key, value) -> {
            g2d.setColor(new Color(69, 93, 133));
            Polygon rhombus = drawRhombus(value.getRb().getSenderId(), value);
            graphedObjects.put(graphedObjectsIterator, new GraphObject(rhombus.xpoints, rhombus.ypoints, value.getRb().getRoutingCombo().dumpValueNotation("", DumpType.INTERNAL), GraphObjectType.RHOMBUS));
            graphedObjectsIterator++;

            g2d.fill(rhombus);

            g2d.setColor(Color.BLACK);
            drawRoutes(timeslots, rhombus, g2d);
        });
    }

    private void drawRoutes(TreeMap<String, Timeslot> timeslots, Polygon rhombuses, Graphics2D g2d) {
        timeslots.forEach((key, value) -> {
            int h = rhombuses.ypoints[1] - rhombuses.ypoints[3];
            int startPointGraph = 0;
            int endpointGraph = 0;
            Point p1 = new Point();
            Point p2 = new Point();

            if(h > 50) {
                int[] xPoints = rhombuses.xpoints;
                int[] yPoints = rhombuses.ypoints;
                int relativeX = 0;
                int relativeY = 0;

                for(int j = (h / 50); j > 0; j--) {
                    int sender = value.getRb().getSenderId();
                    int recipient = value.getRb().getRecipientId();
                    int height = 250 + (spacingTimeSlots * value.getSlot()) - yPoints[0];


                    if(sender < recipient) {
                        relativeX = xPoints[2] - xPoints[1];
                        relativeY = Math.abs(yPoints[3] - yPoints[0]);
                        startPointGraph = 250 + (spacingNodes * sender);
                        endpointGraph = 245 + (spacingNodes * recipient);
                    }
                    else if(sender > recipient) {
                        relativeX = xPoints[0] - xPoints[1];
                        relativeY = Math.abs(yPoints[1] - yPoints[0]);
                        startPointGraph = 250 + (spacingNodes * sender);
                        endpointGraph = 255 + (spacingNodes * recipient);
                    }

                    startPointGraph = (int) (relativeX - ((double) (relativeX * Math.abs(height)) / (double) relativeY)) + startPointGraph;

                    p1 = new Point(startPointGraph, 250 + (spacingTimeSlots * value.getSlot()));
                    p2 = new Point(endpointGraph, 250 + (spacingTimeSlots * value.getSlot()));
                }
            }
            else {
                int sender = value.getRb().getSenderId();
                int recipient = value.getRb().getRecipientId();

                if(sender < recipient) {
                    startPointGraph = 275;
                    endpointGraph = 245 + (spacingNodes * recipient);
                }
                else {
                    startPointGraph = 225;
                    endpointGraph = 255 + (spacingNodes * recipient);
                }

                startPointGraph = startPointGraph + (spacingNodes * sender);

                p1 = new Point(startPointGraph, 250 + (spacingTimeSlots * value.getSlot()));
                p2 = new Point(endpointGraph, 250 + (spacingTimeSlots * value.getSlot()));
            }

            g2d.draw(drawArrowHead(p1, p2));
            g2d.drawLine(p1.x, p1.y, p2.x, p2.y);
        });
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;

        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON);

        g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
                RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        FontMetrics fm = g2d.getFontMetrics();

        g2d.scale(zoomFactor, zoomFactor);
        g2d.setColor(Color.BLACK);

        for(int i = 0; i < nodes.size(); i++) {
            int xPos = (200 + (spacingNodes * i));

            //ToDo Datatype/Object for Nodes??
            //ToDo DumpValueNotation for infolist
            graphedObjects.put(graphedObjectsIterator, new GraphObject(xPos, 100, 100, 100, "Random Information", GraphObjectType.RECTANGLE));
            graphedObjectsIterator++;

            g2d.setColor(new Color(23, 39, 66));
            g2d.fillRect(xPos, 100, 100, 100);

            g2d.setColor(Color.WHITE);
            int textWidth = fm.stringWidth(nodes.get(i).getName());
            int textHeight = fm.getHeight();
            g2d.drawString(nodes.get(i).getName(), 200 + (spacingNodes * i) + (100 - textWidth) / 2, 100 + (100 + textHeight) / 2);

            g2d.setColor(Color.LIGHT_GRAY);
            g2d.drawLine(xPos + 50, 200, xPos + 50, 300 + spacingTimeSlots * timeslots.size());
            g2d.setColor(Color.BLACK);
        }

        drawRhombuses(timeslots, g2d);

        timeslots.forEach((key, value) -> {
            g2d.drawString(value.getTimeString(), 80, 250 + spacingTimeSlots * value.getSlot());
        });
    }

    @Override
    public Dimension getPreferredSize() {
        double width = (300 + (spacingNodes * nodes.size())) * zoomFactor;
        double height = (500 + (spacingTimeSlots * nodes.size())) * zoomFactor * 2;

        return new Dimension((int) width, (int) height);
    }
}
