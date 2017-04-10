package net.gwerder.java.messagevortex.imap;

import net.gwerder.java.messagevortex.MailvortexLogger;

import java.util.logging.Level;


public class ImapCommandLogout extends ImapCommand {

    private static final java.util.logging.Logger LOGGER;
    
    static {
        LOGGER = MailvortexLogger.getLogger((new Throwable()).getStackTrace()[0].getClassName());
    }

    public void init() {
        ImapCommand.registerCommand(this);
    }
    
    public String[] processCommand(ImapLine line) throws ImapException {
        // skip space
        // WRNING this is "non-strict"
        line.skipSP(-1);
        
        // skip lineend
        if(!line.skipCRLF()) {
            throw new ImapException(line,"error parsing command");
        }

        if(line.getConnection()!=null) {
            line.getConnection().setImapState(ImapConnection.CONNECTION_NOT_AUTHENTICATED);
        }
        LOGGER.log(Level.INFO,Thread.currentThread().getName()+" is now in state NOT_AUTHENTICATED");
        return new String[] {"* BYE IMAP4rev1 Server logged out\r\n",line.getTag()+" OK\r\n",null };
    }
    
    public String[] getCommandIdentifier() {
        return new String[] {"LOGOUT"};
    }
    
    public String[] getCapabilities() {
        return new String[] {};
    }
    
}    