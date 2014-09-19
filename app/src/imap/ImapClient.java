package net.gwerder.java.mailvortex.imap;

import net.gwerder.java.mailvortex.MailvortexLogger;
import java.util.logging.Logger;
import java.util.logging.Level;
  
import java.util.List;
import java.util.ArrayList;
import java.net.Socket;
import javax.net.ssl.SSLSocket;
import javax.net.SocketFactory;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import java.io.FileInputStream;
import javax.net.ssl.TrustManagerFactory;
import java.security.KeyStore;
import javax.net.ssl.X509TrustManager;
import java.io.IOException;
import java.util.concurrent.TimeoutException;

public class ImapClient implements Runnable {

    private static final Logger LOGGER;
    
    private static int ccount=0;
    
    static {
        LOGGER = MailvortexLogger.getLogger((new Throwable()).getStackTrace()[0].getClassName());
        LOGGER.setLevel(Level.FINEST);
    }
    
    /* set default timeout of thread to 30s */
    private static final int DEFAULT_TIMEOUT=30*1000;

    private String targetHost="localhost";
    private Object sync=new Object();
    private Object notifyThread=new Object();
    private int targetPort = 143;
    private boolean encrypted;
    private boolean shutdown=false;
    private String currentCommand=null;
    private String[] currentCommandReply=null;
    private boolean currentCommandCompleted=false;
    Socket socket=null;
    private Thread runner=null;
    private static long defaultTimeout=DEFAULT_TIMEOUT; 
    private long timeout=defaultTimeout;  
    private boolean terminated=false;
    
    public ImapClient(String targetHost,int targetPort,boolean encrypted) {
        this.targetHost=targetHost;
        this.targetPort=targetPort;
        this.encrypted=encrypted;
        
        // set up the client runner
        runner=new Thread(this,"ImapClient command processor");
        ccount++;
        runner.setName("Client-"+ccount);
        runner.setDaemon(true);
        runner.start();
    }
    
    private Socket startTLS(Socket sock) throws IOException,java.security.NoSuchAlgorithmException,java.security.KeyManagementException,java.security.KeyStoreException,java.security.cert.CertificateException {
        LOGGER.log(Level.INFO,"doing SSL handshake by client");
        java.security.Security.addProvider(new org.bouncycastle.jce.provider.BouncyCastleProvider());
        KeyStore trustStore = KeyStore.getInstance(KeyStore.getDefaultType());
        trustStore.load(new FileInputStream("keystore.jks"), "changeme".toCharArray());
        TrustManagerFactory trustFactory =  TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());    
        trustFactory.init(trustStore);
        TrustManager[] trustManagers = trustFactory.getTrustManagers();
        SSLContext trustContext = SSLContext.getInstance("SSL");
        trustContext.init(null, trustManagers, null);
        SSLContext.setDefault(trustContext);
        LOGGER.log(Level.INFO,"Getting socket");
        SSLSocket sslSocket = (SSLSocket)(((SSLSocketFactory)(trustContext.getSocketFactory().getDefault())).createSocket(sock,sock.getInetAddress().getHostAddress(),sock.getPort(), false));    
        sslSocket.setUseClientMode(true);    
        sslSocket.setSoTimeout(sock.getSoTimeout());
        LOGGER.log(Level.INFO,"Starting client side SSL");
        sslSocket.startHandshake();
        LOGGER.log(Level.INFO,"CLientTLS Started");
        encrypted=true;
        LOGGER.log(Level.INFO,"SSL handshake by client done");
        return sslSocket;
    }
    
    private void interruptedCatcher() {
        assert false:"This Point should never be reached";
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
     
    public String[] sendCommand(String command) throws TimeoutException { 
        return sendCommand(command,timeout); 
    }
    
    public String[] sendCommand(String command,long millisTimeout) throws TimeoutException {
        synchronized(sync) {
            currentCommand=command;
            LOGGER.log(Level.INFO,"sending \""+ImapLine.commandEncoder(currentCommand)+"\" to server");
            long start = System.currentTimeMillis();
            currentCommandCompleted=false;
            synchronized(notifyThread) {
                notifyThread.notify(); 
            }
            while(!currentCommandCompleted && System.currentTimeMillis()<start+millisTimeout) {
                try{
                    sync.wait(100);
                } catch(InterruptedException e) {
                    LOGGER.log(Level.SEVERE,"this point should never be reached",e);
                }
            }
            LOGGER.log(Level.FINEST,"wakeup succeeded");
            if(!currentCommandCompleted && System.currentTimeMillis()>=start+millisTimeout) {
                throw new TimeoutException("Timeout reached while sending \""+ImapLine.commandEncoder(command)+"\"");
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
    
    public boolean isTLS() {
        return encrypted;
    }
    
    private void terminateSocket() {
        try{
            synchronized(notifyThread) {
                notifyThread.notify(); 
                if(socket!=null) {
                    socket.close();
                }   
            }
        } catch(IOException ioe) {
            LOGGER.log(Level.INFO,"Error tearing down socket on client shutdown (may be safely ignored)",ioe);
        }        
    }

    public void shutdown() {
        shutdown=true;
        try {
            terminateSocket();
            runner.join();
        } catch(InterruptedException ie) {
            interruptedCatcher();
        }     
    }    
    
    public boolean isTerminated() {
        return terminated;
    }
    
    private void waitForWakeupRunner() {
        synchronized(notifyThread) {
            try{
                notifyThread.wait(100);
            } catch(InterruptedException e) {
                interruptedCatcher();
            } 
        }
    }
    
    private void processRunnerCommand() throws IOException  {
        final String REGEXP_IMAP_OK ="\\s+OK.*";
        final String REGEXP_IMAP_BAD="\\s+BAD.*";
        LOGGER.log(Level.FINEST,"IMAP-> C: "+ImapLine.commandEncoder(currentCommand));
        socket.getOutputStream().write((currentCommand+"\r\n").getBytes());
        socket.getOutputStream().flush();

        String tag=null;
        ImapLine il=null;
        try{
            il=new ImapLine(null,currentCommand);
            tag=il.getTag();
        } catch(ImapException ie) {
            // intentionally ignored
            LOGGER.log(Level.INFO,"ImapParsing of \""+ImapLine.commandEncoder(currentCommand)+"\" (may be safelly ignored)",ie);
        }
        String reply="";
        String lastReply="";
        List<String> l=new ArrayList<String>();
        int i=0;
        do{
            i=socket.getInputStream().read();
            if(i>=0) {
                reply+=(char)i;
            }    
            if(reply.endsWith("\r\n")) {
                l.add(reply);
                LOGGER.log(Level.FINEST,"IMAP<- C: "+ImapLine.commandEncoder(reply));
                currentCommandReply=l.toArray(new String[0]);
                lastReply=reply.substring(0,reply.length()-2);
                reply="";
            }
        } while(!lastReply.matches(tag+REGEXP_IMAP_BAD) && !lastReply.matches(tag+REGEXP_IMAP_OK ) && i>=0);
        currentCommandCompleted=lastReply.matches(tag+REGEXP_IMAP_BAD) || lastReply.matches(tag+REGEXP_IMAP_OK );
        currentCommand=null;
        if(il!=null && "logout".equalsIgnoreCase(il.getCommand()) && lastReply.matches(tag+REGEXP_IMAP_OK )) {
            // Terminate connection on successful logout
            shutdown=true;
        }    
        lastReply="";
        synchronized(sync) {
            sync.notify(); 
        }

        LOGGER.log(Level.FINEST,"command has been completely processed");
    }
    
    private void runStep() throws IOException {
        try{
            waitForWakeupRunner();
            if(currentCommand!=null && !"".equals(currentCommand)) {
                processRunnerCommand();
            }    
        } catch(java.net.SocketException se) {
            LOGGER.log(Level.WARNING,"Connection closed by server",se);
            shutdown=true;
            terminated=true;
        }                
        LOGGER.log(Level.FINEST,"Client looping (shutdown="+shutdown+"/socket.closed()="+(socket==null?"null":socket.isClosed())+")");
    }

    public void run() {

        try {
            // initialize socket
            socket = SocketFactory.getDefault().createSocket(targetHost,targetPort);
            if(encrypted) {
                socket=startTLS(socket);
            }
            
            // running socket
            while(!shutdown && !socket.isClosed() && !socket.isInputShutdown() && !socket.isOutputShutdown()) {
                runStep();
            }
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE,"Uncaught exception in ImapClient",e);
            terminated=true;
        } finally {    
            try{
                socket.close();
            } catch(Exception e2) {
                LOGGER.log(Level.INFO,"socket close did fail when shutting down (may be safelly ignored)",e2);
            }
        }
    }
}
