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
import net.gwerder.java.messagevortex.transport.LineSender;
import net.gwerder.java.messagevortex.transport.SecurityRequirement;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeoutException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ImapClient extends LineSender {

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

    public ImapClient( InetSocketAddress addr, SecurityRequirement req ) throws IOException {
        connect( addr, req );
        setProtocol("IMAP");
    }

    /*
    private Socket startTLS(Socket sock) throws IOException,java.security.NoSuchAlgorithmException,java.security.KeyManagementException,java.security.KeyStoreException,java.security.cert.CertificateException {
        LOGGER.log(Level.INFO,"doing SSL handshake by client");
        java.security.Security.addProvider(new org.bouncycastle.jce.provider.BouncyCastleProvider());
        KeyStore trustStore = KeyStore.getInstance(KeyStore.getDefaultType());
        try(FileInputStream is=new FileInputStream("keystore.jks") ) {
            trustStore.load(is, "changeme".toCharArray());
        }
        TrustManagerFactory trustFactory =  TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
        trustFactory.init(trustStore);
        TrustManager[] trustManagers = trustFactory.getTrustManagers();
        SSLContext trustContext = SSLContext.getInstance("SSL");
        trustContext.init(null, trustManagers, null);
        SSLContext.setDefault(trustContext);
        LOGGER.log(Level.INFO,"Getting socket");
        SSLSocket sslSocket = (SSLSocket)((SSLSocketFactory)(SSLSocketFactory.getDefault())).createSocket(sock,sock.getInetAddress().getHostAddress(),sock.getPort(), false);
        sslSocket.setUseClientMode(true);
        LOGGER.log(Level.INFO,"Starting client side SSL");
        sslSocket.setSoTimeout( getTimeout() );
        sslSocket.startHandshake();
        LOGGER.log(Level.INFO,"CLientTLS Started");
        encrypted=true;
        LOGGER.log(Level.INFO,"SSL handshake by client done");
        return sslSocket;
    }
*/

    public String[] sendCommand(String command) throws TimeoutException {
        return sendCommand( command, getTimeout() );
    }

    public String[] sendCommand(String command,long millisTimeout) throws TimeoutException {
        synchronized(sync) {
            currentCommand=command;
            LOGGER.log(Level.INFO,"sending \""+ImapLine.commandEncoder(currentCommand)+"\" to server");
            long start = System.currentTimeMillis();
            currentCommandCompleted=false;
            synchronized(notifyThread) {
                notifyThread.notifyAll();
            }
            while( !currentCommandCompleted && System.currentTimeMillis() < start + millisTimeout ) {
                try {
                    processRunnerCommand();
                    // sync.wait(10);
                } catch(IOException e) {
                    LOGGER.log(Level.SEVERE,"this point should never be reached",e);
                    Thread.currentThread().interrupt();
                }
            }
            LOGGER.log( Level.FINEST, "wakeup succeeded" );
            if( ! currentCommandCompleted && System.currentTimeMillis() > start + millisTimeout ) {
                throw new TimeoutException( "Timeout reached while sending \"" + ImapLine.commandEncoder( command ) + "\"" );
            }
        }
        currentCommand=null;
        if(currentCommandReply==null) {
            currentCommandReply=new String[0];
        } else {
            LOGGER.log(Level.INFO,"got \""+ImapLine.commandEncoder(currentCommandReply[currentCommandReply.length-1])+"\" as reply from server");
        }
        return currentCommandReply;
    }

    /*
    private void terminateSocket() {
        try{
            synchronized(notifyThread) {
                notifyThread.notifyAll();
                if(socket!=null) {
                    socket.close();
                }
            }
        } catch(IOException ioe) {
            LOGGER.log(Level.INFO,"Error tearing down socket on client shutdown (may be safely ignored)",ioe);
        }
    }

    public void shutdown() {
        shutdown = true;
        boolean success = false;
        while (!success) {
            try {
                terminateSocket();
                runner.join();
                success = true;
            } catch (InterruptedException ie) {
                interruptedCatcher(ie);
            }
        }
    }

    public boolean isTerminated() {
        return runner.getState() == Thread.State.TERMINATED;
    }
*/
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

    private void processRunnerCommand() throws IOException  {
        LOGGER.log( Level.INFO, "IMAP C->S: " + ImapLine.commandEncoder( currentCommand ) );
        writeln( currentCommand );

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
        LOGGER.log( Level.INFO, "waiting for incoming reply of command " + tag );
        while( ( !lastReply.matches( tag + REGEXP_IMAP_BAD + "|" + tag + REGEXP_IMAP_OK ) ) ) {
            String reply = read();
            LOGGER.log( Level.INFO, "wakeup (remaining: " + System.currentTimeMillis() );
            lastReply = reply.substring( 0, reply.length() - 2 );
            l.add( lastReply );
            LOGGER.log( Level.INFO, "IMAP C<-S: " + ImapLine.commandEncoder( lastReply ) );
            currentCommandReply=l.toArray( new String[ l.size() ] );
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
        try{
            LOGGER.log( Level.INFO, "Waiting for command to process" );
            waitForWakeupRunner();
            if(currentCommand!=null && !"".equals(currentCommand)) {
                LOGGER.log( Level.INFO, "Processing command" );
                processRunnerCommand();
            }
        } catch(java.net.SocketException se) {
            LOGGER.log(Level.WARNING,"Connection closed by server",se);
            shutdown();
        }
        LOGGER.log(Level.FINEST,"Client looping (shutdown=" + isShutdown() + ")");
    }

    public void run() {

        try {
            while(!isShutdown() && !isClosed() ) {
                runStep();
            }
        } catch (Exception e) {
            LOGGER.log(Level.WARNING,"Uncaught exception in ImapClient",e);
            shutdown();
        } finally {
            try{
                shutdown();
            } catch(Exception e2) {
                LOGGER.log(Level.INFO,"socket close did fail when shutting down (may be safely ignored)",e2);
            }
        }
    }
}
