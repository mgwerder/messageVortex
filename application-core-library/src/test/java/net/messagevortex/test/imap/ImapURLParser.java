package net.messagevortex.test.imap;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.net.InetSocketAddress;
import java.text.ParseException;
import java.util.logging.Level;
import net.messagevortex.ExtendedSecureRandom;
import net.messagevortex.MessageVortexLogger;
import net.messagevortex.transport.imap.ImapPassthruServer;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/**
 * Created by Martin on 13.04.2018.
 */
@RunWith(JUnit4.class)
public class ImapURLParser {

    private static final java.util.logging.Logger LOGGER;
    private static final ExtendedSecureRandom esr = new ExtendedSecureRandom();

    static {
        LOGGER = MessageVortexLogger.getLogger((new Throwable()).getStackTrace()[0].getClassName());
        MessageVortexLogger.setGlobalLogLevel(Level.ALL);
    }

    @Test
    public void parseLegalURL() {
        String[] s = new String[] {
                "imap://localhost",
                "imap://localhost:143",
                "imaps://localhost:143",
                "imap://m:m@localhost:143",
        };
        for( String st:s ) {
            try {
                assertTrue( "Error while checking " + st, new InetSocketAddress( "localhost", 143 ).equals( ImapPassthruServer.getSocketAddressFromUrl(st) ) );
            } catch( ParseException ioe ) {
                ioe.printStackTrace();
                fail( "unexpected exception raised" );
            }
        }
        s = new String[] {
                "imaps://localhost",
                "imap://localhost:993",
                "imaps://localhost:993",
                "imap://m:m@localhost:993",
        };
        for( String st:s ) {
            try {
                assertTrue( "Error while checking " + st, new InetSocketAddress( "localhost", 993 ).equals( ImapPassthruServer.getSocketAddressFromUrl(st) ) );
            } catch( ParseException ioe ) {
                ioe.printStackTrace();
                fail( "unexpected exception raised" );
            }
        }
    }

    @Test
    public void parseIllegalURL() {
        String[] s = new String[] {
                "",
                "imap:/localhost",
                "ima://localhost:localhost",
                "imap//localhost",
                "imap://@localhost",
                "imap://:@localhost",
                "imap://m:@localhost",
                "imap://:m@localhost",
                "imap://localhost_la",
                "imap://localhost:143l",
                "imaps://localhost:l143",
                "imap://m:m@localhost:",
        };
        for( String st:s ) {
            try {
                ImapPassthruServer.getSocketAddressFromUrl(st);
                fail( "unexpectedly no exception raised when testing "+st );
            } catch( ParseException ioe ) {
                // this is expected
            } catch( Exception e ) {
                e.printStackTrace();
                fail( "wrong exception raised" );
            }
        }
        try {
            ImapPassthruServer.getSocketAddressFromUrl( null );
            fail( "unexpectedly no exception raised" );
        } catch( NullPointerException npe ) {
            // this is expected
        } catch( Exception e ) {
            e.printStackTrace();
            fail( "wrong exception raised" );
        }
    }

}
