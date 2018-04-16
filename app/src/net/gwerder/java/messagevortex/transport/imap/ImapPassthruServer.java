package net.gwerder.java.messagevortex.transport.imap;

import net.gwerder.java.messagevortex.MessageVortexLogger;
import net.gwerder.java.messagevortex.MessageVortexStatus;
import net.gwerder.java.messagevortex.transport.Credentials;
import net.gwerder.java.messagevortex.transport.SecurityContext;
import net.gwerder.java.messagevortex.transport.SecurityRequirement;
import sun.misc.Signal;
import sun.misc.SignalHandler;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.text.ParseException;
import java.util.logging.Level;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.lang.System.exit;

/**
 * Created by Martin on 13.04.2018.
 */
public class ImapPassthruServer implements SignalHandler {

    private static final java.util.logging.Logger LOGGER;

    static {
        LOGGER = MessageVortexLogger.getLogger((new Throwable()).getStackTrace()[0].getClassName());
    }

    private static final String imapURL = "(?<protocol>imap[s]?)://(?:(?<username>[\\p{Alnum}\\-\\.]+):(?<password>[\\p{ASCII}&&[^@]]+)@)?(?<server>[\\p{Alnum}\\.\\-]+)(?::(?<port>[\\digit]{1,5}))?";

    private ImapServer localServer;
    private ImapClient remoteServer;

    public ImapPassthruServer(InetSocketAddress listeningAddress, SecurityContext context, Credentials listeningCredentials, InetSocketAddress forwardingServer, Credentials forwardingCredentials ) throws IOException {
        localServer = new ImapServer( listeningAddress, context );
        ImapAuthenticationDummyProxy authProxy = new ImapAuthenticationDummyProxy();
        authProxy.addCredentials( listeningCredentials );
        localServer.setAuth( authProxy );
        remoteServer = new ImapClient( forwardingServer, context );
        Signal.handle( new Signal( "INT" ), this );
    }

    public void handle(Signal sig) {

        if( "INT".equals( sig.getName() ) ) {
            LOGGER.log(Level.INFO, "Received SIGINT signal. Will teardown.");

            try {
                shutdown();
            } catch( IOException ioe ) {
                LOGGER.log( Level.WARNING, "caught exception while shutting down", ioe );
            }

            // Force exit anyway
            System.exit(0);
        } else {
            LOGGER.log(Level.WARNING, "Received unthandöed signal SIG" + sig.getName() + ". IGNORING");
        }
    }

    public void shutdown() throws IOException {
        remoteServer.shutdown();
        localServer.shutdown();
    }

    public static void main( String[] args ) throws Exception {
        if( args.length != 2 ) {
            System.err.println("usage: java -jar messageVortex.jar net.gwerder.java.messagevortex.transport.imap.ImapPassthruServer imap(s)://<accepted_username>:<accepted_password>@<local_interface>:<port> imap(s)://<fetch_username>:<fetch_password>@<sever>:<port>");
            exit(100);
        }

        MessageVortexStatus.displayMessage( null, "IMAP passthru Sserver starting as standalone" );

        InetSocketAddress listener = getSocketAdressFromURL( args[0] );
        Credentials creds = new Credentials( getUsernameFromURL( args[0] ), getPasswordFromURL( args[0] ) );
        ImapPassthruServer s = new ImapPassthruServer( listener, new SecurityContext(SecurityRequirement.STARTTLS ), creds, getSocketAdressFromURL( args[1] ), null );
        MessageVortexStatus.displayMessage( null, "Passthru Server started as standalone" );
    }

    public static String getUsernameFromURL( String url ) throws ParseException {
        if ( url == null ) {
            throw new NullPointerException( "Address may not be null" );
        }
        Pattern p = Pattern.compile( imapURL );
        Matcher m = p.matcher( url );
        if ( ! m.matches() ) {
            throw new ParseException( "Unable to parse imap URL \""+url+"\"", -1 );
        }
        return m.group( "username" );
    }

    public static String getPasswordFromURL( String url ) throws ParseException {
        if ( url == null ) {
            throw new NullPointerException( "Address may not be null" );
        }
        Pattern p = Pattern.compile( imapURL );
        Matcher m = p.matcher( url );
        if ( ! m.matches() ) {
            throw new ParseException( "Unable to parse imap URL \""+url+"\"", -1 );
        }
        return m.group( "password" );
    }

    public static String getProtocolFromURL( String url ) throws ParseException {
        if ( url == null ) {
            throw new NullPointerException( "Address may not be null" );
        }
        Pattern p = Pattern.compile( imapURL );
        Matcher m = p.matcher( url );
        if ( ! m.matches() ) {
            throw new ParseException( "Unable to parse imap URL \""+url+"\"", -1 );
        }
        return m.group( "protocol" );
    }

    public static int getPortFromURL( String url ) throws ParseException {
        if ( url == null ) {
            throw new NullPointerException( "Address may not be null" );
        }
        Pattern p = Pattern.compile( imapURL );
        Matcher m = p.matcher( url );
        if ( ! m.matches() ) {
            throw new ParseException( "Unable to parse imap URL \""+url+"\"", -1 );
        }
        return m.group( "port" ) == null ? -1 : Integer.parseInt( m.group( "port" ) ) ;
    }

    public static InetSocketAddress getSocketAdressFromURL( String url ) throws ParseException {
        if ( url == null ) {
            throw new NullPointerException( "Address may not be null" );
        }
        Pattern p = Pattern.compile( imapURL );
        Matcher m = p.matcher( url );
        if ( ! m.matches() ) {
            throw new ParseException( "Unable to parse imap URL \""+url+"\"", -1 );
        }
        String host = m.group( "server" );
        int port;
        if( m.group( "port" ) != null ) {
            port = Integer.parseInt( m.group( "port" ) );
        } else {
            port = "imaps".equals( m.group( "protocol" ) ) ? 993 : 143;
        }
        return new InetSocketAddress( host, port );
    }
}
