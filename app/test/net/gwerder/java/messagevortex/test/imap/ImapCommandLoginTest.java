package net.gwerder.java.messagevortex.test.imap;

import net.gwerder.java.messagevortex.MessageVortexLogger;
import net.gwerder.java.messagevortex.transport.imap.ImapCommand;
import net.gwerder.java.messagevortex.transport.imap.ImapException;
import net.gwerder.java.messagevortex.transport.imap.ImapLine;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.util.logging.Level;

import static org.junit.Assert.fail;

/**
 * Tests for {@link net.gwerder.java.messagevortex.MessageVortex}.
 *
 * @author martin@gwerder.net (Martin GWERDER)
 */
@RunWith(JUnit4.class)
public class ImapCommandLoginTest {

    static {
        MessageVortexLogger.setGlobalLogLevel(Level.ALL);
    }

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
