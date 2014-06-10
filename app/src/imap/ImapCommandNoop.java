package net.gwerder.java.mailvortex.imap;

public class ImapCommandNoop extends ImapCommand {

	static {
        ImapCommand.registerCommand(new ImapCommandNoop());
    }
	
	public String[] processCommand(ImapLine line) {
		// FIXME return status
		// FIXME reset autologout timer
		// Example:
		//// * 22 EXPUNGE
		//// * 23 EXISTS
		//// * 3 RECENT
		//// * 14 FETCH (FLAGS (\Seen \Deleted))
		return new String[] {line.getIdentifier()+" OK" };
	}
	
	public String[] getCommandIdentifier() {
		return new String[] {"NOOP"};
	}
	
}	
