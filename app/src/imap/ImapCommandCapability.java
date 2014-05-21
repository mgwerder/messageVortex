package net.gwerder.java.mailvortex.imap;

public class ImapCommandCapability extends ImapCommand {

	static {
        ImapCommand.registerCommand(new ImapCommandCapability());
    }
	
	public String[] processCommand(ImapLine line) {
		ImapCommand[] arr=ImapCommand.getCommands();
		String cap="";
		for(int i=0;i<arr.length;i++) {
			// FIXME process =-entries
			String[] arr2=arr[i].getCapabilities();
			for(int j=0;j<arr2.length;j++) {
				cap+=" "+arr[j];
			}
		}
		return new String[] {"* CAPABILITY IMAP4rev1",line.getIdentifier()+" OK" };
	}
	
	public String[] getCommandIdentifier() {
		return new String[] {"CAPABILITY"};
	}
	
}	
