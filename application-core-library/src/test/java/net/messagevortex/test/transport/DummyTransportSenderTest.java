package net.messagevortex.test.transport;

import net.messagevortex.AbstractDaemon;
import net.messagevortex.MessageVortexLogger;
import net.messagevortex.test.GlobalJunitExtension;
import net.messagevortex.test.transport.imap.ImapSSLTest;
import net.messagevortex.transport.TransportReceiver;
import net.messagevortex.transport.dummy.DummyTransportTrx;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.parallel.ResourceAccessMode;
import org.junit.jupiter.api.parallel.ResourceLock;
import org.junit.jupiter.api.parallel.Resources;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Set;
import java.util.Vector;
import java.util.logging.Level;

@ExtendWith(GlobalJunitExtension.class)
public class DummyTransportSenderTest extends AbstractDaemon implements TransportReceiver {

    private List<InputStream> msgs = new Vector<>();
    private static final java.util.logging.Logger LOGGER;
    private final Object semaphore = new Object();

    static {
        LOGGER = MessageVortexLogger.getLogger((new Throwable()).getStackTrace()[0].getClassName());
    }

    @Test
    @ResourceLock(value = Resources.SYSTEM_PROPERTIES, mode = ResourceAccessMode.READ_WRITE)
    public void dummyTransportEndpointTest() {
        Set<Thread> threadSet = ImapSSLTest.getThreadList();
        LOGGER.log(Level.INFO, "Setting up dummy network");
        DummyTransportTrx[] dt = new DummyTransportTrx[10];
        for (int i = 0; i < dt.length; i++) {
            LOGGER.log(Level.INFO, "  Setting up endpoint " + i);
            if (i == 0) {
                try {
                    dt[i] = new DummyTransportTrx("martin@example.com", this);
                } catch (IOException ioe) {
                    ioe.printStackTrace();
                    Assertions.fail("failed to add martin@example.com");
                }
            } else {
                try {
                    dt[i] = new DummyTransportTrx(this);
                } catch (IOException ioe) {
                    ioe.printStackTrace();
                    Assertions.fail("unexpected IOException occurred when setting up endpoints");
                }
            }
        }

        // Test duplicate id generation for transport media
        try {
            new DummyTransportTrx("martin@example.com", this);
            Assertions.fail("duplicate addition of ID to DummyTransportSender unexpectedly succeeded");
        } catch (IOException ioe) {
            // this is expected behaviour
        }
        for (int i = 0; i < dt.length; i++) {
            dt[i].shutdownDaemon();
        }
        Assertions.assertTrue(ImapSSLTest.verifyHangingThreads(threadSet).size() == 0, "error searching for hangig threads");
        DummyTransportTrx.clearDummyEndpoints();
    }

    @Test
    @ResourceLock(value = Resources.SYSTEM_PROPERTIES, mode = ResourceAccessMode.READ_WRITE)
    public void dummyMessageSendingTest() {
        LOGGER.log(Level.INFO, "Setting up dummy network for transfer test");
        try {
            DummyTransportTrx sender = new DummyTransportTrx("sender@example.com", null);
            DummyTransportTrx recipient = new DummyTransportTrx("recipient@example.com", this);
            String message = "Testmessage\0\r\n\r\n\n";
            ByteArrayInputStream bis = new ByteArrayInputStream(message.getBytes(StandardCharsets.UTF_8));
            LOGGER.log(Level.INFO, "Sending message");
            sender.sendMessage("recipient@example.com", bis);
            for (int i = 0; i < 10 && msgs.size() < 1; i++) {
                try {
                    synchronized (semaphore) {
                        semaphore.wait(1000);
                    }
                } catch (InterruptedException ie) {
                    // safe to ignore
                }
            }
            Assertions.assertTrue(msgs.size() > 0, "no message received");
            InputStream is = msgs.get(0);

            ByteArrayOutputStream buffer = new ByteArrayOutputStream();
            int nRead;
            byte[] data = new byte[1024];
            while ((nRead = is.read(data, 0, data.length)) != -1) {
                buffer.write(data, 0, nRead);
            }
            buffer.flush();
            String text = new String(buffer.toByteArray(),StandardCharsets.UTF_8);
            LOGGER.log(Level.INFO, "got message");
            Assertions.assertEquals(message, text, "received message does not match original message");
            LOGGER.log(Level.INFO, "message content verified successfully");
            sender.shutdownDaemon();
            recipient.shutdownDaemon();
        } catch (IOException ioe) {
            Assertions.fail("Got unexpected exception", ioe);
        }
    }


    @Override
    public boolean gotMessage(InputStream is) {
        synchronized (msgs) {
            msgs.add(is);
            synchronized (semaphore) {
                semaphore.notifyAll();
            }
        }
        return true;
    }

}
