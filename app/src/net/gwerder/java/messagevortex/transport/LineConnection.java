package net.gwerder.java.messagevortex.transport;

import net.gwerder.java.messagevortex.MessageVortexLogger;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import java.io.*;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.logging.Level;

/**
 * Created by Martin on 23.01.2018.
 */
public abstract class LineConnection extends Thread {

    static String CRLF= "\r\n";

    static final java.util.logging.Logger LOGGER;
    static {
        LOGGER = MessageVortexLogger.getLogger((new Throwable()).getStackTrace()[0].getClassName());
        MessageVortexLogger.setGlobalLogLevel( Level.ALL);
    }

    private Object lock=new Object();

    boolean shutdown=false;
    long timeout = 90L*1000;
    Socket s=null;
    SSLContext context;
    ServerAuthenticator auth=null;
    BufferedReader inStream=null;
    BufferedWriter outStream=null;
    TransportReceiver receiver=null;

    LineConnection() {}

    public LineConnection createConnection(Socket s) throws IOException {
        throw new UnsupportedOperationException("createConnection must be overloaded");
    }


    LineConnection(SSLContext context,TransportReceiver receiver) throws IOException {
        this.context = context;
        this.receiver=receiver;
        setSocket(s);
    }

    void setSocket(Socket s) throws IOException {
        this.s=s;
        if(this.s!=null) {
            s.setSoTimeout(100);
            inStream = new BufferedReader(new InputStreamReader(s.getInputStream(), StandardCharsets.UTF_8));
            outStream = new BufferedWriter(new OutputStreamWriter(s.getOutputStream(), StandardCharsets.UTF_8));
        }
    }

    public void shutdown() {
        shutdown=true;
    }

    public ServerAuthenticator setAuthenticator(ServerAuthenticator auth) {
        ServerAuthenticator ret=this.auth;
        this.auth=auth;
        return ret;
    }

    public long getTimeout() {
        return timeout;
    }

    public long setTimeout( long timeout ) {
        long ret=this.timeout;
        this.timeout=timeout;
        return ret;
    }

    public void startssl() throws IOException {
        synchronized (lock) {
            s = ((SSLSocketFactory) SSLSocketFactory.getDefault()).createSocket(s,
                    s.getInetAddress().getHostAddress(),
                    s.getPort(),
                    true);
            s.setSoTimeout(100);
            inStream  = new BufferedReader(new InputStreamReader(s.getInputStream(), StandardCharsets.UTF_8));
            outStream = new BufferedWriter(new OutputStreamWriter(s.getOutputStream(), StandardCharsets.UTF_8));
            ((SSLSocket)(s)).setUseClientMode(false);
            ((SSLSocket)(s)).startHandshake();
        }
    }

    public String read() throws IOException {
        synchronized( lock ) {
            String txt=null;
            long start=new Date().getTime();
            while(new Date().getTime()<start+timeout && txt==null) {
                try {
                    txt = inStream.readLine();
                } catch (SocketTimeoutException to) {
                    txt=null;
                }
            }
            if(txt== null){
                throw new SocketTimeoutException();
            }
            LOGGER.log(Level.INFO,"C:"+txt);
            return txt;
        }
    }

    public void write(String txt) throws IOException  {
        synchronized( lock ) {
            outStream.write( txt );
            LOGGER.log(Level.INFO,"S:"+txt);
            outStream.flush();
        }
    }

}
