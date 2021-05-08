package net.messagevortex.test.transport.imap;

import net.messagevortex.transport.imap.ImapCommand;
import net.messagevortex.transport.imap.ImapCommandFactory;
import net.messagevortex.transport.imap.ImapConnection;
import net.messagevortex.transport.imap.ImapLine;

class ImapCommandIWantATimeout extends ImapCommand {

  private volatile boolean shutdownTimeout = false;

  public void init() {
    ImapCommandFactory.registerCommand(this);
  }

  public String[] processCommand(ImapLine line) {
    int i = 0;
    do {
      try {
        wait(100);
      } catch (InterruptedException ie) {
      }
      i++;
    } while (i < 11000000 && !shutdownTimeout);
    return null;
  }

  public String[] getCommandIdentifier() {
    return new String[]{"IWantATimeout"};
  }

  public String[] getCapabilities(ImapConnection conn) {
    return new String[]{};
  }

  public void shutdown() {
    if ( !shutdownTimeout ) {
      shutdownTimeout = true;
      synchronized (this) {
        this.notify();
      }
    }
  }
}
