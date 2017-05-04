package net.gwerder.java.messagevortex.routing.operation;

/**
 * This is a dummy operation mainly for testing it maps output to input ids.
 */
public class IdMapOperation extends AbstractOperation {

    private int[] outputId;
    private int[] inputId;

    public IdMapOperation(InternalPayload p, int sourceId, int targetId, int number) {
        super(p);
        inputId  = new int[number];
        outputId = new int[number];
        for(int i=0;i<inputId.length;i++) {
            inputId[i]=sourceId+i;
            outputId[i]=targetId+i;
        }
    }

    @Override
    public int[] getOutputID() {
        return outputId;
    }

    @Override
    public int[] getInputID() {
        return inputId;
    }

    @Override
    public boolean canRun() {
        return true;
    }

    @Override
    public int[] execute(int[] id) {
        for(int i=0;i<inputId.length;i++) {
            payload.setCalculatedPayload(outputId[i],payload.getPayload(inputId[i]));
        }
        return getOutputID();
    }
}
