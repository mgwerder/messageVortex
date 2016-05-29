package net.gwerder.java.mailvortex.test.imap;

import net.gwerder.java.mailvortex.MailvortexLogger;
import net.gwerder.java.mailvortex.imap.ImapCommand;
import net.gwerder.java.mailvortex.imap.ImapException;
import net.gwerder.java.mailvortex.imap.ImapLine;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.util.logging.Level;

import static org.junit.Assert.fail;

/**
 * Tests for {@link net.gwerder.java.mailvortex.MailVortex}.
 *
 * @author martin@gwerder.net (Martin GWERDER)
 */
@RunWith(JUnit4.class)
public class ImapCommandNoopTest {

    static {
        MailvortexLogger.setGlobalLogLevel(Level.ALL);
    }    

    @Test
    public void noopParsing() {
        ImapCommand ic=ImapCommand.getCommand("Noop");
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