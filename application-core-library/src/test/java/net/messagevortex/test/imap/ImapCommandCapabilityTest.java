package net.messagevortex.test.imap;

import net.messagevortex.MessageVortexLogger;
import net.messagevortex.transport.imap.ImapCommand;
import net.messagevortex.transport.imap.ImapConnection;
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
public class ImapCommandCapabilityTest {

    static {
        MessageVortexLogger.setGlobalLogLevel(Level.ALL);
    }

    @Test
    public void capabilityParsing() {
        ImapCommand ic=ImapCommand.getCommand("Capability");
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

    public static class ImapCommandCapabilityParser extends ImapCommand {

        @Override
        public void init() {
            ImapCommand.registerCommand(this);
        }

        @Override
        public String[] processCommand(ImapLine line) {
            return null;
        }

        @Override
        public String[] getCommandIdentifier(){
            return new String[] {"CapabilityParser"};
        }

        @Override
        public String[] getCapabilities( ImapConnection conn ) {
            return new String[] {"CapabilityParser=one","CapabilityParser=two"};
        }

    }

    @Test
    public void capabilityPropagation() {
        // check if capabilities with "=" are concatenated
        (new ImapCommandCapabilityParser()).init();
        ImapCommand ic=ImapCommand.getCommand("capability");
        try{
            String[] a=ic.processCommand(new ImapLine(null,"A1 CAPABILITY\r\n"));
            String toCheck=a[0].replace( '\r',' ' ).replace( '\n',' ' );
            if((toCheck+" ").indexOf(" CapabilityParser=two,one ")==-1) {
                fail("Capabilities wrong or missing ("+toCheck+")");
            }
        } catch(ImapException ie) {
            fail("Got unexpected exception while checking capabilities");
        }


    }
}