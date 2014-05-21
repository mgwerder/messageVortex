package net.gwerder.java.mailvortex.imap;

public class ImapCommandNoop extends ImapCommand {

	static {
        ImapCommand.registerCommand(new ImapCommandNoop());
    }
	
	public String[] processCommand(ImapLine line) {
		// FIXME return status
		return new String[] {"* CAPABILITY IMAP4rev1",line.getIdentifier()+" OK" };
	}
	
	public String[] getCommandIdentifier() {
		return new String[] {"NOOP"};
	}
	
}	
