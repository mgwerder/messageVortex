package net.gwerder.java.messagevortex.routing.operation;

import net.gwerder.java.messagevortex.MessageVortex;

/**
 * Created by Martin on 04.02.2018.
 */
public interface RoutingSender {

  boolean sendMessage(String target, MessageVortex msg);

}
