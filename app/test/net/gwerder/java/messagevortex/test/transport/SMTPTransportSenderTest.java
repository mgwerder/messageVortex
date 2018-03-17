package net.gwerder.java.messagevortex.test.transport;

import net.gwerder.java.messagevortex.MessageVortexLogger;
import net.gwerder.java.messagevortex.transport.SecurityContext;
import net.gwerder.java.messagevortex.transport.SecurityRequirement;
import net.gwerder.java.messagevortex.transport.smtp.SMTPReceiver;
import net.gwerder.java.messagevortex.transport.smtp.SMTPSender;
import net.gwerder.java.messagevortex.transport.TransportReceiver;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Vector;
import java.util.logging.Level;

import static org.junit.Assert.assertTrue;

/**
 * Teastclass for sending and receiving SMTP based messages
 *
 * Created by Martin on 26.01.2018.
 */
@RunWith(JUnit4.class)
public class SMTPTransportSenderTest implements TransportReceiver {

    public static final String CRLF="\r\n";

    private List<InputStream> msgs=new Vector<>();
    private static final java.util.logging.Logger LOGGER;
    static {
        LOGGER = MessageVortexLogger.getLogger((new Throwable()).getStackTrace()[0].getClassName());
        MessageVortexLogger.setGlobalLogLevel( Level.ALL);
    }

    @Test
    public void basicSMTPTest() throws IOException  {
        LOGGER.log(Level.INFO,"Setup receiver");
        SMTPReceiver receiver=new SMTPReceiver( 0, new SecurityContext( SecurityRequirement.PLAIN ), this );
        LOGGER.log(Level.INFO,"Setup sender");
        SMTPSender send=new SMTPSender("SMTPSender_of_MessageVortex@gwerder.net", "localhost",0, null,null );
        LOGGER.log(Level.INFO,"Sending message");
        try {
            SimpleDateFormat dt1 = new SimpleDateFormat("EEE, d MMM yyyy HH:mm:ss Z");
            String txt = "Date:" + dt1.format(new Date()) + CRLF + "From: SMTPSender_of_MessageVortex@gwerder.net" + CRLF + "To: Martin@gwerder.net" + CRLF + "Subject: Testmail" + CRLF + CRLF + "Testmail" + CRLF;
            send.sendMessage("martin@gwerder.net", new ByteArrayInputStream(txt.getBytes(StandardCharsets.UTF_8.name())));
        } catch (IOException ioe) {
            LOGGER.log(Level.SEVERE, "got unexpected exception while sending message",ioe );
        }
        receiver.shutdown();
        assertTrue("Message not arrived (yet?)",msgs.size()==1);
    }


    @Override
    public boolean gotMessage(InputStream is) {
        synchronized(msgs) {
            msgs.add(is);
        }
        return true;
    }
}
