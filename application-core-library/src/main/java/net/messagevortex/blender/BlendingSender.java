package net.messagevortex.blender;

import net.messagevortex.MessageVortex;
import net.messagevortex.RunningDaemon;

/**
 * Created by Martin on 04.02.2018.
 */
public interface BlendingSender extends RunningDaemon {

  boolean sendMessage(String target, MessageVortex msg);

}
