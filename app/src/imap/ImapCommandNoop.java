package net.gwerder.java.mailvortex.imap;

public class ImapCommandNoop extends ImapCommand {

    static void init() {
        ImapCommand.registerCommand(new ImapCommandNoop());
    }
    /***
     * @fix.me return proper status
     ***/
    public String[] processCommand(ImapLine line) {
        // Example:
        //// * 22 EXPUNGE
        //// * 23 EXISTS
        //// * 3 RECENT
        //// * 14 FETCH (FLAGS (\Seen \Deleted))
        return new String[] {line.getTag()+" OK" };
    }
    
    public String[] getCommandIdentifier() {
        return new String[] {"NOOP"};
    }
    
}    
