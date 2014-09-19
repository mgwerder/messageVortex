package net.gwerder.java.mailvortex.imap;

import java.util.concurrent.ConcurrentHashMap;
import java.util.Map;

public abstract class ImapCommand implements Cloneable {

    private static final Map<String,ImapCommand> commands;
 
    static  {
        commands=new ConcurrentHashMap<String,ImapCommand>();
        (new ImapCommandCapability()).init();
        (new ImapCommandLogin()).init();
        (new ImapCommandLogout()).init();
        (new ImapCommandNoop()).init();
    } 

    public static final void registerCommand(ImapCommand command) {
        String[] arr=command.getCommandIdentifier();
        for(int i=0;i<arr.length;i++) {
            commands.put(arr[i].toLowerCase(),command);        
        }    
    }
    
    public static final void deregisterCommand(String command) {
        commands.remove(command.toLowerCase());        
    }
    
    public static final ImapCommand[] getCommands() {
        return commands.values().toArray(new ImapCommand[commands.size()]);
    }

    public static final ImapCommand getCommand(String name) {
        return commands.get(name.toLowerCase());
    }

    public abstract String[] getCapabilities();
    
    public abstract void init();

    public abstract String[] getCommandIdentifier();

    public abstract String[] processCommand(ImapLine line) throws ImapException;

}
