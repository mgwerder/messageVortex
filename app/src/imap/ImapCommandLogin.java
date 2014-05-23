package net.gwerder.java.mailvortex.imap;

public class ImapCommandLogin extends ImapCommand {

	static {
        ImapCommand.registerCommand(new ImapCommandLogin());
    }
	
	public String[] processCommand(ImapLine line) {
		line.getConnection().setState(ImapConnection.CONNECTION_AUTHENTICATED);
		return new String[] {line.getIdentifier()+" OK Logged in" };
	}

	public static String[] getCapabilities() {
		return new String[] { "LOGIN" };
	}
	
	public String[] getCommandIdentifier() {
		return new String[] {"LOGIN"};
	}
	
}	
