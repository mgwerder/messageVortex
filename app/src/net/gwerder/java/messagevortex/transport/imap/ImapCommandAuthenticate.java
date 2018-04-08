package net.gwerder.java.messagevortex.transport.imap;
// ************************************************************************************
// * Copyright (c) 2018 Martin Gwerder (martin@gwerder.net)
// *
// * Permission is hereby granted, free of charge, to any person obtaining a copy
// * of this software and associated documentation files (the "Software"), to deal
// * in the Software without restriction, including without limitation the rights
// * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
// * copies of the Software, and to permit persons to whom the Software is
// * furnished to do so, subject to the following conditions:
// *
// * The above copyright notice and this permission notice shall be included in all
// * copies or substantial portions of the Software.
// *
// * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
// * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
// * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
// * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
// * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
// * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
// * SOFTWARE.
// ************************************************************************************

import javax.security.auth.callback.CallbackHandler;
import javax.security.sasl.Sasl;
import javax.security.sasl.SaslException;
import javax.security.sasl.SaslServer;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import static net.gwerder.java.messagevortex.transport.imap.ImapConnectionState.CONNECTION_AUTHENTICATED;
import static net.gwerder.java.messagevortex.transport.imap.ImapConnectionState.CONNECTION_NOT_AUTHENTICATED;

/***
* Provides the the Authenticate command to the IMAP server.
*
* @author  Martin Gwerder
* @version 1.0
* @since   2014-12-09
***/
public class ImapCommandAuthenticate extends ImapCommand {

    private static final Logger LOGGER;
    static {
        LOGGER = Logger.getLogger((new Throwable()).getStackTrace()[0].getClassName());
    }

    /***
     * Initializer called by the static constructor of ImapCommand.
     ***/
    public void init() {
        ImapCommand.registerCommand(this);
    }

    /***
     * process authentication command.
     *
     * @param  line           The context of the line triggered
     * @throws ImapException  when problem processing the command
     * FIXME add capabilities to successful login
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
            LOGGER.log(Level.SEVERE, "no Authenticator or connection found while calling login (2)");
            reply=new String[] {line.getTag()+" BAD server configuration error\r\n" };
        } else if(auth(mech,line)) { // line.getConnection().getAuth().login(userid,password)
            line.getConnection().setImapState(CONNECTION_AUTHENTICATED);
            reply=new String[] {line.getTag()+" OK LOGIN completed\r\n" };
        } else {
            reply=new String[] {line.getTag()+" NO bad username or password\r\n" };
        }
        return reply;
    }

    /***
     * Returns the capabilities to be reported by the CAPABILITIES command.
     *
     * @return A list of capabilities
     ***/
    public String[] getCapabilities() {
        return getCapabilities(null);
    }

    /***
     * Returns the Identifier (IMAP command) which are processed by this class.
     *
     * @return A list of identifiers
     ***/
    public String[] getCommandIdentifier() {
        return new String[] {"AUTHENTICATE"};
    }

    private String getAuthToken(ImapLine line) throws ImapException {
        String userid = line.getAString();
        if(userid==null) {
            throw new ImapException(line,"error parsing command (getting userid)");
        }
        return userid;
    }

    private boolean auth(String mech,ImapLine line) {
        Map<String,Object> props=new HashMap<>();
        if(line.getConnection().isTLS()) {
            props.put("Sasl.POLICY_NOPLAINTEXT","false");
        }
        CallbackHandler cbh=new ImapCommandAuthenticateCallbackHandler();
        try{
            SaslServer ss=Sasl.createSaslServer(mech, "imap", "localhost", props, cbh);
        } catch(SaslException se) {
            LOGGER.log(Level.WARNING, "Got Exception",se);
            return false;
        }
        return true;
    }


    private String[] getCapabilities(ImapConnection ic) {
        if(ic==null || ic.getImapState()==CONNECTION_NOT_AUTHENTICATED) {
            return new String[] { "AUTH=GSSAPI","AUTH=DIGEST-MD5","AUTH=CRAM-MD5","AUTH=PLAIN" };
        } else {
            return new String[0];
        }
    }

}
