package net.gwerder.java.messagevortex.routing.operation;

import net.gwerder.java.messagevortex.MessageVortexLogger;

import java.io.Serializable;
import java.util.logging.Level;

/**
 * This is a dummy operation mainly for testing it maps output to input ids.
 */
public class IdMapOperation extends AbstractOperation implements Serializable {

    public static final long serialVersionUID = 100000000019L;

    private static final java.util.logging.Logger LOGGER;
    static {
        LOGGER = MessageVortexLogger.getLogger((new Throwable()).getStackTrace()[0].getClassName());
        MessageVortexLogger.setGlobalLogLevel( Level.ALL);
    }

    private int[] outputId;
    private int[] inputId;

    public IdMapOperation(int sourceId, int targetId, int number) {
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
        LOGGER.log(Level.INFO,"running IDMapper "+inputId[0]+"/"+outputId[0]+"/"+inputId.length);
        for(int i=0;i<inputId.length;i++) {
            payload.setCalculatedPayload(outputId[i],payload.getPayload(inputId[i]));
        }
        return getOutputID();
    }

    @Override
    public String toString() {
        return inputId[0]+"->IdMapper("+inputId.length+")->"+outputId[0];
    }
}
