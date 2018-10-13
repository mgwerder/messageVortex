package net.messagevortex.test.routing;
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

import net.messagevortex.MessageVortexLogger;
import net.messagevortex.asn1.IdentityBlock;
import net.messagevortex.asn1.PayloadChunk;
import net.messagevortex.routing.operation.IdMapOperation;
import net.messagevortex.routing.operation.InternalPayload;
import net.messagevortex.routing.operation.InternalPayloadSpace;
import net.messagevortex.routing.operation.Operation;
import net.messagevortex.transport.RandomString;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.logging.Level;

import static junit.framework.TestCase.assertTrue;
import static org.junit.Assert.fail;

@RunWith(JUnit4.class)
public class InternalPayloadTest {

    private static final java.util.logging.Logger LOGGER;
    static {
            LOGGER = MessageVortexLogger.getLogger((new Throwable()).getStackTrace()[0].getClassName());
            MessageVortexLogger.setGlobalLogLevel( Level.ALL);
    }

    IdentityBlock[] identity;
    InternalPayloadSpace[] space;

    @Before
    public void setup() {
        try {
            identity = new IdentityBlock[]{new IdentityBlock(), new IdentityBlock(), new IdentityBlock(), new IdentityBlock()};
        } catch(IOException ioe) {
            LOGGER.log(Level.SEVERE,"IOException while creating identities",ioe);
            fail("failure while setup phase");
        }
        space=new InternalPayloadSpace[] { new InternalPayloadSpace(),new InternalPayloadSpace(),new InternalPayloadSpace() };
        space[0].getInternalPayload(identity[0]);
        space[0].getInternalPayload(identity[1]);
        space[0].getInternalPayload(identity[2]);
        space[0].getInternalPayload(identity[3]);

        space[1].getInternalPayload(identity[0]);
        space[1].getInternalPayload(identity[1]);

        space[2].getInternalPayload(identity[2]);
        space[2].getInternalPayload(identity[3]);
    }

    @Test
    public void payloadSpaceIsolationTest()  {
        // testing isolation of identities
        assertTrue("PayloadSpace isolation test 0",space[0].getInternalPayload(identity[0])==space[0].getInternalPayload(identity[0]));
        assertTrue("PayloadSpace isolation test 1",space[0].getInternalPayload(identity[0])!=space[0].getInternalPayload(identity[1]));
        assertTrue("PayloadSpace isolation test 2",space[0].getInternalPayload(identity[0])!=space[0].getInternalPayload(identity[2]));
        assertTrue("PayloadSpace isolation test 3",space[0].getInternalPayload(identity[0])!=space[0].getInternalPayload(identity[3]));

        assertTrue("PayloadSpace isolation test 4",space[0].getInternalPayload(identity[0])!=space[1].getInternalPayload(identity[0]));
        assertTrue("PayloadSpace isolation test 5",space[0].getInternalPayload(identity[0])!=space[2].getInternalPayload(identity[0]));
    }

    @Test
    public void payloadSpaceSetAndGetTest() throws Exception {
        InternalPayload p=space[0].getInternalPayload(identity[0]);
        String pl=RandomString.nextString((int)(Math.random()*1024*10+1));
        PayloadChunk pc =new PayloadChunk(100,pl.getBytes(StandardCharsets.UTF_8),null);
        assertTrue("payload space previously unexpetedly not empty",p.setPayload(pc)==null);
        assertTrue("payload space previously unexpetedly not equal",pl.equals(new String(p.getPayload(100).getPayload(), StandardCharsets.UTF_8)));
    }

    @Test
    public void payloadSpaceProcessingTest() throws Exception {
        // just a quick small test
        payloadSpaceProcessingTest(RandomString.nextString(1));

        //test with a 100MB blob
        payloadSpaceProcessingTest(RandomString.nextString(1024*1024*100)); // creating a random sting up to 100 MB

        // fuzz it with some random strings
        for(int i=0;i<100;i++) {
            payloadSpaceProcessingTest(RandomString.nextString((int)(Math.random()*1024*10+1))); // creating a random sting up to 10KB
        }
    }

    private void payloadSpaceProcessingTest(String s) throws Exception {
        LOGGER.log(Level.INFO,"Testing payload handling with "+s.getBytes(StandardCharsets.UTF_8).length+" bytes");
        InternalPayload p=space[0].getInternalPayload(identity[0]);
        PayloadChunk pc =new PayloadChunk(200,s.getBytes(StandardCharsets.UTF_8),null);
        Operation op=new IdMapOperation(200,201,1);
        assertTrue("payload space previously unexpetedly not empty",p.setPayload(pc)==null);
        assertTrue("addin of operation unexpectedly rejected",p.addOperation(op)==true);
        assertTrue("target  payload should not be null",p.getPayload(201)!=null);
        assertTrue("target  payload should be identical",s.equals(new String(p.getPayload(201).getPayload(),StandardCharsets.UTF_8)));
        p.setPayload(new PayloadChunk(200, null,null)); //remove the payload chunk from store
        assertTrue("source payload should be null",p.getPayload(200)==null);
        assertTrue("target payload should be null",p.getPayload(201)==null);
        assertTrue("removal of operation "+op.toString()+" unexpectedly failed",p.removeOperation(op)==true);
    }


}
