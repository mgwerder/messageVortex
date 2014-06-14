package net.gwerder.java.mailvortex.imap;

public class ImapCommandLogin extends ImapCommand {

	static void init() {
        ImapCommand.registerCommand(new ImapCommandLogin());
    }
	
	public String[] processCommand(ImapLine line) {
		line.getConnection().setState(ImapConnection.CONNECTION_AUTHENTICATED);
		// FIXME check credentials
		return new String[] {line.getTag()+" OK Logged in" };
	}

	public static String[] getCapabilities() {
		return new String[] { "LOGIN" };
	}
	
	public String[] getCommandIdentifier() {
		return new String[] {"LOGIN"};
	}
	
}	
