package net.gwerder.java.messagevortex.transport;

import net.gwerder.java.messagevortex.MessageVortexLogger;

import javax.net.ssl.SSLSocketFactory;
import java.io.*;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.logging.Level;

/**
 * Class offering primitives for opening a line and ascii based connection
 *
 * Created by Martin on 23.01.2018.
 */
public class LineSender {

    static final java.util.logging.Logger LOGGER;
    static {
        LOGGER = MessageVortexLogger.getLogger((new Throwable()).getStackTrace()[0].getClassName());
        MessageVortexLogger.setGlobalLogLevel( Level.ALL);
    }

    Object lock=new Object();
    Socket s=null;
    BufferedReader inStream=null;
    BufferedWriter outStream=null;
    String protocol=null;

    public void connect(InetSocketAddress addr,SecurityRequirement req ) throws IOException {
        synchronized( lock ) {
            if(s!=null) {
                s.close();
            }
            protocol="SMTP";
            s = new Socket(addr.getHostString(), addr.getPort());
            if(req==SecurityRequirement.SSLTLS || req==SecurityRequirement.UNTRUSTED_SSLTLS) {
                // open encrypted tcp connect
                starttls();
            }
            inStream  = new BufferedReader( new InputStreamReader( s.getInputStream(), StandardCharsets.UTF_8 ));
            outStream = new BufferedWriter( new OutputStreamWriter( s.getOutputStream(), StandardCharsets.UTF_8 ));
        }
    }

    public void starttls() throws IOException {
        synchronized (lock) {
            s = ((SSLSocketFactory) SSLSocketFactory.getDefault()).createSocket(s,
                    s.getInetAddress().getHostAddress(),
                    s.getPort(),
                    true);
            inStream  = new BufferedReader(new InputStreamReader(s.getInputStream(), StandardCharsets.UTF_8));
            outStream = new BufferedWriter(new OutputStreamWriter(s.getOutputStream(), StandardCharsets.UTF_8));
        }
        protocol="SMTPS";
    }

    public String read() throws IOException {
        synchronized( lock ) {
            String txt=inStream.readLine();
            LOGGER.log(Level.FINER,protocol+" S:"+txt);
            return txt;
        }
    }

    public void write(String txt) throws IOException  {
        synchronized( lock ) {
            outStream.write( txt );
            LOGGER.log(Level.FINER,protocol+" C:"+txt);
            outStream.flush();
        }
    }

    public void close() throws IOException {
        synchronized( lock ) {
            if (s != null) {
                if (!s.isClosed()) {
                    s.close();
                }
                inStream = null;
                outStream = null;
            }
        }
    }

}
