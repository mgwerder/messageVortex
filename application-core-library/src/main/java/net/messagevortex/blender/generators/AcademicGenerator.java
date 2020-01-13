package net.messagevortex.blender.generators;

import net.messagevortex.asn1.BlendingParameter;
import net.messagevortex.asn1.VortexMessage;
import net.messagevortex.asn1.encryption.DumpType;
import net.messagevortex.blender.BlenderContent;

import java.io.IOException;

public class AcademicGenerator implements BlenderGenerator {

    @Override
    public BlenderContent getBlenderContent(BlendingParameter parameter, VortexMessage msg) {
        BlenderContent cont = new BlenderContent();
        try {
            cont.setText("The following content is blended below:\n" + msg.dumpValueNotation("", DumpType.ALL_UNENCRYPTED));
        } catch(IOException ioe) {
            cont.setText(ioe.getMessage());
        }
        cont.addAttachment(new byte[0]);
        return cont;
    }
}
