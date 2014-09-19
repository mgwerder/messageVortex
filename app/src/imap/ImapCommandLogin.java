package net.gwerder.java.mailvortex.imap;
 
import java.util.logging.Logger;
import java.util.logging.Level;
 
public class ImapCommandLogin extends ImapCommand {

    private static final Logger LOGGER;
    static {
        LOGGER = Logger.getLogger((new Throwable()).getStackTrace()[0].getClassName());
    }

    public void init() {
        ImapCommand.registerCommand(this);
    }
    
    private String getAuthToken(ImapLine line) throws ImapException {
        String userid = line.getAString();
        if(userid==null) {
            throw new ImapException(line,"error parsing command (getting userid)");
        }
        return userid;
    }
    
    /***
     * @fix.me add capabilities to successful login
     ***/
    public String[] processCommand(ImapLine line) throws ImapException {
        
        // get userid
        String userid=getAuthToken(line);
        
        // skip space after command
        if(line.skipSP(1)!=1) {
            throw new ImapException(line,"error parsing command (skipping to password)");
        }

        // get password
        String password = getAuthToken(line);

        // skip space
        // WRNING this is "non-strict"
        line.skipSP(-1);
        
        // skip line end
        if(!line.skipCRLF()) {
            throw new ImapException(line,"error parsing command");
        }
        
        // test if there is an associated authenticator in connection
        if(line.getConnection()==null || line.getConnection().getAuth()==null) {
            LOGGER.log(Level.SEVERE, "no Authenticator or connection found while calling login");
            return new String[] {line.getTag()+" BAD server configuration error\r\n" };
        }
        
        if(line.getConnection().getAuth().login(userid,password)) {
            line.getConnection().setState(ImapConnection.CONNECTION_AUTHENTICATED);
            return new String[] {line.getTag()+" OK LOGIN completed\r\n" };
        } else {
            return new String[] {line.getTag()+" NO bad username or password\r\n" };
        }
    }

    
    public String[] getCapabilities() {
        return new String[] { "LOGIN" };
    }
    
    public String[] getCommandIdentifier() {
        return new String[] {"LOGIN"};
    }
    
}    
