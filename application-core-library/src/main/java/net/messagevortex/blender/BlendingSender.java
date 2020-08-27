package net.messagevortex.blender;

import java.io.IOException;
import net.messagevortex.RunningDaemon;
import net.messagevortex.asn1.BlendingSpec;
import net.messagevortex.asn1.VortexMessage;

/**
 * Created by Martin on 04.02.2018.
 */
public interface BlendingSender extends RunningDaemon {

  boolean blendMessage(BlendingSpec target, VortexMessage msg) throws IOException;


}
