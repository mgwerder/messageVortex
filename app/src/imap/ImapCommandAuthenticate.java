package net.gwerder.java.mailvortex.imap;
 
import java.util.logging.Logger;
import java.util.logging.Level;

import javax.security.auth.callback.CallbackHandler;
import java.util.Map;
import java.util.HashMap;
import javax.security.sasl.Sasl;
import javax.security.sasl.SaslServer;
import javax.security.sasl.SaslException;
 
public class ImapCommandAuthenticate extends ImapCommand {

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
        String mech=getAuthToken(line);
        
        // skip space
        // WRNING this is "non-strict"
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
        } else if(auth(mech,line)) { // line.getConnection().getAuth().login(userid,password)
            line.getConnection().setImapState(ImapConnection.CONNECTION_AUTHENTICATED);
            reply=new String[] {line.getTag()+" OK LOGIN completed\r\n" };
        } else {
            reply=new String[] {line.getTag()+" NO bad username or password\r\n" };
        }
        return reply;
    }
    
    private boolean auth(String mech,ImapLine line) {
        Map props=new HashMap<String,Object>();
        if(line.getConnection().isTLS()) {
            props.put("Sasl.POLICY_NOPLAINTEXT","false");
        }
        CallbackHandler cbh=new ImapCommandAuthenticateCallbackHandler();
        try{
            SaslServer ss=Sasl.createSaslServer(mech, "imap", "localhost", props, cbh);
        } catch(SaslException se) {
            LOGGER.log(Level.WARNING, "Got Exception",se);
        }
        return true;
    }

    
    public String[] getCapabilities() {
        return getCapabilities(null);
    }
    
    public String[] getCapabilities(ImapConnection ic) {
        if(ic==null || ic.getImapState()==ImapConnection.CONNECTION_NOT_AUTHENTICATED) {
            return new String[] { "AUTH=GSSAPI","AUTH=DIGEST-MD5","AUTH=CRAM-MD5","AUTH=PLAIN" };
        } else {
            return new String[0];
        }
    }
    
    public String[] getCommandIdentifier() {
        return new String[] {"AUTHENTICATE"};
    }
    
}    
