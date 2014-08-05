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
public class ImapCommandLoginTest {

    static {
        MailvortexLogger.setGlobalLogLevel(Level.ALL);
    }    

    @Test
    public void loginCapabilitiesPropagated() {
        // check if login is passed to capabilities
    }

    @Test
    public void loginParsing() {
        ImapCommand ic=ImapCommand.getCommand("login");
        
        try{
            ic.processCommand(new ImapLine(null,"A1 Login\r\n"));
            fail("error Noop test for \"A1 Login\"");
        } catch(ImapException ie) {
            ;
        }

        try{
            ic.processCommand(new ImapLine(null,"A1 Login \r\n"));
            fail("error Noop test for \"A1 Login \"");
        } catch(ImapException ie) {
            ;
        }

        try{
            ic.processCommand(new ImapLine(null,"A1 Login a\r\n"));
            fail("error Noop test for \"A1 Login a\"");
        } catch(ImapException ie) {
            ;
        }

        try{
            ic.processCommand(new ImapLine(null,"A1 Login a*\r\n"));
            fail("error Noop test for \"A1 Login a*\"");
        } catch(ImapException ie) {
            ;
        }

        try{
            ic.processCommand(new ImapLine(null,"A1 Login a *\r\n"));
            fail("error Noop test for \"A1 Login a *\"");
        } catch(ImapException ie) {
            ;
        }

        try{
            ic.processCommand(new ImapLine(null,"A1 Login a b *\r\n"));
            fail("error Noop test for \"A1 Login a b *\"");
        } catch(ImapException ie) {
            ;
        }

    }
}