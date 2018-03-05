package net.gwerder.java.messagevortex.transport;
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

import net.gwerder.java.messagevortex.ExtendedSecureRandom;
import net.gwerder.java.messagevortex.MessageVortexLogger;

import javax.net.ServerSocketFactory;
import javax.net.SocketFactory;
import javax.net.ssl.*;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.security.GeneralSecurityException;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created by Martin on 23.01.2018.
 */
public class LineReceiver  implements Runnable, StoppableThread {

    static final String CRLF = "\r\n";

    private boolean shutdown=false;
    private long gcLastRun = 0;

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
    private Set<String> suppCiphers = new HashSet<>();
    private Thread runner=null;
    private ServerAuthenticator auth=null;
    private LineConnection connectionTemplate=null;
    TransportReceiver receiver=null;
    private String protocol = "unknown";
    private int timeout=-1;

    public LineReceiver(SSLContext context,TransportReceiver receiver) {
        this.receiver=receiver;
        this.context=context;
    }

    public LineReceiver( int port, LineConnection conn) throws IOException {
        this.port=port;
        this.connectionTemplate=conn;

        try{
            context=SSLContext.getInstance("TLS");
        } catch(GeneralSecurityException gse) {
            throw new IOException("error obtaining valid security context",gse);
        }

        // Determine valid ciphers
        String ks="keystore.jks";
        try{
            context.init(new X509KeyManager[] {new CustomKeyManager(ks,"changeme", "mykey3") }, new TrustManager[] {new AllTrustManager()}, ExtendedSecureRandom.getSecureRandom() );
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

    public String setName( String name) {
        String ret = getName();
        runner.setName( name );
        return ret;
    }

    public String getName() {
        return runner.getName();
    }

    public String setProtocol( String protocol ) {
        String ret = this.protocol;
        this.protocol=protocol;
        return ret;
    }

    public String getProtocol() {
        return protocol;
    }

    public SSLContext setSSLContext( SSLContext  context ) {
        SSLContext ret = getSSLContext();
        this.context=context;
        return ret;
    }

    public SSLContext getSSLContext() {
        return context;
    }
    public int getPort() {
        return serverSocket.getLocalPort();
    }


    public ServerAuthenticator setAuthenticator(ServerAuthenticator ap) {
        ServerAuthenticator old=auth;
        auth=ap;
        return old;
    }

    public TransportReceiver setReceiver( TransportReceiver receiver ) {
        TransportReceiver ret = this.receiver;
        this.receiver = receiver;
        return ret;
    }

    public TransportReceiver getReceiver() {
        return receiver;
    }

    public int setTimeout(int to) {
        int ret=timeout;
        this.timeout=to;
        return ret;
    }

    public int getTimeout() {
        return this.timeout;
    }

    private void shutdownRunner() {
        // initiate shutdown of runner
        shutdown=true;

        // wakeup runner if necesary
        try{
            if( connectionTemplate.isTLS() ) {
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
        // wait for termination
        for(LineConnection ic:conn) {
            ic.waitShutdown();
        }
    }

    public void shutdown() {
        LOGGER.log(Level.INFO,"Server runner shutdown");
        shutdownRunner();
        LOGGER.log(Level.INFO,"Server connections shutdown");
        shutdownConnections();
        LOGGER.log(Level.INFO,"Server shutdown done");
    }

    public boolean isShutdown() {
        return shutdown && runner.getState()== Thread.State.TERMINATED;
    }

    /***
     * Main server task (Do not call).
     *
     * This Task listens for new connections and forks them off as needed.
     ***/
    public void run() {
        Socket socket=null;
        int i=1;
        LOGGER.log(Level.INFO,"Server listener ready..." + serverSocket);
        try {
            while(!shutdown) {
                connectionCleanup();
                socket = serverSocket.accept();
                LOGGER.log(Level.INFO,"Got connection from "+ socket.getInetAddress().getHostName());
                LineConnection lc=null;
                try {
                    lc = connectionTemplate.createConnection(socket);
                    if(timeout>-1) {
                        lc.setTimeout(timeout);
                    }
                    lc.setAuthenticator(auth);
                    lc.setName(runner.getName() + "-CONNECT-" + i);
                    conn.add(lc);
                    lc.setProtocol(getProtocol());
                    lc.start();
                    i++;
                } catch (IOException ioe) {
                    LOGGER.log(Level.SEVERE,"Error exception on creating and running connection socket",ioe);
                    if( lc != null ) {
                        lc.shutdown();
                    }
                }
            }
            serverSocket.close();
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE,"Error exception on server socket",e);
            shutdown();
        }
    }

    /***
     * Garbage collector for list of open connections.
     */
    private void connectionCleanup() {
        if(System.currentTimeMillis()-gcLastRun > 30000 ) {
            gcLastRun=System.currentTimeMillis();
            List<LineConnection> removalList = new ArrayList<>();
            for(LineConnection lc:conn) {
                if(lc.isShutdown() ) {
                    removalList.add(lc);
                }
            }
            conn.removeAll(removalList);
        }
    }

}
