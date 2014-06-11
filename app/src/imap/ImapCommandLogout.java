package net.gwerder.java.mailvortex.imap;

public class ImapCommandLogout extends ImapCommand {

	static {
        ImapCommand.registerCommand(new ImapCommandLogout());
    }
	
	public String[] processCommand(ImapLine line) {
		line.getConnection().setState(ImapConnection.CONNECTION_NOT_AUTHENTICATED);
		return new String[] {"* BYE IMAP4rev1 Server logged out",line.getTag()+" OK" };
	}
	
	public String[] getCommandIdentifier() {
		return new String[] {"LOGOUT"};
	}
	
}	
