package net.messagevortex.test.routing;

import net.messagevortex.MessageVortexLogger;
import net.messagevortex.asn1.IdentityBlock;
import net.messagevortex.asn1.PayloadChunk;
import net.messagevortex.router.operation.IdMapOperation;
import net.messagevortex.router.operation.InternalPayloadSpace;
import net.messagevortex.router.operation.InternalPayloadSpaceStore;
import net.messagevortex.router.operation.Operation;
import net.messagevortex.test.GlobalJunitExtension;
import net.messagevortex.transport.RandomString;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.logging.Level;

@DisplayName("Testing the internal payload space implementation")
@ExtendWith(GlobalJunitExtension.class)
public class InternalPayloadSpaceTest {

    private static final java.util.logging.Logger LOGGER;

    static {
        LOGGER = MessageVortexLogger.getLogger((new Throwable()).getStackTrace()[0].getClassName());
    }

    IdentityBlock[] identity;
    InternalPayloadSpaceStore[] space;

    public void setup() {
        try {
            identity = new IdentityBlock[]{new IdentityBlock(), new IdentityBlock(), new IdentityBlock(), new IdentityBlock()};
        } catch (IOException ioe) {
            LOGGER.log(Level.SEVERE, "IOException while creating identities", ioe);
            Assertions.fail("failure while setup phase");
        }
        space = new InternalPayloadSpaceStore[]{new InternalPayloadSpaceStore(), new InternalPayloadSpaceStore(), new InternalPayloadSpaceStore()};
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
    @DisplayName("Testing for payload space isolation")
    public void payloadSpaceIsolationTest() {
        setup();
        // testing isolation of identities
        Assertions.assertAll("Testing isolation",
                () -> Assertions.assertSame(space[0].getInternalPayload(identity[0]), space[0].getInternalPayload(identity[0]), "PayloadSpace isolation test 0"),
                () -> Assertions.assertNotSame(space[0].getInternalPayload(identity[0]), space[0].getInternalPayload(identity[1]), "PayloadSpace isolation test 1"),
                () -> Assertions.assertNotSame(space[0].getInternalPayload(identity[0]), space[0].getInternalPayload(identity[2]), "PayloadSpace isolation test 2"),
                () -> Assertions.assertNotSame(space[0].getInternalPayload(identity[0]), space[0].getInternalPayload(identity[3]), "PayloadSpace isolation test 3"),

                () -> Assertions.assertNotSame(space[0].getInternalPayload(identity[0]), space[1].getInternalPayload(identity[0]), "PayloadSpace isolation test 4"),
                () -> Assertions.assertNotSame(space[0].getInternalPayload(identity[0]), space[2].getInternalPayload(identity[0]), "PayloadSpace isolation test 5")
        );
    }

    @Test
    @DisplayName("Testing for payload space get and set")
    public void payloadSpaceSetAndGetTest() {
        setup();
        InternalPayloadSpace p = space[0].getInternalPayload(identity[0]);
        String pl = RandomString.nextString((int) (Math.random() * 1024 * 10 + 1));
        PayloadChunk pc = new PayloadChunk(100, pl.getBytes(StandardCharsets.UTF_8), null);
        Assertions.assertNull(p.setPayload(pc), "payload space previously unexpetedly not empty");
        Assertions.assertEquals(pl, new String(p.getPayload(100).getPayload(), StandardCharsets.UTF_8), "payload space previously unexpetedly not equal");
    }

    @Test
    @DisplayName("Testing for payload space operation processing")
    public void payloadSpaceProcessingTest() throws Exception {
        setup();

        // just a quick small test
        payloadSpaceProcessingTest(RandomString.nextString(1));

        //test with a 10MB blob
        payloadSpaceProcessingTest(RandomString.nextString(1024 * 1024 * 10)); // creating a random sting up to 10 MB

        // fuzz it with some random strings
        for (int i = 0; i < 100; i++) {
            payloadSpaceProcessingTest(RandomString.nextString((int) (Math.random() * 1024 * 10 + 1))); // creating a random sting up to 10KB
        }
    }

    private void payloadSpaceProcessingTest(String s) {
        LOGGER.log(Level.INFO, "Testing payload handling with " + s.getBytes(StandardCharsets.UTF_8).length + " bytes");
        InternalPayloadSpace p = space[0].getInternalPayload(identity[0]);
        PayloadChunk pc = new PayloadChunk(200, s.getBytes(StandardCharsets.UTF_8), null);
        Operation op = new IdMapOperation(200, 201, 1);
        Assertions.assertAll("test payload start",
                () -> Assertions.assertNull(p.setPayload(pc), "payload space previously unexpetedly not empty"),
                () -> Assertions.assertEquals(true, p.addOperation(op), "addin of operation unexpectedly rejected"),
                () -> Assertions.assertNotNull(p.getPayload(201), "target  payload should not be null"),
                () -> Assertions.assertEquals(s, new String(p.getPayload(201).getPayload(), StandardCharsets.UTF_8), "target  payload should be identical")
        );
        p.setPayload(new PayloadChunk(200, null, null)); //remove the payload chunk from store
        Assertions.assertAll("Payload processing after source removal",
                () -> Assertions.assertNull(p.getPayload(200), "source payload should be null"),
                () -> Assertions.assertNull(p.getPayload(201), "target payload should be null"),
                () -> Assertions.assertEquals(true, p.removeOperation(op), "removal of operation " + op + " unexpectedly failed")
        );
    }

}
