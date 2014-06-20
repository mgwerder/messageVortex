package net.gwerder.java.mailvortex.imap;
 
import java.util.logging.Logger;
import java.util.logging.Level;
 
public class ImapCommandLogin extends ImapCommand {

    private static final Logger LOGGER;
    static {
        LOGGER = Logger.getLogger((new Throwable()).getStackTrace()[0].getClassName());
    }

    static void init() {
        ImapCommand.registerCommand(new ImapCommandLogin());
    }
    
    /***
     * @fix.me add capabilities to successful login
     ***/
    public String[] processCommand(ImapLine line) throws ImapException {
        line.getConnection().setState(ImapConnection.CONNECTION_AUTHENTICATED);
        
        // get userid
        String userid = line.getAString();
        if(userid==null) {
            throw new ImapException(line,"error parsing command (getting userid)");
        }

        // skip space after command
        if(line.skipSP(1)!=1) {
            throw new ImapException(line,"error parsing command (skipping to password)");
        }

        // get password
        String password = line.getAString();
        if(userid==null) {
            throw new ImapException(line,"error parsing command (getting password)");
        }
        
        // skip space
        // WRNING this is "non-strict"
        line.skipSP(-1);
        
        // skip lineend
        if(!line.skipCRLF()) {
            throw new ImapException(line,"error parsing command");
        }

        if(line.getConnection()==null) {
            LOGGER.log(Level.SEVERE, "no connection found while calling login");
            return new String[] {line.getTag()+" BAD server configuration error" };
        }
        
        if(line.getConnection().getAuth()==null) {
            LOGGER.log(Level.SEVERE, "no Authenticator found while calling login");
            return new String[] {line.getTag()+" BAD server configuration error" };
        }
        
        if(line.getConnection().getAuth().login(userid,password)) {
            return new String[] {line.getTag()+" OK LOGIN completed" };
        } else {
            return new String[] {line.getTag()+" NO bad username or password" };
        }
    }

    public static String[] getCapabilities() {
        return new String[] { "LOGIN" };
    }
    
    public String[] getCommandIdentifier() {
        return new String[] {"LOGIN"};
    }
    
}    
