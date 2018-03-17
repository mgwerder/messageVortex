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

import net.gwerder.java.messagevortex.MessageVortexLogger;
import net.gwerder.java.messagevortex.transport.*;
import org.bouncycastle.util.encoders.Base64;

import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Scanner;
import java.util.logging.Level;

import static net.gwerder.java.messagevortex.transport.SecurityRequirement.STARTTLS;

/**
 * Created by Martin on 23.01.2018.
 */
public class SMTPSender extends SendingSocket implements TransportSender {

    private static final String CRLF = "\r\n";

    static final java.util.logging.Logger LOGGER;
    static {
        LOGGER = MessageVortexLogger.getLogger((new Throwable()).getStackTrace()[0].getClassName());
    }

    String server=null;
    int    port=587;
    Credentials credentials=null;
    String senderAddress;

    public SMTPSender( String senderAddress, String server,int port,Credentials creds, SecurityContext context ) throws IOException {
        super( new InetSocketAddress( server, port ), context );
        this.senderAddress=senderAddress;
        this.server=server;
        if(port>0) {
            this.port=port;
        }
        this.credentials=creds;
    }

    @Override
    public void sendMessage(String address, InputStream is) throws IOException {

        // connect to server
        // FIXME set SSLEngine first
        connect();

        // startTLS if required
        if( credentials!=null && ( credentials.getSecurityRequirement()==SecurityRequirement.SSLTLS || credentials.getSecurityRequirement()==SecurityRequirement.UNTRUSTED_SSLTLS ) ) startTLS();

        // reading server greeting
        String serverGreeting = read();
        if( ! serverGreeting.startsWith( "220 " ) ) {
            throw new IOException( "Unable to communicate with server  (Greeting was '" + serverGreeting +"' )");
        }

        // send ehlo
        write( "EHLO " + InetAddress.getLocalHost().getHostName() + CRLF );
        String[] ehloReply=getReply();
        if( ! ehloReply[ ehloReply.length - 1 ].startsWith( "250 " ) ) {
            throw new IOException( "Invalid EHLO reply  (Reply was '" + Arrays.toString(ehloReply).replaceAll(",","\n") +"' )");
        }

        // start tls (if required)
        if( credentials!=null && ( credentials.getSecurityRequirement()== STARTTLS || credentials.getSecurityRequirement()==SecurityRequirement.UNTRUSTED_STARTTLS ) ) {
            write( "STARTTLS" + CRLF );
            String reply=read();
            if( ! reply.startsWith( "220 " ) ) {
                throw new IOException( "Invalid STARTTLS reply  (Reply was '" + reply +"')" );
            }
            startTLS();
            write( "EHLO " + InetAddress.getLocalHost().getHostName() + CRLF );
            ehloReply=getReply();
            if( ! ehloReply[ ehloReply.length - 1 ].startsWith( "250 " ) ) {
                throw new IOException( "Invalid EHLO reply  (Reply was '" + Arrays.toString(ehloReply).replaceAll(",","\n") +"' )");
            }
        }

        // Log into system
        if( credentials!=null && credentials.getUsername()!=null ) {
            sendAuth();
        }

        String reply;

        // send envelope from
        write( "MAIL FROM: " + senderAddress + CRLF );
        reply=read();
        if( reply==null || ! reply.startsWith( "250 " ) ) {
            throw new IOException( "Invalid MAIL FROM reply  (Reply was '" + reply +"')" );
        }

        // send envelope to
        write( "RCPT TO: " + address + CRLF );
        reply=read();
        if( reply==null || ! reply.startsWith( "250 " ) ) {
            throw new IOException( "Invalid RCPT reply  (Reply was '" + reply +"')" );
        }

        // send data
        write( "DATA" + CRLF );
        reply=read();
        if( reply==null || ! reply.startsWith( "354 " ) ) {
            throw new IOException( "Invalid DATA reply  (Reply was '" + reply +"')" );
        }
        java.util.Scanner s = new Scanner( is, "UTF-8" ).useDelimiter("\\A");
        String txt = s.hasNext() ? s.next() : "";
        write( txt + CRLF + "." + CRLF );

        // get delivery confirmed or denied
        reply=read();
        if( reply==null || ! reply.startsWith( "250 " ) ) {
            throw new IOException( "Invalid EOD reply (Reply was '" + reply +"')" );
        } else {
            LOGGER.log(Level.INFO,"data sent: "+reply );
        }

        // close connection
        write( "QUIT" + CRLF );
        reply=read();
        if( reply==null || ! reply.startsWith( "221 " ) ) {
            throw new IOException( "Invalid QUIT reply  (Reply was '" + reply +"')" );
        }
        closeConnection();
    }

    private void sendAuth() throws IOException {
        write( "AUTH login" + CRLF );
        String reply=read();
        if( ! reply.startsWith( "334 " ) ) {
            throw new IOException( "Invalid AUTH[1] reply  (Reply was '" + reply.substring(0,4) + Arrays.toString(Base64.decode( reply.substring(4) )) + "')" );
        }
        write( new String( Base64.encode( credentials.getUsername().getBytes( StandardCharsets.UTF_8 ) ), StandardCharsets.UTF_8 )+CRLF);
        reply=read();
        if( ! reply.startsWith( "334 " ) ) {
            throw new IOException( "Invalid AUTH[2] reply  (Reply was '" + reply.substring( 0, 4 ) + Arrays.toString( Base64.decode( reply.substring(4) ) ) + "')" );
        }
        write( new String( Base64.encode( credentials.getPassword().getBytes() ), StandardCharsets.UTF_8 ) + CRLF );
        reply=read();
        if( ! reply.startsWith( "235 " ) ) {
            throw new IOException( "Invalid AUTH[3] reply  (Reply was '" + reply + "')" );
        } else {
            LOGGER.log( Level.INFO, "Login successful: " + reply );
        }
    }

    private void sendPlain() throws IOException {
        String txt = credentials.getUsername() + "\0" + credentials.getUsername() + "\0" + credentials.getPassword();
        txt = new String( Base64.encode( txt.getBytes( StandardCharsets.UTF_8 ) ), StandardCharsets.UTF_8 ) + CRLF;
        write( "AUTH plain " + txt + CRLF );
        String reply = read();
        if( ! reply.startsWith( "235 " ) ) {
            throw new IOException( "Invalid AUTH[1] reply  (Reply was '" + reply.substring( 0, 4 ) + Arrays.toString( Base64.decode( reply.substring( 4 ) ) ) + "')" );
        } else {
            LOGGER.log( Level.INFO, "Login successful: " + reply );
        }
    }

    private String[] getReply() throws IOException{
        ArrayList<String> replies=new ArrayList<>();
        String line=null;
        while( line == null || line.charAt(3) != ' ' ) {
            line = read();
            replies.add(line);
        }
        return replies.toArray( new String[ replies.size() ] );
    }

}
