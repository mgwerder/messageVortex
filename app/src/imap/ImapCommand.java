package net.gwerder.java.mailvortex.imap;

import java.util.concurrent.ConcurrentHashMap;

public abstract class ImapCommand implements Cloneable {

 
    static {
        commands=new ConcurrentHashMap<String,ImapCommand>();
        ImapCommandCapability.init();
        ImapCommandLogin.init();
        ImapCommandLogout.init();
        ImapCommandNoop.init();
    } 
    
    private static ConcurrentHashMap<String,ImapCommand> commands;

    public static void registerCommand(ImapCommand command) {
        String[] arr=command.getCommandIdentifier();
        for(int i=0;i<arr.length;i++) {
            commands.put(arr[i].toLowerCase(),command);        
        }    
    }
    
    public static void deregisterCommand(String command) {
        commands.remove(command.toLowerCase());        
    }
    
    public static ImapCommand[] getCommands() {
        return commands.values().toArray(new ImapCommand[commands.size()]);
    }

    public static ImapCommand getCommand(String name) {
        return commands.get(name.toLowerCase());
    }

    public abstract String[] getCapabilities();
    
    static void init() {
        throw new RuntimeException("init not overloaded");
    }

    public abstract String[] getCommandIdentifier();

    public abstract String[] processCommand(ImapLine line) throws ImapException;

}
