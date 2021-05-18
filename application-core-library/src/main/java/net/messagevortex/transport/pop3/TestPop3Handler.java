package net.messagevortex.transport.pop3;

import com.icegreen.greenmail.user.GreenMailUser;
import com.icegreen.greenmail.util.GreenMail;
import com.icegreen.greenmail.util.ServerSetup;
import net.messagevortex.Config;
import net.messagevortex.MessageVortexLogger;
import net.messagevortex.MessageVortexRepository;
import net.messagevortex.transport.Transport;
import net.messagevortex.transport.TransportReceiver;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import java.io.IOException;
import java.io.InputStream;
import java.util.logging.Level;
import java.util.logging.Logger;

/*
 * FIXME: This implementation uses a Greenmail POP3 server.
 * FIXME: It holds all data in memory and gets thus slower and slower.
 *
 * FIXME: still broken code (reverse blending not done)
 */

public class TestPop3Handler implements Transport {

    private static final Logger LOGGER;

    static {
        LOGGER = MessageVortexLogger.getLogger((new Throwable()).getStackTrace()[0].getClassName());
    }

    private final GreenMail server;
    private final TransportReceiver blender;
    private final GreenMailUser outUser;
    private final String section;


    /**
     * <p>Constructor starting a POP3 server from the named config section.</p>
     *
     * @param section the configuration section to be used
     * @throws IOException if server fails to start
     */
    public TestPop3Handler(String section) throws IOException {
        this.section = section;
        Config cfg = Config.getDefault();
        this.blender = MessageVortexRepository.getBlender("",
                cfg.getStringValue(section, "blender"));
        if (this.blender == null) {
            throw new IOException("unable to fetch appropriate blender");
        }
        server = new GreenMail(new ServerSetup[]{new ServerSetup(
                cfg.getNumericValue(section, "pop3_outgoing_port"),
                cfg.getStringValue(section, "pop3_outgoing_address"),
                ServerSetup.PROTOCOL_POP3)
        });
        outUser = server.setUser(
                cfg.getStringValue(section, "pop3_outgoing_user"),
                cfg.getStringValue(section, "pop3_outgoing_password"));
        startDaemon();
    }


    @Override
    public void sendMessage(String address, InputStream os) throws IOException {
        try {
            MimeMessage msg = new MimeMessage(null, os);
            outUser.deliver(msg);
            LOGGER.log(Level.INFO, "got message ready to fetch in transport layer [POP3] named \""
                    + section + "\"");
        } catch (MessagingException me) {
            throw new IOException("exception while creating MimeMessage", me);
        }
    }

    @Override
    public final void startDaemon() {
        server.start();
    }

    @Override
    public void stopDaemon() {
        server.stop();
    }

    @Override
    public void shutdownDaemon() {
        stopDaemon();
    }


}
