package net.gwerder.java.mailvortex.imap;

public class ImapCommandNoop extends ImapCommand {

    static void init() {
        ImapCommand.registerCommand(new ImapCommandNoop());
    }
    /***
     * @fix.me return proper status
     ***/
    public String[] processCommand(ImapLine line) throws ImapException {
        
        // skip space
        // WRNING this is "non-strict"
        line.skipSP(-1);
        
        // skip lineend
        if(!line.skipCRLF()) {
            throw new ImapException(line,"error parsing command");
        }

        // Example:
        //// * 22 EXPUNGE
        //// * 23 EXISTS
        //// * 3 RECENT
        //// * 14 FETCH (FLAGS (\Seen \Deleted))
        return new String[] {line.getTag()+" OK\r\n" };
    }
    
    public String[] getCommandIdentifier() {
        return new String[] {"NOOP"};
    }
    
}    
