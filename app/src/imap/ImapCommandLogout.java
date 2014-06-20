package net.gwerder.java.mailvortex.imap;

public class ImapCommandLogout extends ImapCommand {

    static void init() {
        ImapCommand.registerCommand(new ImapCommandLogout());
    }
    
    public String[] processCommand(ImapLine line) throws ImapException {
        // skip space
        // WRNING this is "non-strict"
        line.skipSP(-1);
        
        // skip lineend
        if(!line.skipCRLF()) {
            throw new ImapException(line,"error parsing command");
        }

        line.getConnection().setState(ImapConnection.CONNECTION_NOT_AUTHENTICATED);
        return new String[] {"* BYE IMAP4rev1 Server logged out",line.getTag()+" OK",null };
    }
    
    public String[] getCommandIdentifier() {
        return new String[] {"LOGOUT"};
    }
    
}    
