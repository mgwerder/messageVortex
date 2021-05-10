package net.messagevortex.test.transport.imap;

import net.messagevortex.MessageVortex;
import net.messagevortex.MessageVortexLogger;
import net.messagevortex.test.GlobalJunitExtension;
import net.messagevortex.transport.imap.*;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.util.logging.Level;

import static org.junit.Assert.fail;

/**
 * Tests for {@link MessageVortex}.
 */
@ExtendWith(GlobalJunitExtension.class)
public class ImapCommandCapabilityTest {

  @Test
  public void capabilityParsing() {
    ImapCommand ic = ImapCommandFactory.getCommand("Capability");
    try {
      ic.processCommand(new ImapLine(null, "A1 Noop\r\n"));
    } catch (ImapException ie) {
      Assertions.fail("error Noop test for \"A1 Noop\" (" + ie + ")");
    }

    try {
      ic.processCommand(new ImapLine(null, "A1 Noop error trigger\r\n"));
      Assertions.fail("error Noop test for \"A1 Noop error trigger\"");
    } catch (ImapException ie) {

    }
  }

  public static class ImapCommandCapabilityParser extends ImapCommand {

    @Override
    public String[] processCommand(ImapLine line) {
      return null;
    }

    @Override
    public String[] getCommandIdentifier() {
      return new String[] {"CapabilityParser"};
    }

    @Override
    public String[] getCapabilities(ImapConnection conn) {
      return new String[] {"CapabilityParser=one", "CapabilityParser=two"};
    }

  }

  @Test
  public void capabilityPropagation() {
    // check if capabilities with "=" are concatenated
    ImapCommandFactory.registerCommand(new ImapCommandCapabilityParser());
    ImapCommand ic = ImapCommandFactory.getCommand("capability");
    try {
      String[] a = ic.processCommand(new ImapLine(null, "A1 CAPABILITY\r\n"));
      String toCheck = a[0].replace('\r', ' ').replace('\n', ' ');
      if ((toCheck + " ").indexOf(" CapabilityParser=two,one ") == -1) {
        Assertions.fail("Capabilities wrong or missing (" + toCheck + ")");
      }
    } catch (ImapException ie) {
      Assertions.fail("Got unexpected exception while checking capabilities");
    }


  }
}
