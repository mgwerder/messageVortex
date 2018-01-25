package net.gwerder.java.messagevortex.transport;

import javax.net.ssl.SSLContext;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.Level;

import static net.gwerder.java.messagevortex.transport.SecurityRequirement.STARTTLS;

/**
 * Created by martin.gwerder on 24.01.2018.
 */
public class SMTPReceiver extends LineReceiver {

    public SMTPReceiver(int port, SSLContext context, boolean encrypted) throws IOException {
        super(port,encrypted,new SMTPConnection().createConnection(null, context ));
    }

    public static void main(String[] args) throws Exception  {
        LOGGER.log(Level.INFO,"Setup receiver");
        SMTPReceiver receiver=new SMTPReceiver(587,null,false);
        LOGGER.log(Level.INFO,"Setup sender");
        SMTPSender send=new SMTPSender("SMTPSender_of_MessageVortex@gwerder.net", "localhost", 587, null );
        LOGGER.log(Level.INFO,"Sending message");
        try {
            SimpleDateFormat dt1 = new SimpleDateFormat("EEE, d MMM yyyy HH:mm:ss Z");
            String txt = "Date:" + dt1.format(new Date()) + CRLF + "From: SMTPSender_of_MessageVortex@gwerder.net" + CRLF + "To: Martin@gwerder.net" + CRLF + "Subject: Testmail" + CRLF + CRLF + "Testmail" + CRLF;
            send.sendMessage("martin@gwerder.net", new ByteArrayInputStream(txt.getBytes(StandardCharsets.UTF_8.name())));
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
        receiver.shutdown();
    }

}
