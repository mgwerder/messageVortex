package net.gwerder.java.messagevortex.transport;

import net.gwerder.java.messagevortex.ExtendedSecureRandom;
import net.gwerder.java.messagevortex.MessageVortexLogger;
import net.gwerder.java.messagevortex.imap.AllTrustManager;
import net.gwerder.java.messagevortex.imap.CustomKeyManager;
import net.gwerder.java.messagevortex.imap.SocketDeblocker;

import javax.net.ServerSocketFactory;
import javax.net.SocketFactory;
import javax.net.ssl.*;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.security.GeneralSecurityException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created by Martin on 23.01.2018.
 */
public abstract class LineReceiver implements Runnable {

    private static final ExtendedSecureRandom esr=new ExtendedSecureRandom();

    static final String CRLF = "\r\n";

    private boolean shutdown=false;

    static final Logger LOGGER;
    static {
        LOGGER = MessageVortexLogger.getLogger((new Throwable()).getStackTrace()[0].getClassName());
    }

    static {
        java.security.Security.addProvider(new org.bouncycastle.jce.provider.BouncyCastleProvider());
    }

    private static int id = 1;

    SSLContext context=null;
    int port;
    ServerSocket serverSocket=null;
    List<LineConnection> conn=new Vector<>();
    boolean encrypted=false;
    private Set<String> suppCiphers = new HashSet<>();
    private Thread runner=null;
    private ServerAuthenticator auth=null;
    private LineConnection connectionTemplate=null;
    TransportReceiver receiver=null;

    public LineReceiver(SSLContext context,TransportReceiver receiver) {
        this.receiver=receiver;
        this.context=context;
    }


    LineReceiver( int port,boolean encrypted, LineConnection conn) throws IOException {
        this.port=port;
        this.encrypted=encrypted;
        this.connectionTemplate=conn;

        try{
            context=SSLContext.getInstance("TLS");
        } catch(GeneralSecurityException gse) {
            throw new IOException("error obtaining valid security context",gse);
        }

        // Determine valid ciphers
        String ks="keystore.jks";
        try{
            context.init(new X509KeyManager[] {new CustomKeyManager(ks,"changeme", "mykey3") }, new TrustManager[] {new AllTrustManager()}, esr.getSecureRandom() );
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
                serverSocket = SSLServerSocketFactory.getDefault().createServerSocket(0);
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
        runner=new Thread(this,"LineReceiverConnectionListener");
        runner.setName("AUTOIDSERVER-"+(id++));
        runner.start();
    }

    public int getPort() {
        return serverSocket.getLocalPort();
    }

    public ServerAuthenticator setAuthenticator(ServerAuthenticator ap) {
        ServerAuthenticator old=auth;
        auth=ap;
        return old;
    }

    private void shutdownRunner() {
        // initiate shutdown of runner
        shutdown=true;

        // wakeup runner if necesary
        try{
            if(encrypted) {
                (SSLSocketFactory.getDefault().createSocket("localhost",this.serverSocket.getLocalPort())).close();
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

                // Preserve
                Thread.currentThread().interrupt();
            }
        } while(!endshutdown);
    }

    private void shutdownConnections() {
        // close all connections
        for(LineConnection ic:conn) {
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
     * This Task listens for new connections and forks them off as needed.
     *
     * @to.do Garbage collector should clean up closed connections from time to time
     ***/
    public void run() {
        Socket socket=null;
        int i=1;
        LOGGER.log(Level.INFO,"Server listener ready..." + serverSocket);
        try {
            while(!shutdown) {
                // FIXME <- Insert garbage collector here (should only run from time to time)
                socket = serverSocket.accept();
                LOGGER.log(Level.INFO,"Got connection from "+ socket.getInetAddress().getHostName());
                LineConnection lc=null;
                lc=connectionTemplate.createConnection(socket);
                lc.setAuthenticator(auth);
                lc.setName(runner.getName()+"-CONNECT-"+i);
                conn.add(lc);
                lc.start();
                i++;
            }
            serverSocket.close();
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE,"Error exception on server socket",e);
        }
    }
}
