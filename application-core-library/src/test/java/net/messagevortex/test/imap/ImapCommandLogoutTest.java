package net.messagevortex.test.imap;

import net.messagevortex.MessageVortexLogger;
import net.messagevortex.transport.imap.ImapCommand;
import net.messagevortex.transport.imap.ImapCommandFactory;
import net.messagevortex.transport.imap.ImapException;
import net.messagevortex.transport.imap.ImapLine;
import net.messagevortex.MessageVortex;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.util.logging.Level;

import static org.junit.Assert.fail;

/**
 * Tests for {@link MessageVortex}.
 *
 * @author martin@gwerder.net (Martin GWERDER)
 */
@RunWith(JUnit4.class)
public class ImapCommandLogoutTest {

    static {
        MessageVortexLogger.setGlobalLogLevel(Level.ALL);
    }

    @Test
    public void logoutParsing() {
        ImapCommand ic= ImapCommandFactory.getCommand("logout");
        try{
            ic.processCommand(new ImapLine(null,"A1 Logout\r\n"));
        } catch(ImapException ie) {
            fail("error logout test for \"A1 Logout\" ("+ie+")");
        }

        try{
            ic.processCommand(new ImapLine(null,"A1 Logout error trigger\r\n"));
            fail("error logout test for \"A1 logout error trigger\"");
        } catch(ImapException ie) {

        }
    }

}
