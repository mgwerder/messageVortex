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

import net.gwerder.java.messagevortex.MessageVortexLogger;
import net.gwerder.java.messagevortex.transport.SecurityContext;
import net.gwerder.java.messagevortex.transport.ServerConnection;
import net.gwerder.java.messagevortex.transport.StoppableThread;

import java.io.IOException;
import java.nio.channels.SocketChannel;
import java.util.logging.Level;
import java.util.logging.Logger;


public class ImapConnection extends ServerConnection implements Comparable<ImapConnection>, StoppableThread {

    public static final int CONNECTION_NOT_AUTHENTICATED = 1;
    public static final int CONNECTION_AUTHENTICATED = 2;
    public static final int CONNECTION_SELECTED = 3;
    private static final Logger LOGGER;
    private static int id = 1;

    static {
        LOGGER = MessageVortexLogger.getLogger((new Throwable()).getStackTrace()[0].getClassName());

    }

    /* Status of the connection (according to RFC */
    private int status=CONNECTION_NOT_AUTHENTICATED;

    /* Authentication authority for this connection */
    private ImapAuthenticationProxy authProxy = null;
    private Thread runner=null;

    /***
     * Creates an imapConnection
     ***/
    public ImapConnection(SocketChannel sock, SecurityContext secContext) throws IOException {
        super( sock, secContext );
    }

    public ImapAuthenticationProxy setAuth(ImapAuthenticationProxy authProxy) {
        ImapAuthenticationProxy oldProxyAuth=getAuth();
        this.authProxy=authProxy;
        if(authProxy!=null) {
            this.authProxy.setImapConnection(this);
        }
        return oldProxyAuth;
    }

    /***
     * Get the authenticator of the connection.
     ***/
    public ImapAuthenticationProxy getAuth() {
        return this.authProxy;
    }

    public void setId(String id) {
        if( runner != null ) {
            runner.setName(id);
        }
    }

    public int setImapState(int status) {
        if(status>3 || status<1) {
            return -status;
        }
        int old = this.status;
        this.status=status;
        return old;
    }

    public int getImapState() {
        return this.status;
    }

    @Override
    public int compareTo(ImapConnection i) {
        return Integer.compare( hashCode(), i.hashCode() );
    }

    @Override
    public boolean equals(Object i) {
        return this==i;
    }

    @Override
    public int hashCode() {
        return super.hashCode()+1;
    }

    private String[] processCommand( String command ) throws ImapException {
        // Extract first word (command) and fetch respective command object
        ImapLine il=null;
        try    {
            il=new ImapLine(this,command );
        } catch(ImapBlankLineException ie) {
            // just ignore blank lines
            LOGGER.log(Level.INFO,"got a blank line as command",ie);
            return new String[0];
        } catch(ImapException ie) {
            // If line violates the form <tag> <command> refuse processing
            LOGGER.log(Level.WARNING,"got invalid line",ie);
            return new String[] {"* BAD "+ie.toString()};
        }

        LOGGER.log(Level.INFO,"got command \""+il.getTag()+" "+il.getCommand()+"\".");
        ImapCommand c=ImapCommand.getCommand(il.getCommand());
        if(c==null) {
            throw new ImapException(il,"Command \""+il.getCommand()+"\" is not implemented");
        }
        LOGGER.log(Level.FINEST,"found command in connection "+Thread.currentThread().getName()+".");
        String[] s=c.processCommand(il);

        LOGGER.log(Level.INFO,"got command \""+il.getTag()+" "+il.getCommand()+"\". Reply is \""+ImapLine.commandEncoder(s==null?"null":s[s.length-1])+"\".");
        return s;
    }

    public void gotLine( String line ) {
        try {
            for(String s: processCommand( line )) {
                writeln(s);
            }
        } catch(IOException|ImapException e ) {
            LOGGER.log(Level.WARNING, "exception while sending reply",e);
        }
    }

}

