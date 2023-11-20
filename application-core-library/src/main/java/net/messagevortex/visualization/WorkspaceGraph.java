package net.messagevortex.visualization;

import org.bouncycastle.asn1.ASN1Sequence;
import org.bouncycastle.asn1.ASN1TaggedObject;
import org.bouncycastle.asn1.DERTaggedObject;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.util.ArrayList;
import java.util.TreeMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

public class WorkspaceGraph extends ZoomPanel {
    private int verticalSlots = 0;
    private final TreeMap<Integer, Integer> inputSlotHeight = new TreeMap<>();
    private final TreeMap<Integer, GraphOperation> operations = new TreeMap<>();
    protected static int objectGraphIterator = 0;
    private final int xSpacing = 15;

    /**
     * <p>Creates a new Workspace graph.</p>
     *
     * @param operations A Treemap containing all Operations as ASN1TaggedObjects
     * @param topbar The topbar element in the UI.
     */
    public WorkspaceGraph(TreeMap<Integer, ASN1TaggedObject> operations, Topbar topbar) {
        createOperations(operations);
        this.topbar = topbar;
        addMouseWheelListener(this);
        if ((double) this.topbar.getZoomSliderValue() / 10 != zoomFactor) {
            setZoomFactor((double) this.topbar.getZoomSliderValue() / 10);
        }

        operationHorizontal();
        operationVertical();
        calculateHorizontalSlots();
        calculateInputSlots();
        calculateOutputSlots();
        calculateInputSlotHeight();
        calculateInputRoutes();
        calculateOutputRoutes();
    }

    private void createOperations(TreeMap<Integer, ASN1TaggedObject> operations) {
        operations.forEach((key, value) -> {
            ASN1Sequence seq = ASN1Sequence.getInstance(value.getBaseObject());
            for (int i = 0; i < seq.size(); i++) {
                ASN1TaggedObject seq1;

                if (i == 0) {
                    seq1 = new DERTaggedObject(true, 160, seq.getObjectAt(i));
                } else if (i == 1) {
                    seq1 = new DERTaggedObject(true, 150, seq.getObjectAt(i));
                } else if (i == 2) {
                    seq1 = new DERTaggedObject(true, 1001, seq.getObjectAt(i));
                } else if (i == 3) {
                    seq1 = (ASN1TaggedObject) seq.getObjectAt(i);
                } else if (i == 4) {
                    seq1 = new DERTaggedObject(true, 410, seq.getObjectAt(i));
                } else if (i == 5) {
                    seq1 = new DERTaggedObject(true, 300, ASN1Sequence.getInstance(((ASN1TaggedObject) (seq.getObjectAt(i))).getBaseObject()));
                } else if (i == 6) {
                    seq1 = new DERTaggedObject(true, 310, ASN1Sequence.getInstance(((ASN1TaggedObject) (seq.getObjectAt(i))).getBaseObject()));
                } else if (i == 7) {
                    seq1 = new DERTaggedObject(true, 1001, seq.getObjectAt(i));
                } else if (i == 8) {
                    seq1 = new DERTaggedObject(true, 300, ASN1Sequence.getInstance(((ASN1TaggedObject) (seq.getObjectAt(i))).getBaseObject()));
                } else if (i == 9) {
                    seq1 = (ASN1TaggedObject) seq.getObjectAt(i);
                } else if (i == 10) {
                    seq1 = new DERTaggedObject(true, 160, seq.getObjectAt(i));
                } else if (i == 11) {
                    seq1 = new DERTaggedObject(true, 410, seq.getObjectAt(i));
                } else if (i == 12) {
                    seq1 = (ASN1TaggedObject) seq.getObjectAt(i);
                } else {
                    throw new RuntimeException("Could not parse Operation Tag");
                }

                this.operations.put(i, new GraphOperation(seq1));
            }
        });
    }

    private void operationHorizontal() {
        operations.forEach((key, value) -> value.setHorizontalSlot(key));
    }

    private void operationVertical() {
        TreeMap<Integer, Integer[]> outputSlots = new TreeMap<>();
        AtomicInteger iterator = new AtomicInteger(0);
        operations.forEach((key, value) -> {
            AtomicInteger occurrences = new AtomicInteger(0);
            value.getInputPayloads().forEach((iKey, iValue) -> outputSlots.forEach((oKey, oValue) -> {
                if (oValue[0] == iValue.getSlotId()) {
                    occurrences.getAndIncrement();
                    iValue.setIsOutput(true);
                    if (operations.get(oValue[2]).getOutputPayloads().size() == 1) {
                        value.setHorizontalSlot(operations.get(oValue[2]).getHorizontalSlot());
                    }
                    operations.get(oValue[2]).getOutputPayloads().get(oValue[1]).setIsInput(true);

                    if (operations.get(oValue[2]).getVerticalSlot() > value.getVerticalSlot()) {
                        value.setVerticalSlot(operations.get(oValue[2]).getVerticalSlot() + 1);
                    }
                    if (operations.get(oValue[2]).getVerticalSlot() > occurrences.get())
                        occurrences.set(operations.get(oValue[2]).getVerticalSlot() + 1);

                    if (occurrences.get() >= verticalSlots) verticalSlots = occurrences.get() + 1;
                }
            }));

            value.getOutputPayloads().forEach((oKey, oValue) -> {
                outputSlots.put(iterator.get(), new Integer[]{oValue.getSlotId(), oKey, key});
                iterator.getAndIncrement();
            });

            if (value.getVerticalSlot() == -1) {
                value.setVerticalSlot(0);
            }
        });
    }

    private void calculateHorizontalSlots() {
        AtomicInteger width = new AtomicInteger(0);
        operations.forEach((key, value) -> {
            operations.forEach((opKey, opValue) -> {
                if (value.getHorizontalSlot() == opValue.getHorizontalSlot() && value.getVerticalSlot() != 0 && value.getVerticalSlot() > opValue.getVerticalSlot()) {
                    value.setStartX(opValue.getStartX());
                    int opValueInitialWidth = opValue.getSlotWidth();
                    opValue.setSlotWidth(Math.max(opValueInitialWidth, value.getSlotWidth()));
                    value.setSlotWidth(Math.max(opValueInitialWidth, value.getSlotWidth()));
                }
            });

            if (value.getStartX() == 0) {
                value.setStartX(100 + width.get());
                width.set(value.getSlotWidth() + width.get());
            }
        });
    }

    private void calculateInputSlots() {
        operations.forEach((key, value) -> {
            AtomicInteger iterator = new AtomicInteger(0);
            value.getInputPayloads().forEach((iKey, iValue) -> {
                iValue.setHorizontalSpacing(iterator.get());
                iterator.getAndIncrement();
            });
        });
    }

    private void calculateOutputSlots() {
        operations.forEach((key, value) -> {
            AtomicInteger iterator = new AtomicInteger(0);
            value.getOutputPayloads().forEach((oKey, oValue) -> {
                oValue.setHorizontalSpacing(iterator.get());
                iterator.getAndIncrement();
            });
        });
    }

    private void calculateInputSlotHeight() {
        AtomicInteger RoutingLayers = new AtomicInteger();
        AtomicInteger verticalLayer = new AtomicInteger();

        operations.forEach((key, value) -> {
            if (value.getVerticalSlot() == verticalLayer.get()) {
                if (value.getInputPayloads().size() / 2 > RoutingLayers.get())
                    RoutingLayers.set(value.getInputPayloads().size() / 2);
            }
        });
        inputSlotHeight.put(0, 80 + 10 * RoutingLayers.get());
        RoutingLayers.set(0);

        for (int i = 1; i < verticalSlots; i++) {
            int finalI = i;
            operations.forEach((key, value) -> {
                if (value.getVerticalSlot() == finalI) {
                    value.getInputPayloads().forEach((iKey, iValue) -> {
                        if (iValue.isOutput()) {
                            RoutingLayers.getAndIncrement();
                        }
                    });
                }
            });

            inputSlotHeight.put(i, (190 + xSpacing * RoutingLayers.get()) + inputSlotHeight.lastEntry().getValue());
            RoutingLayers.set(0);
        }

        operations.forEach((key, value) -> {
            if (value.getVerticalSlot() == verticalLayer.get()) {
                if (value.getInputPayloads().size() / 2 > RoutingLayers.get())
                    RoutingLayers.set(value.getInputPayloads().size() / 2);
            }
        });
        inputSlotHeight.put(verticalSlots, (90 + xSpacing * RoutingLayers.get()) + inputSlotHeight.lastEntry().getValue());
    }

    private int calculateOutputSlotNumber() {
        AtomicInteger outputPayloads = new AtomicInteger(0);

        operations.forEach((key, value) -> {
            outputPayloads.set(Math.max(outputPayloads.get(), value.getPureOutputPayloads().size()));
        });

        return (outputPayloads.get() / 2);
    }

    private void calculateInputRoutes() {
        operations.forEach((key, value) -> {
            final int xStart = value.getSlotWidth() / 2 + value.getStartX();
            final int inputLayerSize = value.getPureInputPayloads().size() / 2;
            final int routingLayerWidth = ((value.getInputPayloads().size() + 1) * xSpacing) / 2;
            AtomicInteger currentLayer = new AtomicInteger(inputLayerSize);
            AtomicBoolean layerIncrease = new AtomicBoolean(false);

            value.getInputPayloads().forEach((iKey, iValue) -> {
                AtomicInteger iterator = new AtomicInteger(0);
                TreeMap<Integer, Point> points = new TreeMap<>();
                if (value.getType() == GraphOperation.OperationType.MAP) return;

                points.put(iterator.get(), new Point(xStart, 200 + inputSlotHeight.get(value.getVerticalSlot())));
                iterator.getAndIncrement();

                points.put(iterator.get(), new Point(xStart - routingLayerWidth + xSpacing * (iValue.getHorizontalSpacing() + 1), 180 + inputSlotHeight.get(value.getVerticalSlot())));
                iterator.getAndIncrement();

                if(!iValue.isOutput()) {
                    int x = (value.getSlotWidth() - (value.getPureInputPayloads().size() * 160 + 40)) / 2 + 100 + (value.getStartX() + (iValue.getHorizontalSpacing() * 160));

                    points.put(iterator.get(), new Point(xStart - routingLayerWidth + xSpacing * (iValue.getHorizontalSpacing() + 1), 220 + currentLayer.get() * 10));
                    iterator.getAndIncrement();

                    points.put(iterator.get(), new Point(x, 220 + currentLayer.get() * 10));
                    iterator.getAndIncrement();

                    points.put(iterator.get(), new Point(x, 200));

                    if (layerIncrease.get()) {
                        currentLayer.getAndIncrement();
                    } else if (inputLayerSize == 1) {
                        currentLayer.set(1);
                    } else if (currentLayer.get() == 1) {
                        layerIncrease.set(true);
                    } else currentLayer.getAndDecrement();
                } else {
                    int xStartOutputs = -1;
                    int targetY = -1;
                    int outputHorizontalSlot = -1;
                    int targetRoutingLayerSlotWidth = -1;
                    for (int i = 0; i < operations.size(); i++) {
                        for (int j = 0; j < operations.get(i).getOutputPayloads().size(); j++) {
                            if (iValue.getSlotId() == operations.get(i).getOutputPayloads().get(j).getSlotId()) {
                                xStartOutputs = operations.get(i).getSlotWidth() / 2 + operations.get(i).getStartX();
                                outputHorizontalSlot = operations.get(i).getOutputPayloads().get(j).getHorizontalSpacing() + 1;
                                targetY = 300 + inputSlotHeight.get(operations.get(i).getVerticalSlot());
                                targetRoutingLayerSlotWidth = ((operations.get(i).getOutputPayloads().size() + 1) * xSpacing) / 2;
                                break;
                            }
                        }
                        if (xStartOutputs != -1) break;
                    }

                    points.put(iterator.get(), new Point(xStart - routingLayerWidth + xSpacing * (iValue.getHorizontalSpacing() + 1), 175 + inputSlotHeight.get(value.getVerticalSlot()) - outputHorizontalSlot * 10));
                    iterator.getAndIncrement();

                    points.put(iterator.get(), new Point(xStartOutputs - targetRoutingLayerSlotWidth + xSpacing * outputHorizontalSlot, 175 + inputSlotHeight.get(value.getVerticalSlot()) - outputHorizontalSlot * 10));
                    iterator.getAndIncrement();

                    points.put(iterator.get(), new Point(xStartOutputs - targetRoutingLayerSlotWidth + xSpacing * outputHorizontalSlot, targetY + 30));
                    iterator.getAndIncrement();

                    points.put(iterator.get(), new Point(xStartOutputs, targetY));
                }

                iterator.getAndIncrement();
                value.addInputRoute(new GraphRoutings(points, iValue.getSlotId()));
            });
        });
    }

    private void calculateOutputRoutes() {
        final int layers = calculateOutputSlotNumber();
        int y = 390 + inputSlotHeight.lastEntry().getValue() + layers * 10;

        operations.forEach((key, value) -> {
            AtomicInteger currentLayer = new AtomicInteger(value.getPureOutputPayloads().size() / 2);
            AtomicBoolean layerIncrease = new AtomicBoolean(false);
            final int routingLayerWidth = ((value.getOutputPayloads().size() + 1) * xSpacing) / 2;

            for(int i = 0; i < value.getPureOutputPayloads().size(); i++ ) {
                GraphPayloadSlot slot = (GraphPayloadSlot) value.getPureOutputPayloads().values().toArray()[i];
                AtomicInteger iterator = new AtomicInteger(0);
                TreeMap<Integer, Point> points = new TreeMap<>();
                int xStart = (value.getSlotWidth() - (value.getPureOutputPayloads().size() * 160 + 40)) / 2;
                int x = xStart + 100 + (value.getStartX() + (i * 160));

                points.put(iterator.get(), new Point(x, y));
                iterator.getAndIncrement();

                points.put(iterator.get(), new Point(x, y - 30 - currentLayer.get() * 10));
                iterator.getAndIncrement();

                points.put(iterator.get(), new Point(value.getSlotWidth() / 2 + value.getStartX() - routingLayerWidth + xSpacing * (slot.getHorizontalSpacing() + 1), y - 30 - currentLayer.get() * 10));
                iterator.getAndIncrement();

                points.put(iterator.get(), new Point(value.getSlotWidth() / 2 + value.getStartX() - routingLayerWidth + xSpacing * (slot.getHorizontalSpacing() + 1), 330 + inputSlotHeight.get(value.getVerticalSlot())));
                iterator.getAndIncrement();

                if(value.getType() == GraphOperation.OperationType.MAP) {
                    int slotHeight = 0;
                    if(value.getVerticalSlot() != 0) slotHeight = inputSlotHeight.get(value.getVerticalSlot() - 1);
                    else slotHeight = -100;

                    points.put(iterator.get(), new Point(value.getSlotWidth() / 2 + value.getStartX() - routingLayerWidth + xSpacing * (slot.getHorizontalSpacing() + 1), 300 + slotHeight));
                } else {
                    points.put(iterator.get(), new Point(value.getSlotWidth() / 2 + value.getStartX(), 300 + inputSlotHeight.get(value.getVerticalSlot())));
                }

                if (layerIncrease.get()) {
                    currentLayer.getAndIncrement();
                } else if (value.getPureOutputPayloads().size() == 1) {
                    currentLayer.set(1);
                } else if (currentLayer.get() == 1) {
                    layerIncrease.set(true);
                } else currentLayer.getAndDecrement();

                value.addOutputRoute(new GraphRoutings(points, slot.getSlotId()));
            }
        });
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        Graphics2D g2d = (Graphics2D) g;
        AtomicInteger layers = new AtomicInteger();

        operations.forEach((key, value) -> {
            if (value.getPureOutputPayloads().size() / 2 > layers.get()) {
                layers.set(value.getPureOutputPayloads().size() / 2);
            }
        });

        int y = 390 + inputSlotHeight.lastEntry().getValue() + layers.get() * 10;

        g2d.scale(zoomFactor, zoomFactor);

        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON);

        g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
                RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        FontMetrics fm = g2d.getFontMetrics();

        operations.forEach((key, value) -> {
            //Draw Operations
            g2d.setColor(new Color(23, 39, 66));
            value.drawOperation(g2d, fm, inputSlotHeight.get(value.getVerticalSlot()));

            //Draw Payload slots
            g2d.setColor(new Color(69, 93, 133));
            for(int i = 0; i < value.getPureInputPayloads().size(); i++) {
                GraphPayloadSlot slot = (GraphPayloadSlot) value.getPureInputPayloads().values().toArray()[i];
                slot.drawSlot(g2d, fm, value.getSlotWidth(), value.getStartX(), value.getPureInputPayloads().size(), i);
            }
            g2d.setColor(new Color(135, 45, 45));
            for(int i = 0; i < value.getPureOutputPayloads().size(); i++) {
                GraphPayloadSlot slot = (GraphPayloadSlot) value.getPureOutputPayloads().values().toArray()[i];
                slot.drawSlot(g2d, fm, value.getSlotWidth(), value.getStartX(), value.getPureOutputPayloads().size(), y, i);
            }

            //Draw Routes
            g2d.setColor(Color.BLACK);
            value.getInputRoutes().forEach((irKey, irValue) -> irValue.drawRoute(g2d));
            value.getOutputRoutes().forEach((orKey, orValue) -> orValue.drawRoute(g2d));
        });
    }

    @Override
    public Dimension getPreferredSize() {
        final double[] width = {0};
        double height = (800 + inputSlotHeight.lastEntry().getValue()) * zoomFactor;

        operations.forEach((key, value) -> width[0] = Math.max(width[0], (value.getStartX() + value.getSlotWidth())));
        width[0] *= zoomFactor;

        return new Dimension((int) width[0], (int) height);
    }

    @Override
    public void setZoomFactor(double zoomFactor) {
        this.zoomFactor = zoomFactor;
        if (this.zoomFactor > 5) this.zoomFactor = 5;
        if (this.zoomFactor < 0.2) this.zoomFactor = 0.2;

        this.revalidate();
        this.repaint();
    }

    @Override
    public void mouseMoved(MouseEvent e) {
        AtomicReference<GraphRoutings> r = new AtomicReference<>();
        AtomicBoolean found = new AtomicBoolean(false);
        operations.forEach((key, value) -> {
            value.getInputRoutes().forEach((iKey, iValue) -> {
                if (iValue.checkMouseOver(e.getPoint(), this.zoomFactor)) {
                    r.set(iValue);
                    found.set(true);
                }
            });
            value.getOutputRoutes().forEach((oKey, oValue) -> {
                if (oValue.checkMouseOver(e.getPoint(), this.zoomFactor)) {
                    r.set(oValue);
                    found.set(true);
                }
            });
        });
        if (found.get()) {
            r.get().highlightRoute(Color.RED, 3);
            ToolTipManager.sharedInstance().setEnabled(true);
            this.setToolTipText("Payloadslot ID: " + r.get().getSlotID());
        } else {
            ToolTipManager.sharedInstance().setEnabled(false);
        }
        this.repaint();
    }
}
