package net.messagevortex.blender;

import net.messagevortex.RunningDaemon;
import net.messagevortex.asn1.BlendingSpec;
import net.messagevortex.asn1.VortexMessage;

/**
 * Created by Martin on 04.02.2018.
 */
public interface BlendingSender extends RunningDaemon {

  public boolean blendMessage(BlendingSpec target, VortexMessage msg);

}
