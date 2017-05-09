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
import net.gwerder.java.messagevortex.asn1.AddRedundancyOperation;
import net.gwerder.java.messagevortex.asn1.IdentityBlock;
import net.gwerder.java.messagevortex.asn1.PayloadChunk;
import net.gwerder.java.messagevortex.asn1.SymmetricKey;
import net.gwerder.java.messagevortex.routing.operation.AddRedundancy;
import net.gwerder.java.messagevortex.routing.operation.InternalPayload;
import net.gwerder.java.messagevortex.routing.operation.InternalPayloadSpace;
import net.gwerder.java.messagevortex.routing.operation.Operation;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.logging.Level;

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
            int repeat=20;
            ExtendedSecureRandom esr=new ExtendedSecureRandom();
            for(int gfSize:new int[] {8,16}) {
                for (int i = 0; i < repeat; i++) {
                    int stripesRange=gfSize==8?220:500;
                    int dataStripes = (int) (Math.random() * stripesRange)+1;
                    int redundancy = (int) (Math.random() * (stripesRange*1.1 - dataStripes))+1;
                    SymmetricKey[] keys = new SymmetricKey[dataStripes + redundancy];
                    LOGGER.log(Level.INFO,"fuzzing with data:"+dataStripes+"/redundancy:"+redundancy+"/GF("+gfSize+") ["+(i+(gfSize>8?repeat:0)+1)+"/"+(repeat*2)+"]");
                    for (int j = 0; j < keys.length; j++) keys[j] = new SymmetricKey();
                    Operation op = new AddRedundancy(p, new AddRedundancyOperation(1, dataStripes, redundancy, Arrays.asList(keys), 10,gfSize));
                    p.addOperation(op);
                    byte[] inBuffer=new byte[dataStripes*10+(int)(Math.random()*(dataStripes*1000))];
                    esr.nextBytes(inBuffer);
                    p.setPayload(new PayloadChunk(1,inBuffer));
                }
                // FIXME this test is highly incomplete
            }
        }catch(IOException|NoSuchAlgorithmException ioe) {
            ioe.printStackTrace();
            fail("Exception while testing redundancy operation");
        }
    }

}
