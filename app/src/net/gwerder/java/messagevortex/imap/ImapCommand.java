package net.gwerder.java.messagevortex.imap;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public abstract class ImapCommand implements Cloneable {

    private static final Map<String,ImapCommand> COMMANDS;
 
    static  {
        COMMANDS=new ConcurrentHashMap<>();
        (new ImapCommandCapability()).init();
        (new ImapCommandLogin()).init();
        (new ImapCommandLogout()).init();
        (new ImapCommandNoop()).init();
    } 

    public static void registerCommand(ImapCommand command) {
        String[] arr=command.getCommandIdentifier();
        for(String a:arr) {
            COMMANDS.put(a.toLowerCase(),command);
        }    
    }
    
    public static void deregisterCommand(String command) {
        COMMANDS.remove(command.toLowerCase());
    }
    
    public static ImapCommand[] getCommands() {
        return COMMANDS.values().toArray(new ImapCommand[COMMANDS.size()]);
    }

    public static ImapCommand getCommand(String name) {
        return COMMANDS.get(name.toLowerCase());
    }

    public abstract String[] getCapabilities();
    
    public abstract void init();

    public abstract String[] getCommandIdentifier();

    public abstract String[] processCommand(ImapLine line) throws ImapException;

}
