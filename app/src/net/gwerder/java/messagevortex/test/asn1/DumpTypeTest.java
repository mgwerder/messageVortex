package net.gwerder.java.messagevortex.test.asn1;

import net.gwerder.java.messagevortex.MessageVortexLogger;
import net.gwerder.java.messagevortex.asn1.encryption.DumpType;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;

import static org.junit.Assert.assertTrue;

/**
 * Created by martin.gwerder on 23.06.2016.
 */
@RunWith(JUnit4.class)
public class DumpTypeTest {


    private static final java.util.logging.Logger LOGGER;

    static {
        LOGGER = MessageVortexLogger.getLogger((new Throwable()).getStackTrace()[0].getClassName());
        MessageVortexLogger.setGlobalLogLevel( Level.ALL);
    }


    @Test
    public void testingLogicDumpPublicKey() {
        Map<DumpType,Boolean> arr=new HashMap<>(  );
        arr.put(DumpType.ALL,true);
        arr.put(DumpType.PUBLIC_ONLY,true);
        arr.put(DumpType.PRIVATE_COMMENTED,false);
        for(Map.Entry<DumpType,Boolean> e:arr.entrySet()) {
            assertTrue("bad reply for "+e.getKey()+"",e.getValue()==e.getKey().dumpPublicKey());
        }
    }

    @Test
    public void testingLogicDumpPrivateKey() {
        Map<DumpType,Boolean> arr=new HashMap<>(  );
        arr.put(DumpType.ALL,true);
        arr.put(DumpType.PUBLIC_ONLY,false);
        arr.put(DumpType.PRIVATE_COMMENTED,false);
        for(Map.Entry<DumpType,Boolean> e:arr.entrySet()) {
            assertTrue("bad reply for "+e.getKey()+"",e.getValue()==e.getKey().dumpPrivateKey());
        }
    }

}
