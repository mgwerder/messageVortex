package net.gwerder.java.mailvortex.imap;

import java.util.logging.Logger;
import net.gwerder.java.mailvortex.MailvortexLogger;
import java.util.logging.Level;
 
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Set;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLContext;
import java.net.Socket;
import java.nio.charset.Charset;


public class ImapConnection extends StoppableThread implements Comparable<ImapConnection> {

    private static final Logger LOGGER;
    static {
        LOGGER = MailvortexLogger.getLogger((new Throwable()).getStackTrace()[0].getClassName());

    }
    private int timeout = defaultTimeout;
    
    public static final int CONNECTION_NOT_AUTHENTICATED = 1;
    public static final int CONNECTION_AUTHENTICATED     = 2;
    public static final int CONNECTION_SELECTED          = 3;
    
    /* holds sockets and streams of the connection (maintained by updateSocket())*/
    private Socket plainSocket=null;
    private SSLSocket sslSocket=null;
    private Socket currentSocket=null;
    private InputStream input=null;
    private OutputStream output=null;
    
    /* SSLcontext with enabled ciphers */
    private SSLContext context;
    
    /* Status of the connection (according to RFC */
    private int status=CONNECTION_NOT_AUTHENTICATED;
    
    /* list of supported ciphers */
    private Set<String> suppCiphers;
    
    /* wether the connection is encrypted or not */
    private boolean encrypted=false;
    
    /* Authentication authority for this connection */
    private ImapAuthenticationProxy authProxy = null;
    private Thread runner=null;

    private static int id=1;
    private static int defaultTimeout = 3 * 60 * 1000;
    
    /***
     * Creates a connection object without sockets (primarily for testing) 
     ***/
    protected ImapConnection() {
        runner=null;
    }
    
    /***
     * Creates an imapConnection
     ***/
    public ImapConnection(Socket sock,SSLContext context,Set<String> suppCiphers, boolean encrypted) {
        this();
        
        // store parameters in class
        this.plainSocket=sock;    
        this.context=context;
        this.suppCiphers=suppCiphers;
        this.encrypted=encrypted;
        
        // update socket information
        updateSocket();
        
        // create and start runner
        runner=new Thread(this);
        int a=id++;
        this.setID("AID-"+a);
        runner.start();
    }
    
    private void interruptedCatcher(Exception e) {
        assert false:"This Point should never be reached";
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
    
    public void setID(String id) {
        runner.setName(id);
    }
    
    /***
     * Set timeout of the connection.
     *
     * @param timeout   The new Timeout
     * @returns         Previous timeout
     ***/
    public int setTimeout(int timeout) {
        int ot=this.timeout;
        this.timeout=timeout;
        return ot;
    }
    
    /***
     * Get the authenticator of the connection.
     ***/
    public int getTimeout() { 
        return this.timeout; 
    }
    
    public static int setDefaultTimeout(int timeout) {
        int ot=defaultTimeout;
        defaultTimeout=timeout;
        return ot;
    }
    
    public static long getDefaultTimeout() { 
        return defaultTimeout; 
    }
     
    public int setState(int status) {
        if(status>3 || status<1) {
            return -status;    
        }    
        int old=status;
        this.status=status;
        return old;
    }
    
    private void updateSocket() {
        if(sslSocket!=null) {
            currentSocket=sslSocket;
        } else {
            currentSocket=plainSocket;
        }
        if(currentSocket!=null) {
            try{
                input=currentSocket.getInputStream();
                output=currentSocket.getOutputStream();
            } catch(IOException e) {
                 LOGGER.log(Level.SEVERE,"unable to get current IO streams",e);
            }
        }
    }
    
    public int compareTo(ImapConnection i) {
        return (new Integer(hashCode())).compareTo(new Integer(i.hashCode()));
    }
    
    public boolean equals(Object i) {
        return this==i;
    }
    
    public int hashCode() {
        return super.hashCode()+1;
    }
    
    /***
     * Closes all connections and terminate all subsequent runners.
     *
     * @FIX.ME Implementation of shudown in ImapConnection should be improved
     ***/
    public int shutdown() {
        // flag runner to shutdown
        shutdown=true;
        
        // wait for runner to terminate
        while(runner.isAlive()) {
            try {
                LOGGER.log(Level.INFO,"waiting for connection to shutdown ("+runner.getName()+"/"+runner.isAlive()+")");
                plainSocket.close();
                runner.join(1000);
            } catch(InterruptedException e) {
                 // discard this exception
                 interruptedCatcher(e);
            } catch(IOException e) {
                LOGGER.log(Level.WARNING,"IOException while shutting down input stream (may be normal)",e);
            }
        }    
        LOGGER.log(Level.WARNING,"connection shutdown done");
        return 0;
    }
    
    /***
     * start TLS handshake on existing connection.
     ***/
    private boolean startTLS() throws IOException {
        LOGGER.log(Level.INFO,"doing SSL handshake in serverConnection "+runner.getName());
        this.sslSocket = (SSLSocket) context.getSocketFactory().createSocket(plainSocket,plainSocket.getInetAddress().getHostAddress(),plainSocket.getPort(),false);
        String[] arr = suppCiphers.toArray(new String[0]);
        this.sslSocket.setUseClientMode(false);
        this.sslSocket.setEnabledCipherSuites(arr);
        this.sslSocket.setSoTimeout(plainSocket.getSoTimeout());
        LOGGER.log(Level.INFO,"start SSL handshake");
        this.sslSocket.startHandshake();
        LOGGER.log(Level.INFO,"SSL handshake done");
        updateSocket();
        return true;
    }
    
    public boolean isTLS() {
        return sslSocket!=null;
    }
    
    private String[] processCommand(String command,InputStream i) throws ImapException {
        // Extract first word (command) and fetch respective command object
        ImapLine il=null;
        try    {
            il=new ImapLine(this,command,i);
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
        LOGGER.log(Level.FINEST,"found command in connection "+this.getName()+".");
        String[] s=c.processCommand(il);
        
        LOGGER.log(Level.INFO,"got command \""+il.getTag()+" "+il.getCommand()+"\". Reply is \""+ImapLine.commandEncoder(s==null?"null":s[s.length-1])+"\".");
        return s;
    }
    
    private void processCommands() throws IOException {
        try{
            for(String s1:processCommand("",input)) {
                if(s1==null) {
                    shutdown=true;
                    LOGGER.log(Level.FINE,"server connection shutdown initated."    );
                } else {
                    output.write((s1).getBytes(Charset.defaultCharset()));
                    LOGGER.log(Level.INFO,"IMAP-> S: "+ImapLine.commandEncoder(s1));
                }    
            }
            output.flush();
            LOGGER.finest("command is processed");
        } catch(ImapException ie) {
            LOGGER.log(Level.WARNING,"error while parsing imap command",ie);
        }
    }
    
    public void run() {
        try{
            if(encrypted) {
                startTLS();
            }
            
            while(!shutdown) {
                currentSocket.setSoTimeout((int)timeout);
                processCommands();
            }
        } catch(java.net.SocketTimeoutException  e) {
            shutdown=true;
            LOGGER.log(Level.WARNING,"connection reached its timeout",e);
        } catch(IOException e) {
            LOGGER.log(Level.WARNING,"Error while IO with peer partner",e);
        }    
        try{
            input.close();
            output.close();
            if(sslSocket!=null) {
                sslSocket.close();
            }    
            plainSocket.close();
        } catch(Exception e2) {
            // all exceptions may be safely ignored
            interruptedCatcher(e2);
        }
        LOGGER.log(Level.INFO,"server connection closed");
        return;
    }
    
}
 
