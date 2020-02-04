package net.messagevortex.blender.generators;

import net.messagevortex.asn1.BlendingParameter;
import net.messagevortex.asn1.VortexMessage;
import net.messagevortex.blender.BlenderContent;

public interface BlenderGenerator {

  BlenderContent getBlenderContent(BlendingParameter parameter, VortexMessage msg);

}
