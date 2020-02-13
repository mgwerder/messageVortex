package net.messagevortex.blender.generators;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.logging.Level;

import net.messagevortex.ExtendedSecureRandom;
import net.messagevortex.MessageVortexLogger;
import net.messagevortex.asn1.BlendingParameter;
import net.messagevortex.asn1.VortexMessage;
import net.messagevortex.blender.BlenderContent;

public class GenericGenerator implements BlenderGenerator {

  private static final java.util.logging.Logger LOGGER;

  static {
    LOGGER = MessageVortexLogger.getLogger((new Throwable()).getStackTrace()[0].getClassName());
  }

  @Override
  public BlenderContent getBlenderContent(BlendingParameter parameter, VortexMessage msg) {
    BlenderContent cont = new BlenderContent();
    cont.setText("Hi\n\nHave you seen that one? Nice image!\n\nLove\nTeddy");
    File f = new File("images/F5Blender");
    cont.addAttachment(getFileContent(getRandomFile(f)));
    return cont;
  }

  private static File getRandomFile(File f) {
    File[] fa = f.listFiles();
    ExtendedSecureRandom sr = new ExtendedSecureRandom();
    return fa[sr.nextInt(fa.length)];
  }

  private static byte[] getFileContent(File f) {
    byte[] b = null;
    try {
      b = new byte[(int) (f.length())];
      new FileInputStream(f).read(b);
    } catch (IOException ioe) {
      LOGGER.log(Level.WARNING, "unable to reat file \"" + f.getAbsolutePath() + "\"", ioe);
    }
    return b;
  }
}
