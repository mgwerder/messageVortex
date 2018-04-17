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
import net.gwerder.java.messagevortex.transport.*;
import org.bouncycastle.util.encoders.Base64;

import javax.security.auth.callback.CallbackHandler;
import javax.security.sasl.Sasl;
import javax.security.sasl.SaslClient;
import javax.security.sasl.SaslException;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeoutException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ImapClient extends ClientConnection {

    private static final String REGEXP_IMAP_OK ="\\s+OK.*";
    private static final String REGEXP_IMAP_BAD="\\s+BAD.*";

    private static final Logger LOGGER;

    static {
        LOGGER = MessageVortexLogger.getLogger((new Throwable()).getStackTrace()[0].getClassName());
        LOGGER.setLevel(Level.FINEST);

        java.security.Security.addProvider(new org.bouncycastle.jce.provider.BouncyCastleProvider());
    }


    private final  Object   sync                    = new Object();
    private final  Object   notifyThread            = new Object();

    private        String   currentCommand          = null;
    private        String[] currentCommandReply     = null;
    private        boolean  currentCommandCompleted = false;

    public ImapClient(InetSocketAddress addr, SecurityContext secContext ) throws IOException {
        super( addr, secContext );
        setProtocol("IMAP");
    }

    public void imapStartTLS() throws IOException {
        String tag = ImapLine.getNextTag();
        try {
            String[] ret = sendCommand(tag + " STARTTLS");
            // check if result was  OK
            if( ret!=null && ret.length >= 1 && ret[ ret.length -1 ]!=null && ret[ ret.length -1 ].startsWith( tag+" OK" )) {
                startTLS();
            }
        }catch(TimeoutException te) {
            throw new IOException( "Timeout while communicating with server",te );
        }
    }

    public boolean authenticate( Credentials creds ){
        // FIXME this dummy always selects plain
        // get capabilities
        // get best auth type supported
        return authenticate( creds, SaslMechanisms.DIGEST_MD5 );
    }

    public boolean authenticate( Credentials creds, SaslMechanisms mech ) {
        CallbackHandler clientHandler = new SaslClientCallbackHandler( creds );

        Map<String, String> props = new HashMap<>();
        if( ! isTLS() ) {
            props.put(Sasl.POLICY_NOPLAINTEXT, "true");
        }

        try {
            String tag = ImapLine.getNextTag();
            writeln( tag+" AUTHENTICATE "+mech.toString() );
            SaslClient sc = Sasl.createSaslClient(new String[]{ mech.toString() }, "username", "IMAP", "FQHN", props, clientHandler);
            if( sc==null ) {
                LOGGER.log( Level.WARNING, "requested unsupported sasl mech ("+mech+")" );
                return false;
            }
            String saslchallenge = readln();
            if( saslchallenge==null || !saslchallenge.startsWith( "+ " ) ) {
                // got bad challenge from server
                LOGGER.log( Level.WARNING, "Got a bad challenge from server ("+saslchallenge+")" );
                return false;
            } else if( saslchallenge!=null && saslchallenge.equals( "+ " ) ) {
                LOGGER.log( Level.INFO, "Got a empty challenge from server" );
            } else {
                LOGGER.log( Level.INFO, "Got a challenge from server ("+saslchallenge+")" );
            }
            byte[] c = new byte[0];
            if( saslchallenge.length()>2 ) {
                LOGGER.log( Level.INFO, "Got a challenge from server ("+saslchallenge.length()+" bytes)" );
                c=Base64.decode( saslchallenge.substring(2) );
            }
            byte[] saslReply = sc.evaluateChallenge(c);
            String reply = new String(Base64.encode( saslReply ));
            LOGGER.log( Level.INFO, "sending reply to server ("+saslReply.length+" bytes;"+reply+")" );
            writeln( reply );
            String imapReply=readln();
            return imapReply!=null && imapReply.toLowerCase().startsWith( tag.toLowerCase()+" ok" );
        } catch( IOException ioe ) {
            LOGGER.log( Level.WARNING, "exception while authenticating",ioe );
            return false;
        }
    }

    public String[] sendCommand(String command) throws TimeoutException {
        return sendCommand( command, getTimeout() );
    }

    public String[] sendCommand(String command,long millisTimeout) throws TimeoutException {
        synchronized( sync ) {
            currentCommand = command;
            LOGGER.log( Level.INFO, "sending \"" + ImapLine.commandEncoder(currentCommand) + "\" to server" );
            long start = System.currentTimeMillis();
            currentCommandCompleted = false;
            currentCommandReply = new String[0];
            synchronized( notifyThread ) {
                notifyThread.notifyAll();
            }
            try {
                while( !currentCommandCompleted && System.currentTimeMillis() < start + millisTimeout ) {
                    processLine( command, millisTimeout - ( System.currentTimeMillis() - start ) );
                    try{
                        sync.wait( 10 );
                    } catch( InterruptedException ie ) {
                        // may be safely ignored
                    }
                }
            } catch( IOException ioe ) {
                LOGGER.log( Level.WARNING, "got IO exception while processing command", ioe );
            }
            LOGGER.log( Level.FINEST, "wakeup succeeded" );
            if( ! currentCommandCompleted && System.currentTimeMillis() > start + millisTimeout ) {
                throw new TimeoutException( "Timeout reached while sending \"" + ImapLine.commandEncoder( command ) + "\"" );
            }
        }
        currentCommand = null;
        if( currentCommandReply == null ) {
            currentCommandReply = new String[0];
        } else {
            LOGGER.log( Level.INFO, "got \"" + ImapLine.commandEncoder( currentCommandReply[ currentCommandReply.length - 1 ] ) + "\" as reply from server (" + currentCommandReply.length + ")" );
        }
        return currentCommandReply;
    }

    private void interruptedCatcher(InterruptedException ie) {
        assert false:"This Point should never be reached ("+ie.toString()+")";
        Thread.currentThread().interrupt();
    }

    private void waitForWakeupRunner() {
        synchronized(notifyThread) {
            try{
                notifyThread.wait(100);
            } catch(InterruptedException e) {
                interruptedCatcher(e);
            }
        }
    }

    public void processLine( String line ) throws IOException  {
        processLine( line, getTimeout() );
    }

    public void processLine( String line,long timeout ) throws IOException  {
        currentCommand=line;
        LOGGER.log( Level.INFO, "IMAP C->S: " + ImapLine.commandEncoder( currentCommand ) );
        long start = System.currentTimeMillis();
        writeln( currentCommand, timeout );

        String tag = null;
        ImapLine il = null;
        try{
            il = new ImapLine(null,currentCommand);
            tag = il.getTag();
        } catch( ImapException ie ) {
            // intentionally ignored
            LOGGER.log( Level.INFO, "ImapParsing of \"" + ImapLine.commandEncoder( currentCommand ) + "\" (may be safelly ignored)", ie );
        }
        String lastReply = "";
        List<String> l = new ArrayList<>();
        LOGGER.log( Level.INFO, "waiting for incoming reply of command " + tag  + " (" + l.size() + ")" );
        while( ( !lastReply.matches( tag + REGEXP_IMAP_BAD + "|" + tag + REGEXP_IMAP_OK ) ) && System.currentTimeMillis()-start < timeout ) {
            String reply = readln( timeout-(System.currentTimeMillis()-start) );
            if(reply != null ) {
                l.add( reply );
                lastReply = reply;
                LOGGER.log( Level.INFO, "IMAP C<-S: " + ImapLine.commandEncoder( reply ) + " (" + l.size() + ")" );
                currentCommandReply = l.toArray( new String[ l.size() ] );
            }
        }
        currentCommandCompleted = lastReply.matches( tag + REGEXP_IMAP_OK + "|" + tag + REGEXP_IMAP_BAD );
        currentCommand = null;
        if( il != null && "logout".equalsIgnoreCase( il.getCommand() ) && lastReply.matches( tag + REGEXP_IMAP_OK ) ) {
            // Terminate connection on successful logout
            shutdown();
        }
        synchronized( sync ) {
            sync.notifyAll();
        }

        LOGGER.log( Level.FINEST, "command has been completely processed" );
    }

    private void runStep() throws IOException {
        LOGGER.log( Level.INFO, "Waiting for command to process" );startTLS();
        waitForWakeupRunner();
        if(currentCommand!=null && !"".equals(currentCommand)) {
            LOGGER.log( Level.INFO, "Processing command" );
            processLine( currentCommand );
        }
        LOGGER.log(Level.FINEST,"Client looping (shutdown=" + isShutdown() + ")");
    }

    public void run() {

        try {
            while(!isShutdown() ) {
                runStep();
            }
        } catch (Exception e) {
            LOGGER.log(Level.WARNING,"Uncaught exception in ImapClient",e);
            try {
                shutdown();
            } catch (IOException ioe ) {
                LOGGER.log( Level.WARNING,"Uncaught exception while shutting down", ioe );
            }
        } finally {
            try{
                shutdown();
            } catch(Exception e2) {
                LOGGER.log(Level.INFO,"socket close did fail when shutting down (may be safely ignored)",e2);
            }
        }
    }
}
