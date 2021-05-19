package net.messagevortex.transport;

import net.messagevortex.RunningDaemon;

/**
 * <p>Defines prerequisites for the transport layer.</p>
 *
 * This wrapper interface was created for consistency with the other layer definitions.
 */
public interface Transport extends RunningDaemon,TransportSender {

}
