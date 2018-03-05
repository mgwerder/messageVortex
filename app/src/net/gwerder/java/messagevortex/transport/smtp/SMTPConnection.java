package net.gwerder.java.messagevortex.transport.smtp;
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

import net.gwerder.java.messagevortex.Config;
import net.gwerder.java.messagevortex.MessageVortexLogger;
import net.gwerder.java.messagevortex.transport.LineConnection;
import net.gwerder.java.messagevortex.transport.SecurityRequirement;
import net.gwerder.java.messagevortex.transport.TransportReceiver;
import org.bouncycastle.util.encoders.Base64;

import javax.net.ssl.SSLContext;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;
import java.util.logging.Level;

/**
 * Created by martin.gwerder on 24.01.2018.
 */
public class SMTPConnection extends LineConnection {

    static final java.util.logging.Logger LOGGER;
    static {
        LOGGER = MessageVortexLogger.getLogger((new Throwable()).getStackTrace()[0].getClassName());
    }

    public LineConnection createConnection(Socket s) throws IOException {
        LineConnection ret=new SMTPConnection( getSSLContext(), getReceiver(), getSecurityRequirement() );
        ret.setSocket(s);
        return ret;
    }

    public SMTPConnection( SSLContext context, TransportReceiver receiver, SecurityRequirement encrypted  ) throws IOException {
        super( context, receiver, encrypted );
    }

    @Override
    public void gotLine( String line ) {
        try {
            write("220 " + InetAddress.getLocalHost().getHostName() + " ESMTP" + CRLF );
        } catch(UnknownHostException uhe) {
            LOGGER.log(Level.SEVERE, "Unable to determine local hostname",uhe);
        } catch(IOException ioe) {
            LOGGER.log(Level.SEVERE, "Unable to communicate",ioe);
        }
        String command=line;
        try {
            String envelopeFrom=null;
            String envelopeTo=null;
            while ( command == null || ! "quit".equals( command.toLowerCase() ) ) {
                command = read( false );
                if(command.toLowerCase().startsWith("helo ")) {
                    write("250 Hi " + command.toLowerCase().substring(6) + " nice meeting you");
                }else if(command.toLowerCase().startsWith("ehlo ")) {
                    write("250-Hi "+command.toLowerCase().substring(6)+" nice meeting you");
                    write("250-ENHANCEDSTATUSCODES" + CRLF );
                    write("250 AUTH login" + CRLF );
                }else if( "auth login".equals( command.toLowerCase() ) ) {
                    write("334 "+new String(Base64.encode("Username:".getBytes( StandardCharsets.UTF_8 )))+CRLF);
                    String username=new String(Base64.decode(read( false )));
                    Config.getDefault().getStringValue("smtp_incomming_username");
                    write("334 "+new String(Base64.encode("Password:".getBytes(StandardCharsets.UTF_8)))+CRLF);
                    String password=new String(Base64.decode(read( false )));
                    Config.getDefault().getStringValue("smtp_incomming_password");
                }else if(command.toLowerCase().startsWith("mail from")) {
                    envelopeFrom=command.substring(10).trim();
                    write("250 OK"+CRLF);
                    // FIXME reject if not apropriate
                }else if(command.toLowerCase().startsWith("rcpt to")) {
                    envelopeTo=command.substring(8).trim();
                    write("250 OK"+CRLF);
                    // FIXME reject if not apropriate
                }else if( "data".equals( command.toLowerCase() ) ) {
                    if( envelopeFrom!=null && envelopeTo!=null ) {
                        write("354 send the mail data, end with ."+CRLF);
                        String l = null;
                        StringBuilder sb = new StringBuilder();
                        while ( l == null || ! ".".equals(l)) {
                            if(line!=null) {
                                sb.append(l + CRLF);
                            }
                            l = read(false );
                        }
                        if(getReceiver()!=null) {
                            LOGGER.log(Level.INFO, "Message passed to blender layer");
                            getReceiver().gotMessage(new ByteArrayInputStream(sb.toString().getBytes()));
                        } else {
                            LOGGER.log(Level.WARNING, "blender layer unknown ... message discarded");
                        }
                        write("250 OK"+CRLF);
                    } else {
                        write("554 ERROR"+CRLF);
                    }
                }else if( "rset".equals( command.toLowerCase().trim() ) ) {
                    envelopeFrom=null;
                    envelopeTo=null;
                    write("250 OK"+CRLF);
                }else if( "noop".equals( command.toLowerCase().trim() ) ) {
                    write("250 OK"+CRLF);
                }else if( "quit".equals( command.toLowerCase().trim() ) ) {
                    write("221 bye"+CRLF);
                    command="quit";
                } else {
                    write("500 Syntax Error"+CRLF);
                }
            }
        }catch (SocketTimeoutException ste) {
            LOGGER.log(Level.WARNING, "Connection closed due to timeout", ste);
        }catch (IOException ioe) {
            if(!isShutdown()) {
                LOGGER.log(Level.WARNING, "error while communicating", ioe);
            }
        } finally {
            shutdown();
        }
    }
}
