package net.messagevortex.transport;

import net.messagevortex.RunningDaemon;

import javax.mail.internet.MimeMessage;

public interface Transport extends RunningDaemon,TransportSender {

    static void send(MimeMessage message) {
    }
}
