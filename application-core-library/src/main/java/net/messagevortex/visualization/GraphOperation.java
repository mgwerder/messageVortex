package net.messagevortex.visualization;

import net.messagevortex.asn1.OperationFactory;
import net.messagevortex.asn1.encryption.DumpType;
import org.bouncycastle.asn1.ASN1Sequence;
import org.bouncycastle.asn1.ASN1TaggedObject;
import sun.reflect.generics.tree.Tree;

import java.awt.*;
import java.io.IOException;
import java.util.TreeMap;

public class GraphOperation {
    enum OperationType {
        MERGE,
        SPLIT,
        ENCRYPT,
        DECRYPT,
        ADD_REDUNDANCY,
        REMOVE_REDUNDANCY,
        MAP,
    }

    private final ASN1TaggedObject operation;
    private OperationType type;
    private final TreeMap<Integer, GraphPayloadSlot> inputPayloads = new TreeMap<>();
    private final TreeMap<Integer, GraphPayloadSlot> outputPayloads = new TreeMap<>();
    private final TreeMap<Integer, GraphRoutings> inputRoutes = new TreeMap<>();
    private final TreeMap<Integer, GraphRoutings> outputRoutes = new TreeMap<>();
    private int verticalSlot = -1;
    private int horizontalSlot = -1;
    private int slotWidth;
    private int startX = 0;

    /**
     * <p>Creates and Parses a new Graph Operation object.</p>
     *
     * @param operation ASN1TaggedObject containing a single Operation.
     */
    public GraphOperation(ASN1TaggedObject operation) {
        this.operation = operation;
        parseOperation();
    }

    // Parses the Operation using the Tag of the ASN1TaggedObject
    private void parseOperation() {
        TreeMap<Integer, Integer> inputs = new TreeMap<>();
        TreeMap<Integer, Integer> outputs = new TreeMap<>();
        OperationType type = null;

        switch (operation.getTagNo()) {
            case 150:
                inputs.put(0, Integer.valueOf((ASN1Sequence.getInstance(operation.getBaseObject()).getObjectAt(0)).toString()));
                outputs.put(0, Integer.valueOf((ASN1Sequence.getInstance(operation.getBaseObject()).getObjectAt(1)).toString()));
                outputs.put(1, Integer.valueOf((ASN1Sequence.getInstance(operation.getBaseObject()).getObjectAt(3)).toString()));
                type = OperationType.SPLIT;
                break;
            case 160:
                inputs.put(0, Integer.valueOf((ASN1Sequence.getInstance(operation.getBaseObject()).getObjectAt(0)).toString()));
                inputs.put(1, Integer.valueOf((ASN1Sequence.getInstance(operation.getBaseObject()).getObjectAt(1)).toString()));
                outputs.put(0, Integer.valueOf((ASN1Sequence.getInstance(operation.getBaseObject()).getObjectAt(2).toString())));
                type = OperationType.MERGE;
                break;
            case 300:
                inputs.put(0, Integer.valueOf((ASN1Sequence.getInstance(operation.getBaseObject()).getObjectAt(0)).toString()));
                outputs.put(0, Integer.valueOf((ASN1Sequence.getInstance(operation.getBaseObject()).getObjectAt(2)).toString()));
                type = OperationType.ENCRYPT;
                break;
            case 310:
                inputs.put(0, Integer.valueOf((ASN1Sequence.getInstance(operation.getBaseObject()).getObjectAt(0)).toString()));
                outputs.put(0, Integer.valueOf((ASN1Sequence.getInstance(operation.getBaseObject()).getObjectAt(2)).toString()));
                type = OperationType.DECRYPT;
                break;
            case 400: {
                int totalLength = Integer.parseInt(((ASN1TaggedObject) (ASN1Sequence.getInstance(operation.getBaseObject()).getObjectAt(1))).getBaseObject().toString()) + Integer.parseInt(((ASN1TaggedObject) (ASN1Sequence.getInstance(operation.getObject()).getObjectAt(2))).getObject().toString());
                for (int j = 0; j < totalLength; j++) {
                    outputs.put(j, Integer.parseInt(((ASN1TaggedObject) (ASN1Sequence.getInstance(operation.getBaseObject()).getObjectAt(4))).getBaseObject().toString()) + j);
                }
                inputs.put(0, Integer.parseInt(((ASN1TaggedObject) (ASN1Sequence.getInstance(operation.getBaseObject()).getObjectAt(0))).getBaseObject().toString()));
                type = OperationType.ADD_REDUNDANCY;
                break;
            }
            case 410: {
                int totalLength = Integer.parseInt(((ASN1TaggedObject) (ASN1Sequence.getInstance(operation.getBaseObject()).getObjectAt(1))).getBaseObject().toString()) + Integer.parseInt(((ASN1TaggedObject) (ASN1Sequence.getInstance(operation.getObject()).getObjectAt(2))).getObject().toString());
                for (int j = 0; j < totalLength; j++) {
                    inputs.put(j, Integer.parseInt(((ASN1TaggedObject) (ASN1Sequence.getInstance(operation.getBaseObject()).getObjectAt(0))).getBaseObject().toString()) + j);
                }
                outputs.put(0, Integer.parseInt(((ASN1TaggedObject) (ASN1Sequence.getInstance(operation.getBaseObject()).getObjectAt(4))).getBaseObject().toString()));
                type = OperationType.REMOVE_REDUNDANCY;
                break;
            }
            case 1001:
                inputs.put(0, Integer.valueOf((ASN1Sequence.getInstance(operation.getBaseObject()).getObjectAt(0)).toString()));
                outputs.put(0, Integer.valueOf((ASN1Sequence.getInstance(operation.getBaseObject()).getObjectAt(1)).toString()));
                type = OperationType.MAP;
                break;
        }

        this.type = type;

        if(!inputs.isEmpty()) {
            inputs.forEach((key, value) -> inputPayloads.put(key, new GraphInputSlot(value)));
        }

        outputs.forEach((key, value) -> outputPayloads.put(key, new GraphOutputSlot(value)));
    }

    private String getOperationDump() throws IOException {
        return OperationFactory.getInstance(this.operation).dumpValueNotation("", DumpType.ALL);
    }

    /**
     * <p>Sets the vertical slot in the graph of the Operation.</p>
     *
     * @param verticalSlot An Integer indicating the vertical slot of the Operation in the graph.
     */
    public void setVerticalSlot(int verticalSlot) {
        this.verticalSlot = verticalSlot;
    }

    /**
     * <p>Sets the horizontal slot in the graph of the Operation and calculates the width of the slot given the Input- and Outputpayloads.</p>
     *
     * @param horizontalSlot An integer indicating the horizontal slot of the operation in the graph.
     */
    public void setHorizontalSlot(int horizontalSlot) {
        if(getPureInputPayloads().size() > getPureOutputPayloads().size()) this.slotWidth = (getPureInputPayloads().size() * 160 + 40);
        else this.slotWidth = (getPureOutputPayloads().size() * 160 + 40);

        this.horizontalSlot = horizontalSlot;
    }

    /**
     * <p>Sets the X-Coordinate at which the slot starts.</p>
     *
     * @param startX An integer indicating the X-Coordinate at which the horizontal slot starts.
     */
    public void setStartX(int startX) {
        this.startX = startX;
    }

    /**
     * <p>Overwrites the automatically calculated Width of the slot.</p>
     *
     * @param width An integer with the width of the slot.
     */
    public void setSlotWidth(int width) {
        this.slotWidth = width;
    }

    /**
     * <p>Adds a new Input route for one of the inputslots.</p>
     *
     * @param routing A Graphrouting object, which contains the coordinates from which the route will be drawn.
     */
    public void addInputRoute(GraphRoutings routing) {
        int key = 0;
        if(!inputRoutes.isEmpty()) key = inputRoutes.lastKey() + 1;
        this.inputRoutes.put(key, routing);
    }

    /**
     * <p>Adds a new Output route for one of the outputslots.</p>
     *
     * @param routing A Graphrouting object, which contains the coordinates from which the route will be drawn.
     */
    public void addOutputRoute(GraphRoutings routing) {
        int key = 0;
        if(!outputRoutes.isEmpty()) key = outputRoutes.lastKey() + 1;
        this.outputRoutes.put(key, routing);
    }

    /**
     * <p>Returns the Type of the Operation.</p>
     *
     * @return A OperationType.
     */
    public OperationType getType() {
        return this.type;
    }

    /**
     * <p>Returns the Operation.</p>
     *
     * @return An ASN1TaggedObject representing the Operation.
     */
    public ASN1TaggedObject getOperation() {
        return this.operation;
    }

    /**
     * <p>Returns the Input Payloadslots of this operation.</p>
     *
     * @return A Treemap with all input Payloadslots for this operation.
     */
    public TreeMap<Integer, GraphPayloadSlot> getInputPayloads() {
        return this.inputPayloads;
    }

    /**
     * <p>Returns all input payloadslots, which are not the output of another Operation.</p>
     *
     * @return A Treemap only containing the payloadslots which are not the output of another operation.
     */
    public TreeMap<Integer, GraphPayloadSlot> getPureInputPayloads() {
        TreeMap<Integer, GraphPayloadSlot> pureInputPayloads = new TreeMap<>();

        this.inputPayloads.forEach((key, value) -> {
            if(!value.isOutput()) pureInputPayloads.put(key, value);
        });

        return pureInputPayloads;
    }

    /**
     * <p>Returns the Output Payloadslots of this operation.</p>
     *
     * @return A Treemap with all the output Payloadslots for this operation.
     */
    public TreeMap<Integer, GraphPayloadSlot> getOutputPayloads() {
        return this.outputPayloads;
    }

    /**
     * <p>Returns all output payloadslots, which are not the input to another Operation.</p>
     *
     * @return A Treemap only containing the payloadslots which are not the output of another operation.
     */
    public TreeMap<Integer, GraphPayloadSlot> getPureOutputPayloads() {
        TreeMap<Integer, GraphPayloadSlot> pureOutputPayloads = new TreeMap<>();

        this.outputPayloads.forEach((key, value) -> {
            if(!value.isInput()) pureOutputPayloads.put(key, value);
        });

        return pureOutputPayloads;
    }

    /**
     * <p>Returns the vertical slot of the Operation in the graph.</p>
     *
     * @return An integer with the vertical slot of the Operation in the graph.
     */
    public int getVerticalSlot() {
        return this.verticalSlot;
    }

    /**
     * <p>Returns the horizontal slot of the operation.</p>
     *
     * @return An integer with the horizontal slot of the operation.
     */
    public int getHorizontalSlot() {
        return this.horizontalSlot;
    }

    /**
     * <p>Returns the width of the operation's horizontal slot.</p>
     *
     * @return An integer with the width of the horizontal slot in which the operation is.
     */
    public int getSlotWidth() {
        return this.slotWidth;
    }

    /**
     * <p>Returns the X-Coordinate at which the horizontal slot starts.</p>
     *
     * @return An integer with the X-Coordinate at which the horizontal slot starts.
     */
    public int getStartX() {
        return this.startX;
    }

    /**
     * <p>Returns the Inputroutes of the Operation.</p>
     *
     * @return A Treemap containing all Routes for the Input payloads.
     */
    public TreeMap<Integer, GraphRoutings> getInputRoutes() {
        return this.inputRoutes;
    }

    /**
     * <p>Returns the Outputroutes of the Operation.</p>
     *
     * @return A Treemap containing all Routes for the Output payloads.
     */
    public TreeMap<Integer, GraphRoutings> getOutputRoutes() {
        return this.outputRoutes;
    }

    public void drawOperation(Graphics2D g2d, FontMetrics fm, int inputSlotHeight) {
        int x = ((slotWidth / 2) + startX) - 50;
        int y = 200 + inputSlotHeight;

        if(type != GraphOperation.OperationType.MAP) {
            Polygon polygon = new Polygon();

            polygon.addPoint(x, y + 33);
            polygon.addPoint(x, y + 66);
            polygon.addPoint(x + 33, y + 100);
            polygon.addPoint(x + 66, y + 100);
            polygon.addPoint(x + 100, y + 66);
            polygon.addPoint(x + 100, y + 33);
            polygon.addPoint(x + 66, y);
            polygon.addPoint(x + 33, y);

            g2d.fill(polygon);
            int textWidth = fm.stringWidth(type.toString());
            int textHeight = fm.getHeight();
            g2d.setColor(Color.WHITE);
            g2d.drawString(type.toString(), x + (100 - textWidth) / 2, y + (100 + textHeight) / 2);
            g2d.setColor(new Color(23, 39, 66));
            try {
                WorkspaceGraph.graphedObjects.put(WorkspaceGraph.objectGraphIterator, new GraphObject(x, y, 100, 100, getOperationDump(), GraphObjectType.HEXAGON));
                WorkspaceGraph.objectGraphIterator++;
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
