package net.gwerder.java.mailvortex.imap;

import java.util.logging.Logger;
import java.util.logging.Level;  
  
import java.io.IOException;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.Set;
import java.util.HashSet;
import java.net.Socket;
import java.net.ServerSocket;
import javax.net.SocketFactory;
import javax.net.ServerSocketFactory;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory; 
import javax.net.ssl.SSLServerSocket;
import javax.net.ssl.SSLServerSocketFactory;
import javax.net.ssl.SSLException;
import javax.net.ssl.X509TrustManager;
import javax.net.ssl.X509KeyManager;
import javax.net.ssl.TrustManager;
import javax.net.ssl.SSLContext;
import java.security.SecureRandom;
import java.util.concurrent.TimeoutException;


public class ImapServer extends StoppableThread  {
    
    private static final Logger LOGGER;
    static {
        LOGGER = Logger.getLogger((new Throwable()).getStackTrace()[0].getClassName());
    }
    private Set<String>    suppCiphers=new HashSet<String>();

    int port;
    ServerSocket serverSocket=null;
    ConcurrentSkipListSet<ImapConnection> conn=new ConcurrentSkipListSet<ImapConnection>();
    boolean encrypted=false;
    final SSLContext context=SSLContext.getInstance("TLS");
    private Thread runner=null;
            
    public ImapServer(boolean encrypted) throws java.security.NoSuchAlgorithmException,java.security.KeyManagementException,java.security.GeneralSecurityException,IOException {
        this(encrypted?993:143,encrypted);
    }
    
    public int getPort() {
        return serverSocket.getLocalPort();
    }
    
    public ImapServer(final int port,boolean encrypted) throws java.security.NoSuchAlgorithmException,java.security.KeyManagementException,java.security.GeneralSecurityException,IOException {
        java.security.Security.addProvider(new org.bouncycastle.jce.provider.BouncyCastleProvider());
        this.conn=conn;
        this.port=port;
        this.encrypted=encrypted;
        
        // Determine valid cyphers
        String ks="keystore.jks";
        context.init(new X509KeyManager[] {new CustomKeyManager(ks,"changeme", "mykey3") }, new TrustManager[] {new AllTrustManager()}, new SecureRandom() );
        SSLContext.setDefault(context);
        String[] arr=((SSLServerSocketFactory) context.getServerSocketFactory().getDefault()).getSupportedCipherSuites(); 
        for(int i=0; i<arr.length; i++) {
            boolean supported=true;
            serverSocket=null;;
            try{ 
                serverSocket = (SSLServerSocket) context.getServerSocketFactory().getDefault().createServerSocket(0);
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
                    serverSocket.close();
                } catch(Exception e2) {
                    LOGGER.log(Level.FINEST,"cleanup failed (never mind)",e2);
                };
                serverSocket=null;
            }
            if(supported) {
                suppCiphers.add(arr[i]);
            }
        }
        
        // open socket
        this.serverSocket = (ServerSocket)ServerSocketFactory.getDefault().createServerSocket(port);
        this.port=serverSocket.getLocalPort();
        LOGGER.log(Level.INFO,"Server listener ready..." + serverSocket);    
        runner=new Thread(this,"ImapServerConnectionListener");
        runner.start();
    }
    
    private void shutdownRunner() {
        // initiate shutdown of runner
        shutdown=true;
            
        // wakeup runner if necesary
        try{
            SocketFactory.getDefault().createSocket("localhost",this.serverSocket.getLocalPort());
        } catch(Exception e) {
            // Intentionally  ignored
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
        for(ImapConnection ic:conn) {
            boolean endshutdown=false;
            do {
                try{
                    ic.join();
                    endshutdown=true;
                } catch(InterruptedException ie) {
                    // reloop if exception is risen
                }
            } while(!endshutdown);    
        }
        
    }
    
    public int shutdown() {
        shutdownRunner();
        shutdownConnections();
        return 0;
    }
    
    public void run() {
        Socket socket=null; 
        try {
            while(!shutdown) {
                socket = serverSocket.accept();
                ImapConnection imc=null;
                imc=new ImapConnection(socket,context,suppCiphers,encrypted);
                conn.add(imc);
                socket=null;
            }
            serverSocket.close();
        } catch (IOException e) {
            if(socket!=null) {
                try{
                    socket.close();
                } catch(IOException e2) {
                    // intentionaly ignored
                } ;
            }    
            LOGGER.log(Level.SEVERE,"Error exception on server socket",e);
        }
    }
}


