package net.messagevortex.blender.generators;

import java.io.IOException;
import net.messagevortex.asn1.BlendingParameter;
import net.messagevortex.asn1.VortexMessage;
import net.messagevortex.blender.BlenderContent;

public interface BlenderGenerator {
  
  /**
   * <p>creates the blended message including the decoy text if needed.</p>
   *
   * @param parameter the parameters required for blending
   * @param msg       the message to be blended
   * @return the blended message
   * @throws IOException if blending fails
   */
  BlenderContent getBlenderContent(BlendingParameter parameter, VortexMessage msg)
          throws IOException;
  
}
