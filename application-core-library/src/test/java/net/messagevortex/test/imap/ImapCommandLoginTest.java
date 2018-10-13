package net.messagevortex.test.imap;

import static org.junit.Assert.fail;

import java.util.logging.Level;
import net.messagevortex.ExtendedSecureRandom;
import net.messagevortex.MessageVortex;
import net.messagevortex.MessageVortexLogger;
import net.messagevortex.transport.imap.ImapClient;
import net.messagevortex.transport.imap.ImapCommand;
import net.messagevortex.transport.imap.ImapConnection;
import net.messagevortex.transport.imap.ImapException;
import net.messagevortex.transport.imap.ImapLine;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/**
 * Tests for {@link MessageVortex}.
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
