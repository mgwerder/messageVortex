package net.gwerder.java.messagevortex.routing.operation;
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

import net.gwerder.java.messagevortex.MessageVortexLogger;
import net.gwerder.java.messagevortex.asn1.*;

import java.io.IOException;
import java.io.Serializable;
import java.util.logging.Level;
/**
 * This is the core of the redundancy add operation.
 *
 * It builds redundant data blocksfrom the existing data blocks.
 */
public class AddRedundancy extends AbstractOperation implements Serializable {

    public static final long serialVersionUID = 100000000018L;

    private static final java.util.logging.Logger LOGGER;
    static {
        LOGGER = MessageVortexLogger.getLogger((new Throwable()).getStackTrace()[0].getClassName());
        MessageVortexLogger.setGlobalLogLevel( Level.ALL);
    }

    AbstractRedundancyOperation operation;

    public AddRedundancy(AddRedundancyOperation op ) {
        this.operation=op;
    }

    @Override
    public boolean canRun() {
        return payload.getPayload(operation.getInputId())!=null;
    }

    @Override
    public int[] getOutputID() {
        int[] ret=new int[operation.getDataStripes()+operation.getRedundancy()];
        int id=operation.getOutputId();
        for(int i=0;i<ret.length;i++) {
            ret[i]=id+i;
        }
        return ret;
    }

    @Override
    public int[] getInputID() {
        int[] ret=new int[1];
        for(int i=0;i<ret.length;i++) {
            ret[i]=operation.getInputId()+i;
        }
        return ret;
    }

    public int[] execute(int[] id) {
        if(!canRun()) {
            return new int[0];
        }
        LOGGER.log(Level.INFO, "executing add redundancy operation ("+toString()+")");
        byte[] in=payload.getPayload(operation.getInputId()).getPayload();

        // do the padding
        int paddingSize=4;
        int size=in.length+paddingSize;
        int keySize=operation.getkeys()[1].getKeySize()/8;
        if(size%(keySize*operation.getDataStripes())>0) {
            size=keySize*operation.getDataStripes()*(size/(keySize*operation.getDataStripes())+1);
        }
        byte[] in2=new byte[size];
        byte[] pad=VortexMessage.getLongAsBytes(in.length,paddingSize);
        LOGGER.log(Level.INFO, "  calculated padded size (original: "+in.length+"; blocks: "+operation.getDataStripes()+"; block size: "+keySize+"; padded size: "+size+")");

        // copy length prefix
        System.arraycopy(pad,0,in2,0,paddingSize);

        // copy data
        System.arraycopy(in,0,in2,paddingSize,in.length);

        // repeat length prefix
        for(int i=0;i<in2.length-in.length-paddingSize;i++) {
            in2[i+paddingSize+in.length]=pad[i%pad.length];
        }

        // do the redundancy calc
        MathMode mm=GaloisFieldMathMode.getGaloisFieldMathMode(operation.getGFSize());
        LOGGER.log(Level.INFO, "  preparing data matrix");
        Matrix data=new Matrix(in2.length/operation.getDataStripes(),operation.getDataStripes(),mm,in2);
        LOGGER.log(Level.INFO, "  data matrix is "+data.getX()+"x"+data.getY());
        LOGGER.log(Level.INFO, "  preparing redundancy matrix");
        RedundancyMatrix r=new RedundancyMatrix(operation.getDataStripes(),operation.getDataStripes()+operation.getRedundancy(),mm);
        LOGGER.log(Level.INFO, "  redundancy matrix is "+r.getX()+"x"+r.getY());
        LOGGER.log(Level.INFO, "  calculating");
        Matrix out=r.mul(data);

        // set output
        LOGGER.log(Level.INFO, "  setting output chunks");
        int tot=0;
        assert out.getY()==(operation.getRedundancy()+operation.getDataStripes());
        try {
            for (int i = 0; i < out.getY(); i++) {
                int plid = operation.getOutputId() + i;
                byte[] b=operation.getkeys()[i].encrypt(out.getRowAsByteArray(i));
                payload.setCalculatedPayload(plid, new PayloadChunk( plid, b, payload.getPayload(plid).getUsagePeriod() ));
                tot+=b.length;
            }
        } catch(IOException ioe) {
            for(int i: getOutputID()) {
                payload.setCalculatedPayload(i,null);
            }
            LOGGER.log(Level.INFO, "  failed");
            return new int[0];
        }
        LOGGER.log(Level.INFO, "  done (chunk size: "+out.getRowAsByteArray(0).length+"; total:"+tot+")");

        return getOutputID();
    }

    public String toString() {
        return getInputID()[0]+"->addRedundancy("+getOutputID().length+")->"+getOutputID()[0];
    }

}
