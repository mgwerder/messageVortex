package net.messagevortex.test.transport.imap;

import net.messagevortex.ExtendedSecureRandom;
import net.messagevortex.MessageVortex;
import net.messagevortex.MessageVortexLogger;
import net.messagevortex.transport.imap.*;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.logging.Level;

import static org.junit.Assert.fail;

/**
 * Tests for {@link MessageVortex}.
 *
 * @author martin@gwerder.net (Martin GWERDER)
 */
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
        ImapCommand ic= ImapCommandFactory.getCommand("login");

        try{
            ic.processCommand(new ImapLine(null,"A1 Login\r\n"));
            Assertions.fail("error Noop test for \"A1 Login\"");
        } catch(ImapException ie) {

        }

        try{
            ic.processCommand(new ImapLine(null,"A1 Login \r\n"));
            Assertions.fail("error Noop test for \"A1 Login \"");
        } catch(ImapException ie) {

        }

        try{
            ic.processCommand(new ImapLine(null,"A1 Login a\r\n"));
            Assertions.fail("error Noop test for \"A1 Login a\"");
        } catch(ImapException ie) {

        }

        try{
            ic.processCommand(new ImapLine(null,"A1 Login a*\r\n"));
            Assertions.fail("error Noop test for \"A1 Login a*\"");
        } catch(ImapException ie) {

        }

        try{
            ic.processCommand(new ImapLine(null,"A1 Login a *\r\n"));
            Assertions.fail("error Noop test for \"A1 Login a *\"");
        } catch(ImapException ie) {

        }

        try{
            ic.processCommand(new ImapLine(null,"A1 Login a b *\r\n"));
            Assertions.fail("error Noop test for \"A1 Login a b *\"");
        } catch(ImapException ie) {

        }

    }
}
