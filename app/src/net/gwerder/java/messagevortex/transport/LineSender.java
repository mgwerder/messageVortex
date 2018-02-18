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

    static final String CRLF ="\r\n";

    static final java.util.logging.Logger LOGGER;
    static {
        LOGGER = MessageVortexLogger.getLogger((new Throwable()).getStackTrace()[0].getClassName());
        MessageVortexLogger.setGlobalLogLevel( Level.ALL);
    }

    Object lock=new Object();
    Socket s=null;
    BufferedReader inStream=null;
    BufferedWriter outStream=null;
    String protocol="unknown";

    boolean isTLS = false;

    public void connect(InetSocketAddress addr,SecurityRequirement req ) throws IOException {
        synchronized( lock ) {
            if(s!=null) {
                s.close();
            }
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
        if(!isTLS()) {
            synchronized (lock) {
                s = ((SSLSocketFactory) SSLSocketFactory.getDefault()).createSocket(s,
                        s.getInetAddress().getHostAddress(),
                        s.getPort(),
                        true);
                inStream = new BufferedReader(new InputStreamReader(s.getInputStream(), StandardCharsets.UTF_8));
                outStream = new BufferedWriter(new OutputStreamWriter(s.getOutputStream(), StandardCharsets.UTF_8));
            }
            protocol += "S";
        }
    }

    public boolean isTLS() {
        return isTLS;
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

    public void writeln(String txt) throws IOException {
        write(txt + CRLF );
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
