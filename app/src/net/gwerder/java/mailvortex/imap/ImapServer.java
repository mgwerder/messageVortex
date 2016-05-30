package net.gwerder.java.mailvortex.imap;

import net.gwerder.java.mailvortex.MailvortexLogger;

import javax.net.ServerSocketFactory;
import javax.net.SocketFactory;
import javax.net.ssl.*;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.security.GeneralSecurityException;
import java.security.SecureRandom;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.logging.Level;
import java.util.logging.Logger;


public class ImapServer extends StoppableThread  {
    
    private static final Logger LOGGER;
    
    static {
        LOGGER = MailvortexLogger.getLogger((new Throwable()).getStackTrace()[0].getClassName());
    }
    
    private Set<String>    suppCiphers=new HashSet<String>();

    int port;
    ServerSocket serverSocket=null;
    ConcurrentSkipListSet<ImapConnection> conn=new ConcurrentSkipListSet<ImapConnection>();
    boolean encrypted=false;
    final SSLContext context;
    private Thread runner=null;
    private ImapAuthenticationProxy auth=null;
    
    private static int id=1;
            
    public ImapServer(boolean encrypted) throws IOException {
        this(encrypted?993:143,encrypted);
    }
    
    public ImapServer(final int port,boolean encrypted) throws IOException {
        java.security.Security.addProvider(new org.bouncycastle.jce.provider.BouncyCastleProvider());
        this.port=port;
        this.encrypted=encrypted;
        try{
          context=SSLContext.getInstance("TLS");
        } catch(GeneralSecurityException gse) {
          throw new IOException("error obtaining valid security context",gse);
        }  

        setName("AUTOIDSERVER-"+(id++));
        
        // Determine valid cyphers
        String ks="keystore.jks";
        try{
          context.init(new X509KeyManager[] {new CustomKeyManager(ks,"changeme", "mykey3") }, new TrustManager[] {new AllTrustManager()}, new SecureRandom() );
        } catch(GeneralSecurityException gse) {
          throw new IOException("Error initializing security context for connection",gse);
        }
        SSLContext.setDefault(context);
        String[] arr=((SSLServerSocketFactory)SSLServerSocketFactory.getDefault()).getSupportedCipherSuites();
        LOGGER.log(Level.FINE,"Detecting supported cipher suites");
        for(int i=0; i<arr.length; i++) {
            boolean supported=true;
            serverSocket=null;
            try{ 
                serverSocket = context.getServerSocketFactory().getDefault().createServerSocket(0);
                ((SSLServerSocket)serverSocket).setEnabledCipherSuites(new String[] {arr[i]});
                SocketDeblocker t=new SocketDeblocker(serverSocket.getLocalPort(),30);
                t.start();
                SSLSocket s=(SSLSocket)serverSocket.accept();
                s.close();
                serverSocket.close();
                serverSocket=null;
                t.shutdown();
                LOGGER.log(Level.FINER,"Cipher suite \""+arr[i]+"\" seems to be supported");
            } catch(SSLException e) {
                LOGGER.log(Level.FINER,"Cipher suite \""+arr[i]+"\" seems to be unsupported",e);
                supported=false;
                try{
                    if(serverSocket!=null) {
                        serverSocket.close();
                    }    
                } catch(Exception e2) {
                    LOGGER.log(Level.FINEST,"cleanup failed (never mind)",e2);
                }
                serverSocket=null;
            }
            if(supported) {
                suppCiphers.add(arr[i]);
            }
        }
        
        // open socket
        this.serverSocket = ServerSocketFactory.getDefault().createServerSocket(this.port);
        this.port=serverSocket.getLocalPort();
        runner=new Thread(this,"ImapServerConnectionListener");
        runner.start();
    }
    
    public int getPort() {
        return serverSocket.getLocalPort();
    }
    
    public ImapAuthenticationProxy setAuth(ImapAuthenticationProxy ap) {
        ImapAuthenticationProxy old=auth;
        auth=ap;
        return old;
    }
    
    private void shutdownRunner() {
        // initiate shutdown of runner
        shutdown=true;
            
        // wakeup runner if necesary
        try{
            if(encrypted) {
                (context.getSocketFactory().getDefault().createSocket("localhost",this.serverSocket.getLocalPort())).close();
            } else {
                (SocketFactory.getDefault().createSocket("localhost",this.serverSocket.getLocalPort())).close();
            }    
        } catch(Exception e) {
            LOGGER.log(Level.WARNING,"Wakeup of listener failed (already dead?)",e);
        }
        
        // Shutdown runner task
        boolean endshutdown=false;
        do {
            try{
                runner.join();
                endshutdown=true;
            } catch(InterruptedException ie) {
                // reloop if exception is risen
            }
        } while(!endshutdown);    
    }
    
    private void shutdownConnections() {
        // close all connections
        for(ImapConnection ic:conn) {
            ic.shutdown();
        }
    }
    
    public int shutdown() {
        LOGGER.log(Level.INFO,"Server runner shutdown");    
        shutdownRunner();
        LOGGER.log(Level.INFO,"Server connections shutdown");    
        shutdownConnections();
        LOGGER.log(Level.INFO,"Server shutdown done");    
        return 0;
    }
    
    /***
     * Main server task (Do not call).
     *
     * This Task listens for new connections and forkes them off as needed.
     *
     * @to.do Garbage collector should clean up closed connections from time to time
     ***/
    public void run() {
        Socket socket=null; 
        int i=1;
        LOGGER.log(Level.INFO,"Server listener ready..." + serverSocket);    
        try {
            while(!shutdown) {
                // <- Insert garbage collector here (should only run from time to time)
                socket = serverSocket.accept();
                ImapConnection imc=null;
                imc=new ImapConnection(socket,context,suppCiphers,encrypted);
                imc.setAuth(auth);
                imc.setID(getName()+"-CONNECT-"+i);
                conn.add(imc);
                socket=null;
                i++;
            }
            serverSocket.close();
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE,"Error exception on server socket",e);
        }
    }
}


