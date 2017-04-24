package net.gwerder.java.messagevortex.imap;

import java.util.logging.Level;
import java.util.logging.Logger;
 
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
     * Process the login command.
     *
     * @param line The Imap line representing a login command
     * @return array of lines representing the server reply
     * @throws ImapException For all parsing errors
     */
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
        // WARNING this is "non-strict"
        line.skipSP(-1);
        
        // skip line end
        if(!line.skipCRLF()) {
            throw new ImapException(line,"error parsing command");
        }
        
        String[] reply=null;
        
        if(line.getConnection()==null) {
            LOGGER.log(Level.SEVERE, "no connection found while calling login");
            reply=new String[] {line.getTag()+" BAD server configuration error\r\n" };
        } else if(!line.getConnection().isTLS()) {
            LOGGER.log(Level.SEVERE, "no TLS but logging in with username and password");
            reply=new String[] {line.getTag()+" BAD authentication with username and password refused due current security strength\r\n" };
        } else if(line.getConnection().getAuth()==null) {
            LOGGER.log(Level.SEVERE, "no Authenticator or connection found while calling login");
            reply=new String[] {line.getTag()+" BAD server configuration error\r\n" };
        } else if(line.getConnection().getAuth().login(userid,password)) {
            line.getConnection().setImapState(ImapConnection.CONNECTION_AUTHENTICATED);
            reply=new String[] {line.getTag()+" OK LOGIN completed\r\n" };
        } else {
            reply=new String[] {line.getTag()+" NO bad username or password\r\n" };
        }
        return reply;
    }

    public String[] getCapabilities() {
        return new String[] { "LOGIN" };
    }
    
    public String[] getCommandIdentifier() {
        return new String[] {"LOGIN"};
    }
    
}    
