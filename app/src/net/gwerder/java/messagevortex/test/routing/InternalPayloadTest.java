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

import net.gwerder.java.messagevortex.MessageVortexLogger;
import net.gwerder.java.messagevortex.asn1.AddRedundancyOperation;
import net.gwerder.java.messagevortex.asn1.IdentityBlock;
import net.gwerder.java.messagevortex.asn1.Payload;
import net.gwerder.java.messagevortex.asn1.SymmetricKey;
import net.gwerder.java.messagevortex.routing.operation.AddRedundancy;
import net.gwerder.java.messagevortex.routing.operation.InternalPayload;
import net.gwerder.java.messagevortex.routing.operation.InternalPayloadSpace;
import net.gwerder.java.messagevortex.routing.operation.Operation;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.logging.Level;

import static junit.framework.TestCase.assertTrue;
import static org.junit.Assert.assertThat;
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

}
