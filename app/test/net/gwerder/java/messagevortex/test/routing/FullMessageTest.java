package net.gwerder.java.messagevortex.test.routing;

import net.gwerder.java.messagevortex.MessageVortexLogger;
import net.gwerder.java.messagevortex.asn1.*;
import net.gwerder.java.messagevortex.asn1.encryption.Algorithm;
import net.gwerder.java.messagevortex.asn1.encryption.DumpType;
import net.gwerder.java.messagevortex.asn1.encryption.SecurityLevel;
import net.gwerder.java.messagevortex.test.TestHelper;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import static org.junit.Assert.assertTrue;

/**
 * Created by martin.gwerder on 13.05.2017.
 */
@RunWith(JUnit4.class)
public class FullMessageTest {

    private static final Logger LOGGER;
    static {
        LOGGER = MessageVortexLogger.getLogger((new Throwable()).getStackTrace()[0].getClassName());
        MessageVortexLogger.setGlobalLogLevel( Level.ALL);
    }

    @Test
    public void createAndDumpTest() throws Exception {
        // building a complex message
        VortexMessage message=new VortexMessage(new PrefixBlock(new SymmetricKey(Algorithm.AES128)),new InnerMessageBlock( Algorithm.AES128,new AsymmetricKey(Algorithm.RSA.getParameters(SecurityLevel.LOW))));
        IdentityBlock blockIdentity = message.getInnerMessage().getIdentity();
        blockIdentity.setSerial(42);
        blockIdentity.setReplay(84);
        blockIdentity.setUsagePeriod(new UsagePeriod(100));
        RoutingBlock routing=message.getInnerMessage().getRouting();
        List<SymmetricKey> sk=new ArrayList<>();
        for(int i=0;i<20;i++) {
            sk.add(new SymmetricKey());
        }
        routing.addOperation(new AddRedundancyOperation(1000,10,10,sk,2000,8));
        message.setDecryptionKey(new AsymmetricKey(Algorithm.RSA.getParameters(SecurityLevel.LOW)));
        testMessageEncoding(message);
    }

    private void testMessageEncoding(VortexMessage original) throws IOException {
        for(DumpType dt:DumpType.values()) {
            VortexMessage b=new VortexMessage(original.toBytes(dt),original.getDecryptionKey());
            LOGGER.log(Level.INFO, "Testing of object with " + dt.name() + " ("+b.getDecryptionKey()+")");
            LOGGER.log(Level.INFO, "  encoding objects");
            VortexMessage m2=new VortexMessage( b.toBytes(dt),b.getDecryptionKey());
            VortexMessage m3=new VortexMessage(m2.toBytes(dt),b.getDecryptionKey());
            assertTrue("found fixme in dump text\n"+m3.dumpValueNotation("",dt),m3.dumpValueNotation("",dt).toUpperCase().indexOf("FIXME")>-1);
            LOGGER.log(Level.INFO, "  Testing object reencoding ("+m2.getDecryptionKey()+"/"+m3.getDecryptionKey()+")");
            for(VortexMessage v:new VortexMessage[] {m2,m3}) {
                assertTrue("test for toBytes() capability in prefix when aplying dump type "+dt+"",v.getPrefix().toBytes(dt)!=null);
                assertTrue("test for toBytes() capability in InnerMessage when aplying dump type "+dt+"",v.getInnerMessage().toBytes(dt)!=null);
                assertTrue("test for toBytes() capability in InnerMessage when aplying dump type ALL_UNENCRYPTED",v.getInnerMessage().toBytes(DumpType.ALL_UNENCRYPTED)!=null);
            }
            LOGGER.log(Level.INFO, "  Testing value dump equality ("+m2.getPrefix().getDecryptionKey()+"/"+m3.getPrefix().getDecryptionKey()+")");
            assertTrue("test for equal() equality in prefix.key when aplying dump type "          +dt+"\n"+TestHelper.compareDumps(m2.getPrefix().getKey().dumpValueNotation("",dt),m3.getPrefix().getKey().dumpValueNotation("",dt)),m2.getPrefix().getKey().dumpValueNotation("",dt).equals(m3.getPrefix().getKey().dumpValueNotation("",dt)));
            assertTrue("test for equal() equality in prefix.decryptionKey when aplying dump type "+dt+"\n"+TestHelper.compareDumps(m2.getPrefix().getDecryptionKey().dumpValueNotation("",dt),m3.getPrefix().getDecryptionKey().dumpValueNotation("",dt)),TestHelper.prepareDump(m2.getPrefix().getDecryptionKey().dumpValueNotation("",dt)).equals(TestHelper.prepareDump(m3.getPrefix().getDecryptionKey().dumpValueNotation("",dt))));
            String d2=m2.dumpValueNotation("",dt);
            String d3=m3.dumpValueNotation("",dt);
            assertTrue("test for dumpValueNotation() equality in prefix when aplying dump type "  +dt+"\n"+TestHelper.compareDumps(d2,d3)+"\n"+m2.getPrefix().getDecryptionKey().equals(m3.getPrefix().getDecryptionKey())+"\n"+m2.getPrefix().getKey().equals(m3.getPrefix().getKey()),TestHelper.prepareDump(d2).equals(TestHelper.prepareDump(d3)));
            assertTrue("test for dumpValueNotation() equality in InnerMessage when aplying dump type "+dt+"\n"+TestHelper.compareDumps(m2.getInnerMessage().dumpValueNotation("",dt),m3.getInnerMessage().dumpValueNotation("",dt)),m2.getInnerMessage().dumpValueNotation("",dt ).equals(m3.getInnerMessage().dumpValueNotation("",dt)));
            assertTrue("test for dumpValueNotation() when aplying dump type "+dt+"\n"+ TestHelper.compareDumps(m2.dumpValueNotation("",dt),m3.dumpValueNotation("",dt)),TestHelper.prepareDump(m2.dumpValueNotation("",dt)).equals(TestHelper.prepareDump(m3.dumpValueNotation("",dt))));
            LOGGER.log(Level.INFO, "  Testing equal() equality");
            assertTrue("test for equal() equality in prefix when aplying dump type "+dt,m2.getPrefix().equals(m3.getPrefix()));
            assertTrue("test for equal() equality in InnerMessage.prefix when aplying dump type "+dt,m2.getInnerMessage().getPrefix().equals(m3.getInnerMessage().getPrefix()));
            assertTrue("test for equal() equality in InnerMessage.identity when aplying dump type "+dt,m2.getInnerMessage().getIdentity().equals(m3.getInnerMessage().getIdentity()));
            assertTrue("test for equal() equality in InnerMessage.routing when aplying dump type "+dt,m2.getInnerMessage().getRouting().equals(m3.getInnerMessage().getRouting()));
            assertTrue("test for equal() equality in InnerMessage.payload when aplying dump type "+dt,Arrays.equals(m2.getInnerMessage().getPayload(),m3.getInnerMessage().getPayload()));
            assertTrue("test for byte equality of InnerMessage.prefix.key when aplying dump type "+dt, Arrays.equals(m2.getInnerMessage().getPrefix().getKey().toBytes(dt),m3.getInnerMessage().getPrefix().getKey().toBytes(dt)));
            assertTrue("test for byte equality of InnerMessage.prefix.decryptionKey when aplying dump type "+dt, Arrays.equals(m2.getInnerMessage().getPrefix().getDecryptionKey().toBytes(dt),m3.getInnerMessage().getPrefix().getDecryptionKey().toBytes(dt)));
            assertTrue("test for byte equality of InnerMessage.prefix when aplying dump type "+dt, Arrays.equals(m2.getInnerMessage().getPrefix().toBytes(dt),m3.getInnerMessage().getPrefix().toBytes(dt)));
            assertTrue("test for equal() equality when aplying dump type "+dt,m2.equals(m3));
        }
    }
}
