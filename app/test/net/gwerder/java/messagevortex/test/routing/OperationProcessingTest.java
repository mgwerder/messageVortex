package net.gwerder.java.messagevortex.test.routing;
/***
 * Copyright (c) 2018 Martin Gwerder (martin@gwerder.net)
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 ***/

import net.gwerder.java.messagevortex.ExtendedSecureRandom;
import net.gwerder.java.messagevortex.MessageVortexLogger;
import net.gwerder.java.messagevortex.asn1.*;
import net.gwerder.java.messagevortex.routing.operation.*;
import net.gwerder.java.messagevortex.routing.operation.Operation;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

@RunWith(JUnit4.class)
public class OperationProcessingTest {

    private static final java.util.logging.Logger LOGGER;
    static {
            LOGGER = MessageVortexLogger.getLogger((new Throwable()).getStackTrace()[0].getClassName());
            MessageVortexLogger.setGlobalLogLevel( Level.ALL);
    }

    @Test
    public void redundancyOperationTest()  {
        try {
            IdentityBlock identity = new IdentityBlock();
            InternalPayloadSpace ps=new InternalPayloadSpace();
            InternalPayload p=ps.getInternalPayload(identity);
            redundancyOperationTest(p,3,2,8);
            int repeat=20;
            for(int gfSize:new int[] {8,16}) {
                for (int i = 0; i < repeat; i++) {
                    // determine data to be processed
                    int stripesRange=gfSize==8?220:500;
                    int dataStripes = (int) (Math.random() * stripesRange)+1;
                    int redundancy = (int) (Math.random() * (stripesRange*1.1 - dataStripes))+1;

                    LOGGER.log(Level.INFO,"running fuzzer "+((gfSize/8-1)*repeat+i)+"/"+(repeat*2)+"");
                    redundancyOperationTest(p,dataStripes,redundancy,gfSize);

                }
            }
        }catch(IOException|NoSuchAlgorithmException ioe) {
            ioe.printStackTrace();
            fail("Exception while testing redundancy operation");
        }
    }

    private void redundancyOperationTest(InternalPayload p,int dataStripes,int redundancy,int gfSize) throws IOException,NoSuchAlgorithmException {
        ExtendedSecureRandom esr=new ExtendedSecureRandom();
        // create symmetric keys for stripes
        SymmetricKey[] keys = new SymmetricKey[dataStripes + redundancy];
        for (int j = 0; j < keys.length; j++) keys[j] = new SymmetricKey();

        // create random data
        byte[] inBuffer=new byte[dataStripes*10+(int)(Math.random()*(dataStripes*10))];
        esr.nextBytes(inBuffer);

        // do the test
        LOGGER.log(Level.INFO,"  fuzzing with dataStipes:"+dataStripes+"/redundancyStripes:"+redundancy+"/GF("+gfSize+")/dataSize:"+inBuffer.length+"");
        Operation iop = new AddRedundancy(new AddRedundancyOperation(1, dataStripes, redundancy, Arrays.asList(keys), 1000,gfSize));
        assertTrue("add operation not added",p.addOperation(iop));
        assertTrue("payload not added",p.setPayload(new PayloadChunk(1,inBuffer))==null);

        // straight operation
        Operation oop=new RemoveRedundancy(new RemoveRedundancyOperation(1000, dataStripes, redundancy, Arrays.asList(keys), 2000,gfSize));
        assertTrue("remove operation not added",p.addOperation(oop));
        byte[] b=p.getPayload(2000).getPayload();
        assertTrue("error testing straight redundancy calculation",b!=null && Arrays.equals(inBuffer,b));

        // redundancy operation
        Operation oop2=new RemoveRedundancy(new RemoveRedundancyOperation(3000, dataStripes, redundancy, Arrays.asList(keys), 4000,gfSize));
        assertTrue("add operation for rebuild test not added",p.addOperation(oop2));
        // set random passthrus
        List<Operation> l=new ArrayList<>();
        while(l.size()<iop.getOutputID().length) {
            int i=esr.nextInt(dataStripes+redundancy);
            Operation o=new IdMapOperation(1000+i,3000+i,1);
            if(p.addOperation(o)) {
                l.add(o);
            }
        }
        assertTrue("error in determining canRun",oop2.canRun());
        assertTrue("error testing redundancy calculation with random stripes",Arrays.equals(inBuffer,p.getPayload(4000).getPayload()));
        assertTrue("remove operation for rebuild test not added",p.removeOperation(oop2));
        for(Operation o:l){
            assertTrue("error removing passthru operation",p.removeOperation(o));
        }

        assertTrue("unable to successfully remove add operation",p.removeOperation(iop));
        assertTrue("unable to successfully remove remove redundancy operation",p.removeOperation(oop));
        assertTrue("unable remove payload data",p.setPayload(new PayloadChunk(1,null))!=null);

    }

}