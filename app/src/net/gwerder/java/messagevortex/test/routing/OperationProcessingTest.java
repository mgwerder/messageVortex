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
import net.gwerder.java.messagevortex.asn1.AbstractRedundancyOperation;
import net.gwerder.java.messagevortex.asn1.Identity;
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
            Identity identity = new Identity();
            InternalPayloadSpace ps=new InternalPayloadSpace();
            InternalPayload p=ps.getPayload(identity);
            SymmetricKey[] keys=new SymmetricKey[] {new SymmetricKey(),new SymmetricKey(),new SymmetricKey(),new SymmetricKey(),new SymmetricKey(),new SymmetricKey()};
            Operation op=new AddRedundancy(identity,new AbstractRedundancyOperation(1,3,3, Arrays.asList(keys),10,8));
            p.setOperation(op);
            // FIXME this test is highly incomplete
        }catch(IOException|NoSuchAlgorithmException ioe) {
            ioe.printStackTrace();
            fail("Exception while testing redundancy operation");
        }
    }

}
