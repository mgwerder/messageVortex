package net.gwerder.java.mailvortex.imap;

public class ImapCommandCapability extends ImapCommand {

	static {
        ImapCommand.registerCommand(new ImapCommandCapability());
    }
	
	public String[] processCommand(ImapLine line) {
		// FIXME list capabilities of all commands (if any)
		return new String[] {"* CAPABILITY" };
	}
	
	public String[] getCommandIdentifier() {
		return new String[] {"CAPABILITY"};
	}
	
}	
