package net.gwerder.java.mailvortex.imap;

import java.util.logging.Logger;
import java.util.logging.Level;
 
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Set;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLContext;
import java.net.Socket;


public class ImapConnection extends StoppableThread implements Comparable<ImapConnection> {

    private static final Logger LOGGER;
    private long lastCommand = System.currentTimeMillis();
    private long timeout = defaultTimeout;
    
    static {
        LOGGER = Logger.getLogger((new Throwable()).getStackTrace()[0].getClassName());
    }
    
    public static final int CONNECTION_NOT_AUTHENTICATED = 1;
    public static final int CONNECTION_AUTHENTICATED     = 2;
    public static final int CONNECTION_SELECTED          = 3;
    
    
    private Socket plainSocket=null;
    private SSLSocket sslSocket=null;
    private Socket currentSocket=null;
    private SSLContext context;
    private int status=CONNECTION_NOT_AUTHENTICATED;
    private Set<String> suppCiphers;
    private boolean encrypted=false;
    private ImapAuthenticationProxy authProxy = null;
    private InputStream input=null;
    private OutputStream output=null;
    private Thread runner=null;

    private static long defaultTimeout = 3 * 60 * 1000;
    
    protected ImapConnection() {
        runner=null;
    }
    
    public ImapConnection(Socket sock,SSLContext context,Set<String> suppCiphers, boolean encrypted) {
        this.plainSocket=sock;    
        this.context=context;
        this.suppCiphers=suppCiphers;
        this.encrypted=encrypted;
        updateSocket();
        runner=new Thread(this);
        runner.start();
    }
    
    public ImapAuthenticationProxy setAuth(ImapAuthenticationProxy authProxy) {
        ImapAuthenticationProxy oldProxyAuth=getAuth();
        this.authProxy=authProxy;
        this.authProxy.setImapConnection(this);
        return oldProxyAuth;
    }
    
    private void interruptedCatcher(Exception e) {
        assert false:"This Point should never be reached";
    }
    
    public ImapAuthenticationProxy getAuth() {
        return this.authProxy;
    }
    
    public long setTimeout(long timeout) {
        long ot=this.timeout;
        this.timeout=timeout;
        return ot;
    }
    
    public long getTimeout() { 
        return this.timeout; 
    }
    
    public static long setDefaultTimeout(long timeout) {
        long ot=defaultTimeout;
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
                runner.join();
            } catch(InterruptedException e) {
                 // discard this exception
                 interruptedCatcher(e);
            }
        }    
        return 0;
    }
    
    /***
     * start TLS handshake on existing connection.
     ***/
    private boolean startTLS() throws IOException {
        this.sslSocket = (SSLSocket) context.getSocketFactory().createSocket(plainSocket,plainSocket.getInetAddress().getHostAddress(),plainSocket.getPort(),false);
        String[] arr = suppCiphers.toArray(new String[0]);
        this.sslSocket.setUseClientMode(false);
        this.sslSocket.setEnabledCipherSuites(arr);
        System.out.println("## Starting server side SSL");
        this.sslSocket.startHandshake();
        return false;
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
            return new String[] {ie.getTag()+" BAD "+ie.toString()};
        }
        
        ImapCommand c=ImapCommand.getCommand(il.getCommand());
        if(c==null) {
            throw new ImapException(il,"Command \""+il.getCommand()+"\" is not implemented");
        }
        
        String[] s=c.processCommand(il);
        
        return s;
    }
    
    public void run() {
        try{
            if(encrypted) {
                startTLS();
            }
            
            while(!shutdown) {
            
                String s="";
                
                while(input.available()>0 && !s.endsWith("\r\n")) {
                    int b=input.read();
                    s+=(char)b;
                }
                
                if(!"".equals(s)) {
                    LOGGER.finest("IMAP<- S: "+s);
                    try    {
                    
                        for(String s1:processCommand(s,input)) {
                            if(s1==null) {
                                shutdown=true;
                                LOGGER.finest("server connection shutdown initated "+shutdown);
                            } else {
                                output.write((s1+"\r\n").getBytes());
                                LOGGER.finest("IMAP-> S: "+s1);
                            }    
                        }
                        LOGGER.finest("command is processed");
                    } catch(ImapException ie) {
                        LOGGER.log(Level.WARNING,"error while parsing imap line \""+s+"\"",ie);
                    }
                
                    output.flush();
                    s="";
                } else {
                    
                    // no command is pending so lets check if we have a timeout
                    if(lastCommand+timeout<System.currentTimeMillis()) {
                        
                        // we have reached a timeout
                        shutdown=true;
                        
                    } else {
                        
                        // No Timeout and no command
                        // Let's just sleep a bit and then recheck
                        Thread.sleep(200); // remove afte
                        
                    }
                }    
            }
        } catch(IOException e) {
            LOGGER.log(Level.WARNING,"Error while IO with peer partner",e);
        } catch(InterruptedException e) {
            // ignore this exception
            interruptedCatcher(e);
        }    
        try{
            input.close();
            output.close();
            if(sslSocket!=null) {
                sslSocket.close();
            }    
            plainSocket.close();
        } catch(Exception e2) {
            // all exceptions may be safely ignored$
            interruptedCatcher(e2);
        }
        LOGGER.log(Level.FINEST,"## server connection closed");
    }
    
}
 
