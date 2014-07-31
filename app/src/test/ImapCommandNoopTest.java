package net.gwerder.java.mailvortex.test;

import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.junit.Ignore;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import static org.junit.Assert.*;

import java.util.logging.Level;
import net.gwerder.java.mailvortex.*;
import net.gwerder.java.mailvortex.imap.*;

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
            ;
        }
    }

}