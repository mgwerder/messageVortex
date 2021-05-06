package net.messagevortex.blender.generators;

import net.messagevortex.asn1.BlendingParameter;
import net.messagevortex.asn1.VortexMessage;
import net.messagevortex.asn1.encryption.DumpType;
import net.messagevortex.blender.BlenderContent;

import java.io.IOException;


/**
 * An academic generator creating readable identifiable messages for scientific purposes.
 */
public class AcademicGenerator implements BlenderGenerator {
  
  /**
   * <p>create an ASN.1 representation as blending text containing all possible
   * data in unencrypted, readable form.</p>
   *
   * @param parameter the lending parameters to use
   * @param msg the message to encode
   * @return the blended message
   */
  @Override
  public BlenderContent getBlenderContent(BlendingParameter parameter, VortexMessage msg) {
    BlenderContent cont = new BlenderContent();
    try {
      cont.setText("The following content is blended below:\n"
          + msg.dumpValueNotation("", DumpType.ALL_UNENCRYPTED));
    } catch (IOException ioe) {
      cont.setText(ioe.getMessage());
    }
    cont.addAttachment(new byte[0]);
    return cont;
  }
}
