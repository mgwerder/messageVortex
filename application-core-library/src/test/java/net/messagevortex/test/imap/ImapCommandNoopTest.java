package net.messagevortex.test.imap;

import net.messagevortex.MessageVortexLogger;
import net.messagevortex.transport.imap.ImapCommand;
import net.messagevortex.transport.imap.ImapCommandFactory;
import net.messagevortex.transport.imap.ImapException;
import net.messagevortex.transport.imap.ImapLine;
import net.messagevortex.MessageVortex;
import org.junit.jupiter.api.Test;


import java.util.logging.Level;

import static org.junit.Assert.fail;

/**
 * Tests for {@link MessageVortex}.
 *
 * @author martin@gwerder.net (Martin GWERDER)
 */
public class ImapCommandNoopTest {

    static {
        MessageVortexLogger.setGlobalLogLevel(Level.ALL);
    }

    @Test
    public void noopParsing() {
        ImapCommand ic= ImapCommandFactory.getCommand("Noop");
        try{
            ic.processCommand(new ImapLine(null,"A1 Noop\r\n"));
        } catch(ImapException ie) {
            fail("error Noop test for \"A1 Noop\" ("+ie+")");
        }

        try{
            ic.processCommand(new ImapLine(null,"A1 Noop error trigger\r\n"));
            fail("error Noop test for \"A1 Noop error trigger\"");
        } catch(ImapException ie) {

        }
    }

}
