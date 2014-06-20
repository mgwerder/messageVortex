package net.gwerder.java.mailvortex.imap;
 
public class ImapCommandLogin extends ImapCommand {

    static void init() {
        ImapCommand.registerCommand(new ImapCommandLogin());
    }
    
    /***
     * @fix.me add capabilities to successful login
     ***/
    public String[] processCommand(ImapLine line) throws ImapException {
        line.getConnection().setState(ImapConnection.CONNECTION_AUTHENTICATED);
        
        // skip space after command
        if(line.skipSP(1)!=1) {
            throw new ImapException(line,"error parsing command");
        }

        // get userid
        String userid = line.getAString();

        // skip space after command
        if(line.skipSP(1)!=1) {
            throw new ImapException(line,"error parsing command");
        }

        // get password
        String password = line.getAString();
        
        // skip space
        if(line.skipSP(1)!=1) {
            throw new ImapException(line,"error parsing command");
        }
        
        // skip lineend
        if(!line.skipCRLF()) {
            throw new ImapException(line,"error parsing command");
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
