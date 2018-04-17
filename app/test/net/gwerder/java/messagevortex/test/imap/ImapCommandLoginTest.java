package net.gwerder.java.messagevortex.test.imap;

import net.gwerder.java.messagevortex.ExtendedSecureRandom;
import net.gwerder.java.messagevortex.MessageVortexLogger;
import net.gwerder.java.messagevortex.transport.AllTrustManager;
import net.gwerder.java.messagevortex.transport.CustomKeyManager;
import net.gwerder.java.messagevortex.transport.SecurityContext;
import net.gwerder.java.messagevortex.transport.imap.*;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509KeyManager;
import java.io.File;
import java.net.InetSocketAddress;
import java.util.logging.Level;

import static net.gwerder.java.messagevortex.transport.SecurityRequirement.PLAIN;
import static net.gwerder.java.messagevortex.transport.SecurityRequirement.SSLTLS;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * Tests for {@link net.gwerder.java.messagevortex.MessageVortex}.
 *
 * @author martin@gwerder.net (Martin GWERDER)
 */
@RunWith(JUnit4.class)
public class ImapCommandLoginTest {


    private static final java.util.logging.Logger LOGGER;

    static {
        ImapConnection.setDefaultTimeout(2000);
        ImapClient.setDefaultTimeout(2000);
        LOGGER = MessageVortexLogger.getLogger((new Throwable()).getStackTrace()[0].getClassName());
        MessageVortexLogger.setGlobalLogLevel(Level.ALL);
    }

    private ExtendedSecureRandom esr = new ExtendedSecureRandom();

    @Test
    public void loginCapabilitiesPropagated() {
        // FIXME check if login is passed to capabilities
    }

    @Test
    public void loginParsing() {
        ImapCommand ic=ImapCommand.getCommand("login");

        try{
            ic.processCommand(new ImapLine(null,"A1 Login\r\n"));
            fail("error Noop test for \"A1 Login\"");
        } catch(ImapException ie) {

        }

        try{
            ic.processCommand(new ImapLine(null,"A1 Login \r\n"));
            fail("error Noop test for \"A1 Login \"");
        } catch(ImapException ie) {

        }

        try{
            ic.processCommand(new ImapLine(null,"A1 Login a\r\n"));
            fail("error Noop test for \"A1 Login a\"");
        } catch(ImapException ie) {

        }

        try{
            ic.processCommand(new ImapLine(null,"A1 Login a*\r\n"));
            fail("error Noop test for \"A1 Login a*\"");
        } catch(ImapException ie) {

        }

        try{
            ic.processCommand(new ImapLine(null,"A1 Login a *\r\n"));
            fail("error Noop test for \"A1 Login a *\"");
        } catch(ImapException ie) {

        }

        try{
            ic.processCommand(new ImapLine(null,"A1 Login a b *\r\n"));
            fail("error Noop test for \"A1 Login a b *\"");
        } catch(ImapException ie) {

        }

    }
}
