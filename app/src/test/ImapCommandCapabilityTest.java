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
public class ImapCommandCapabilityTest {

    static {
        MailvortexLogger.setGlobalLogLevel(Level.ALL);
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
            ;
        }
    }

    private class ImapCommandCapabilityParser extends ImapCommand {
        public void init() {
            ImapCommand.registerCommand(this);
        }
    
        public String[] processCommand(ImapLine line) {
            return null;
        }
    
        public String[] getCommandIdentifier() {
            return new String[] {"CapabilityParser"};
        }
        
        public String[] getCapabilities() {
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
            if((a[0]+" ").indexOf(" CapabilityParser=two,one ")==-1) {
                fail("Capabilities wrong or missing ("+a[0]+")");
            }
        } catch(ImapException ie) {
            fail("Got unexpected exception while checking capabilities");
        }

        
    }
}