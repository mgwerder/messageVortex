package net.messagevortex.routing.operation;

import net.messagevortex.MessageVortex;

/**
 * Created by Martin on 04.02.2018.
 */
public interface RoutingSender {

  boolean sendMessage(String target, MessageVortex msg);

}
