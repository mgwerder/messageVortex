package net.gwerder.java.messagevortex.transport;

import net.gwerder.java.messagevortex.Config;
import org.bouncycastle.util.encoders.Base64;

import javax.net.ssl.SSLContext;
import java.io.*;
import java.net.InetAddress;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.logging.Level;

/**
 * Created by martin.gwerder on 24.01.2018.
 */
public class SMTPConnection extends LineConnection {

    @Override
    public LineConnection createConnection(Socket s, SSLContext context) throws IOException {
        return new SMTPConnection(s, context);
    }

    public SMTPConnection() {}

    private SMTPConnection(Socket s, SSLContext context) throws IOException {
        super(s,context);
    }

    @Override
    public void run() {
        try {
            write("220 " + InetAddress.getLocalHost().getHostName() + " ESMTP"+CRLF);
        } catch(UnknownHostException uhe) {
            LOGGER.log(Level.SEVERE, "Unable to determine local hostname",uhe);
        } catch(IOException ioe) {
            LOGGER.log(Level.SEVERE, "Unable to communicate",ioe);
        }
        String command=null;
        try {
            String envelopeFrom=null;
            String envelopeTo=null;
            while (command == null || ! "quit".equals(command!=null?command.toLowerCase():"")) {
                command = read();
                if(command.toLowerCase().startsWith("helo ")) {
                    write("250 Hi " + command.toLowerCase().substring(6) + " nice meeting you");
                }else if(command.toLowerCase().startsWith("ehlo ")) {
                    write("250-Hi "+command.toLowerCase().substring(6)+" nice meeting you");
                    write("250-ENHANCEDSTATUSCODES"+CRLF);
                    write("250 AUTH login"+CRLF);
                }else if(command.toLowerCase().equals("auth login")) {
                    write("334 "+new String(Base64.encode("Username:".getBytes()))+CRLF);
                    String username=new String(Base64.decode(read()));
                    Config.getDefault().getStringValue("smtp_incomming_username");
                    write("334 "+new String(Base64.encode("Password:".getBytes()))+CRLF);
                    String password=new String(Base64.decode(read()));
                    Config.getDefault().getStringValue("smtp_incomming_password");
                }else if(command.toLowerCase().startsWith("mail from")) {
                    envelopeFrom=command.substring(10).trim();
                    write("250 OK"+CRLF);
                    // FIXME reject if not apropriate
                }else if(command.toLowerCase().startsWith("rcpt to")) {
                    envelopeTo=command.substring(8).trim();
                    write("250 OK"+CRLF);
                    // FIXME reject if not apropriate
                }else if(command.toLowerCase().equals("data")) {
                    if( envelopeFrom!=null && envelopeTo!=null ) {
                        write("354 send the mail data, end with ."+CRLF);
                        String line = null;
                        StringBuilder sb = new StringBuilder();
                        while (line == null || ! ".".equals(line)) {
                            line = read();
                            sb.append(line + CRLF);
                        }
                        write("250 OK"+CRLF);
                    } else {
                        write("554 ERROR"+CRLF);
                    }
                }else if(command.toLowerCase().trim().equals("rset")) {
                    envelopeFrom=null;
                    envelopeTo=null;
                    write("250 OK"+CRLF);
                }else if(command.toLowerCase().trim().equals("noop")) {
                    write("250 OK"+CRLF);
                }else if(command.toLowerCase().trim().equals("quit")) {
                    write("221 bye"+CRLF);
                    command="quit";
                } else {
                    write("500 Syntax Error"+CRLF);
                }
            }
        }catch (SocketTimeoutException ste) {
            LOGGER.log(Level.WARNING, "Connection closed due to timeout", ste);
        }catch (IOException ioe) {
            if(!shutdown) {
                LOGGER.log(Level.WARNING, "error while communicating", ioe);
            }
        } finally {
            if(s.isConnected()) {
                try {
                    s.close();
                } catch (IOException ioe) {
                    // may be safely ignored
                }
            }
        }
    }
}
