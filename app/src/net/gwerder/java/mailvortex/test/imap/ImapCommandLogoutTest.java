package net.gwerder.java.mailvortex.test.imap;

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
public class ImapCommandLogoutTest {

    static {
        MailvortexLogger.setGlobalLogLevel(Level.ALL);
    }    

    @Test
    public void logoutParsing() {
        ImapCommand ic=ImapCommand.getCommand("logout");
        try{
            ic.processCommand(new ImapLine(null,"A1 Logout\r\n"));
        } catch(ImapException ie) {
            fail("error logout test for \"A1 Logout\" ("+ie+")");
        }

        try{
            ic.processCommand(new ImapLine(null,"A1 Logout error trigger\r\n"));
            fail("error logout test for \"A1 logout error trigger\"");
        } catch(ImapException ie) {
            ;
        }
    }

}