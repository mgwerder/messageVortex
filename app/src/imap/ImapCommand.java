package net.gwerder.java.mailvortex.imap;

import java.util.concurrent.ConcurrentHashMap;

public abstract class ImapCommand implements Cloneable {

	static private final ConcurrentHashMap<String,ImapCommand> commands=new ConcurrentHashMap<String,ImapCommand>();

	public static void registerCommand(ImapCommand command) {
		String[] arr=command.getCommandIdentifier();
		for(int i=0;i<arr.length;i++) commands.put(arr[i],command);
	}

	public static String[] getCapabilities() {
		return new String[0];
	}
	
	public abstract String[] getCommandIdentifier();

	public abstract String[] processCommand(ImapLine line);

}
